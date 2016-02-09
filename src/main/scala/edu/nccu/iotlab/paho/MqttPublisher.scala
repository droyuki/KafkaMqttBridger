package edu.nccu.iotlab.paho

import com.typesafe.config.ConfigFactory
import org.eclipse.paho.client.mqttv3.{MqttException, MqttMessage, MqttClient}
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence

/**
  * Created by WeiChen on 2016/2/9.
  */
object MqttPublisher {
  def main(args: Array[String]) {
    val conf = ConfigFactory.load()
    val brokerUrl = conf.getString("broker")
    val topic = "mqtt_test"
    val msg = "Hello world test data"

    var client: MqttClient = null

    // Creating new persistence for mqtt client
    val persistence = new MqttDefaultFilePersistence("/tmp")

    try {
      // mqtt client with specific url and client id
      client = new MqttClient(brokerUrl, MqttClient.generateClientId, persistence)

      client.connect()

      val msgTopic = client.getTopic(topic)
      val message = new MqttMessage(msg.getBytes("utf-8"))

      while (true) {
        msgTopic.publish(message)
        println("Publishing Data, Topic : %s, Message : %s".format(msgTopic.getName, message))
        Thread.sleep(100)
      }
    }

    catch {
      case e: MqttException => println("Exception Caught: " + e)
    }

    finally {
      client.disconnect()
    }
  }
}
