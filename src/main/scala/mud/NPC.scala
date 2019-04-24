package mud

import akka.actor.Actor

class NPC extends Actor {
  var dead: Boolean

  def receive = {
    case m => println("Oops in NPC: " + m)
  }

  while (!dead) {
    ActivityManager ! ActivityManager.Enqueue("command", 10)
  }
}