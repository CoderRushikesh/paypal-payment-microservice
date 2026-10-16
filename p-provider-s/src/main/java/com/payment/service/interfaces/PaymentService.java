package com.payment.service.interfaces;

import com.payment.pojo.CreateOrderReq;
import com.payment.pojo.OrderResponse;

public interface PaymentService {

	public OrderResponse createOrder(CreateOrderReq createOrderReq);
	public OrderResponse captureOrder(String orderId);
}
