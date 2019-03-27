package mud

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import java.net.ServerSocket
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.Future

object Main extends App {
  import PlayerManager._

  val system = ActorSystem("TheSystem")
  val playerManager = system.actorOf(Props[PlayerManager], "PlayerManager")
  val roomManager = system.actorOf(Props[RoomManager], "RoomManager")
  system.scheduler.schedule(0.seconds, 100.millis, playerManager, CheckInput)

  //  implicit val ec = system.dispatcher

  val ss = new ServerSocket(8080)

  //  val in = Console.in
  //  val out = Console.out

  //  Future {
  //    out.println("What is your name?")
  //    val name = in.readLine()
  //    playerManager ! NewPlayer(name, in, out, roomManager)
  //  }

  while (true) {
    val sock = ss.accept()
    val in = new BufferedReader(new InputStreamReader(sock.getInputStream))
    val out = new PrintStream(sock.getOutputStream)
    Future {
      out.println("Welcome to Grandma's Hell House! Enjoy your visit! \nEnter 'help' to see list of commands\n")
      out.println("What is your name?")
      val name = in.readLine()
      playerManager ! NewPlayer(name, sock, in, out, roomManager)
    }
  }
  // send message to player manager 10 times/sec
  //  system.scheduler.schedule(0.seconds, 100.millis, playerManager, CheckInput)

}
