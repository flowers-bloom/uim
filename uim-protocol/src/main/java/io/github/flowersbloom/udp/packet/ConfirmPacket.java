package io.github.flowersbloom.udp.packet;

import io.github.flowersbloom.udp.Command;
import lombok.Data;

/**
 * 确认报文
 */
@Data
public class ConfirmPacket extends BasePacket {
    String senderId;

    public ConfirmPacket() {
        this.command = Command.CONFIRM_PACKET;
    }
}
