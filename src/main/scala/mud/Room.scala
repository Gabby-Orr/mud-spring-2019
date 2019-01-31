package mud

class Room(
  name:              String,
  desc:              String,
  private var items: List[Item],
  exits:             Array[Int]) {

  def exitss(): String = {
    var e = ""
    if (exits(0) != -1) e += "north  "
    if (exits(1) != -1) e += "south  "
    if (exits(2) != -1) e += "east  "
    if (exits(3) != -1) e += "west  "
    if (exits(4) != -1) e += "up  "
    if (exits(5) != -1) e += "down  "
    e
  }

  def itemstring(): String = {
    if (items.length != 0) {
      var itemnames = List(" ")
      items.foreach(i => itemnames ::= (s"${i.itemName}"))
      itemnames.mkString("  ")
    } else { "None" }
  }

  def description(): String = {
    s"$name\n$desc\nExits: ${exitss()}\nItems: ${itemstring()}\n"
  }

  def getExit(dir: Int): Option[Room] = {
    if (exits(dir) == -1) None else Some(Room.rooms(exits(dir)))
  }

  def getItem(itemName: String): Option[Item] = {
    val found = items.find(x => x.name == itemName)
    found match {
      case None => None
      case Some(x) => {
        items = items.patch(items.indexOf(x), Nil, 1)
        Some(x)
      }
    }
  }

  def dropItem(item: Item): Unit = {
    items ::= item
  }
}

object Room {
  val rooms = readRooms()

  def readRooms(): Array[Room] = {
    val source = scala.io.Source.fromFile("map.txt") //iterator of characters
    val lines = source.getLines()
    val rooms = Array.fill(lines.next.trim.toInt)(readRoom(lines)) // .trim keeps a space from messing shit up
    source.close()
    rooms
  }

  def readRoom(lines: Iterator[String]): Room = {
    val name = lines.next
    val desc = lines.next
    val items = List.fill(lines.next.trim.toInt) {
      Item(lines.next, lines.next)
    }
    val exits = lines.next.split(",").map(_.trim.toInt)
    new Room(name, desc, items, exits)
  }

}