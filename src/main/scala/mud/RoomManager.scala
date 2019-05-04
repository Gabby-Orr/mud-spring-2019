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
    // will be changed to StartRoom(rooms.get("porch"))
    case NPCRoom(place) =>
      sender ! NPC.StartRoom(rooms(place))
    case ShortestPath(locref, dest) => {
      val loc = roommap(locref)
      sender ! Player.Path(shortest(loc, dest, Set.empty).mkString(" "))
      //      sender ! Player.Path(s"${shortest(loc, dest, Set.empty).mkString(" ")}\n${shortroom.mkString(" ")}")
      //short = List.empty
    }
    case m => println("Ooops in RoomManager: " + m)
  }

  private var shortroom = List[String]()
  private var shortexit = List[String]()

  def shortest(loc: String, dest: String, visited: Set[String]): List[String] = {
    val newVisited = visited + loc
    if (loc == dest) shortexit //shortroom
    else if (!rooms.contains(dest)) {
      "That destination is not on the map"
    } else {
      for (i <- exitmap(loc); if (i != "-1"); if (!visited(i))) yield {
        //shortroom = i :: shortest(i, dest, newVisited)
        var dir = exitmap(loc).indexOf(i)
        //        println("before match: " + dir)
        dir match {
          case 0 => shortexit = "north" :: shortest(i, dest, newVisited)
          case 1 => shortexit = "south" :: shortest(i, dest, newVisited)
          case 2 => shortexit = "east" :: shortest(i, dest, newVisited)
          case 3 => shortexit = "west" :: shortest(i, dest, newVisited)
          case 4 => shortexit = "up" :: shortest(i, dest, newVisited)
          case 5 => shortexit = "down" :: shortest(i, dest, newVisited)
          case _ => shortexit :+ " "
        }
        //        println("after match: " + dir)
        //shortexit = dir :: shortest(i, dest, newVisited)

      }
    }
    shortexit //shortroom
  }

  //  def readRooms(): Map[String, ActorRef] = {
  //    val source = scala.io.Source.fromFile("map.txt")
  //    val lines = source.getLines()
  //    val rooms = Array.fill(lines.next.trim.toInt)(readRoom(lines)).toMap
  //    source.close()
  //    rooms
  //  }

  def readRooms(): MyBSTMap[String, ActorRef] = {
    val source = scala.io.Source.fromFile("map.txt")
    val lines = source.getLines()
    val rooms = new MyBSTMap[String, ActorRef](_ < _)
    val roomsArray = Array.fill(lines.next.trim.toInt)(readRoom(lines))
    source.close()
    for ((k, v) <- roomsArray) {
      rooms += ((k, v))
    }
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
    exitmap += keyword -> exits
    keyword -> theroom
  }

}

object RoomManager {
  case class ExitInfo(keyword: String, exitKeys: Array[String])
  case object GetStart
  case class NPCRoom(place: String)
  case class ShortestPath(locref: ActorRef, dest: String)
}