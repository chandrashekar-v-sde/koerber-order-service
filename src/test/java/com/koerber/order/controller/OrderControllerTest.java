package com.koerber.order.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koerber.order.dto.OrderRequest;
import com.koerber.order.dto.OrderResponse;
import com.koerber.order.exception.GlobalExceptionHandler;
import com.koerber.order.exception.OrderNotFoundException;
import com.koerber.order.service.OrderService;

@WebMvcTest(controllers = OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OrderService orderService;

	@Test
	void testPlaceOrderSuccess() throws Exception {

		final OrderRequest request = new OrderRequest();
		request.setProductId(1002L);
		request.setQuantity(3);

		final OrderResponse response = new OrderResponse();
		response.setOrderId(5012L);
		response.setProductId(1002L);
		response.setProductName("Smartphone");
		response.setQuantity(3);
		response.setStatus("PLACED");
		response.setReservedFromBatchIds(List.of(9L));
		response.setMessage("Order placed. Inventory reserved.");

		Mockito.when(orderService.placeOrder(Mockito.any())).thenReturn(response);

		mockMvc.perform(post("/order").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.orderId").value(5012)).andExpect(jsonPath("$.status").value("PLACED"))
				.andExpect(jsonPath("$.productName").value("Smartphone"));
	}

	@Test
	void testGetOrderSuccess() throws Exception {

		final OrderResponse response = new OrderResponse();
		response.setOrderId(5012L);
		response.setProductId(1002L);
		response.setProductName("Smartphone");
		response.setQuantity(3);
		response.setStatus("PLACED");

		Mockito.when(orderService.getOrderById(5012L)).thenReturn(response);

		mockMvc.perform(get("/order/5012")).andExpect(status().isOk()).andExpect(jsonPath("$.orderId").value(5012))
				.andExpect(jsonPath("$.status").value("PLACED"));
	}

	@Test
	void testGetOrderNotFound() throws Exception {

		Mockito.when(orderService.getOrderById(999L)).thenThrow(new OrderNotFoundException("Order not found"));

		mockMvc.perform(get("/order/999")).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void testPlaceOrderFailure() throws Exception {

		final OrderRequest request = new OrderRequest();
		request.setProductId(1002L);
		request.setQuantity(3);

		Mockito.when(orderService.placeOrder(Mockito.any())).thenThrow(new RuntimeException("Something went wrong"));

		mockMvc.perform(post("/order").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.status").value(500));
	}
}
