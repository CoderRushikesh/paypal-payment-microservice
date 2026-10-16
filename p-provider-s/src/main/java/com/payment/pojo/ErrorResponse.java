package com.payment.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Standard error payload returned by every failing endpoint")
public class ErrorResponse {

	@Schema(description = "Machine-readable error code", example = "PAYPAL_ERROR")
	private String errorCode;

	@Schema(description = "Human-readable explanation",
			example = "UNPROCESSABLE_ENTITY: ORDER_NOT_APPROVED")
	private String errorMessage;
}