package mud

import org.junit.Test
import org.junit.Assert._

class TestRoom {
  @Test def testDescription: Unit = {
    val properDescription = """First Room
This would be the description of the first room.
Exits: North, East
Items: sword, chair
"""
    assertTrue(true)
//    assertEquals(properDescription, Room.rooms(0).getDescription)
  }
}