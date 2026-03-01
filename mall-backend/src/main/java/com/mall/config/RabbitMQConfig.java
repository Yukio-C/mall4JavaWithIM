package com.mall.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 延迟任务配置 (使用死信队列 DLX 实现)
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 显式配置监听器工厂，确保 @RabbitListener 能够正确使用 JSON 转换器
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }

    // 1. 业务交换机
    public static final String ORDER_EXCHANGE = "order.direct";
    
    // 2. 延迟队列 (用于存放未到期的消息)
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";
    public static final String ORDER_DELAY_ROUTING_KEY = "order.delay";

    // 3. 死信交换机 & 死信队列 (用于存放过期后的消息，消费者监听此队列)
    public static final String ORDER_DEAD_LETTER_EXCHANGE = "order.dlx.direct";
    public static final String ORDER_DEAD_LETTER_QUEUE = "order.dlx.queue";
    public static final String ORDER_DEAD_LETTER_ROUTING_KEY = "order.dlx";

    // 4. 客服排队队列 (用户消息分发)
    public static final String SUPPORT_EXCHANGE = "support.direct";
    public static final String SUPPORT_WAIT_QUEUE = "support.wait.queue";
    public static final String SUPPORT_WAIT_ROUTING_KEY = "support.wait";

    /**
     * 客服业务交换机
     */
    @Bean
    public DirectExchange supportExchange() {
        return new DirectExchange(SUPPORT_EXCHANGE);
    }

    /**
     * 客服等待队列
     */
    @Bean
    public Queue supportWaitQueue() {
        return new Queue(SUPPORT_WAIT_QUEUE);
    }

    /**
     * 绑定客服队列
     */
    @Bean
    public Binding supportWaitBinding() {
        return BindingBuilder.bind(supportWaitQueue())
                .to(supportExchange())
                .with(SUPPORT_WAIT_ROUTING_KEY);
    }

    /**
     * 业务交换机
     */
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange orderDeadLetterExchange() {
        return new DirectExchange(ORDER_DEAD_LETTER_EXCHANGE);
    }

    /**
     * 延迟队列配置：核心在于设置死信交换机
     */
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        // 消息过期后，投递到哪个死信交换机
        args.put("x-dead-letter-exchange", ORDER_DEAD_LETTER_EXCHANGE);
        // 投递到死信交换机时使用的路由键
        args.put("x-dead-letter-routing-key", ORDER_DEAD_LETTER_ROUTING_KEY);
        // 消息的存活时间 (TTL)，单位毫秒，例如 30 分钟 = 1800000ms
        // 为了方便测试，建议先设为 30000ms (30秒)
        args.put("x-message-ttl", 30000); 
        
        return QueueBuilder.durable(ORDER_DELAY_QUEUE).withArguments(args).build();
    }

    /**
     * 死信队列配置：消费者真正消费的队列
     */
    @Bean
    public Queue orderDeadLetterQueue() {
        return new Queue(ORDER_DEAD_LETTER_QUEUE);
    }

    /**
     * 绑定延迟队列到业务交换机
     */
    @Bean
    public Binding orderDelayBinding() {
        return BindingBuilder.bind(orderDelayQueue())
                .to(orderExchange())
                .with(ORDER_DELAY_ROUTING_KEY);
    }

    /**
     * 绑定死信队列到死信交换机
     */
    @Bean
    public Binding orderDeadLetterBinding() {
        return BindingBuilder.bind(orderDeadLetterQueue())
                .to(orderDeadLetterExchange())
                .with(ORDER_DEAD_LETTER_ROUTING_KEY);
    }
}
