package com.koerber.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.koerber.order.dto.OrderRequest;
import com.koerber.order.dto.OrderResponse;
import com.koerber.order.service.OrderService;

@RestController
@RequestMapping("/order")
@Tag(name = "Order API", description = "Handles product order operations")
public class OrderController {

	private static final Logger LOGGER = LogManager.getLogger(OrderController.class);

	private final OrderService orderService;

	public OrderController(final OrderService orderService) {
		this.orderService = orderService;
	}

    @Operation(summary = "Place a new order",
            description = "Creates a new order and reserves inventory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order placed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or insufficient inventory"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
	@PostMapping
	public ResponseEntity<OrderResponse> placeOrder(@RequestBody final OrderRequest request) {
		LOGGER.info("Place order request: {}", request);
		return ResponseEntity.ok(orderService.placeOrder(request));
	}

    @Operation(summary = "Get order details",
            description = "Fetch order details by order ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
	@GetMapping("/{orderId}")
	public ResponseEntity<OrderResponse> getOrder(@PathVariable final Long orderId) {
		LOGGER.info("Get order request for orderId: {}", orderId);
		return ResponseEntity.ok(orderService.getOrderById(orderId));
	}
}
