package org.vbaklaiev;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        PBQOrderBook pbq = new PBQOrderBook();
        Order o1 = new Order("1", Order.Side.SELL, new BigDecimal("100.00"), 10);
        Order o2  = new Order("2", Order.Side.SELL, new BigDecimal("99.00"), 5);
        pbq.addOrder(o1);
        pbq.addOrder(o2);
        pbq.cancelOrder("1");
        System.out.println("PBQ match: " + pbq.matchNext(Order.Side.BUY).id);

        TreeMapOrderBook orderBook = new TreeMapOrderBook();
        Order b1 = new Order("A1", Order.Side.BUY, new BigDecimal("100.50"), 10);
        Order b2 = new Order("A2", Order.Side.BUY, new BigDecimal("101.00"), 5);
        Order s3 = new Order("S1", Order.Side.SELL, new BigDecimal("101.00"), 5);

        orderBook.addOrder(b1);
        orderBook.addOrder(b2);
        orderBook.addOrder(s3);

        System.out.println("Tree match: " + orderBook.matchNext(Order.Side.BUY).id);


    }
}