package com.hmall.cart.listerner;

import com.hmall.cart.service.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CartListener {

	private final ICartService cartService;

	private final RabbitTemplate rabbitTemplate;

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue("cart.clear.queue"),
			exchange = @Exchange(name = "trade.topic", type = ExchangeTypes.TOPIC),
			key = "order.create"
	))
	public void deleteCartItemByIds(List<Long> ids){
		cartService.removeByItemIds(ids);
	}

}
