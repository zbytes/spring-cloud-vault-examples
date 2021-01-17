package io.github.zbytes.examples.orderservice.infrastructure.jpa;

import io.github.zbytes.examples.orderservice.domain.CustomerOrder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;
import java.util.Optional;

interface CustomerOrderJpaEntityRepository extends PagingAndSortingRepository<CustomerOrderJpaEntityRepository.CustomerOrderJpaEntity, String> {

    String TBL_CUSTOMER_ORDERS = "customer_orders";

    Optional<CustomerOrderJpaEntity> findByCustomerIdAndOrderId(String customerId, String orderId);

    List<CustomerOrderJpaEntity> findAllByCustomerId(String customerId);

    default CustomerOrder toDomain(CustomerOrderJpaEntity rawEntity) {
        return CustomerOrder.builder()
                .quantity(rawEntity.getQuantity())
                .itemId(rawEntity.getItemId())
                .customerId(rawEntity.getCustomerId())
                .orderId(rawEntity.getOrderId())
                .build();
    }

    default CustomerOrderJpaEntity toEntity(CustomerOrder entity) {
        CustomerOrderJpaEntity rawEntity = new CustomerOrderJpaEntity();
        rawEntity.setCustomerId(entity.getCustomerId());
        rawEntity.setOrderId(entity.getOrderId());
        rawEntity.setQuantity(entity.getQuantity());
        rawEntity.setItemId(entity.getItemId());
        return rawEntity;
    }

    @Entity
    @Table(name = TBL_CUSTOMER_ORDERS)
    @Getter
    @Setter
    @EqualsAndHashCode(of = "orderId")
    class CustomerOrderJpaEntity {
        @Id
        private String orderId;
        @Column(nullable = false)
        private String customerId;
        @Column(nullable = false)
        private String itemId;
        @Column(nullable = false)
        private Integer quantity;
    }
}
