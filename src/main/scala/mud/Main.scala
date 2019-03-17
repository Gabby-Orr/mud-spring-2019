package mud

import io.StdIn._
import akka.actor.Props
import akka.actor.ActorSystem

object Main {
  import PlayerManager._
  def main(args: Array[String]): Unit = {
    println("Welcome to Grandma's Hell House! Enjoy your visit! \nEnter 'help' to see list of commands\n")

    // send message to player manager 10 times/sec

    val system = ActorSystem("TheSystem")
    val PlayerManager = system.actorOf(Props[PlayerManager], "PlayerManager")
    
    
    PlayerManager ! CheckInput

    val player = new Player
    var command = "word"
    println(s"Welcome ${player.name}\n")

    while (command != "exit") {
      command = readLine()
      player.processCommand(command)
    }
  }

}
