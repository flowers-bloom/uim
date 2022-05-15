package com.github.flowersbloom.packet;

import com.github.flowersbloom.Command;
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
