package mud

import io.StdIn._

class Player(
  val name:              String     = readLine(s"Enter player name: \n"),
  private var inventory: List[Item] = List.empty,
  private var loc:       Room       = Room.rooms(0)) {

  def processCommand(command: String): Unit = {
    val input = command.split(" ")
    input(0) match {
      case "north" => move("north")
      case "south" => move("south")
      case "east"  => move("east")
      case "west"  => move("west")
      case "up"    => move("up")
      case "down"  => move("down")
      case "look"  => println(loc.description)
      case "inv"   => println(inventoryListing())
      case "get" => {
        var finditem = loc.getItem(input(1))
        finditem match {
          case None    => println("That item is either not in room or in your inventory already")
          case Some(x) => addToInventory(x)
        }
      }
      case "drop" => {
        var finditem = getFromInventory(input(1))
        finditem match {
          case None    => println("That item is not in your inventory")
          case Some(x) => loc.dropItem(x)
        }
      }
      case "help" => printHelp()
      case "exit" => println("Bye, come visit Grandma again soon!\n")
      case _      => println("That is not an accepted command\n Let grandma help you by typing 'help'")
    }
  }

  def getFromInventory(itemName: String): Option[Item] = {
    val found = inventory.find(x => x.itemName == itemName)
    found match {
      case None => None
      case Some(x) => {
        inventory = inventory.patch(inventory.indexOf(x), Nil, 1)
        Some(x)
      }
    }
  }
  
  def addToInventory(item: Item): Unit = {
    inventory = item :: inventory
    println({item.itemd})
  }

  def inventoryListing(): String = {
    var invlist = ""
      inventory.map(i => invlist += s"${i.itemName}--  ${i.itemd}\n")
      s"Inventory:\n$invlist"
  }

  def move(dir: String): Unit = {
    var maybeloc: Option[Room] = None
    dir match {
      case "north" => maybeloc = loc.getExit(0)
      case "south" => maybeloc = loc.getExit(1)
      case "east"  => maybeloc = loc.getExit(2)
      case "west"  => maybeloc = loc.getExit(3)
      case "up"    => maybeloc = loc.getExit(4)
      case "down"  => maybeloc = loc.getExit(5)
    }
    if (maybeloc != None) {
      loc = maybeloc.get
      println(loc.description)
    } else println("No exit there")

  }

  def printHelp(): Unit = {
    println("Only the following commands are supported:\n")
    println(s"'north', 'south', 'east', 'west', 'up', 'down'  -- Movement; get from one room to another")
    println(s"'look'                                          -- See descripton of current room, along with items & exits in room")
    println("'inv'                                           -- See contents of your inventory")
    println("'get {item}'                                    -- Type 'get' followed by desired item to add that item to inventory")
    println("'drop {item}'                                   -- Type 'drop' followed by unwanted item to drop that item into room")
    println("'exit'                                          -- Leave the game")
  }
}
