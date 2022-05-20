package com.github.flowersbloom.udp.packet;

import com.github.flowersbloom.udp.Command;
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
