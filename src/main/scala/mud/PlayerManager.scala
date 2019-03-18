package mud

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

class PlayerManager extends Actor {

  import PlayerManager._
  import Main._

  def receive = {
    case NewPlayer => {
      println("Making this player")
      val player = context.actorOf(Props(new Player), "player")
    }
    case Initialization => {
      for (p <- context.children) {
        p ! Player.Initialize
      }
    }
    case CheckInput => {
      for (p <- context.children) {
        p ! Player.InputCheck
      }
    }
    case m => println("Oops in PlayerManager: " + m)
  }

}

object PlayerManager {
  case object NewPlayer
  case object CheckInput
  case object Initialization
}