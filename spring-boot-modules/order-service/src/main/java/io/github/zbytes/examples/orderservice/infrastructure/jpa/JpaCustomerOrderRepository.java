package io.github.zbytes.examples.orderservice.infrastructure.jpa;

import io.github.zbytes.examples.orderservice.domain.CustomerOrder;
import io.github.zbytes.examples.orderservice.domain.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
class JpaCustomerOrderRepository implements CustomerOrderRepository {
    private final CustomerOrderJpaEntityRepository entityRepository;

    @Override
    public Optional<CustomerOrder> getOrderByCustomerIdAndOrderId(String customerId, String orderId) {
        return entityRepository.findByCustomerIdAndOrderId(customerId, orderId).map(entityRepository::toDomain);
    }

    @Override
    public List<CustomerOrder> getOrdersByCustomerId(String customerId) {
        return entityRepository.findAllByCustomerId(customerId).stream().map(entityRepository::toDomain).collect(Collectors.toList());
    }

    @Override
    public void save(CustomerOrder customerOrder) {
        entityRepository.save(entityRepository.toEntity(customerOrder));
    }

    @Override
    public void delete(CustomerOrder customerOrder) {
        entityRepository.delete(entityRepository.toEntity(customerOrder));
    }
}
