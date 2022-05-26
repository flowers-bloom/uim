package io.github.flowersbloom.udp.packet;


import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

@Data
public abstract class BasePacket implements Serializable, Transform {
    private static final AtomicLong counter = new AtomicLong(1);
    protected static final int DEFAULT_SLICE_LENGTH = 500;
    public transient long serialNumber;
    public transient byte command;

    protected static long generateSerialNumber() {
        return counter.getAndIncrement();
    }
}
