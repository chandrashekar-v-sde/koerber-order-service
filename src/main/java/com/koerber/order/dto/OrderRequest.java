package com.koerber.order.dto;

import lombok.Data;

@Data
public class OrderRequest {

	private Long productId;

	private Integer quantity;
}
