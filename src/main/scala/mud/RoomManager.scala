package mud

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import scala.collection.mutable

class RoomManager extends Actor {
  import RoomManager._
  private var exitmap = mutable.Map[String, Array[String]]()
  private var roommap = mutable.Map[ActorRef, String]()

  val rooms = readRooms()
  for (room <- context.children) room ! Room.LinkExits(rooms)


  def receive = {
    case ExitInfo(keyword, exitKeys) => {
      exitmap += keyword -> exitKeys
    }
    case GetStart =>
      sender ! Player.StartRoom(rooms("porch"))
    case NPCRoom(place) =>
      sender ! NPC.StartRoom(rooms(place))
    case ShortestPath(locref, dest) => {
      val loc = roommap(locref)
      sender ! Player.Path(shortest(loc, dest))
    }
    case m => println("Ooops in RoomManager: " + m)
  }
  
  def shortest(loc: String, dest: String): String = {
    val visited = Set[String](loc)
    if (loc == dest) ""
    else if (!rooms.contains(dest)){
      "That destination is not on the map"
    }
    else {
      for (i <- exitmap(loc); if (!visited(i))) yield {
        exitmap(i) + shortest(i, dest)
      }
    }
    ""
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
      Item(lines.next, lines.next, lines.next.trim.toInt, lines.next.trim.toInt)
    }
    val exits = lines.next.split(",").map(_.trim)
    var characters = new DLList[ActorRef] //Buffer.empty[ActorRef]
    val theroom = context.actorOf(Props(new Room(name, desc, items, exits, characters)), keyword)
    roommap += theroom -> keyword
    keyword -> theroom
  }

}

object RoomManager {
  case class ExitInfo(keyword: String, exitKeys: Array[String])
  case object GetStart
  case class NPCRoom(place: String)
  case class ShortestPath(locref: ActorRef, dest: String)
}