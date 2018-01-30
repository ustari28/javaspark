package com.alan.javaspark;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import scala.Tuple2;

import java.util.*;
import java.util.logging.Logger;

public class KafkaStreaming {

    private static final Logger LOG = Logger.getLogger(KafkaStreaming.class.getName());
    private static final String STREAM_HOST = "localhost:9092";
    private static final Long STREAM_DELAY = 5L;

    public static void main(String[] agrs) throws InterruptedException {
        final Map<String, Object> kafkaParams = new HashMap<>();
        final Collection<String> topics = Arrays.asList("spark");
        final Collection<String> servers = Arrays.asList(STREAM_HOST);

        kafkaParams.put("bootstrap.servers", String.join(",", servers));
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", UUID.randomUUID().toString());
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);

        LOG.warning("Starting group.id with => " + kafkaParams.get("group.id"));

        JavaSparkContext sc = new JavaSparkContext(new SparkConf().setMaster("local[*]").setAppName("kafkastreaming"));
        JavaStreamingContext javaStreamingContext = new JavaStreamingContext(sc, Durations.seconds(STREAM_DELAY));
        JavaInputDStream<ConsumerRecord<String, String>> stream =
                KafkaUtils.createDirectStream(
                        javaStreamingContext,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
                );
        JavaPairDStream<String, Integer> pairs = stream.flatMap(s -> Arrays.asList(s.value().split(" ")).iterator())
                .mapToPair(w -> new Tuple2<>(w, 1)).reduceByKey((x, y) -> (x + y));
        pairs.foreachRDD(rdd -> {
            rdd.top(50, new ComparatorOrderDesc()).forEach(r -> {
                StringBuilder sb = new StringBuilder();
                sb.append(r._1).append(" => ").append(r._2);
                LOG.warning(sb.toString());
            });
        });
        javaStreamingContext.start();
        javaStreamingContext.awaitTermination();
    }
}
