package edu.nccu.iotlab.kafka

import com.typesafe.config.ConfigFactory
import org.eclipse.paho.client.mqttv3.{IMqttDeliveryToken, MqttMessage, MqttCallback, MqttClient}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

object SimpleBridge {
  def main(args: Array[String]) {
    mqttSub()
  }

  def mqttSub(): Unit = {
    val conf = ConfigFactory.load()
    val brokerUrl = conf.getString("broker")
    val mTopic = "mqtt"
    val persistence = new MemoryPersistence
    val client = new MqttClient(brokerUrl, MqttClient.generateClientId, persistence)
    client.connect
    client.subscribe(mTopic)

    val callback = new MqttCallback {
      override def messageArrived(topic: String, message: MqttMessage): Unit = {
        println("--------------- Message Arrived ---------------\nTopic : %s\nMessage : %s\n-----------------------------------------------".format(topic, message))
        KafkaProducer.getInstance().send(mTopic, message.toString)
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