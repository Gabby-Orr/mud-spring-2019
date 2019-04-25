package mud

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem

class ActivityManager extends Actor {
  import ActivityManager._
  val pq = new SSLSortedPQ[Activity]((d1, d2) => d1.delay < d2.delay)
  private var counter = 0

  def receive = {
    case CheckQueue => {
      while (!pq.isEmpty) {
      counter += 1
        if (pq.peek.delay <= counter) {
          val act = pq.dequeue()
          act.receiver ! act.command
        }
      }
    }
    case Enqueue(command, delay) => {
      pq.enqueue(Activity(command, sender, delay))
    }
    case m => println("Oops in ActivityManager: " + m)
  }

}
object ActivityManager {
  case object CheckQueue
  case class Enqueue(command: String, delay: Int)
  case class Activity(command: Any, receiver: ActorRef, delay: Int)
}