package mud

import scala.collection.mutable.Buffer

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorPath

class Room(
  rname:             String,
  desc:              String,
  private var items: List[Item],
  exitKeys:          Array[String],
  players:           Buffer[ActorRef]) extends Actor {

  import Room._

  private var exits: Array[Option[ActorRef]] = null

  def receive = {
    case LinkExits(roomsMap) =>
      exits = exitKeys.map(keyword => roomsMap.get(keyword))
    case GetDescription =>
      sender ! Player.PrintMessage(description())
    case GetExit(dir, player, name) => {
      var leaving = getExit(dir)
      leaving match {
        case Some(x) => {
          players -= player
          self ! RoomMessage(name + " escapes and the door slams behind them.")
        }
        case None =>
      }
      sender ! Player.TakeExit(getExit(dir))
     // players += player
    }
    case GetItem(itemName) =>
      sender ! Player.TakeItem(getItem(itemName))
    case DropItem(name, item) => {
      dropItem(item)
      self ! RoomMessage(name + " threw down the " + item.itemName)
    }
    case NewPlayer(player) =>
      players += player
    case RoomMessage(message) =>
      for (p <- players) {
        p ! Player.PrintMessage(message)
      }
    case m => println("Ooops in Room: " + m)
  }

  def exitss(): String = {
    //    Console.out.println(exits(0) + exits(1))
    var e = ""
    if (exits(0) != None) e += "north  "
    if (exits(1) != None) e += "south  "
    if (exits(2) != None) e += "east  "
    if (exits(3) != None) e += "west  "
    if (exits(4) != None) e += "up  "
    if (exits(5) != None) e += "down  "
    e
  }

  def itemstring(): String = {
    if (items.length != 0) {
      var itemnames = List(" ")
      items.foreach(i => itemnames ::= (s"${i.itemName}"))
      itemnames.mkString("  ")
    } else { "None" }
  }

  def playerstring(): String = {
    var names = List(" ")
    players.foreach(i => names ::= (i.path.name))
    names.mkString("  ")
  }

  def description(): String = {
    s"$rname\n$desc\nExits: ${exitss()}\nItems: ${itemstring()}\nPlayers: ${playerstring}"
  }

  def getExit(dir: Int): Option[ActorRef] = {
    exits(dir)
  }

  def getItem(itemName: String): Option[Item] = {
    val found = items.find(x => x.name.toLowerCase() == itemName.toLowerCase())
    found match {
      case None => None
      case Some(x) => {
        items = items.patch(items.indexOf(x), Nil, 1)
        Some(x)
      }
    }
  }

  def dropItem(item: Item): Unit = {
    items ::= item
  }
}

object Room {
  case class LinkExits(roomsMap: Map[String, ActorRef])
  case object GetDescription
  case class GetExit(dir: Int, player: ActorRef, name: String)
  case class GetItem(itemName: String)
  case class DropItem(name: String, item: Item)
  case class NewPlayer(player: ActorRef)
  case class RoomMessage(message: String)
}