package com.alan.javaspark;

import com.google.common.collect.Lists;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NewStream {

    public static void main(String[] agrs) {
        List<String> list = Lists.newArrayList("x", "y", "z");
        IntStream.range(0, list.size()).forEach(i -> System.out.println(list.get(i)));
        IntStream.range(0, list.size()).mapToObj(i -> list.get(i)).filter(x -> x.equals("x")).forEach(System.out::println);
        String z = IntStream.range(0, list.size()).mapToObj(i -> list.get(i)).collect(Collectors.joining(","));
        System.out.println("Joining:" + z);
        Long nop = System.currentTimeMillis();
        IntStream.range(0, 100000).forEach(System.out::print);
        System.out.println();
        System.out.println("Finish no parallel:" + (System.currentTimeMillis() - nop));
        Long wp = System.currentTimeMillis();
        IntStream.range(0, 100000).parallel().forEach(System.out::print);
        System.out.println();
        System.out.println("Finish with parallel:" + (System.currentTimeMillis() - wp));
    }
}
