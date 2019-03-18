package mud

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ActorSystem

class PlayerManager extends Actor {

  import PlayerManager._

  def receive = {
    case NewPlayer(name) => {
      //      println("Making this player")
      //      val player = context.actorOf(Props(new Player), "player")
      //    }
      if (context.children.exists(_.path.name == name)) {
        Console.out.println("Name isn't unique. Be more creative.")
      } else {
        context.actorOf(Props(new Player(name)), name)
        Console.out.println("> ")
      }
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
  case class NewPlayer(name: String)
  case object CheckInput
  case object Initialization
}