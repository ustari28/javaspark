package com.alan.javaspark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.StorageLevels;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Created by alan-davila on 18/01/18.
 */
public class SocketStreamingMain {
    private static final Logger LOG = Logger.getLogger(SocketStreamingMain.class.getName());
    private static final Integer STREAM_PORT = 9999;
    private static final String STREAM_HOST = "localhost";
    private static final Long STREAM_DELAY = 5L;

    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("Alan");
        JavaStreamingContext javaStreamingContext = new JavaStreamingContext(conf, Durations.seconds(STREAM_DELAY));

        JavaReceiverInputDStream<String> javaReceiverInputDStream = javaStreamingContext.socketTextStream(STREAM_HOST,
                STREAM_PORT, StorageLevels.MEMORY_AND_DISK_SER);
        JavaDStream<String> words = javaReceiverInputDStream.flatMap(line -> Arrays.asList(line.split(" ")).iterator());
        JavaPairDStream<String, Integer> pairs = words.mapToPair(w -> new Tuple2<>(w, 1)).reduceByKey((x, y) -> (x + y));
        pairs.print();
        javaStreamingContext.start();
        javaStreamingContext.awaitTermination();
    }
}
