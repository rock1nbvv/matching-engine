package org.vbaklaiev;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 *
 Feature	Logic
 Price priority	TreeMap: sorted ascending (sell) or descending (buy)
 Time priority	LinkedList: appends at tail, removes from head
 Matching	Iterates TreeMap + peeks queue
 Cancellation	O(1) via orderIndex map (with username check)
 Partial fills	Handled naturally by reducing volume
 */
public class TreeMapOrderBook implements OrderBook {

    private final TreeMap<Integer, LinkedList<Order>> buyBook = new TreeMap<>(Comparator.reverseOrder());
    private final TreeMap<Integer, LinkedList<Order>> sellBook = new TreeMap<>();
    private final Map<Long, OrderRef> orderIndex = new HashMap<>();

    private static class OrderRef {
        int price;
        LinkedList<Order> queue;
        Order order;
    }

    @Override
    public List<Trade> submitOrder(Order order) {
        List<Trade> trades = new ArrayList<>();
        TreeMap<Integer, LinkedList<Order>> book = (order.side == Side.BUY) ? sellBook : buyBook;

        Iterator<Map.Entry<Integer, LinkedList<Order>>> it = book.entrySet().iterator();
        while (order.volume > 0 && it.hasNext()) {
            Map.Entry<Integer, LinkedList<Order>> entry = it.next();
            int bookPrice = entry.getKey();

            boolean priceMatch = order.side == Side.BUY
                    ? order.price >= bookPrice
                    : order.price <= bookPrice;
            if (!priceMatch) break;

            LinkedList<Order> queue = entry.getValue();
            while (order.volume > 0 && !queue.isEmpty()) {
                Order resting = queue.peekFirst();
                int tradedVolume = Math.min(order.volume, resting.volume);
                trades.add(new Trade(
                        order.side == Side.BUY ? order.username : resting.username,
                        order.side == Side.BUY ? resting.username : order.username,
                        resting.price,
                        tradedVolume
                ));

                order.volume -= tradedVolume;
                resting.volume -= tradedVolume;

                if (resting.volume == 0) {
                    queue.pollFirst();
                    orderIndex.remove(resting.id);
                }
            }

            if (queue.isEmpty()) it.remove();
        }

        if (order.volume > 0) {
            TreeMap<Integer, LinkedList<Order>> ownBook = order.side == Side.BUY ? buyBook : sellBook;
            LinkedList<Order> queue = ownBook.computeIfAbsent(order.price, k -> new LinkedList<>());
            queue.addLast(order);

            OrderRef ref = new OrderRef();
            ref.price = order.price;
            ref.queue = queue;
            ref.order = order;
            orderIndex.put(order.id, ref);
        }

        return trades;
    }

    @Override
    public void cancelOrder(String username, long orderId) {
        OrderRef ref = orderIndex.remove(orderId);
        if (ref == null || !ref.order.username.equals(username)) return;

        boolean removed = ref.queue.remove(ref.order);
        if (removed && ref.queue.isEmpty()) {
            TreeMap<Integer, LinkedList<Order>> book =
                    ref.order.side == Side.BUY ? buyBook : sellBook;
            book.remove(ref.price);
        }
    }
}