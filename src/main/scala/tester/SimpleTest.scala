package tester

import scala.sys.process._
import java.io.OutputStream
import java.io.InputStream
import java.io.BufferedReader
import java.io.PrintStream
import java.io.InputStreamReader

object SimpleTest extends App {
  val process = java.lang.Runtime.getRuntime.exec("sbt run".split(" "))
  doTest(new BufferedReader(new InputStreamReader(process.getInputStream)), new PrintStream(process.getOutputStream))
  
  def doTest(is: BufferedReader, os: PrintStream): Unit = {
    // Test code goes here. I probably need to cut out the opening lines the sbt prints.
    ???
  }
}