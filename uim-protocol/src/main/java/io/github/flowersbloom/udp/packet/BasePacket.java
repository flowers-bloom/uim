package io.github.flowersbloom.udp.packet;

import lombok.Data;

@Data
public abstract class BasePacket {
    Byte command;
    Long serialNumber = System.currentTimeMillis();
}
