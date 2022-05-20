package io.github.flowersbloom.udp;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class NettyConstant {
    /**
     * 在线状态集合初始化容量
     */
    public static final int INIT_CAPACITY = 128;

    /**
     * 心跳发送频率
     */
    public static final int HEARTBEAT_SEND_RATE_SECONDS = 3;

    /**
     * 心跳检测频率
     */
    public static final int HEARTBEAT_DETECT_RATE_SECONDS = 3;

    /**
     * 心跳超时时间
     */
    public static final int HEARTBEAT_TIMEOUT_SECONDS = 10;

    /**
     * 消息发送超时时间
     */
    public static final int MSG_SEND_TIMEOUT_SECONDS = 3;

    /**
     * 活跃心跳
     */
    public static final ConcurrentHashMap<String, Long> HEARTBEAT_ACTIVE_MAP = new ConcurrentHashMap<>(INIT_CAPACITY);

    /**
     * 活跃地址
     */
    public static final ConcurrentHashMap<String, InetSocketAddress> ADDRESS_ACTIVE_MAP = new ConcurrentHashMap<>(INIT_CAPACITY);
}
