package edu.nccu.iotlab.bridge

import akka.actor.{ActorRef, Props, ActorSystem, Actor}
import akka.event.Logging
import com.redis._
import com.typesafe.config.ConfigFactory

/**
  * Created by WeiChen on 2016/3/1.
  */
case class SubscribeMessage(channels: List[String])

case class UnsubscribeMessage(channels: List[String])

case object GoDown
sealed trait Mqtt
case class Topic(t:String) extends Mqtt

class TopicManager extends Actor {
  println("starting bridge service ..")
  val log = Logging(context.system, this)
  val system = ActorSystem("kafkaMqttBridge")
  val conf = ConfigFactory.load()
  val redisHost = conf.getString("redisHost")
  val redisPort = conf.getInt("redisPort")
  val r = new RedisClient(redisHost, redisPort)
  val s = system.actorOf(Props(new Subscriber(r)))
  s ! Register(callback)

  def receive = {
    case SubscribeMessage(chs) => {
      log.info("sub" + print(chs))
      sub(chs)
    }
    case UnsubscribeMessage(chs) => {
      log.info("unsub" + print(chs))
      unsub(chs)
    }
    case GoDown =>
      r.quit
      system.shutdown()
      system.awaitTermination()

    case x => log.info("Got in Sub " + x)
  }

  def sub(channels: List[String]) = {
    s ! Subscribe(channels.toArray)
  }

  def unsub(channels: List[String]) = {
    s ! Unsubscribe(channels.toArray)
  }

  def callback(pubsub: PubSubMessage) = pubsub match {
    case E(exception) => println("Fatal error caused consumer dead. Please init new consumer reconnecting to master or connect to backup")
    case S(channel, no) => println("subscribed to " + channel + " and count = " + no)
    case U(channel, no) => println("unsubscribed from " + channel + " and count = " + no)
    case M(channel, msg) =>
      msg match {
        // exit will unsubscribe from all channels and stop subscription service
        case "exit" =>
          println("unsubscribe all ..")
          r.unsubscribe

        // message "+x" will subscribe to channel x
        case x if x startsWith "+" =>
          val s: Seq[Char] = x
          s match {
            case Seq('+', rest@_*) => r.subscribe(rest.toString) { m => }
          }

        // message "-x" will unsubscribe from channel x
        case x if x startsWith "-" =>
          val s: Seq[Char] = x
          s match {
            case Seq('-', rest@_*) => r.unsubscribe(rest.toString)
          }

        // other message receive
        case x => {
          println("[RedisSubActor]new topic detected : " + x)
          val mqttSub = context.actorOf(Props[MqttSub], x)
          mqttSub ! MqttTopicSub(x)
        }
      }
  }
}
