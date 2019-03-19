package mud

import io.StdIn._
import scala.collection.mutable.Buffer
import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import java.io.BufferedReader
import java.io.PrintStream

class Player(
  val name: String         = readLine(s"Enter player name: \n"),
  in:       BufferedReader = Console.in,
  out:      PrintStream    = Console.out) extends Actor {

  private var inventory: Buffer[Item] = Buffer.empty
  private var loc: ActorRef = null

  import Player._

  def receive = {
    case InputCheck => {
      if (in.ready()) {
        println(">")
        processCommand(in.readLine())
      }
    }
    case Initialize(roomManager) => {
      roomManager ! RoomManager.GetStart
    }
    case StartRoom(room: ActorRef) => {
      loc = room
    }
    case PrintMessage(message: String) => out.println(message)

    case TakeExit(optRoom: Option[ActorRef]) => {
      optRoom match {
        case Some(x) => {
          loc = x
          loc ! Room.GetDescription
        }
        case None => out.println("You cannot escape that direction.")
      }
    }
    case TakeItem(optItem: Option[Item]) => {
      optItem match {
        case Some(x) => addToInventory(x)
        case None    => out.println("You cannot escape that direction.")
      }
    }
    case m => println("Oops in Player: " + m)
  }

  def processCommand(command: String): Unit = {
    val input = command.split(" ")
    input(0) match {
      case "north" => move("north")
      case "south" => move("south")
      case "east"  => move("east")
      case "west"  => move("west")
      case "up"    => move("up")
      case "down"  => move("down")
      case "look"  => loc ! Room.GetDescription
      case "inv"   => println(inventoryListing())
      case "get" => {
        loc ! Room.GetItem(input(1))
        //        var finditem = loc.getItem(input(1))
        //        finditem match {
        //          case None    => println("That item is either not in room or in your inventory already")
        //          case Some(x) => addToInventory(x)
        //        }
      }
      case "drop" => {
        var finditem = getFromInventory(input(1))
        finditem match {
          case None    => println("That item is not in your inventory")
          case Some(x) => loc ! Room.DropItem(x)
        }
      }
      case "help" => printHelp()
      case "exit" => {
        println("Bye, come visit Grandma again soon!\n")
      }
      case _ => println("That is not an accepted command\n Let grandma help you by typing 'help'")
    }
  }

  def getFromInventory(itemName: String): Option[Item] = {
    val found = inventory.find(x => x.itemName == itemName)
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
    println({ item.itemd })
  }

  def inventoryListing(): String = {
    if (inventory.length != 0) {
      var invlist = ""
      inventory.map(i => invlist += s"${i.itemName}--  ${i.itemd}\n")
      s"Inventory:\n$invlist"
    } else "None"
  }

  def move(dir: String): Unit = {
    //    var maybeloc: Option[Room] = None
    dir match {
      case "north" => loc ! Room.GetExit(0) //maybeloc = loc.getExit(0)
      case "south" => loc ! Room.GetExit(1) //maybeloc = loc.getExit(1)
      case "east"  => loc ! Room.GetExit(2) //maybeloc = loc.getExit(2)
      case "west"  => loc ! Room.GetExit(3) //maybeloc = loc.getExit(3)
      case "up"    => loc ! Room.GetExit(4) //maybeloc = loc.getExit(4)
      case "down"  => loc ! Room.GetExit(5) //maybeloc = loc.getExit(5)
    }
    //    if (maybeloc != None) {
    //      loc = maybeloc.get
    //      println(loc.description)
    //    } else println("No exit there")

  }

  def printHelp(): Unit = {
    println("Only the following commands are supported:\n")
    println(s"'north', 'south', 'east', 'west', 'up', 'down'  -- Movement; get from one room to another")
    println(s"'look'                                          -- See descripton of current room, along with items & exits in room")
    println("'inv'                                           -- See contents of your inventory")
    println("'get {item}'                                    -- Type 'get' followed by desired item to add that item to inventory")
    println("'drop {item}'                                   -- Type 'drop' followed by unwanted item to drop that item into room")
    println("'exit'                                          -- Leave the game")
  }
}

object Player {
  case object InputCheck
  case class PrintMessage(message: String)
  case class TakeExit(optRoom: Option[ActorRef])
  case class TakeItem(optItem: Option[Item])
  case class Initialize(roomManager: ActorRef)
  case class StartRoom(room: ActorRef)
}
