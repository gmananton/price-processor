package com.price.processor.subscriber;

import com.price.processor.PriceProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Implements subscriber waiting currency rates to process</p>
 * <p>Can emulate slow processing using sleepTimeoutMs</p>
 */
public class Subscriber implements PriceProcessor {

    private static final Logger log = LoggerFactory.getLogger(Subscriber.class);
    private final String name;
    private final int sleepTimeoutMs;

    public Subscriber(String name) {
        this(name, 0);
    }

    /**
     * @param name           name of the processor. Uniquely identifies it i.e. to be stored in a map
     * @param sleepTimeoutMs if > 0 then emulates slow processing by sleeping thread execution
     */
    public Subscriber(String name, int sleepTimeoutMs) {
        this.name = name;
        this.sleepTimeoutMs = sleepTimeoutMs;
        log.info("Subscriber '{}' created with processing time={} ms", name, sleepTimeoutMs);
    }

    @Override
    public void onPrice(String ccyPair, double rate) {
        log.info("{} started processing ccyPair={}, rate={}", name, ccyPair, rate);
        try {
            Thread.sleep(sleepTimeoutMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("{} finished processing ccyPair={}, rate={}", name, ccyPair, rate);
    }

    @Override
    public void subscribe(PriceProcessor priceProcessor) {
        //no implementation is intended
    }

    @Override
    public void unsubscribe(PriceProcessor priceProcessor) {
        //no implementation is intended
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subscriber that = (Subscriber) o;

        return name.equals(that.name);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Subscriber{");
        sb.append("name='").append(name).append('\'');
        sb.append(", sleepTimeoutMs=").append(sleepTimeoutMs);
        sb.append('}');
        return sb.toString();
    }
}
