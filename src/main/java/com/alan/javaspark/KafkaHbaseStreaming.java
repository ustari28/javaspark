package com.alan.javaspark;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
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

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

public class KafkaHbaseStreaming {

    private static final Logger LOG = Logger.getLogger(KafkaStreaming.class.getName());
    private static final String STREAM_HOST = "localhost:9092";
    private static final Long STREAM_DELAY = 5L;

    public static void main(String[] agrs) throws InterruptedException, IOException {
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
        Table table = hbase();
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
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            List<Put> puts = Lists.newArrayList();
            rdd.filter(r -> StringUtils.isNotEmpty(r._1)).collect().forEach(r -> {
                Get g = new Get(Bytes.toBytes(r._1));
                try {
                    Result result = table.get(g);
                    Put p = new Put(Bytes.toBytes(r._1));
                    Integer previous = Bytes.toInt(Optional.ofNullable(result.getValue(Bytes.toBytes("count"),
                            Bytes.toBytes("count"))).orElse(Bytes.toBytes(0)));
                    p.addImmutable(Bytes.toBytes("word"), Bytes.toBytes("word"), Bytes.toBytes(r._1));
                    p.addImmutable(Bytes.toBytes("count"), Bytes.toBytes("count"), Bytes.toBytes(r._2 +
                            previous));
                    p.addImmutable(Bytes.toBytes("date"), Bytes.toBytes("date"), Bytes.toBytes(date));
                    puts.add(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
            LOG.warning("#rows " + puts.size());
            table.put(puts);
        });
        javaStreamingContext.start();
        javaStreamingContext.awaitTermination();
    }

    /**
     * Create connection to hbase system. Hbase-site.xml is the file for basic configuration. You might change this
     * properties with you environment.
     *
     * @return Object with connection.
     * @throws IOException Exception.
     */
    public static Table hbase() throws IOException {
        Configuration config = HBaseConfiguration.create();
        String path = "hbase-site.xml";
        config.addResource(new Path(KafkaHbaseStreaming.class.getClassLoader().getResource(path).getPath()));
        TableName words = TableName.valueOf("words");
        String word = "word";
        String count = "count";
        String date = "date";
        Connection connection = ConnectionFactory.createConnection(config);
        return connection.getTable(words);
    }
}
