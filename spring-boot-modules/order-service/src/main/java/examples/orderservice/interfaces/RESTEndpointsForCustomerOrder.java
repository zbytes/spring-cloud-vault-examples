package examples.orderservice.interfaces;

import examples.orderservice.application.CustomerOrderService;
import examples.orderservice.domain.CustomerOrder;
import examples.orderservice.domain.CustomerOrderRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers/{customerId}/orders")
public class RESTEndpointsForCustomerOrder {

    private final CustomerOrderService orderService;
    private final CustomerOrderRepository orderRepository;

    public static URI buildOrderURI(String customerId, String orderId) {
        return URI.create(String.format("/api/customers/%s/orders/%s", customerId, orderId));
    }

    @PostMapping
    public ResponseEntity<Void> placeOrder(@PathVariable("customerId") String customerId,
                                           @Valid @RequestBody PlaceOrderCommand body) {
        String orderId = orderService.placeOrder(customerId, body.getItemId(), body.getQuantity());
        return ResponseEntity.created(buildOrderURI(customerId, orderId)).build();
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<Void> modifyOrder(@PathVariable("customerId") String customerId,
                                            @PathVariable("orderId") String orderId,
                                            @Valid @RequestBody ModifyOrderCommand body) {
        orderService.modifyOrder(customerId, orderId, body.getItemId(), body.getQuantity());
        return ResponseEntity.ok().location(buildOrderURI(customerId, orderId)).build();
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable("customerId") String customerId,
                                            @PathVariable("orderId") String orderId) {
        orderService.deleteOrder(customerId, orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<CustomerOrderResponse> getOrder(@PathVariable("customerId") String customerId,
                                                          @PathVariable("orderId") String orderId) {
        return ResponseEntity.of(orderRepository.getOrderByCustomerIdAndOrderId(customerId, orderId).map(this::toOrderResponse));
    }

    @GetMapping
    public ResponseEntity<List<CustomerOrderResponse>> getOrders(@PathVariable("customerId") String customerId) {
        List<CustomerOrder> customerOrders = orderRepository.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(customerOrders.stream().map(this::toOrderResponse).collect(Collectors.toList()));
    }

    private CustomerOrderResponse toOrderResponse(CustomerOrder customerOrder) {
        return CustomerOrderResponse.builder()
                .orderId(customerOrder.getOrderId())
                .itemId(customerOrder.getItemId())
                .customerId(customerOrder.getCustomerId())
                .quantity(customerOrder.getQuantity()).build();
    }

    @Jacksonized
    @Builder
    @Value
    public static class PlaceOrderCommand {
        String itemId;
        Integer quantity;
    }

    @Jacksonized
    @Builder
    @Value
    public static class ModifyOrderCommand {
        String itemId;
        Integer quantity;
    }

    @Jacksonized
    @Builder
    @Value
    public static class CustomerOrderResponse {
        String orderId;
        String customerId;
        String itemId;
        Integer quantity;
    }

}
