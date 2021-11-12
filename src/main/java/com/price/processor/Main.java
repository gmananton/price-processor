package com.price.processor;

import com.price.processor.publisher.PriceThrottler;
import com.price.processor.subscriber.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;


/**
 * Created by Anton Mikhaylov on 08.11.2021.
 */
public class Main {


    public static void main(String[] args) {

        Logger log = LoggerFactory.getLogger(Main.class);

        PriceProcessor service = new PriceThrottler();
        PriceProcessor clientFast = new Subscriber("client_fast");
        PriceProcessor clientSlow = new Subscriber("client_slow", 5000);

        service.subscribe(clientSlow);
        service.subscribe(clientFast);

        var reader = new Scanner(System.in);
        while (reader.hasNextLine()) {
            var cmd = reader.nextLine();
            var inputArgs = cmd.trim().split("\\s");
            for (int i = 0; i < inputArgs.length; i += 2) {
                try {
                    service.onPrice(inputArgs[i], Double.parseDouble(inputArgs[i + 1]));
                } catch (NumberFormatException e) {
                    log.error("Incorrect number format for price: " + e.getMessage());
                }
            }
        }
        reader.close();
    }


}
