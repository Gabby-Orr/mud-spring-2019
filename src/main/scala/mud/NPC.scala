package mud

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem

class NPC(name: String) extends Actor {
  import NPC._
  private var dead = false
  private var loc: ActorRef = null

  def receive = {
    case Initiate(place) => {
      Main.roomManager ! RoomManager.NPCRoom(place)
      Main.activityManager ! ActivityManager.Enqueue(Move(util.Random.nextInt(6)), 10)
    }
    case StartRoom(place) => {
      loc = place
      loc ! Room.NewPlayer(self)
      loc ! Room.RoomMessage(name + " enters and the door slams behind them.")
    }
    case Move(dir) => {
      dir match {
        case 0 => loc ! Room.NPCExit(0, self, name)
        case 1 => loc ! Room.NPCExit(1, self, name)
        case 2 => loc ! Room.NPCExit(2, self, name)
        case 3 => loc ! Room.NPCExit(3, self, name)
        case 4 => loc ! Room.NPCExit(4, self, name)
        case 5 => loc ! Room.NPCExit(5, self, name)
      }
    }
    case TakeExit(optRoom: Option[ActorRef]) => {
      optRoom match {
        case Some(x) => {
          loc = x
          loc ! Room.NewPlayer(self)
          loc ! Room.RoomMessage(name + " enters and the door slams behind them.")
          Main.activityManager ! ActivityManager.Enqueue(Move(util.Random.nextInt(6)), 10)
        }
        case None => Main.activityManager ! ActivityManager.Enqueue(Move(util.Random.nextInt(6)), 10)
      }
    }
    case PrintMessage(message) => //
    case m                     => println("Oops in NPC: " + m)
  }

}
object NPC {
  case class Initiate(place: String)
  case class StartRoom(place: ActorRef)
  case class PrintMessage(message: String)
  case class TakeExit(optRoom: Option[ActorRef])
  case class Move(dir: Int)

}