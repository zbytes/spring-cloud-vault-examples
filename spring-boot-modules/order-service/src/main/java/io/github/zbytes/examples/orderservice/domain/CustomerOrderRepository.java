package io.github.zbytes.examples.orderservice.domain;

import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository {

    Optional<CustomerOrder> getOrderByCustomerIdAndOrderId(String customerId, String orderId);

    List<CustomerOrder> getOrdersByCustomerId(String customerId);

    void save(CustomerOrder customerOrder);

    void delete(CustomerOrder customerOrder);
}
