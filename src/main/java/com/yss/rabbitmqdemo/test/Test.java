package com.yss.rabbitmqdemo.test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.yss.rabbitmqdemo.util.RabbitmqConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author yss
 * @date 2022/3/22
 */
@RestController
@RequestMapping("/test")
@Slf4j
public class Test {

    @Autowired
    private SimpleMessageListenerContainer simpleMessageListenerContainer;

    /**
     * 延迟队列监听
     * @return
     */
    @GetMapping("/delayQueue")
    public String delayQueue() {
        //队列名
        String[] str = {"QUEUE"};
        //动态传入队列名
        simpleMessageListenerContainer.setQueueNames(str);
        simpleMessageListenerContainer.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
            //获取消息
            String msg = new String(message.getBody());
            //获取队列名（消息来源自哪个队列）
            String queueName = message.getMessageProperties().getConsumerQueue();
            System.out.println("receive message:" + msg);
            System.out.println("queue name:" + queueName);
        });
        return null;
    }

    /**
     *  添加交换机
     * @param exchangeName 交换机名称
     * @param type         交换机类型(direct/topic/fanout)
     */
    @GetMapping("/addExchange")
    public void addExchange(String exchangeName,String type) {
        //通过工具类获取连接对象
        Connection connection = RabbitmqConnection.getConnection();
        //获取连接中的通道
        Channel channel = null;
        try {
            channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName,type);
            RabbitmqConnection.closeConnection(channel,connection);
            log.info("------------添加交换机成功---------------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 添加队列
     * @param queueName 队列名
     * @param exchangeName 交换机名
     * @param routeKey 路由key
     */
    @GetMapping("/addQueue")
    public void addQueue(String queueName,String exchangeName,String routeKey) {
        //通过工具类获取连接对象
        Connection connection = RabbitmqConnection.getConnection();
        //获取连接中的通道
        Channel channel = null;
        try {
            Channel channelNew = connection.createChannel();
            Map<String,Object> map = new HashMap(2);
            map.put("x-message-ttl",43200000);
            channelNew.queueDeclare(queueName, true, false, false, map);
            // 绑定队列和交换机
            channelNew.queueBind(queueName,exchangeName,routeKey);
            //关闭
            RabbitmqConnection.closeConnection(channelNew,connection);
            log.info("------------添加队列成功---------------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除队列
     * @param queueName 队列名
     */
    @GetMapping("/delQueue")
    public void delQueue(String queueName) throws IOException {
        //通过工具类获取连接对象
        Connection connection = RabbitmqConnection.getConnection();
        //获取连接中的通道
        Long start = System.currentTimeMillis();

        Callable<Boolean> threadA = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                simpleMessageListenerContainer.removeQueueNames(queueName);
                return true;
            }
        };
        FutureTask<Boolean> taskA = new FutureTask<Boolean>(threadA);
        new Thread(taskA).start();

        log.info("======={}",System.currentTimeMillis() - start);
        Channel channel = connection.createChannel();
        channel.queueDelete(queueName);
        RabbitmqConnection.closeConnection(channel,connection);
        log.info("------------队列删除成功---------------");
    }

    /**
     * 清楚队列消息
     * @param queueName 队列名
     */
    @GetMapping("/clearQueue")
    public void clearQueue(String queueName) throws IOException {
        //通过工具类获取连接对象
        Connection connection = RabbitmqConnection.getConnection();
        //获取连接中的通道
        Channel channel = connection.createChannel();
        channel.queuePurge(queueName);
        RabbitmqConnection.closeConnection(channel,connection);
        log.info("------------消息清除---------------");
    }
}
