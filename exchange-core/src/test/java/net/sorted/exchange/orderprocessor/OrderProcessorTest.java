package net.sorted.exchange.orderprocessor;


import java.util.List;
import net.sorted.exchange.OrderIdDao;
import net.sorted.exchange.OrderIdDaoInMemory;
import net.sorted.exchange.OrderSnapshotPublisher;
import net.sorted.exchange.PrivateTradePublisher;
import net.sorted.exchange.PublicTradePublisher;
import net.sorted.exchange.TradeIdDaoInMemory;
import net.sorted.exchange.Order;
import net.sorted.exchange.orderbook.OrderBook;
import net.sorted.exchange.orderbook.OrderBookInMemory;
import net.sorted.exchange.orderbook.OrderBookSnapshot;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static net.sorted.exchange.Side.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OrderProcessorTest {

    private OrderProcessor orderProcessor;
    OrderBook orderBook;
    PrivateTradePublisher privateTradePublisher;
    PublicTradePublisher publicTradePublisher;
    OrderSnapshotPublisher snapshotPublisher;

    @Before
    public void before() {

        orderBook = new OrderBookInMemory(new TradeIdDaoInMemory());
        OrderIdDao orderIdDao = new OrderIdDaoInMemory();
        privateTradePublisher = mock(PrivateTradePublisher.class);
        publicTradePublisher = mock(PublicTradePublisher.class);
        snapshotPublisher = mock(OrderSnapshotPublisher.class);

        orderProcessor = new OrderProcessorInMemory(orderBook, privateTradePublisher, publicTradePublisher, snapshotPublisher);
    }

    @Test
    public void testOrderSubmitToEmptyOrderBook() {
        orderProcessor.submitOrder(new Order("0", (double) 100.0, BUY, 1000, "USDAUD"));

        // No matches with only 1 order in the book ...

        // ... so no private trades (for aggressor or passive)
        ArgumentCaptor<List> privateTradeCaptor = ArgumentCaptor.forClass(List.class);
        verify(privateTradePublisher, times(2)).publishTrades(privateTradeCaptor.capture());
        assertEquals(0, privateTradeCaptor.getAllValues().get(0).size());
        assertEquals(0, privateTradeCaptor.getAllValues().get(1).size());

        // ... so no public trades
        ArgumentCaptor<List> publicTradeCaptor = ArgumentCaptor.forClass(List.class);
        verify(publicTradePublisher, times(1)).publishTrades(publicTradeCaptor.capture());
        assertEquals(0, publicTradeCaptor.getValue().size());

        // But there will be one buy, so level 1 on buy side will have a volume of that trade but there will be nothing on sell side
        ArgumentCaptor<OrderBookSnapshot> snapshotCaptor = ArgumentCaptor.forClass(OrderBookSnapshot.class);
        verify(snapshotPublisher, times(1)).publishSnapshot(snapshotCaptor.capture());
        assertEquals(1, snapshotCaptor.getValue().getBuyLevels().size());
        assertEquals(1000, snapshotCaptor.getValue().getBuyLevels().get(0).getVolume());
        assertEquals(0, snapshotCaptor.getValue().getSellLevels().size());

    }

    @Test
    public void testOrderSubmitWithMatches() {
        orderProcessor.submitOrder(new Order("0", (double) 90.0, BUY, 500, "USDAUD"));
        orderProcessor.submitOrder(new Order("10", (double) 100.0, BUY, 500, "USDAUD"));
        orderProcessor.submitOrder(new Order("20", (double) 100.0, BUY, 500, "USDAUD"));
        orderProcessor.submitOrder(new Order("30", (double) 100.0, SELL, 1000, "USDAUD"));

        // 2 BUYs match the aggressor SELL

        // ... so 1 private trades for aggressor and 2 passive trades for buyers
        // NB each submitOrder will cause 2 calls to privateTradePublisher, we are only interested in the last 2 (6 and 7)
        ArgumentCaptor<List> privateTradeCaptor = ArgumentCaptor.forClass(List.class);
        verify(privateTradePublisher, times(8)).publishTrades(privateTradeCaptor.capture());
        assertEquals(1, privateTradeCaptor.getAllValues().get(6).size());
        assertEquals(2, privateTradeCaptor.getAllValues().get(7).size());

        // ... 1 public trade
        ArgumentCaptor<List> publicTradeCaptor = ArgumentCaptor.forClass(List.class);
        verify(publicTradePublisher, times(4)).publishTrades(publicTradeCaptor.capture());
        assertEquals(1, publicTradeCaptor.getAllValues().get(3).size());

        // One Order left in the book as the others have been matched which will be shown in the snapshot
        ArgumentCaptor<OrderBookSnapshot> snapshotCaptor = ArgumentCaptor.forClass(OrderBookSnapshot.class);
        verify(snapshotPublisher, times(4)).publishSnapshot(snapshotCaptor.capture());
        OrderBookSnapshot snapshot = snapshotCaptor.getAllValues().get(3);
        assertEquals(1, snapshot.getBuyLevels().size());
        assertEquals(500, snapshot.getBuyLevels().get(0).getVolume());
        assertEquals(0, snapshot.getSellLevels().size());

    }
}