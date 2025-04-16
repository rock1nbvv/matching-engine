package org.vbaklaiev;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enforces price-time priority using PriorityQueue with Comparator.
 * Matches against the best priced opposite orders.
 * Efficient insert/match, O(n) cancellation due to heap scan.
 * Trade contains correct buyer/seller names, price, and volume.
 */

public class PriorityQueueOrderBook implements OrderBook {

    private final PriorityQueue<Order> buyBook = new PriorityQueue<>(
            Comparator.comparingInt((Order o) -> o.price).reversed()
                    .thenComparing(o -> o.timestamp)
    );

    private final PriorityQueue<Order> sellBook = new PriorityQueue<>(
            Comparator.comparingInt((Order o) -> o.price)
                    .thenComparing(o -> o.timestamp)
    );

    private final Map<Long, Order> orderIndex = new ConcurrentHashMap<>();

    @Override
    public List<Trade> submitOrder(Order incoming) {
        List<Trade> trades = new ArrayList<>();
        PriorityQueue<Order> oppositeBook = (incoming.side == Side.BUY) ? sellBook : buyBook;

        while (incoming.volume > 0 && !oppositeBook.isEmpty()) {
            Order top = oppositeBook.peek();

            boolean priceMatch = incoming.side == Side.BUY
                    ? incoming.price >= top.price
                    : incoming.price <= top.price;

            if (!priceMatch) break;

            int tradedVolume = Math.min(incoming.volume, top.volume);
            int tradePrice = top.price;

            trades.add(new Trade(
                    incoming.side == Side.BUY ? incoming.username : top.username,
                    incoming.side == Side.BUY ? top.username : incoming.username,
                    tradePrice,
                    tradedVolume
            ));

            incoming.volume -= tradedVolume;
            top.volume -= tradedVolume;

            if (top.volume == 0) {
                oppositeBook.poll();
                orderIndex.remove(top.id);
            }
        }

        if (incoming.volume > 0) {
            PriorityQueue<Order> sameBook = incoming.side == Side.BUY ? buyBook : sellBook;
            sameBook.add(incoming);
            orderIndex.put(incoming.id, incoming);
        }

        return trades;
    }

    @Override
    public void cancelOrder(String username, long orderId) {
        Order order = orderIndex.remove(orderId);
        if (order == null || !order.username.equals(username)) {
            return; // not found or not authorized
        }
        // Linear time removal from PQ
        PriorityQueue<Order> book = order.side == Side.BUY ? buyBook : sellBook;
        book.remove(order);
    }
}
