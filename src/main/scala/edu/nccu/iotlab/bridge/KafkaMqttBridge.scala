package edu.nccu.iotlab.bridge

import java.util.Properties

import com.typesafe.config.ConfigFactory
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import org.eclipse.paho.client.mqttv3.{IMqttDeliveryToken, MqttMessage, MqttCallback, MqttClient}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

/**
  * Created by WeiChen on 2016/2/10.
  */
object KafkaMqttBridge {
  val conf = ConfigFactory.load()
  val brokerUrl = conf.getString("broker")
  val kafkaBroker = conf.getString("kafkaBroker")
  //Set up persistence for messages
  val persistence = new MemoryPersistence
  //Initializing Mqtt Client specifying brokerUrl, clientID and MqttClientPersistance
  val client = new MqttClient(brokerUrl, MqttClient.generateClientId, persistence)
  //Connect to MqttBroker
  client.connect
  val callback = new MqttCallback {
    override def deliveryComplete(token: IMqttDeliveryToken): Unit = ???

    override def messageArrived(topic: String, message: MqttMessage): Unit = {
      //太慢可換成batch send
      val props = new Properties()
      props.put("metadata.broker.list", kafkaBroker)
      props.put("serializer.class", "kafka.serializer.StringEncoder")
      val config = new ProducerConfig(props)
      val producer = new Producer[String, String](config)
      val msg = message.toString
      val data = new KeyedMessage[String,String](topic, msg, msg);
      producer.send(data);
    }

    override def connectionLost(cause: Throwable): Unit = {
      reconnect()
    }
  }
  client.setCallback(callback)
  def main(args: Array[String]) {


    val topic = Array("123","567")
    subscribe(topic)

  }

  def reconnect(): Unit = {

  }

  def subscribe(topic: Array[String]): Unit = {
    val qos = Array.fill(topic.length)(0)
    client.subscribe(topic, qos)
  }
}
