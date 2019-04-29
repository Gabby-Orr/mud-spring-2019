package mud

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem

class NPC(name: String) extends Actor {
  import NPC._
  private var dead = false
  private var loc: ActorRef = null
  private var delay: Int = 25
  private var health: Int = 500 //TODO: make health & speed & damage different for diff. NPCs
  private var hitspeed: Int = 20
  private var damage: Int = 15

  def receive = {
    case Initiate(place) => {
      Main.roomManager ! RoomManager.NPCRoom(place)
      Main.activityManager ! ActivityManager.Enqueue(Move(util.Random.nextInt(6)), delay)
    }
    case StartRoom(place) => {
      loc = place
      loc ! Room.NewPlayer(self, name)
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
    case TakeExit(optRoom: Option[ActorRef]) =>
      {
        optRoom match {
          case Some(x) => {
            loc = x
            loc ! Room.NewPlayer(self, name)
            loc ! Room.RoomMessage(name + " enters and the door slams behind them.")
            Main.activityManager ! ActivityManager.Enqueue(Move(util.Random.nextInt(6)), delay)
          }
          case None => Main.activityManager ! ActivityManager.Enqueue(Move(util.Random.nextInt(6)), delay)
        }
      }
    case Player.FoundVictim(victim: ActorRef) => {
      Main.activityManager ! ActivityManager.Enqueue(Attack(victim), hitspeed)
    }
    case Player.NoVictim =>
    case Attack(victim: ActorRef) => {
      victim ! Player.GotHitNPC(name, loc, damage)
    }
    case Player.GotHit(attacker: String, weapon: Item, place: ActorRef) => {
      health -= weapon.damage
      if (health <= 0) {
        dead = true //TODO: Remove player from room
        sender ! Player.HitResult(name, dead, health)
        // TODO: Put victim's items in room
      } else {
        sender ! Player.HitResult(name, dead, health)
        loc ! Room.FindPlayer(attacker)
      }
      //sender ! Player.HitResult(name, dead, health)
    }
    case HitResult(name: String, dead: Boolean) => {
      if (!dead) loc ! Room.FindPlayer(name)
    }
    case Player.PrintMessage(message) => //TODO: figure out how to get rid of these messages
    case m                     => println("Oops in NPC: " + m)
  }
  // TODO: Apply combat
}
object NPC {
  case class Initiate(place: String)
  case class StartRoom(place: ActorRef)
  case class PrintMessage(message: String)
  case class TakeExit(optRoom: Option[ActorRef])
  case class Move(dir: Int)
  case class FoundVictim(victim: ActorRef)
  case object NoVictim
  case class Attack(victim: ActorRef)
  case class GotHit(attacker: String, weapon: Item, place: ActorRef)
  case class HitResult(name: String, dead: Boolean)
}