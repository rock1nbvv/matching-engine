package org.vbaklaiev;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Price Priority	Efficient with TreeMap (O(log n))
 * Time Priority	Maintained by LinkedList at each price
 * Order Cancellation	Fast: O(1) with orderId → OrderRef map
 * Matching Speed	O(log n) to find best price + O(1) to remove from price level
 * Insert/Match	O(log n) + O(1)
 */
//Internally, a PriorityQueue is implemented as a binary heap.
//It guarantees that the head (peek/poll) is always the highest-priority element (min or max depending on comparator).
//
//But the rest of the elements are not in sorted order when you iterate.
public class OLD_TreeMapOrderBook {
    // BUY book: highest price first; SELL book: lowest price first
    private final TreeMap<BigDecimal, LinkedList<Order>> buyBook = new TreeMap<>(Comparator.reverseOrder());
    private final TreeMap<BigDecimal, LinkedList<Order>> sellBook = new TreeMap<>();
    // Index to access and remove an order in O(1)
    /**
     * Suppose you want to cancel an order by its orderId. Without an index:
     * You must search every queue at every price level.
     * This is O(n) or worse, depending on depth and volume.
     * High cancellation frequency would make performance degrade quickly.
     */
    private final Map<String, OrderRef> orderIndex = new HashMap<>();// for efficient order cancellation and modification by order ID.


    // Internal reference to locate an order in the book efficiently
    private static class OrderRef {
        BigDecimal price;              // Price level (key in TreeMap)
        LinkedList<Order> queue;       // Queue at that price level
        Order order;                   // Reference to the actual order
    }

    public void addOrder(Order order) {
        TreeMap<BigDecimal, LinkedList<Order>> book = (order.side == Order.Side.BUY) ? buyBook : sellBook;
        // Get or create the price level (queue of orders)
        LinkedList<Order> queue = book.computeIfAbsent(order.price, k -> new LinkedList<>());
        queue.addLast(order);

        OrderRef ref = new OrderRef();
        ref.price = order.price;
        ref.queue = queue;
        ref.order = order;
        orderIndex.put(order.id, ref);
    }

    public boolean cancelOrder(String orderId) {
        // Look up the order location
        OrderRef ref = orderIndex.remove(orderId);
        if (ref == null) return false;

        boolean removed = ref.queue.remove(ref.order);
        if (ref.queue.isEmpty()) {
            TreeMap<BigDecimal, LinkedList<Order>> book = ref.order.side == Order.Side.BUY ? buyBook : sellBook;
            book.remove(ref.price);
        }
        return removed;
    }

    public Order matchNext(Order.Side side) {
        // If BUY is incoming, match against SELL book (lowest price)
        TreeMap<BigDecimal, LinkedList<Order>> book = (side == Order.Side.BUY) ? sellBook : buyBook;
        if (book.isEmpty()) return null;

        Map.Entry<BigDecimal, LinkedList<Order>> best = book.firstEntry();
        LinkedList<Order> queue = best.getValue();
        Order order = queue.pollFirst();
        if (queue.isEmpty()) {
            book.remove(best.getKey());
        }
        if (order != null) orderIndex.remove(order.id);
        return order;
    }
}
