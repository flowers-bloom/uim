package io.github.flowersbloom.udp.handler;

import io.github.flowersbloom.udp.packet.BasePacket;

/**
 * 消息监听
 */
public interface MessageListener {
    void handle(BasePacket basePacket);
}
