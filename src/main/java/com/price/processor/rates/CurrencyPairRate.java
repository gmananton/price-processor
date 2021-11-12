package com.price.processor.rates;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Currency pair and its price (rate)
 */
@Getter
@Setter
@RequiredArgsConstructor
public class CurrencyPairRate {

    private final String ccyPair;
    private final double rate;

    @Override
    public int hashCode() {
        return ccyPair.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CurrencyPairRate that = (CurrencyPairRate) o;

        return ccyPair.equals(that.ccyPair);
    }
}
