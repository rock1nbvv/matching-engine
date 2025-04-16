package org.vbaklaiev;

import java.math.BigDecimal;
import java.time.Instant;

public class Order {
    public final long id;
    public final String username;
    public int price;
    public int volume;
    public final Side side;
    public final Instant timestamp;

    public Order(long id, String username, int price, int volume, Side side) {
        this.id = id;
        this.username = username;
        this.price = price;
        this.volume = volume;
        this.side = side;
        this.timestamp = Instant.now();
    }
}
