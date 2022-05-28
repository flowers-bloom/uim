package io.github.flowersbloom.udp.packet;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public ByteBuf toNewBuf(long serialNumber) {
        this.serialNumber = serialNumber;
        if (serialNumber == 0) {
            this.serialNumber = generateSerialNumber();
        }
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        byteBuf.writeLong(this.serialNumber);
        byteBuf.writeByte(this.command);
        return byteBuf;
    }

    @Override
    public List<ByteBuf> toNewBufList(long serialNumber) {
        this.serialNumber = serialNumber;
        if (serialNumber == 0) {
            this.serialNumber = generateSerialNumber();
        }
        return new ArrayList<>();
    }
}
