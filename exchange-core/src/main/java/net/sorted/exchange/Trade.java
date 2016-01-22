package net.sorted.exchange;


import org.joda.time.DateTime;

public class Trade {
    private final String tradeId;
    private final String orderId;
    private final String instrumentId;
    private final long quantity;
    private final double price;
    private final Side side;
    private final DateTime tradeDate;

    public Trade(String tradeId, String orderId, String instrumentId, long quantity, double price, Side side, DateTime tradeDate) {
        this.tradeId = tradeId;
        this.orderId = orderId;
        this.instrumentId = instrumentId;
        this.quantity = quantity;
        this.price = price;
        this.side = side;
        this.tradeDate = tradeDate;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public long getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public DateTime getTradeDate() {
        return tradeDate;
    }
}