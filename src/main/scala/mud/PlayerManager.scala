package mud

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ActorSystem
import java.io.PrintStream
import java.io.BufferedReader
import java.net.Socket

class PlayerManager extends Actor {

  import PlayerManager._

  def receive = {
    case NewPlayer(name, sock, in, out, roomManager) => {
      if (context.children.exists(_.path.name == name)) {
        Console.out.println("Grandma already has a grandchild with that name, make a better one.")
      } else {
        val newguy = context.actorOf(Props(new Player(sock, name, in, out)), name)
        out.println("> ")
        newguy ! Player.Initialize(roomManager)
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
  case class NewPlayer(name: String, sock: Socket, in: BufferedReader, out: PrintStream, roomManager: ActorRef)
  case object CheckInput
  case object Initialization
}