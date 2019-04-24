package mud

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ActorSystem
import java.io.PrintStream
import java.io.BufferedReader
import java.net.Socket
import scala.collection.mutable.Buffer
import scala.collection.mutable.Map

class PlayerManager extends Actor {

  import PlayerManager._
  private var characters = Map[String, ActorRef]()

  def receive = {
    case NewPlayer(name, sock, in, out, roomManager) => {
      if (context.children.exists(_.path.name == name)) {
        out.println("Grandma already has a grandchild with that name, make a better one.")
        sock.close
      } else {
        val newguy = context.actorOf(Props(new Player(sock, name, in, out)), name)
        out.println("> ")
        newguy ! Player.Initialize(roomManager)
        characters += name -> newguy
      }
    }
    case TellSomebody(messenger, receiver, message) => {
      characters(receiver) ! Player.PrintMessage(messenger + " whispered '" + message + "'")
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
  case class TellSomebody(messenger: String, receiver: String, message: String)
}