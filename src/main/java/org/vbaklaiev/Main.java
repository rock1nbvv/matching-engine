package org.vbaklaiev;

import java.math.BigDecimal;
import java.util.List;

public class Main {
    public static void main(String[] args) {
//        PBQOrderBook pbq = new PBQOrderBook();
//        Order o1 = new Order("1", Order.Side.SELL, new BigDecimal("100.00"), 10);
//        Order o2  = new Order("2", Order.Side.SELL, new BigDecimal("99.00"), 5);
//        pbq.addOrder(o1);
//        pbq.addOrder(o2);
//        pbq.cancelOrder("1");
//        System.out.println("PBQ match: " + pbq.matchNext(Side.BUY).id);
//
//        TreeMapOrderBook orderBook = new TreeMapOrderBook();
//        Order b1 = new Order("A1", Side.BUY, new BigDecimal("100.50"), 10);
//        Order b2 = new Order("A2", Side.BUY, new BigDecimal("101.00"), 5);
//        Order s3 = new Order("S1", Side.SELL, new BigDecimal("101.00"), 5);
//
//        orderBook.addOrder(b1);
//        orderBook.addOrder(b2);
//        orderBook.addOrder(s3);
//
//        System.out.println("Tree match: " + orderBook.matchNext(Side.BUY).id);

        OrderBook orderBook = new PriorityQueueOrderBook();

        // Add a sell order: Alice wants to sell 100 units at $50
        Order sellOrder = new Order(1L, "alice", 50, 100, Side.SELL);
        List<Trade> trades1 = orderBook.submitOrder(sellOrder);
        System.out.println("Alice's order submitted, trades: " + trades1.size());

        // Add a buy order: Bob wants to buy 50 units at $55 (price match)
        Order buyOrder = new Order(2L, "bob", 55, 50, Side.BUY);
        List<Trade> trades2 = orderBook.submitOrder(buyOrder);

        System.out.println("Bob's order matched:");
        for (Trade trade : trades2) {
            System.out.printf("Buyer: %s, Seller: %s, Price: %d, Volume: %d%n",
                    trade.buyer, trade.seller, trade.price, trade.volume);
        }

        // Cancel remaining order (should be Alice's residual 50 units at $50)
        orderBook.cancelOrder("alice", 1L);
        System.out.println("Alice's remaining order cancelled.");
    }
}