package edu.nccu.iotlab.mqtt

import com.typesafe.config.ConfigFactory
import org.eclipse.paho.client.mqttv3.{IMqttDeliveryToken, MqttMessage, MqttCallback, MqttClient}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

/**
  * Created by WeiChen on 2016/2/9.
  */
object MqttSubscriber {
  def main(args: Array[String]) {
    val conf = ConfigFactory.load()
    val brokerUrl = conf.getString("broker")
    val topic = "mqtt"

    //Set up persistence for messages
    val persistence = new MemoryPersistence

    //Initializing Mqtt Client specifying brokerUrl, clientID and MqttClientPersistance
    val client = new MqttClient(brokerUrl, MqttClient.generateClientId, persistence)

    //Connect to MqttBroker
    client.connect

    //Subscribe to Mqtt topic
    client.subscribe(topic)

    //Callback automatically triggers as and when new message arrives on specified topic
    val callback = new MqttCallback {

      override def messageArrived(topic: String, message: MqttMessage): Unit = {
        println("--------------- Message Arrived ---------------\nTopic : %s\nMessage : %s\n-----------------------------------------------".format(topic, message))
      }

      override def connectionLost(cause: Throwable): Unit = {
        println(cause)
      }

      override def deliveryComplete(token: IMqttDeliveryToken): Unit = {

      }
    }

    //Set up callback for MqttClient
    client.setCallback(callback)

  }
}