package mud

class SSLSortedPQ[A](sort:(A, A) => Boolean) extends MudPriorityQueue[A] {
  import SSLSortedPQ._
  private var front:Node[A] = null
  
  def dequeue: A = {
    val ret = front.data
    front = front.next
    ret
  }
  def enqueue(a: A): Unit = {
    if(front == null || sort(a, front.data)) {
      front = new Node[A](a, front)
    } else {
      var rover:Node[A] = front
      while(rover.next != null && sort(rover.next.data, a) ) {
        rover = rover.next
      }
      rover.next = new Node[A](a, rover.next)
    }
  }
  def isEmpty: Boolean = front == null
  def peek: A = front.data
}
object SSLSortedPQ {
  class Node[A](val data: A, var next: Node[A])
}