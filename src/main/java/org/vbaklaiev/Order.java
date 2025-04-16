package org.vbaklaiev;

import java.math.BigDecimal;
import java.time.Instant;

public class Order {
    public enum Side {BUY, SELL}

    final String id;
    final Side side;
    final BigDecimal price;
    final Instant timestamp;
    volatile boolean isCancelled;

    public Order(String id, Side side, BigDecimal price, int quantity) {
        this.id = id;
        this.side = side;
        this.price = price;
        this.timestamp = Instant.now();
    }

    Instant getTimestamp() {
        return this.timestamp;
    }

    BigDecimal getPrice() {
        return this.price;
    }
}
