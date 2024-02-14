package com.hmall.api.client;

import com.hmall.api.pojo.PayOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("pay-service")
public interface PayClient {
	@GetMapping("/order/{id}")
	public PayOrder getPayOrderByOrderId(@PathVariable("id") Long id);
}
