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
    public List<Trade> submitOrder(Order incoming) {
        List<Trade> trades = new ArrayList<>();
        TreeMap<Integer, LinkedList<Order>> book = (incoming.side == Side.BUY) ? sellBook : buyBook;

        Iterator<Map.Entry<Integer, LinkedList<Order>>> it = book.entrySet().iterator();
        while (incoming.volume > 0 && it.hasNext()) {
            Map.Entry<Integer, LinkedList<Order>> entry = it.next();
            int bookPrice = entry.getKey();

            boolean priceMatch = incoming.side == Side.BUY
                    ? incoming.price >= bookPrice
                    : incoming.price <= bookPrice;
            if (!priceMatch) break;

            LinkedList<Order> queue = entry.getValue();
            while (incoming.volume > 0 && !queue.isEmpty()) {
                Order resting = queue.peekFirst();
                int tradedVolume = Math.min(incoming.volume, resting.volume);
                trades.add(new Trade(
                        incoming.side == Side.BUY ? incoming.username : resting.username,
                        incoming.side == Side.BUY ? resting.username : incoming.username,
                        resting.price,
                        tradedVolume
                ));

                incoming.volume -= tradedVolume;
                resting.volume -= tradedVolume;

                if (resting.volume == 0) {
                    queue.pollFirst();
                    orderIndex.remove(resting.id);
                }
            }

            if (queue.isEmpty()) it.remove();
        }

        if (incoming.volume > 0) {
            TreeMap<Integer, LinkedList<Order>> ownBook = incoming.side == Side.BUY ? buyBook : sellBook;
            LinkedList<Order> queue = ownBook.computeIfAbsent(incoming.price, k -> new LinkedList<>());
            queue.addLast(incoming);

            OrderRef ref = new OrderRef();
            ref.price = incoming.price;
            ref.queue = queue;
            ref.order = incoming;
            orderIndex.put(incoming.id, ref);
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