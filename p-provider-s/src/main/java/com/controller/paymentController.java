package com.controller;

import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.payment.pojo.CreateOrderReq;
import com.payment.pojo.ErrorResponse;
import com.payment.pojo.OrderResponse;
import com.payment.service.interfaces.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Create and capture PayPal orders")
public class paymentController {

	private final PaymentService paymentService;

	@Operation(
			summary = "Step 1: Create a PayPal order",
			description = """
				Creates an order in PayPal and returns its `id` and a `redirectUrl`.

				**Next:** open `redirectUrl` in a browser, log in with a sandbox buyer account \
				and approve the payment. Then call **Capture Order** with the same `id`.
				"""
		)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Order created",
			content = @Content(schema = @Schema(implementation = OrderResponse.class),
				examples = @ExampleObject(value = """
					{ "id": "5O190127TN364715T", "status": "CREATED", "redirectUrl": null }
					"""))),
		@ApiResponse(responseCode = "400",
			description = "Validation failed: invalid amount, or missing currency, return URL or cancel URL",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
					{ "errorCode": "INVALID_AMOUNT", "errorMessage": "Amount must be greater than zero" }
					"""))),
		@ApiResponse(responseCode = "422", description = "PayPal rejected the request (e.g. invalid currency)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
					{ "errorCode": "PAYPAL_ERROR", "errorMessage": "UNPROCESSABLE_ENTITY: CURRENCY_NOT_SUPPORTED" }
					"""))),
		@ApiResponse(responseCode = "503", description = "PayPal unavailable or authentication failed",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(responseCode = "500", description = "Unexpected internal error",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/Payments")
	public OrderResponse createOrder(@RequestBody CreateOrderReq createOrderReq) {

		log.info("Creating Order in paypal provider service ||createOrderReq: {}", createOrderReq);

		OrderResponse response = paymentService.createOrder(createOrderReq);

		log.info("Response from payment service: {}", response);
		return response;
	}

	@Operation(
			summary = "Step 2: Capture an approved PayPal order",
			description = """
				Collects the funds for an order. **The buyer must have approved the payment first.**

				Before calling this:
				1. Call **Create Order** and copy the `redirectUrl`
				2. Open it in a browser and approve with a sandbox buyer account
				3. Use the order `id` (the `token` in the return URL) as `orderId` here

				Calling this before approval, or twice for the same order, returns `422`.
				"""
		)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Order captured",
			content = @Content(schema = @Schema(implementation = OrderResponse.class),
				examples = @ExampleObject(value = """
					{ "id": "5O190127TN364715T", "status": "COMPLETED", "redirectUrl": null }
					"""))),
		@ApiResponse(responseCode = "404", description = "Order not found in PayPal",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(responseCode = "422", description = "Order not approved by buyer, or already captured",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
					{ "errorCode": "PAYPAL_ERROR", "errorMessage": "UNPROCESSABLE_ENTITY: ORDER_NOT_APPROVED" }
					"""))),
		@ApiResponse(responseCode = "502", description = "Unexpected response from PayPal",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(responseCode = "503", description = "PayPal service unavailable",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/{orderId}/capture")
	public OrderResponse captureOrder(
			@Parameter(description = "PayPal order id returned by Create Order",
					example = "5O190127TN364715T", required = true)
			@PathVariable String orderId) {

		log.info("Capturing order in PayPal provider service ||orderId:{}", orderId);

		OrderResponse response = paymentService.captureOrder(orderId);
		log.info("Order capture response from service: {}", response);

		return response;
	}

	@PostConstruct
	public void init() {
		LoggerFactory.getLogger(paymentController.class).info("paymentController initialized");
	}
}