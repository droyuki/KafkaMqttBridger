package edu.nccu.iotlab.bridge

import akka.actor.{Inbox, Props, ActorSystem}

/**
  * Created by WeiChen on 2016/3/1.
  */
object StartBridge {
  def main(args: Array[String]) {
    val bridge = ActorSystem("kafkaMqttBridge")
    val topicManager = bridge.actorOf(Props(new TopicManager))
    topicManager ! SubscribeMessage(List("topics"))
  }
}
