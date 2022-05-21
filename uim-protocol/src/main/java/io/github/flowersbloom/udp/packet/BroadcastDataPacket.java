package io.github.flowersbloom.udp.packet;

import io.github.flowersbloom.udp.Command;
import lombok.Data;

/**
 * 广播报文
 */
@Data
public class BroadcastDataPacket extends BasePacket {
    String senderId;
    String senderNickname;
    String content;

    public BroadcastDataPacket() {
        this.command = Command.BROADCAST_DATA_PACKET;
    }
}
