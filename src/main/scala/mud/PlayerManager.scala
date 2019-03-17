package mud

import akka.actor.Actor
import akka.actor.ActorRef

class PlayerManager extends Actor {
  // we gon have some form of InputStream & OutputStream
  // but will start by just using Console.in & Console.out
  import PlayerManager._
  
  def receive = {
    case CheckInput => println("ay we're gonna check input")
    case m          => println("Oops in PlayerManager: " + m)
  }

}

object PlayerManager {
  case object CheckInput
}