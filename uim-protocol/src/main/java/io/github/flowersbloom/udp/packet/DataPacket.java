package io.github.flowersbloom.udp.packet;

import io.github.flowersbloom.udp.Command;
import lombok.Data;

/**
 * 数据报文
 */
@Data
public class DataPacket extends BasePacket {
    String senderId;
    String receiverId;
    String content;

    public DataPacket() {
        this.command = Command.DATA_PACKET;
    }
}
