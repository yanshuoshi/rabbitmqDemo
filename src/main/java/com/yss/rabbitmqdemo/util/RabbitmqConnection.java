package com.yss.rabbitmqdemo.util;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.concurrent.TimeoutException;


/**
 * rabbitmq 连接工具类
 * @author wangqi
 * @date 2022/1/24
 */
@Slf4j
public class RabbitmqConnection {

    private static ConnectionFactory connectionFactory;

    //重量级资源 类加载执行只执行一次
    static {
        //创建连接mq的连接工厂对象
        connectionFactory = new ConnectionFactory();
        //设置连接rabbitmq主机
        connectionFactory.setHost("localhost");
        //设置端口号
        connectionFactory.setPort(5672);
        //设置连接哪个虚拟主机
        connectionFactory.setVirtualHost("/");
        //设置访问虚拟主机的用户名和密码
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("admin");
    }

    /**
     *  定义提供连接对象的方法
     */
    public static Connection getConnection() {
        try {
            return connectionFactory.newConnection();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭通道和关闭连接工具方法
     */
    public static void closeConnection(Channel channel, Connection conn) {
        try {
            if (channel != null) {
                channel.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

}
