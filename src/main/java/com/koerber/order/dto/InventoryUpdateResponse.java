package com.koerber.order.dto;

import java.util.List;

import lombok.Data;

@Data
public class InventoryUpdateResponse {

	private Long orderId;

	private Long productId;

	private String productName;

	private Integer quantity;

	private String status;

	private List<Long> reservedFromBatchIds;

	private String message;
}
