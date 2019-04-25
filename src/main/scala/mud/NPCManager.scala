package mud

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ActorSystem

class NPCManager extends Actor {
  import NPCManager._
  private var npcs = Map[String, ActorRef]()

  def receive = {
    case CreateNPC(name, loc) => {
      println("NPCman got message to create npc")
      val newguy = context.actorOf(Props(new NPC(name)), name)
      newguy ! NPC.Initiate(loc)
      npcs += name -> newguy
    }
    case m => println("Oops in PlayerManager: " + m)
  }
}
object NPCManager {
  case class CreateNPC(name: String, loc: String)
}