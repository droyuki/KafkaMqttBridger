package edu.nccu.iotlab.bridge

import akka.actor.Actor
import akka.actor.Actor.Receive

/**
  * Created by WeiChen on 2016/3/1.
  */
case class MqttTopicSub(topic: String)
class MqttSub extends Actor{
  def receive ={
    case MqttTopicSub(t) => {
      println(s"[MqttSubActor]sub to mqtt topic $t")
    }
  }
}
