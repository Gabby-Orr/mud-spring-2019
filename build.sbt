name := "MUD"
version := "1.0"
scalaVersion := "2.12.6"

mainClass in (Compile, packageBin) := Some("mud.Main")

mainClass in (Compile, run) := Some("mud.Main")

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor" % "2.5.19",
	"com.typesafe.akka" %% "akka-testkit" % "2.5.19" % Test,
	"org.scala-lang.modules" %% "scala-xml" % "1.1.1",
	"com.novocode" % "junit-interface" % "0.11" % Test,
	"org.scalactic" %% "scalactic" % "3.0.5",
	"org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

