package com.payment.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request to create a PayPal order")
public class CreateOrderReq {

	@Schema(description = "ISO 4217 currency code", example = "USD",
			requiredMode = Schema.RequiredMode.REQUIRED)
	private String currencyCode;

	@Schema(description = "Order total, must be greater than 0", example = "100.50",
			requiredMode = Schema.RequiredMode.REQUIRED)
	private Double amount;

	@Schema(description = "URL PayPal redirects to after the buyer approves",
			example = "https://your-frontend.com/payment/success",
			requiredMode = Schema.RequiredMode.REQUIRED)
	private String returnUrl;

	@Schema(description = "URL PayPal redirects to if the buyer cancels",
			example = "https://your-frontend.com/payment/cancel",
			requiredMode = Schema.RequiredMode.REQUIRED)
	private String cancelUrl;
}