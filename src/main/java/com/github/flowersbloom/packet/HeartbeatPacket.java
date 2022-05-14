package com.github.flowersbloom.packet;

import com.github.flowersbloom.Command;
import lombok.Data;

/**
 * 心跳报文
 */
@Data
public class HeartbeatPacket extends BasePacket {
    String userId;

    public HeartbeatPacket() {
        this.command = Command.HEARTBEAT_PACKET;
    }
}
