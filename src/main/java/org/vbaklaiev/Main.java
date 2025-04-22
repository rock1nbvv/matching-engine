package org.vbaklaiev;

public class Main {
    public static void main(String[] args) {
        OrderBook TrorderBook = new TreeMapOrderBook();

        TrorderBook.submitOrder(new Order(1L, "alice", 100, 10, Side.SELL));
        TrorderBook.submitOrder(new Order(2L, "bob", 105, 5, Side.BUY)); // matches Alice

        TrorderBook.cancelOrder("alice", 1L); // cancels remaining quantity (if any)
    }
}