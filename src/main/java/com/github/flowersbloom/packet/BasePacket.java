package com.github.flowersbloom.packet;

import lombok.Data;

@Data
public abstract class BasePacket {
    Byte command;
    Long serialNumber = System.currentTimeMillis();
}
