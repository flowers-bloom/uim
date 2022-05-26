package io.github.flowersbloom.packet;

import io.github.flowersbloom.command.BizCommand;
import io.github.flowersbloom.udp.packet.BasePacket;

public class ActiveQueryPacket extends BasePacket {
    public ActiveQueryPacket() {
        this.command = BizCommand.ACTIVE_QUERY_PACKET;
    }
}
