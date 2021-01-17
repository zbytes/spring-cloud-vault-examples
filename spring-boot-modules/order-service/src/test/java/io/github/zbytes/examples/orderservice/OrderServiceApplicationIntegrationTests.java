package io.github.zbytes.examples.orderservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static io.github.zbytes.examples.orderservice.interfaces.RESTEndpointsForCustomerOrder.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {OrderServiceApplication.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
class OrderServiceApplicationIntegrationTests {

    private TestRestTemplate REST;
    @LocalServerPort
    private Integer serverPort;
    private String orderId;
    private String customerId;

    @BeforeEach
    void setUp() {
        this.orderId = null;
        this.customerId = UUID.randomUUID().toString();
        REST = new TestRestTemplate();
        REST.setUriTemplateHandler(new RootUriTemplateHandler("http://localhost:" + serverPort));
    }

    @Test
    void testCreatedForPlaceOrder() {

        PlaceOrderCommand command = PlaceOrderCommand.builder()
                .itemId("ITEM0001")
                .quantity(20)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PlaceOrderCommand> httpEntity = new HttpEntity<>(command, headers);

        ResponseEntity<Void> response = REST.postForEntity("/api/customers/" + customerId + "/orders", httpEntity, Void.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getHeaders().getLocation());

        String[] parts = response.getHeaders().getLocation().toString().split("/");

        this.orderId = parts[parts.length - 1];
    }

    @Test
    void testOkForModifyOrder() {

        testCreatedForPlaceOrder();

        ModifyOrderCommand command = ModifyOrderCommand.builder()
                .itemId("ITEM0002")
                .quantity(40)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ModifyOrderCommand> httpEntity = new HttpEntity<>(command, headers);

        ResponseEntity<Void> response = REST.exchange("/api/customers/" + customerId + "/orders/" + orderId, HttpMethod.PUT, httpEntity, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getLocation());

    }

    @Test
    void testOkForDeleteOrder() {

        testCreatedForPlaceOrder();

        ResponseEntity<Void> response = REST.exchange("/api/customers/" + customerId + "/orders/" + orderId, HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testOkForGetOrder() {

        testCreatedForPlaceOrder();

        ResponseEntity<CustomerOrderResponse> response = REST.getForEntity("/api/customers/" + customerId + "/orders/" + orderId, CustomerOrderResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(orderId, response.getBody().getOrderId());

    }

    @Test
    void testOkForGetOrders() {

        testCreatedForPlaceOrder();

        testCreatedForPlaceOrder();

        ResponseEntity<CustomerOrderResponse[]> response = REST.getForEntity("/api/customers/" + customerId + "/orders", CustomerOrderResponse[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().length);
    }
}
