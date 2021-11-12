package com.price.processor;

import com.price.processor.rates.CurrencyPairRate;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <p>Intermediate cache for storing currency rates</>
 * <p>Each time the rate is added, it's being ranked considering its update frequency</p>
 * <p>rank is number [0, 1] inclusively, rank = 1 / inputCount, where inputCount is how many times the rate was received</p>
 * <p>Each time rate is being requested through {@link RankingBuffer#pop()} method, the highest rate is returned from cache.
 * Thus we can get the less frequently updated rates that are prioritized for slow consumers. Fast and frequently updated
 * rates are being updated in cache meanwhile, so when their turn comes we can get the latest updated value
 * </p>
 */
public class RankingBuffer {

    private static final Logger log = LoggerFactory.getLogger(RankingBuffer.class);
    private static final Map<CurrencyPairRate, Statistics> stats = new HashMap<>();


    public void push(CurrencyPairRate ccyPairRate) {
        var prev = stats.get(ccyPairRate);
        if (prev == null) {
            stats.put(ccyPairRate, Statistics.empty(ccyPairRate));
        } else {
            var inputCount = prev.getInputCount();
            inputCount++;
            stats.put(ccyPairRate, Statistics.of(ccyPairRate, inputCount));
        }
        log.info("Updated rank for {} : {}", ccyPairRate.getCcyPair(), stats.get(ccyPairRate).getRank());
    }

    /**
     * Retrieves currency pair rate with the highest rank (the least frequently updated) and removes it from cache
     *
     * @return currency pair rate with the highest rank
     */
    public Optional<CurrencyPairRate> pop() {
        var highestRankPair = getHighestRankCcyPair();
        if (highestRankPair.isEmpty()) {
            return Optional.empty();
        }
        var highestRankStats = stats.get(highestRankPair.get());
        stats.remove(highestRankPair.get());
        return Optional.of(highestRankStats.getCcyPairRate());
    }

    public boolean isEmpty() {
        return stats.isEmpty();
    }

    private Optional<CurrencyPairRate> getHighestRankCcyPair() {
        return stats.values().stream()
                .max(Comparator.comparing(Statistics::getRank))
                .map(Statistics::getCcyPairRate);
    }


    @Getter
    private static class Statistics {

        private CurrencyPairRate ccyPairRate;
        private long inputCount;

        private Statistics() {
        }

        private Statistics(CurrencyPairRate ccyPairRate, long inputCount) {
            this.ccyPairRate = ccyPairRate;
            this.inputCount = inputCount;
        }

        public static Statistics empty(CurrencyPairRate ccyPairRate) {
            return new Statistics(ccyPairRate, 1);
        }

        public static Statistics of(CurrencyPairRate ccyPairRate, long inputCount) {
            return new Statistics(ccyPairRate, inputCount);
        }

        /**
         * rank = 1 / inputCount values: [0, 1] -> more frequently updated pairs have a lower rank
         * <p>
         * rates with higher rank must be sent in first place
         *
         * @return calculated rank
         */

        public double getRank() {
            return 1 / (double) inputCount;
        }

    }


}
