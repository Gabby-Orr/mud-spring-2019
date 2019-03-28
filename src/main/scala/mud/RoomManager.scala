package mud

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import scala.collection.mutable.Buffer

class RoomManager extends Actor {
  import RoomManager._

  val rooms = readRooms()
  for (room <- context.children) room ! Room.LinkExits(rooms)

  def receive = {
    case GetStart =>
      sender ! Player.StartRoom(rooms("kitchen"))
    case m => println("Ooops in RoomManager: " + m)
  }

  def readRooms(): Map[String, ActorRef] = {
    val source = scala.io.Source.fromFile("map.txt")
    val lines = source.getLines()
    val rooms = Array.fill(lines.next.trim.toInt)(readRoom(lines)).toMap
    source.close()
    rooms
  }

  def readRoom(lines: Iterator[String]): (String, ActorRef) = {
    val keyword = lines.next
    val name = lines.next
    val desc = lines.next
    val items = List.fill(lines.next.trim.toInt) {
      Item(lines.next, lines.next)
    }
    val exits = lines.next.split(",").map(_.trim)
    var players = Buffer.empty[ActorRef]
    keyword -> context.actorOf(Props(new Room(name, desc, items, exits, players)), keyword)
  }

}

object RoomManager {
  case object GetStart
}