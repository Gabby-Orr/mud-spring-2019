package mud

import io.StdIn._
import scala.collection.mutable.Buffer
import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import java.io.BufferedReader
import java.io.PrintStream
import java.net.Socket

class Player(
  sock:     Socket,
  val name: String         = readLine(s"Enter player name: \n"),
  in:       BufferedReader,
  out:      PrintStream) extends Actor {

  private var inventory: Buffer[Item] = Buffer.empty
  private var loc: ActorRef = null
  private var health: Int = 100
  private var inhand: Item = null
  private var dead: Boolean = false
  private var canmove: Boolean = true

  import Player._

  def receive = {
    case InputCheck => {
      if (in.ready()) {
        out.println(">")
        processCommand((in.readLine()))
      }
    }
    case Initialize(roomManager) => {
      roomManager ! RoomManager.GetStart
    }
    case StartRoom(room: ActorRef) => {
      loc = room
      loc ! Room.NewPlayer(self, name)
      loc ! Room.RoomMessage(name + " enters and the door slams behind them.")
      loc ! Room.GetDescription
    }
    case PrintMessage(message: String) => out.println(message)
    case NoVictim                      => out.println("That victim does not exist or is not in the room.")
    case FoundVictim(victim: ActorRef) => {
      out.println("victim found")
      if (inhand == null) out.println("You have no weapons equipped.")
      else {
        Main.activityManager ! ActivityManager.Enqueue(Attack(victim, inhand), inhand.speed)
      }
    }
    case Attack(victim: ActorRef, weapon: Item) => {
      victim ! GotHit(name, weapon, loc)
      canmove = false
    }
    case GotHit(attacker: String, weapon: Item, place: ActorRef) => {
      out.println("OUCH! " + attacker + " attacked you with " + weapon.name + " in room: " + place.path.name + "!") //TODO: change place to string
      health -= weapon.damage
      out.println("You took " + weapon.damage + " damage! Health is at " + health)
      if (health <= 0) {
        dead = true //TODO: Remove player from room
        // TODO: Put victim's items in room
        out.println("Oh no, you died. Guess you can be with Grandpa now.")
        sock.close()
      } else {
        out.println("Options are to kill or flee")
        canmove = false
      }
      sender ! Player.HitResult(name, dead, health)
    }
    case GotHitNPC(attacker, place, damage) => {
      out.println("OUCH! " + attacker + " attacked you in room: " + place + "!") //TODO: change place to string
      health -= damage
      out.println("You took " + damage + " damage! Health is at " + health)
      if (health <= 0) {
        dead = true //TODO: Remove player from room
        // TODO: Put victim's items in room
        out.println("Oh no, you died. Guess you can be with Grandpa now.")
        sock.close()
      } else {
        out.println("Options are to kill or flee")
        canmove = false
      }
      sender ! NPC.HitResult(name, dead)
    }
    case HitResult(victim: String, dead: Boolean, health: Int) => {
      if (dead) {
        out.println("You killed " + victim)
        canmove = true
      } else out.println(victim + " survived attack, but their health is at " + health)
    }
    case TakeExit(optRoom: Option[ActorRef]) => {
      optRoom match {
        case Some(x) => {
          loc = x
          loc ! Room.NewPlayer(self, name)
          loc ! Room.RoomMessage(name + " enters and the door slams behind them.")
          loc ! Room.GetDescription
        }
        case None => out.println("You cannot escape that direction.")
      }
    }
    case TakeItem(optItem: Option[Item]) => {
      optItem match {
        case Some(x) => {
          addToInventory(x)
          loc ! Room.RoomMessage(name + " snatched the " + x.itemName)
        }
        case None => out.println("That item is either not available or in your inventory already.")
      }
    }
    case Path(path) => out.println(path)
    case m          => println("Oops in Player: " + m)
  }

  def processCommand(command: String): Unit = {
    val input = command.split(" ")
    input(0).toLowerCase() match {
      case "north" => move("north")
      case "south" => move("south")
      case "east"  => move("east")
      case "west"  => move("west")
      case "up"    => move("up")
      case "down"  => move("down")
      case "look"  => loc ! Room.GetDescription
      case "say" => {
        var message = s"${name} screamed '${input.drop(1).mkString(" ")}'"
        loc ! Room.RoomMessage(message)
      }
      case "tell" => {
        var receiver = input(1)
        var message = input.drop(2).mkString(" ")
        context.parent ! PlayerManager.TellSomebody(name, receiver, message)
      }
      case "inv" => out.println(inventoryListing())
      case "get" => {
        loc ! Room.GetItem(input(1))
      }
      case "drop" => {
        var finditem = getFromInventory(input(1))
        finditem match {
          case None    => out.println("That item is not in your inventory")
          case Some(x) => loc ! Room.DropItem(name, x)
        }
      }
      case "equip" => {
        var weapon = input(1)
        val found = inventory.find(x => x.itemName.toLowerCase() == weapon.toLowerCase())
        found match {
          case None => out.println("That item is not in your inventory")
          case Some(x) => {
            inhand = x
            out.println("You equipped the " + weapon)
          }
        }
      }
      case "unequip" => {
        var weapon = input(1)
        if (weapon.toLowerCase() == inhand.itemName.toLowerCase()) {
          inhand = null
          out.println("You unequipped the " + weapon)
        } else out.println("That item was not equipped.")
      }
      case "health" => out.println("Health level is at " + health + "%")
      case "help"   => printHelp()
      case "exit" => {
        out.println("Bye, come visit Grandma again soon!\n")
        sock.close
      }
      case "shortestpath" => {
        Main.roomManager ! RoomManager.ShortestPath(loc, input(1))
      }
      case "kill" => {
        loc ! Room.FindPlayer(input(1))
      }
      case "flee" => {
        val dir = util.Random.nextInt(6)
        dir match {
          //TODO: maybe change this to put into priority queue
          case 0 => loc ! Room.GetExit(0, self, name)
          case 1 => loc ! Room.GetExit(1, self, name)
          case 2 => loc ! Room.GetExit(2, self, name)
          case 3 => loc ! Room.GetExit(3, self, name)
          case 4 => loc ! Room.GetExit(4, self, name)
          case 5 => loc ! Room.GetExit(5, self, name)
        }
      }
      case _ => out.println("That is not an accepted command\n Let grandma help you by typing 'help'")
    }
  }

  def getFromInventory(itemName: String): Option[Item] = {
    val found = inventory.find(x => x.itemName.toLowerCase() == itemName.toLowerCase())
    found match {
      case None => None
      case Some(x) => {
        inventory = inventory.patch(inventory.indexOf(x), Nil, 1)
        Some(x)
      }
    }
  }

  def addToInventory(item: Item): Unit = {
    inventory += item
    out.println({ item.itemd })
  }

  def inventoryListing(): String = {
    if (inventory.length != 0) {
      var invlist = ""
      inventory.map(i => invlist += s"${i.itemName}--  ${i.itemd}\n")
      s"Inventory:\n$invlist"
    } else "None"
  }

  //  def getOut(dir: Int): Unit = {
  //    loc ! Room.GetExit(dir, self, name)
  //  }

  def move(dir: String): Unit = {
    //    var moves = Map[String, Unit]("north" -> getOut(0), "south" -> getOut(1), "east" -> getOut(2), "west" -> getOut(3), "up" -> getOut(4), "down" -> getOut(5))
    //    moves(dir)
    if (!canmove) out.println("You cannot exit during combat, either flee or kill")
    else {
      dir match {
        case "north" => loc ! Room.GetExit(0, self, name)
        case "south" => loc ! Room.GetExit(1, self, name)
        case "east"  => loc ! Room.GetExit(2, self, name)
        case "west"  => loc ! Room.GetExit(3, self, name)
        case "up"    => loc ! Room.GetExit(4, self, name)
        case "down"  => loc ! Room.GetExit(5, self, name)
      }
    }
  }

  def printHelp(): Unit = {
    out.println("Only the following commands are supported:\n")
    out.println(s"'north', 'south', 'east', 'west', 'up', 'down'  -- Movement; get from one room to another")
    out.println("'shortestPath {room}'                             -- Type 'shortestPath' followed by 1st word of desired room to find quickest way to destination")
    out.println("'say' {message} 							                   -- Say message to everyone in room")
    out.println("'tell' {user} {message}                          -- Send message to a specific user")
    out.println(s"'look'                                          -- See descripton of current room, along with items & exits in room")
    out.println("'inv'                                            -- See contents of your inventory")
    out.println("'get {item}'                                     -- Type 'get' followed by desired item to add that item to inventory")
    out.println("'drop {item}'                                    -- Type 'drop' followed by unwanted item to drop that item into room")
    out.println("health'                                          -- View health level")
    out.println("'equip {item}'														    		 -- Type 'equip' followed by desired item in inventory to equip item for combat")
    out.println("'unequip {item}'																 -- Type 'unequip' followed by equipped item to unequip item")
    out.println("'kill {player/npc}'                              -- Type 'kill' followed by desired victim to engage in combat")
    out.println("'flee'                                           -- 1/5 chance of escaping combat")
    out.println("'exit'                                            -- Leave the game")
  }
}

object Player {
  case object InputCheck
  case class PrintMessage(message: String)
  case class Path(path: String)
  case class TakeExit(optRoom: Option[ActorRef])
  case class TakeItem(optItem: Option[Item])
  case class Initialize(roomManager: ActorRef)
  case class StartRoom(room: ActorRef)
  case class FoundVictim(victim: ActorRef)
  case object NoVictim
  case class Attack(victim: ActorRef, weapon: Item)
  case class GotHit(attacker: String, weapon: Item, place: ActorRef)
  case class GotHitNPC(attacker: String, place: ActorRef, damage: Int)
  case class HitResult(name: String, dead: Boolean, health: Int)
}
