package net.sorted.exchange.web.msghandler;


import java.io.IOException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import net.sorted.exchange.config.RabbitMqConfig;
import net.sorted.exchange.messages.ExchangeMessage;
import net.sorted.exchange.web.ClientOrderSnapshot;
import net.sorted.exchange.web.OrderSnapshotCache;
import net.sorted.exchange.web.SnapshotConverter;
import net.sorted.exchange.web.WebSocketSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SnapshotListener {

    private final WebSocketSender webSocketSender;
    private final Channel snapshotChannel;
    private final Consumer consumer;

    private final OrderSnapshotCache snapshotCache;

    private Logger log = LogManager.getLogger(SnapshotListener.class);

    @Autowired
    public SnapshotListener(WebSocketSender webSocketSender, @Qualifier("snapshotChannel") Channel snapshotChannel, OrderSnapshotCache snapshotCache) throws IOException {
        this.webSocketSender = webSocketSender;
        this.snapshotChannel = snapshotChannel;
        this.snapshotCache = snapshotCache;

        String queueName = snapshotChannel.queueDeclare().getQueue();
        snapshotChannel.queueBind(queueName, RabbitMqConfig.PUBLISH_SNAPSHOT_EXCHANGE_NAME, "");

        consumer = new DefaultConsumer(snapshotChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                ExchangeMessage.OrderBookSnapshot message = ExchangeMessage.OrderBookSnapshot.parseFrom(body);
                sendSnapshotToAll(message);
            }
        };

        snapshotChannel.basicConsume(queueName, true, consumer);
        log.debug("Started listening for snapshots");
    }

    private void sendSnapshotToAll(ExchangeMessage.OrderBookSnapshot message) {

        ClientOrderSnapshot s = SnapshotConverter.messageToClient(message);
        String instrumentId = message.getInstrumentId();

        snapshotCache.setSnapshot(instrumentId, s);

        webSocketSender.sendMessage("/topic/snapshot/"+instrumentId, s);
        log.debug("Sent snapshot for instrument {} message= {}", instrumentId, s);
    }

}
