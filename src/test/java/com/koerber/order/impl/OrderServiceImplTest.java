package com.koerber.order.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.koerber.order.dto.InventoryUpdateResponse;
import com.koerber.order.dto.OrderRequest;
import com.koerber.order.dto.OrderResponse;
import com.koerber.order.entity.Order;
import com.koerber.order.exception.InventoryServiceException;
import com.koerber.order.exception.OrderNotFoundException;
import com.koerber.order.repository.OrderRepository;
import com.koerber.order.service.impl.OrderServiceImpl;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private OrderServiceImpl orderService;

	@Test
	void testPlaceOrderSuccess() {

		ReflectionTestUtils.setField(orderService, "inventoryServiceUrl", "http://localhost:8081");

		final OrderRequest request = new OrderRequest();
		request.setProductId(1002L);
		request.setQuantity(3);

		final Order savedOrder = new Order();
		savedOrder.setOrderId(5012L);
		savedOrder.setProductId(1002L);
		savedOrder.setQuantity(3);

		when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

		final InventoryUpdateResponse inventoryResponse = new InventoryUpdateResponse();
		inventoryResponse.setProductName("Smartphone");
		inventoryResponse.setStatus("PLACED");

		when(restTemplate.postForObject(anyString(), any(), eq(InventoryUpdateResponse.class)))
				.thenReturn(inventoryResponse);

		final OrderResponse response = orderService.placeOrder(request);

		assertEquals("PLACED", response.getStatus());
	}

	@Test
	void testPlaceOrderInventoryFailure() {

		ReflectionTestUtils.setField(orderService, "inventoryServiceUrl", "http://localhost:8081");

		final OrderRequest request = new OrderRequest();
		request.setProductId(1002L);
		request.setQuantity(3);

		final Order savedOrder = new Order();
		savedOrder.setOrderId(5012L);

		when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

		when(restTemplate.postForObject(anyString(), any(), eq(InventoryUpdateResponse.class)))
				.thenThrow(new RuntimeException("Inventory down"));

		assertThrows(InventoryServiceException.class, () -> orderService.placeOrder(request));
	}

	@Test
	void testGetOrderByIdSuccess() {

		ReflectionTestUtils.setField(orderService, "inventoryServiceUrl", "http://localhost:8081");

		final Long orderId = 5012L;

		final Order order = new Order();
		order.setOrderId(orderId);
		order.setProductId(1002L);
		order.setProductName("Smartphone");
		order.setQuantity(3);
		order.setStatus("PLACED");

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

		when(restTemplate.getForObject(anyString(), eq(Long[].class))).thenReturn(new Long[] { 9L });

		final OrderResponse response = orderService.getOrderById(orderId);

		assertEquals(orderId, response.getOrderId());
		assertEquals(1, response.getReservedFromBatchIds().size());
	}

	@Test
	void testGetOrderByIdNotFound() {

		when(orderRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(999L));
	}
}
