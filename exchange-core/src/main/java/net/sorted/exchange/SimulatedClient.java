package net.sorted.exchange;


import java.io.IOException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import net.sorted.exchange.config.ExchangeConfig;
import net.sorted.exchange.config.RabbitMqConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SimulatedClient {

    Channel submit;
    String clientId = "dug";

    public SimulatedClient(Channel submit, String clientId) {
        this.submit = submit;
        this.clientId =clientId;
    }

    public void submitLimitOrder(String instrument, int qty, String price, String side) {
        String message = "{\"clientId\":\"" + clientId + "\",\"instrument\":\"" + instrument + "\",\"quantity\":" + qty + ",\"price\":\"" + price + "\",\"side\":\"" + side + "\",\"type\":\"LIMIT\"}";
        try {
            submit.basicPublish(RabbitMqConfig.ORDER_SUBMIT_EXCHANGE_NAME, "", MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(ExchangeConfig.class);

        RabbitMqConfig rabbitMqConfig = ctx.getBean(RabbitMqConfig.class);
        Channel submit = rabbitMqConfig.getSubmitOrderChannel();
        SimulatedClient client = new SimulatedClient(submit, "doug");


        client.submitLimitOrder("AMZN", 1000, "100.02", "SELL");
        client.submitLimitOrder("DELL", 1000, "100.02", "BUY");
        client.submitLimitOrder("REJECT", 1000, "100.02", "SELL");
        client.submitLimitOrder("AMZN", 1000, "100.02", "BUY");
        client.submitLimitOrder("DELL", 1000, "100.02", "SELL");

        System.exit(0);
    }
}