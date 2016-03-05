package edu.nccu.iotlab.bridge


import java.util.Properties
import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, Callback, RecordMetadata}
import org.eclipse.paho.client.mqttv3.{IMqttDeliveryToken, MqttMessage, MqttCallback, MqttClient}

/**
  * Created by WeiChen on 2016/3/1.
  */
case class MqttSub(topic: String)
case class Shutdown(name: String)
case class StartProduce(topic:String)

class BridgeActor extends Actor with ActorLogging {
  private val conf = ConfigFactory.load()
  private val props = new Properties()
  private val kafkaBroker = conf.getString("kafkaBroker")
  private val mqttBroker = conf.getString("broker")
  props.put("metadata.broker.list", kafkaBroker)
  props.put("serializer.class", "kafka.serializer.StringEncoder")
  props.put("request.required.acks", "1")
  props.put("bootstrap.servers",kafkaBroker)
  props.put("session.timeout.ms", "30000")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  private var producer: KafkaProducer[String, String] = _

  def receive = {
    case MqttSub(t) => {
      println(s"[MQTT]sub to mqtt topic $t")
      //      mqttSub(t)
      //kafkaPub ! KafkaPub(t)
    }

    case Shutdown(t) => {
      println(s"[MQTT]$t will shutdown")
      //kafkaPub ! PoisonPill
      sender() ! KafkaKilled(t)
    }

    case StartProduce(t) => {
      producer = initProducer()
      produce(producer,t)
    }
  }

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case e: Exception ⇒
      //can handle failing here
      log.error(s"Write failed $e")
      Resume
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    log.debug("closing producer")
    producer.close()
    log.debug("closed producer")
  }

  private def initProducer(): KafkaProducer[String, String] = {
    log.debug(s"Config ${props}")
    new KafkaProducer(props)
  }

  private def produce(producer: KafkaProducer[String, String], topic:String): Unit = {
    log.info("Start kafka producer")
    val mqttClient = new MqttClient(mqttBroker, MqttClient.generateClientId())
    mqttClient.connect()
    mqttClient.subscribe(topic)
    log.debug(s"mqtt sub $topic")
    val callback = new MqttCallback {
      override def messageArrived(topic: String, message: MqttMessage): Unit = {
        log.debug("\n--------------- MQTT ----------------\nTopic : %s\nMessage : %s\n-------------------------------------".format(topic, message))
        val msg = new ProducerRecord[String, String](topic, message.toString, message.toString)
        producer.send(msg, new Callback {
          override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
            val maybeMetadata = Option(metadata)
            val maybeException = Option(exception)
            if (maybeMetadata.isDefined) {
              log.debug(s"actor ${self.path.toString}: onCompletion offset ${metadata.offset},partition ${metadata.partition}")
            }
            if (maybeException.isDefined) {
              log.error(exception, s" onCompletion received error")
            }
          }
        })
      }

      override def connectionLost(cause: Throwable): Unit = {
        println(cause)
      }

      override def deliveryComplete(token: IMqttDeliveryToken): Unit = {
      }
    }
    mqttClient.setCallback(callback)
  }

}