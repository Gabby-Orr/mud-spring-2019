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

class PlayerManager extends Actor {//(players: Map[String, ActorRef]) extends Actor {

  import PlayerManager._
  private var players = Map[String, ActorRef]()

  def receive = {
    case NewPlayer(name, sock, in, out, roomManager) => {
      if (context.children.exists(_.path.name == name)) {
        Console.out.println("Grandma already has a grandchild with that name, make a better one.")
      } else {
        val newguy = context.actorOf(Props(new Player(sock, name, in, out)), name)
        out.println("> ")
//        newguy ! Player.TakeExit(rooms("kitchen"))
        newguy ! Player.Initialize(roomManager)
        players += name -> newguy
      }
    }
    case TellSomebody(messenger, receiver, message) => {
      players(receiver) ! Player.PrintMessage(messenger + " whispered " + message)
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