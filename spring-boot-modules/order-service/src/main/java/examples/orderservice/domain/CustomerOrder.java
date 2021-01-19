package examples.orderservice.domain;

import examples.orderservice.core.Rejects;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CustomerOrder {
    String orderId;
    String customerId;
    String itemId;
    Integer quantity;

    public void updateItem(String itemId, Integer quantity) {

        Rejects.ifBlank(itemId, () -> "Item Id cannot be blank");
        Rejects.ifNegative(quantity, () -> "Quantity cannot be negative");

        this.itemId = itemId;
        this.quantity = quantity;
    }
}
