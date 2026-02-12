package com.koerber.order.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.koerber.order.dto.InventoryUpdateRequest;
import com.koerber.order.dto.InventoryUpdateResponse;
import com.koerber.order.dto.OrderRequest;
import com.koerber.order.dto.OrderResponse;
import com.koerber.order.entity.Order;
import com.koerber.order.exception.InventoryServiceException;
import com.koerber.order.exception.OrderNotFoundException;
import com.koerber.order.repository.OrderRepository;
import com.koerber.order.service.OrderService;

import jakarta.transaction.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

	private static final Logger logger = LogManager.getLogger(OrderServiceImpl.class);

	private final OrderRepository orderRepository;
	private final RestTemplate restTemplate;

	@Value("${inventory.service.url}")
	private String inventoryServiceUrl;

	public OrderServiceImpl(final OrderRepository orderRepository, final RestTemplate restTemplate) {
		this.orderRepository = orderRepository;
		this.restTemplate = restTemplate;
	}

    /**
     * Places a new order and reserves inventory.
     * @param request {@link OrderRequest} containing productId and quantity
     * @return {@link OrderResponse} with reservation details
     * @throws InventoryServiceException if inventory reservation fails
     */
	@Override
	@Transactional
	public OrderResponse placeOrder(final OrderRequest request) {

		logger.info("Starting order placement for productId={}", request.getProductId());

		try {

			Order order = new Order();
			order.setProductId(request.getProductId());
			order.setQuantity(request.getQuantity());
			order.setStatus("PLACED");
			order.setOrderDate(LocalDate.now());

			order = orderRepository.save(order);

			final InventoryUpdateResponse inventoryResponse = restTemplate.postForObject(
					inventoryServiceUrl + "/inventory/update", buildInventoryRequest(order),
					InventoryUpdateResponse.class);

			if (inventoryResponse == null) {
				throw new InventoryServiceException("Inventory service returned null response");
			}

			order.setProductName(inventoryResponse.getProductName());
			order.setStatus(inventoryResponse.getStatus());
			orderRepository.save(order);

			final OrderResponse response = new OrderResponse();
			response.setOrderId(order.getOrderId());
			response.setProductId(order.getProductId());
			response.setProductName(order.getProductName());
			response.setQuantity(order.getQuantity());
			response.setStatus(order.getStatus());
			response.setReservedFromBatchIds(inventoryResponse.getReservedFromBatchIds());
			response.setMessage(inventoryResponse.getMessage());

			logger.info("Order placed successfully: {}", order.getOrderId());

			return response;

		} catch (final Exception ex) {

			logger.error("Order processing failed", ex);
			throw new InventoryServiceException(ex.getMessage() != null ? ex.getMessage() : "Inventory error occurred");
		}
	}

    /**
     * Fetches order details by order ID.
     * @param orderId ID of the order
     * @return {@link OrderResponse} containing order details
     * @throws OrderNotFoundException if order does not exist
     * @throws InventoryServiceException if inventory service fails
     */
	@Override
	public OrderResponse getOrderById(final Long orderId) {

		logger.info("Fetching order details for orderId={}", orderId);

		try {

			final Order order = orderRepository.findById(orderId)
					.orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

			final Long[] batchIdsArray = restTemplate
					.getForObject(inventoryServiceUrl + "/inventory/reservation/" + orderId, Long[].class);

			final OrderResponse response = new OrderResponse();
			response.setOrderId(order.getOrderId());
			response.setProductId(order.getProductId());
			response.setProductName(order.getProductName());
			response.setQuantity(order.getQuantity());
			response.setStatus(order.getStatus());
			response.setMessage("Order fetched successfully");

			if (batchIdsArray != null) {
				response.setReservedFromBatchIds(List.of(batchIdsArray));
			}

			logger.info("Order details fetched successfully for orderId={}", orderId);

			return response;

		} catch (final OrderNotFoundException ex) {

			logger.error("Order not found: {}", ex.getMessage());
			throw ex;

		} catch (final Exception ex) {

			logger.error("Failed to fetch reservation details from inventory service for orderId {} ", orderId, ex);
			throw new InventoryServiceException(
					"Failed to fetch reservation details from inventory service for orderId");

		}
	}

	private InventoryUpdateRequest buildInventoryRequest(final Order order) {

		final InventoryUpdateRequest updateRequest = new InventoryUpdateRequest();
		updateRequest.setOrderId(order.getOrderId());
		updateRequest.setProductId(order.getProductId());
		updateRequest.setQuantity(order.getQuantity());
		return updateRequest;
	}
}
