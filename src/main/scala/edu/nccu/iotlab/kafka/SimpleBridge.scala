package edu.nccu.iotlab.kafka

import com.typesafe.config.ConfigFactory
import org.eclipse.paho.client.mqttv3.{IMqttDeliveryToken, MqttMessage, MqttCallback, MqttClient}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

object SimpleBridge {
  def main(args: Array[String]) {
    if (args.length != 2) {
      System.err.println("Usage: jar -cp mqttBridge.jat edu.nccu.iotlab.kafka.SimpleBridge <MQTt Topic> <Kafka Topic>")
      System.exit(1)
    }
    val Array(mTopic, kTopic) = args.take(2)
    mqttSub(mTopic, kTopic)
  }

  def mqttSub(mTopic:String, kTopic:String): Unit = {
    val conf = ConfigFactory.load()
    val brokerUrl = conf.getString("broker")
    val persistence = new MemoryPersistence
    val client = new MqttClient(brokerUrl, MqttClient.generateClientId, persistence)
    client.connect
    client.subscribe(mTopic)

    val callback = new MqttCallback {
      override def messageArrived(topic: String, message: MqttMessage): Unit = {
        println("--------------- Message Arrived ---------------\nTopic : %s\nMessage : %s\n-----------------------------------------------".format(topic, message))
        KafkaProducer.getInstance().send(kTopic, message.toString)
      }

      override def connectionLost(cause: Throwable): Unit = {
        println(cause)
      }

      override def deliveryComplete(token: IMqttDeliveryToken): Unit = {
      }
    }
    client.setCallback(callback)
  }
}