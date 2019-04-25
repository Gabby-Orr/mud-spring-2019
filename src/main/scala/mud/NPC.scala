package mud

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem

class NPC(name: String) extends Actor {
  import NPC._
  private var dead = false
  private var loc: ActorRef = null

  def receive = {
    case Message => println("NPC got a message")
    case Initiate(place) => {
      println("got message to initialize")
      Main.roomManager ! RoomManager.NPCRoom(place)
    }
    case StartRoom(place) => {
      loc = place
      loc ! Room.NewPlayer(self)
      loc ! Room.RoomMessage(name + " enters and the door slams behind them.")
      println("after startroom, " + name + " is in " + loc)
    }
    case PrintMessage(message) => //
    case m => println("Oops in NPC: " + m)
  }

//  while (!dead) {
//    Main.activityManager ! ActivityManager.Enqueue("command", 10)
//  }
}
object NPC {
  case object Message
  case class Initiate(place: String)
  case class StartRoom(place: ActorRef)
  case class PrintMessage(message: String)
}