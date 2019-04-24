package mud

import akka.actor.Actor

class NPCManager extends Actor {
  import NPCManager._

  def receive = {
    case CreateNPC => ???
    case m         => println("Oops in PlayerManager: " + m)
  }
}
object NPCManager {
  case object CreateNPC
}