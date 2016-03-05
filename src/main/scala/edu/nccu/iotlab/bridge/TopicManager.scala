package edu.nccu.iotlab.bridge


import akka.actor._
import com.redis._
import com.typesafe.config.ConfigFactory

/**
  * Created by WeiChen on 2016/3/1.
  */
case class SubscribeMessage(channels: List[String])
case class KafkaKilled(name:String)
class TopicManager extends Actor with ActorLogging {
  var childMap = Map.empty[String, ActorRef]
  val conf = ConfigFactory.load()
  val redisHost = conf.getString("redisHost")
  val redisPort = conf.getInt("redisPort")
  val r = new RedisClient(redisHost, redisPort)
  val s = context.actorOf(Props(new Subscriber(r)))
  s ! Register(callback)

  def getOrCreate(name: String) = childMap get name getOrElse {
    val child = context.actorOf(Props[BridgeActor], name = name)
    childMap += name -> child
    context watch child
    log.info(s"\n create bridge actor $name")
    child
  }


  def receive = {
    case SubscribeMessage(chs) => {
      log.info("\nsub" + print(chs))
      sub(chs)
    }
    case KafkaKilled(name) => {
      log.warning(s"Actor $name is going down...")
      val child = context.actorSelection(name)
      child ! PoisonPill
      childMap -= name
      println("[Topic list]"+childMap.keys.mkString(","))
      //log.warning(s"Mqtt actor $name has been killed.")
    }

    case Terminated(child) => {
      val name = child.path.name
      log.info(s"Actor $name has been killed.")
    }

    case x => log.debug("Got: " + x)
  }

  def sub(channels: List[String]) = {
    s ! Subscribe(channels.toArray)
  }

  def callback(callBack: PubSubMessage) = callBack match {
    case E(exception) => println("Fatal error caused consumer dead. Please init new consumer reconnecting to master or connect to backup")
    case S(channel, no) => println("subscribed to " + channel + " and count = " + no)
    case U(channel, no) => println("unsubscribed from " + channel + " and count = " + no)
    case M(channel, msg) =>
      msg match {
        // exit will unsubscribe from all channels and stop subscription service
        case "exit" =>
          println("unsubscribe all ..")
          r.unsubscribe

        // message "+x" will subscribe to topic x
        case x if x startsWith "+" =>
          val s: Seq[Char] = x
          s match {
            case Seq('+', topic@_*) => {
              val name = topic.toString()
              log.debug("[Redis]Get topic " + name)
              val mqttSub = getOrCreate(name)
              mqttSub ! StartProduce(name)
              log.info("\n[Topic list]"+childMap.keys.mkString(","))
            }
          }

        // message "-x" will unsubscribe from topic x
        case x if x startsWith "-" =>
          val s: Seq[Char] = x
          s match {
            case Seq('-', topic@_*) => {
              if(childMap.contains(topic.toString())) {
                val child = getOrCreate(topic.toString())
                child ! Shutdown(topic.toString())
              } else {
                log.warning(s"Actor $topic not exist!")
              }
            }
          }

        // check whether topic exist or not
        case x => {
          if (childMap.contains(x)) {
            log.info(s"\nTopic $x exist!")
          } else {
            log.info(s"\nTopic $x not exist!")
          }
        }
      }
  }
}
