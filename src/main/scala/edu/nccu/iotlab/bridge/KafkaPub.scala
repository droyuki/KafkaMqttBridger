package edu.nccu.iotlab.bridge

import akka.actor.Actor
import akka.actor.Actor.Receive

/**
  * Created by WeiChen on 2016/3/4.
  */
case class KafkaTopicPub(topic: String)
class KafkaPub extends Actor{
  override def receive = {
    case KafkaTopicPub(topic:String) => {
      println(s"[KafkaPubActor]pub to kafka topic $topic")
    }

  }
}
