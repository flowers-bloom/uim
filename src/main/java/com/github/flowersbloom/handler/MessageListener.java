package com.github.flowersbloom.handler;

import com.github.flowersbloom.packet.BasePacket;

/**
 * 消息监听
 */
public interface MessageListener {
    void handle(BasePacket basePacket);
}
