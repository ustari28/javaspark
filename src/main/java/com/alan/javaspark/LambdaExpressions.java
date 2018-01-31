package com.alan.javaspark;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LambdaExpressions {

    private static final Logger LOG = Logger.getLogger(LambdaExpressions.class.getName());

    public static void main(String[] agrs) {
        LOG.setLevel(Level.INFO);
        predicate("hello", s -> s.equals("hola"));
        predicate("hello", s -> s.equals("hello"));
        consumer("hello", s -> LOG.info("Message from consumer:".concat(s)));
        function("hello", s -> s.toUpperCase());
        suplier(() -> "STOCK");
    }

    /**
     * Predicate functions. Only to evaludate one condition.
     *
     * @param s         Parameter to evaluate.
     * @param predicate Function.
     */
    public static void predicate(final String s, final Predicate<String> predicate) {
        if (predicate.test(s)) LOG.info("[OK]");
        else LOG.severe("[FAILED]");
    }

    /**
     * Function without output value.
     *
     * @param s        Object  to use.
     * @param consumer Function.
     */
    public static void consumer(final String s, final Consumer<String> consumer) {
        consumer.accept(s);
    }

    /**
     * Function with ouput value.
     * @param s Input value.
     * @param function Function.
     */
    public static void function(final String s, final Function<String, String> function) {
        LOG.info("Applying function: " + function.apply(s));
    }

    /**
     * Only to supply a result.
     * @param supplier Function.
     */
    public static void suplier(final Supplier<String> supplier) {
        LOG.info("Supplier :" + supplier.get());
    }
}
