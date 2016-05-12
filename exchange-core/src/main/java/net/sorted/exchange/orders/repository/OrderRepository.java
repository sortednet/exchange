package net.sorted.exchange.orders.repository;

import java.util.List;
import net.sorted.exchange.orders.domain.Order;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, Long> {
    List<Order> findByInstrumentId(String instrumentId);
}
