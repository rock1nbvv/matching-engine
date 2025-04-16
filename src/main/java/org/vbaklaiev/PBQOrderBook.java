package org.vbaklaiev;

import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Matching speed - O(log n)
 * Insertion speed - O(log n)
 * Cancellation speed - O(1)find O(n) removal
 * thread-safe
 */
public class PBQOrderBook {

    private final PriorityBlockingQueue<Order> buyQueue = new PriorityBlockingQueue<>(11,
            Comparator.comparing(Order::getPrice).reversed()
                    .thenComparing(Order::getTimestamp));

    private final PriorityBlockingQueue<Order> sellQueue = new PriorityBlockingQueue<>(11,
            Comparator.comparing(Order::getPrice)
                    .thenComparing(Order::getTimestamp));

    private final ConcurrentMap<String, Order> orderIndex = new ConcurrentHashMap<>();

    public void addOrder(Order order) {
        orderIndex.put(order.id, order);
        (order.side == Order.Side.BUY ? buyQueue : sellQueue).add(order);
    }

    public boolean cancelOrder(String orderId) {
        Order order = orderIndex.remove(orderId);
        if (order == null) return false;
        order.isCancelled = true; // optional lazy cancel
        return true;
    }

    public Order matchNext(Order.Side side) {
        PriorityBlockingQueue<Order> queue = (side == Order.Side.BUY) ? sellQueue : buyQueue;
        while (true) {
            Order top = queue.peek();
            if (top == null) return null;
            if (top.isCancelled) {
                queue.poll(); // discard cancelled
                continue;
            }
            return queue.poll();
        }
    }
}
