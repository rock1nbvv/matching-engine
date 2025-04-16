package org.vbaklaiev;

import java.util.List;

public interface OrderBook {
    List<Trade> submitOrder(Order order);
    void cancelOrder(String username, long orderId);
}

