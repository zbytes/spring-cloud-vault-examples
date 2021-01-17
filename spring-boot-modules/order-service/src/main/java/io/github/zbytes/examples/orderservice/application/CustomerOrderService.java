package io.github.zbytes.examples.orderservice.application;

import io.github.zbytes.examples.orderservice.core.EntityNotFound;
import io.github.zbytes.examples.orderservice.domain.CustomerOrder;
import io.github.zbytes.examples.orderservice.domain.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerOrderService {

    private final CustomerOrderRepository repository;

    public String placeOrder(String customerId, String itemId, Integer quantity) {

        CustomerOrder customerOrder = CustomerOrder.builder()
                .orderId(UUID.randomUUID().toString())
                .customerId(customerId)
                .itemId(itemId)
                .quantity(quantity)
                .build();

        repository.save(customerOrder);

        return customerOrder.getOrderId();
    }

    public void modifyOrder(String customerId, String orderId, String itemId, Integer quantity) {

        CustomerOrder entity = repository.getOrderByCustomerIdAndOrderId(customerId, orderId)
                .orElseThrow(() -> new EntityNotFound(String.format("[customerId=%s,orderId=%s]", orderId, customerId)));

        entity.updateItem(itemId, quantity);

        repository.save(entity);
    }

    public void deleteOrder(String customerId, String orderId) {
        CustomerOrder entity = repository.getOrderByCustomerIdAndOrderId(customerId, orderId)
                .orElseThrow(() -> new EntityNotFound(String.format("[customerId=%s,orderId=%s]", orderId, customerId)));


        repository.delete(entity);
    }
}
