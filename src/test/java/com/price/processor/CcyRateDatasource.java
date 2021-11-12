package com.price.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Anton Mikhaylov on 08.11.2021.
 */
public class CcyRateDatasource {

    private static final int FAST_CHANGE_PERIOD_MS = 10; //100 times per second
    private static final int SLOW_CHANGE_PERIOD_MS = 10000; //1 time per 10 seconds
    private static final int FAST_CHANGING_PAIRS_COUNT = 10; //How many currency pairs will be frequently changed (others will be changed slowly)

    //How big each rate change will be (newValue is oldValue +/- oldValue * percentage%)
    private static final BigDecimal DIFF = BigDecimal.valueOf(10).setScale(2, RoundingMode.HALF_UP).divide(BigDecimal.valueOf(100));

    private static final Map<String, Double> STORAGE = new ConcurrentHashMap<>();
    private static final Set<String> FAST_CHANGING_CURRENCIES = new HashSet<>();
    private static final Set<String> SLOW_CHANGING_CURRENCIES = new HashSet<>();

    static {
        STORAGE.putAll(RatesGenerator.generate());
        int i = 0;
        for (String ccy : STORAGE.keySet()) {
            if (i >= FAST_CHANGING_PAIRS_COUNT) {
                break;
            }
            FAST_CHANGING_CURRENCIES.add(ccy);
            i++;
        }
        STORAGE.keySet()
                .stream()
                .filter(ccyPair -> !FAST_CHANGING_CURRENCIES.contains(ccyPair))
                .forEach(SLOW_CHANGING_CURRENCIES::add);

    }

    private final PriceProcessor processor;
    Logger log = LoggerFactory.getLogger(CcyRateDatasource.class);

    public CcyRateDatasource(PriceProcessor processor) {
        this.processor = processor;
        log.info("CcyRateDatasource initialized with data: {}", STORAGE.toString());
        log.info("The next currency pairs will be changed each {} ms: {}", FAST_CHANGE_PERIOD_MS, FAST_CHANGING_CURRENCIES);
    }

    public void scheduleDataUpdate() {

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(FAST_CHANGING_CURRENCIES.size());

        FAST_CHANGING_CURRENCIES.forEach(ccyPair -> {
            executor.scheduleAtFixedRate(() -> {
                updateRate(ccyPair);
                processor.onPrice(ccyPair, STORAGE.get(ccyPair));
            }, 1000, FAST_CHANGE_PERIOD_MS, TimeUnit.MILLISECONDS);
        });


        SLOW_CHANGING_CURRENCIES.forEach(ccyPair -> {
            executor.scheduleAtFixedRate(() -> {
                updateRate(ccyPair);
                processor.onPrice(ccyPair, STORAGE.get(ccyPair));
            }, 1000, SLOW_CHANGE_PERIOD_MS, TimeUnit.MILLISECONDS);
        });


    }

    private void updateRate(String ccyPair) {
        BigDecimal old = BigDecimal.valueOf(STORAGE.get(ccyPair)).setScale(2, RoundingMode.HALF_UP);

        if (new Random().nextInt(2) == 0) {
            STORAGE.put(ccyPair, old.subtract(DIFF).doubleValue());
            return;
        }
        STORAGE.put(ccyPair, old.add(DIFF).doubleValue());
    }


}
