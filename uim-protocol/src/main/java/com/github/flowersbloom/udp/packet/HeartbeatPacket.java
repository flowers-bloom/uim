package com.github.flowersbloom.udp.packet;

import com.github.flowersbloom.udp.Command;
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
