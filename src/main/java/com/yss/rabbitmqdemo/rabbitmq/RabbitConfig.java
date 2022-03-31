package com.yss.rabbitmqdemo.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * rabbitmq 配置类
 * @author wangqi
 * @date 2022/1/19
 */
@Configuration
public class RabbitConfig {
    /**
     * 主题交换机
     */
    @Bean("topicExchange1")
    public TopicExchange getTopicExchange(){
        return new TopicExchange("TOPIC_EXCHANGE1");
    }

    /**
     * 扇形交换机
     */
    @Bean("fanoutExchange1")
    public FanoutExchange getFanoutExchange(){
        return new FanoutExchange("FANOUT_EXCHANGE1");
    }

    /**
     * 直连交换机
     */
    @Bean("directExchange1")
        public DirectExchange getDirectExchange(){
        return new DirectExchange("DIRECT_EXCHANGE1");
    }

    /**
     * 队列1
     */
    @Bean("oneQueue1")
    public Queue getOneQueue(){
        Map<String,Object> map = new HashMap();
        map.put("x-message-ttl",43200000);
        return new Queue("ONE_QUEUE1",true, false, false, map);
    }

    /**
     * 队列2
     */
    @Bean("twoQueue1")
    public Queue getTwoQueue(){
        Map<String,Object> map = new HashMap();
        map.put("x-message-ttl",43200000);
        return new Queue("TWO_QUEUE1",true, false, false, map);
    }

    /**
     * 队列3
     */
    @Bean("threeQueue1")
    public Queue getThreeQueue(){
        Map<String,Object> map = new HashMap();
        map.put("x-message-ttl",43200000);
        return new Queue("THREE_QUEUE1",true, false, false, map);
    }

    /**
     * 绑定交换机(主题类型)
     * @param queue 队列
     * @param exchange 交换机
     */
    @Bean
    public Binding bindSecond(@Qualifier("oneQueue1") Queue queue, @Qualifier("topicExchange1") TopicExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("#.test.#");
    }

    /**
     * 绑定队列交换机（直连类型）
     * @param queue 队列
     * @param exchange 交换机
     */
    @Bean
    public Binding bindThird(@Qualifier("twoQueue1") Queue queue,@Qualifier("directExchange1") DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("osce");
    }

    /**
     * 绑定扇形交换机
     * @param queue 队列
     * @param exchange 交换机
     * @return
     */
    @Bean
    public Binding bindThree(@Qualifier("threeQueue1") Queue queue,@Qualifier("fanoutExchange1") FanoutExchange exchange){
        return BindingBuilder.bind(queue).to(exchange);
    }

    /**
     * 延迟交换机
     */
    @Bean("delayExchange1")
    public CustomExchange delayExchange() {
        Map<String, Object> arg = new HashMap<>(16);
        arg.put("x-delayed-type", "direct");
        return new CustomExchange("DELAY_EXCHANGE1","x-delayed-message", true, false, arg);
    }

    /**
     * 延迟队列
     */
    @Bean("delayQueue1")
    public Queue delayQueue() {
        return new Queue("DELAY_QUEUE1");
    }

    /**
     * 延迟队列绑定关系
     * @param queue 队列
     * @param exchange 交换机
     */
    @Bean
    public Binding bindingDead(@Qualifier("delayQueue1") Queue queue, @Qualifier("delayExchange1") CustomExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("osce-delayed").noargs();
    }

    /**
     * 监听器
     * @param connectionFactory 连接
     */
    @Bean
    public SimpleMessageListenerContainer messageContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        // 最小消费者数
        container.setConcurrentConsumers(1);
        // 最大消费者数
        container.setMaxConcurrentConsumers(5);
        //是否重回队列
        container.setDefaultRequeueRejected(false);
        //签收模式
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);
        container.setExposeListenerChannel(true);
        return container;
    }

}
