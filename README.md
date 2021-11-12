# Price processor

## Task

You have to write a PriceThrottler class which will implement the following requirements:

1) Implement PriceProcessor interface
2) Distribute updates to its listeners which are added through subscribe() and removed through unsubscribe()
3) Some subscribers are very fast (i.e. onPrice() for them will only take a microsecond) and some are very slow
   (onPrice() might take 30 minutes). Imagine that some subscribers might be showing a price on a screen and some might
   be printing them on a paper
4) Some ccyPairs change rates 100 times a second and some only once or two times a day
5) ONLY LAST PRICE for each ccyPair matters for subscribers. I.e. if a slow subscriber is not coping with updates for
   EURUSD - it is only important to deliver the latest rate
6) It is important not to miss rarely changing prices. I.e. it is important to deliver EURRUB if it ticks once per day
   but you may skip some EURUSD ticking every second
7) You don't know in advance which ccyPair are frequent and which are rare. Some might become more frequent at different
   time of a day
8) You don't know in advance which of the subscribers are slow and which are fast.
9) Slow subscribers should not impact fast subscribers

In short words the purpose of PriceThrottler is to solve for slow consumers

## Implementation

- When `PriceThrottler` receives update on currency pair rate, it adds it to `RankingBuffer` - some kind of intermediate
  cache - for each existing subscriber
- Each subscriber has its own `RankingBuffer` where all the unprocessed rates are stored
- `RankingBuffer` ranks each currency pair rate based on how frequent the updates are
- Rank is calculated as `1/total rate income count`, so it's values are bounded between [0,1]
- The more frequent update is - the less rank it gets. Thus, the less frequently updated rates are to be processed
  first. It's important for slow consumers not to lose such updates, while frequently changed rates are constantly
  rewritten in cache. So using the buffer's pop() method works like it's some kind of queue.
- When all the "slow" rates are being processed - then subscriber can take any of remaining "fast" rate for further
  processing
- When subscriber is added to throttler service two things happen: RankingBuffer for the subscriber is created and
  processing task is added to execution pool. This processing task is constantly polling it's buffer-"queue" to get the
  most ranked rate for processing
- When subscriber is being unsubscribed from throttler, it's tasks are removed from executor and the buffer with rates
  is cleared

## How to run

```
./gradlew run
```

Then you can input currency pairs and rates separated with space, i.e.:

```
USD/RUB 77.1 BTC/RUB 4635984.17 USD/EUR 0.87 BTC/RUB 4635984.16 BTC/RUB 4635984.15 BTC/RUB 4635984.14 BTC/RUB 4635984.13 BTC/RUB 4635984.12 BTC/RUB 4635984.11
```

Also there's some set of unit tests

Also there're `CcyRateDatasource` and `RateGenerator` classes which generate a big amount of constantly changing rates (
these are not unit tests, just data stream emulation to play with)