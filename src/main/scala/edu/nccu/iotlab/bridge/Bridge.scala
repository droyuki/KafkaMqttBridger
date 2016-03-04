package edu.nccu.iotlab.bridge

import akka.actor.{Inbox, Props, ActorSystem}

/**
  * Created by WeiChen on 2016/3/1.
  */
object Bridge {
  def main(args: Array[String]) {
    val ss = ActorSystem("sub")
    val s = ss.actorOf(Props(new TopicManager))
    s ! SubscribeMessage(List("topics"))
  }

}
