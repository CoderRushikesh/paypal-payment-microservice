package com.payment.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "PayPal order result")
public class OrderResponse {

	@Schema(description = "PayPal order id", example = "5O190127TN364715T")
	private String id;

	@Schema(description = "Order status: CREATED, APPROVED, COMPLETED, ...", example = "CREATED")
	private String status;

	@Schema(description = "PayPal approval URL the buyer must visit",
			example = "https://www.sandbox.paypal.com/checkoutnow?token=5O190127TN364715T",
			nullable = true)
	private String redirectUrl;
}