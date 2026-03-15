package com.yyyouth.common.constants;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2025/11/27
 * rabbitmq常量
 */
public class RabbitConstants {

    public static final String EXCHANGE_NAME = "exchange.sssp";


    /**
     * 队列post
     */
    public static final String QUEUE_NAME_POST = "queue.sssp.post";

    /**
     * 路由post的键
     */
    public static final String ROUTING_KEY_POST = "routing.sssp.post";
}
