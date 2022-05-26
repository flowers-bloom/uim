package io.github.flowersbloom.udp.packet;

import io.github.flowersbloom.udp.Command;
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
