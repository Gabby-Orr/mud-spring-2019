package mud

import scala.io.StdIn._
import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.duration._

object Main {
  import PlayerManager._
  def main(args: Array[String]): Unit = {
    println("Welcome to Grandma's Hell House! Enjoy your visit! \nEnter 'help' to see list of commands\n")

    // send message to player manager 10 times/sec

    val system = ActorSystem("TheSystem")
    val PlayerManager = system.actorOf(Props[PlayerManager], "PlayerManager")
    implicit val ec = system.dispatcher
        
    PlayerManager ! NewPlayer
    system.scheduler.schedule(0.seconds, 100.millis, PlayerManager, CheckInput)

    
//    val player = new Player
//    var command = "word"
//    println(s"Welcome ${player.name}\n")

//    while (command != "exit") {
//      command = readLine()
//      player.processCommand(command)
//    }
  }

}
