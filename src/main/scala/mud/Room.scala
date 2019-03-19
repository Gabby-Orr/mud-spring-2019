package mud

import akka.actor.Actor
import akka.actor.ActorRef

class Room(
  name:              String,
  desc:              String,
  private var items: List[Item],
  exitKeys:          Array[String]) extends Actor {

  import Room._

  private var exits: Array[Option[ActorRef]] = null

  def receive = {
    case LinkExits(roomsMap) =>
      exits = exitKeys.map(keyword => roomsMap.get(keyword))
    case GetDescription =>
      sender ! Player.PrintMessage(description())
    case GetExit(dir) =>
      sender ! Player.TakeExit(getExit(dir))
    case GetItem(itemName) =>
      sender ! Player.TakeItem(getItem(itemName))
    case DropItem(item) =>
      dropItem(item)
    case m => println("Ooops in Room: " + m)
  }

  def exitss(): String = {
//    Console.out.println(exits(0) + exits(1))
    var e = ""
    if (exits(0) != None) e = "north  "
    if (exits(1) != None) e = "south  " // != "-1"
    if (exits(2) != None) e = "east  "
    if (exits(3) != None) e = "west  "
    if (exits(4) != None) e = "up  "
    if (exits(5) != None) e = "down  "
    e
  }

  def itemstring(): String = {
    if (items.length != 0) {
      var itemnames = List(" ")
      items.foreach(i => itemnames ::= (s"${i.itemName}"))
      itemnames.mkString("  ")
    } else { "None" }
  }

  def description(): String = {
    s"$name\n$desc\nExits: ${exitss()}\nItems: ${itemstring()}\n"
  }

  def getExit(dir: Int): Option[ActorRef] = {
    exits(dir)
  }

  def getItem(itemName: String): Option[Item] = {
    val found = items.find(x => x.name == itemName)
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
  case class GetExit(dir: Int)
  case class GetItem(itemName: String)
  case class DropItem(item: Item)
}