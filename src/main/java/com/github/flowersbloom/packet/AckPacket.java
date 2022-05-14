package com.github.flowersbloom.packet;

import com.github.flowersbloom.Command;
import lombok.Data;

/**
 * 消息收到报文
 */
@Data
public class AckPacket extends BasePacket {
    public AckPacket() {
        this.command = Command.ACK_PACKET;
    }
}
