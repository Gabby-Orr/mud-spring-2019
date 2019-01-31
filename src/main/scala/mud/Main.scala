package mud

import io.StdIn._

object Main {
  def main(args: Array[String]): Unit = {
    println("Welcome to Grandma's Hell House! Enjoy your visit! \nEnter 'help' to see list of commands\n")

    val player = new Player
    var command = "word"
    println(s"Welcome ${player.name}\n")

    while (command != "exit") {
      command = readLine()
      player.processCommand(command)
    }
  }

}
