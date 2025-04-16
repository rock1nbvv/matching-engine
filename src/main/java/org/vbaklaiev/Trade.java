package org.vbaklaiev;

class Trade {
    String buyer;
    String seller;
    int price;
    int volume;
    public Trade(String buyer, String seller, int price, int volume) {
        this.buyer = buyer;
        this.seller = seller;
        this.price = price;
        this.volume = volume;
    }
}