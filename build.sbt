name := "KafkaMqttBridge"

version := "1.0"

scalaVersion := "2.11.7"

assemblyJarName in assembly := "mqttBridge.jar"

resolvers += "MQTT Repository" at "https://repo.eclipse.org/content/repositories/paho-releases/"

libraryDependencies ++= Seq(
  "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.0.2" excludeAll (
    ExclusionRule(organization = "org.scala-lang")),
  "org.apache.kafka" % "kafka_2.11" % "0.8.2.2" excludeAll (
    ExclusionRule("jline", "jline")
    ),
  "net.debasishg" %% "redisclient" % "3.0",
  "com.typesafe" % "config" % "1.2.1",
  "org.scalatest" %% "scalatest" % "2.2.4"
)

baseAssemblySettings
assemblyMergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
  case "log4j.properties" => MergeStrategy.discard
  case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}
