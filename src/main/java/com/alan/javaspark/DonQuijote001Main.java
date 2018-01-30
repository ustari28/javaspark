package com.alan.javaspark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;

/**
 * Created by alan-davila on 15/01/18.
 */
public class DonQuijote001Main {
    private static final Logger LOG = Logger.getLogger(DonQuijote001Main.class.getName());
    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        class Comp implements Comparator<Tuple2<String, Integer>>,Serializable {


            @Override
            public int compare(Tuple2<String, Integer> o1, Tuple2<String, Integer> o2) {
                return Integer.compare(o1._2, o2._2);
            }
        }
        JavaSparkContext sc = new JavaSparkContext(new SparkConf().setMaster("local").setAppName("alan"));
        JavaRDD<String> lines = sc.textFile("src/main/resources/Don_Quijote_de_la_Mancha_cervantes_001.txt");
        JavaRDD<String> words = lines.flatMap(line -> Arrays.asList(line.split(" ")).iterator());
        JavaPairRDD<String, Integer> counts = words.mapToPair(w -> new Tuple2<>(w, 1)).reduceByKey((x,y) -> x + y);
        counts.top(10, new Comp()).forEach(t -> LOG.warning("("+t._1+","+t._2+")"));
        LOG.info("Nº words: " + words.count());
        //JavaPairRDD<String, Integer> counts = words.mapToPair(r -> new Tuple2<>(r, 1)).reduceByKey((a, b) -> a+b);

    }


}
