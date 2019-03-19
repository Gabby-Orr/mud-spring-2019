package mud

import scala.io.StdIn._
import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.duration._
import scala.concurrent.Future


object Main { //just extend App instead of passing roomManager in NewPlayer
  import PlayerManager._
  def main(args: Array[String]): Unit = {
    println("Welcome to Grandma's Hell House! Enjoy your visit! \nEnter 'help' to see list of commands\n")

    val system = ActorSystem("TheSystem")
    val playerManager = system.actorOf(Props[PlayerManager], "PlayerManager")
    val roomManager = system.actorOf(Props[RoomManager], "RoomManager")
    implicit val ec = system.dispatcher

    val in = Console.in
    val out = Console.out
    
    Future {
      out.println("What is your name?")
      val name = in.readLine()
      playerManager ! NewPlayer(name, in, out, roomManager)
    }
        // send message to player manager 10 times/sec
    system.scheduler.schedule(0.seconds, 100.millis, playerManager, CheckInput)
  }

}
