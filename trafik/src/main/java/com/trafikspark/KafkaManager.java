package com.trafikspark;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**Класс позволяет отправить сообщение в топик alert.(localhost:9092)*/
public class KafkaManager {
    private Properties props;
    private KafkaProducer<String, String> producer;
    private String topicName;

    KafkaManager(){
        props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(props);
        topicName = "alert";
    }

    void messageKafkaTopic() {
        producer.send(new ProducerRecord<>(topicName, "aaaaaaaaaaaa"));
        producer.close();
    }
}
