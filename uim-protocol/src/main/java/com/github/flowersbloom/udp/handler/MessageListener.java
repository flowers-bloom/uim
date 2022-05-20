package com.github.flowersbloom.udp.handler;

import com.github.flowersbloom.udp.packet.BasePacket;

/**
 * 消息监听
 */
public interface MessageListener {
    void handle(BasePacket basePacket);
}
