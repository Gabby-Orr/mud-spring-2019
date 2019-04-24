package mud

import akka.actor.Actor

class ActivityManager extends Actor {
  import ActivityManager._
  val pq = new SSLSortedPQ[Int]((d1, d2) => d1 < d2)
  private var messagemap = Map[Int, String]()
  private var counter = 0

  def receive = {
    case CheckQueue => {
      if (pq.peek == counter) {
        messagemap(counter)
      }
    }
    case Enqueue(command, delay) => {
      messagemap += delay -> command
      pq.enqueue(delay)
    }
    case m                       => println("Oops in ActivityManager: " + m)
  }

}
object ActivityManager {
  case object CheckQueue
  case class Enqueue(command: String, delay: Int)
}