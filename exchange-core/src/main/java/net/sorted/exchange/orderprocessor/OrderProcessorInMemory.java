package net.sorted.exchange.orderprocessor;


import net.sorted.exchange.OrderSnapshotPublisher;
import net.sorted.exchange.PrivateTradePublisher;
import net.sorted.exchange.PublicTradePublisher;
import net.sorted.exchange.orderbook.MatchedTrades;
import net.sorted.exchange.Order;
import net.sorted.exchange.orderbook.OrderBook;
import net.sorted.exchange.orderbook.OrderBookSnapshot;

public class OrderProcessorInMemory implements OrderProcessor {

    private final OrderBook orderBook;
    private final PrivateTradePublisher privateTradePublisher;
    private final PublicTradePublisher publicTradePublisher;
    private final OrderSnapshotPublisher snapshotPublisher;

    private final Object lock = new Object();

    public OrderProcessorInMemory(OrderBook orderBook,
                                  PrivateTradePublisher privateTradePublisher,
                                  PublicTradePublisher publicTradePublisher,
                                  OrderSnapshotPublisher snapshotPublisher) {
        this.orderBook = orderBook;
        this.privateTradePublisher = privateTradePublisher;
        this.publicTradePublisher = publicTradePublisher;
        this.snapshotPublisher = snapshotPublisher;
    }


    @Override
    public void submitOrder(Order order) {

        MatchedTrades matches = null;
        OrderBookSnapshot snapshot = null;
        synchronized (lock) {
            matches = orderBook.addOrder(order);
            snapshot = orderBook.getSnapshot();
        }

        privateTradePublisher.publishTrades(matches.getAggressorTrades());
        privateTradePublisher.publishTrades(matches.getPassiveTrades());
        publicTradePublisher.publishTrades(matches.getPublicTrades());
        snapshotPublisher.publishSnapshot(snapshot);
    }

    @Override
    public void updateOrder(Order order) {

    }

    @Override
    public void cancelOrder(Order order) {

    }


//    private Order getOrder(ExchangeOrder exchangeOrder) {
//        String orderId = orderIdDao.getNextOrderId();
//        Order o = new Order(orderId,
//                Double.parseDouble(exchangeOrder.getPrice()),
//                exchangeOrder.getSide(),
//                exchangeOrder.getQuantity(),
//                exchangeOrder.getInstrument());
//
//        return o;
//    }
}