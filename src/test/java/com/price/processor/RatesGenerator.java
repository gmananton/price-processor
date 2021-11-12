package com.price.processor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anton Mikhaylov on 08.11.2021.
 */
public class RatesGenerator {

    private static final List<String> currencies = List.of("AED", "AFN", "ALL", "AMD", "ANG", "AOA", "ARS", "AUD",
            "AWG", "AZN", "BAM", "BBD", "BDT", "BGN", "BHD", "BIF", "BMD", "BND", "BOB", "BRL", "BSD", "BTN", "BWP",
            "BYN", "BZD", "CAD", "CDF", "CHF", "CLP", "CNY", "COP", "CRC", "CUC", "CUP", "CVE", "CZK", "DJF", "DKK",
            "DOP", "DZD", "EGP", "ERN", "ETB", "EUR", "FJD", "FKP", "GBP", "GEL", "GGP", "GHS", "GIP", "GMD", "GNF",
            "GTQ", "GYD", "HKD", "HNL", "HRK", "HTG", "HUF", "IDR", "ILS", "IMP", "INR", "IQD", "IRR", "ISK", "JEP",
            "JMD", "JOD", "JPY", "KES", "KGS", "KHR", "KMF", "KPW", "KRW", "KWD", "KYD", "KZT", "LAK", "LBP", "LKR",
            "LRD", "LSL", "LYD", "MAD", "MDL", "MGA", "MKD", "MMK", "MNT", "MOP", "MRU", "MUR", "MVR", "MWK", "MXN",
            "MYR", "MZN", "NAD", "NGN", "NIO", "NOK", "NPR", "NZD", "OMR", "PAB", "PEN", "PGK", "PHP", "PKR", "PLN",
            "PYG", "QAR", "RON", "RSD", "RUB", "RWF", "SAR", "SBD", "SCR", "SDG", "SEK", "SGD", "SHP", "SLL", "SOS",
            "SPL", "SRD", "STN", "SVC", "SYP", "SZL", "THB", "TJS", "TMT", "TND", "TOP", "TRY", "TTD", "TVD", "TWD",
            "TZS", "UAH", "UGX", "USD", "UYU", "UZS", "VEF", "VND", "VUV", "WST", "XAF", "XCD", "XDR", "XOF", "XPF",
            "YER", "ZAR", "ZMW", "ZWD");

    public static Map<String, Double> generate() {
        Map<String, Double> rates = new HashMap<>();
        while (rates.size() < 200) {
            int lastPos = currencies.size() - 1;
            int firstPos = getRandomIntInclusive(0, lastPos);
            lastPos = getRandomIntInclusive(firstPos, lastPos);
            String pair = currencies.get(firstPos) + "/" + currencies.get(lastPos);
            rates.put(pair, getRandomRate());
        }
        return rates;
    }

    private static int getRandomIntInclusive(int min, int max) {
        return (int) (Math.random() * (max - min + 1) + min);
    }

    private static double getRandomRate() {
        double max = 50;
        double min = 0.1;
        return BigDecimal.valueOf(Math.random() * (max - min + 1) + min)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }


}
