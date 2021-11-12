package com.price.processor.publisher;

import com.price.processor.PriceProcessor;
import com.price.processor.RankingBuffer;
import com.price.processor.rates.CurrencyPairRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>Price throttler implementation</p>
 * <p>When receiving updated rates it distributes it over individual consumer queues for later processing</p>
 * <p>Each "consumer queue" is actually a cache of currency rates that is being ranked using {@link RankingBuffer},
 * so it delivers only the highly ranked rates for further processing</p>
 * <p>When subscribing a client for further rates processing, the task is added to a thread pool</p>
 */
public class PriceThrottler implements PriceProcessor, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(PriceThrottler.class);

    /**
     * Pool for adding consumers' processing tasks
     */
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Each consumer takes its own ranked rates cache
     */
    private static final Map<PriceProcessor, RankingBuffer> consumerQueues = new ConcurrentHashMap<>();

    /**
     * Storing added tasks for later cancelling when consumer is unsubscribed
     */
    private static final Map<PriceProcessor, Runnable> tasks = new ConcurrentHashMap<>();


    @Override
    public void onPrice(String ccyPair, double rate) {
        var ccyPairRate = new CurrencyPairRate(ccyPair, rate);
        consumerQueues.values().forEach(buffer -> buffer.push(ccyPairRate));
    }

    /**
     * <p>When subscribing a consumer for processing currency rates,</p>
     * <p>- The new record for this consumer is created in ranking buffer cache</p>
     * <p>- The new polling task  is created</p>
     *
     * @param consumer subscriber processing client
     */
    @Override
    public void subscribe(PriceProcessor consumer) {
        consumerQueues.put(consumer, new RankingBuffer());
        var task = createProcessingTask(consumer);
        executor.submit(task);
        tasks.put(consumer, task);
    }

    @Override
    public void unsubscribe(PriceProcessor consumer) {
        consumerQueues.remove(consumer);
        ((ThreadPoolExecutor) executor).remove(tasks.get(consumer));
        tasks.remove(consumer);
    }

    /**
     * Each task is constantly polling cache for consumer's unprocessed rates
     *
     * @param consumer
     * @return callable task for adding to execution pool
     */
    private Runnable createProcessingTask(PriceProcessor consumer) {
        log.info("Creating processing task for {}", consumer.toString());

        return () -> {
            while (true) {
                var buffer = consumerQueues.get(consumer);
                if (buffer.isEmpty()) {
                    continue;
                }

                var rate = buffer.pop();
                if (rate.isPresent()) {
                    consumer.onPrice(rate.get().getCcyPair(), rate.get().getRate());
                }
            }
        };
    }

    @Override
    public void close() throws Exception {
        consumerQueues.clear();
        executor.shutdown();
    }
}
