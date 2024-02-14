package com.hmall.common.config;

import com.hmall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.AmqpResourceNotAvailableException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.PostConstruct;

@Configuration
@DependsOn("rabbitTemplate")
@ConditionalOnClass(RabbitTemplate.class)
@RequiredArgsConstructor
public class MqConfig2 {

	private final RabbitTemplate rabbitTemplate;

	@PostConstruct
	public void postProcessor() {
		rabbitTemplate.addBeforePublishPostProcessors(new MessagePostProcessor() {
			@Override
			public Message postProcessMessage(Message message) throws AmqpException {
				message.getMessageProperties().setHeader("userId", UserContext.getUser());
				return message;
			}
		});

		rabbitTemplate.addAfterReceivePostProcessors(new MessagePostProcessor() {
			@Override
			public Message postProcessMessage(Message message) throws AmqpException {
				Long userId = message.getMessageProperties().getHeader("userId");
				UserContext.setUser(userId);
				return message;
			}
		});
	}
}