package edu.nccu.iotlab.kafka

import java.util.Properties

import com.typesafe.config.ConfigFactory
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}

/**
  * Created by WeiChen on 2016/3/3.
  */
class KafkaConn private(broker:String){
  private val props = new Properties()
  props.put("metadata.broker.list", broker)
  props.put("serializer.class", "kafka.serializer.StringEncoder")
  props.put("request.required.acks", "1");
  private val config = new ProducerConfig(props)
  private val producer = new Producer[String, String](config)
  def send(topic:String,msg:String): Unit ={
    val data = new KeyedMessage[String, String](topic, msg, msg)
    producer.send(data)
  }
}
object KafkaConn{
  val conf = ConfigFactory.load()
  val kafkaBroker = conf.getString("kafkaBroker")
  val kc = new KafkaConn(kafkaBroker)
  def getInstance(): KafkaConn ={
    kc
  }
}