package com.hmall.trade.listeners;

import com.hmall.api.client.PayClient;
import com.hmall.api.pojo.PayOrder;
import com.hmall.common.domain.MultiDelayMessage;
import com.hmall.trade.constants.MqConstants;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DelayListener {
	private final RabbitTemplate rabbitTemplate;
	private final IOrderService orderService;
	private final PayClient payClient;

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(MqConstants.DELAY_ORDER_QUEUE),
			exchange = @Exchange(name = MqConstants.DELAY_EXCHANGE, type = ExchangeTypes.TOPIC),
			key = MqConstants.DELAY_ORDER_ROUTING_KEY
	))
	public void delayListener(MultiDelayMessage<Long> msg){
		//检查是否已支付
		Long orderId = msg.getData();
		Order order = orderService.getById(orderId);
		if(order == null || order.getStatus() == 2) return;
		//查询订单
		PayOrder payOrder = payClient.getPayOrderByOrderId(orderId);
		if(payOrder.getStatus() == 3 || payOrder.getStatus() == 2) return;
		//发送消息
		if(msg.hasNextDelay()){
			rabbitTemplate.convertAndSend(MqConstants.DELAY_EXCHANGE, MqConstants.DELAY_ORDER_ROUTING_KEY, msg, new MessagePostProcessor() {
				@Override
				public Message postProcessMessage(Message message) throws AmqpException {
					message.getMessageProperties().setDelay(msg.removeNextDelay().intValue());
					return message;
				}
			});
			return;
		}
		//取消订单
		orderService.cancelOrder(orderId);
	}
}
