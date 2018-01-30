package com.alan.javaspark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by alan-davila on 15/01/18.
 */
public class JavaSparkMain {
    private static final Logger LOG = Logger.getLogger(JavaSparkMain.class.getName());
    public static void main(String[] args){
        JavaSparkContext sc = new JavaSparkContext(new SparkConf().setMaster("local[*]").setAppName("socketstreaming"));
        JavaRDD<String[]> lines = sc.textFile("src/main/resources/data.txt").map(s -> s.split(";"));
        List<String[]> result = lines.take(2);
        for(String[] x : result) {
            LOG.info("-------------");
            for(String field: x){
                LOG.info(field);
            }
        }
    }
}
