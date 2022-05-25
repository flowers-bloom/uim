package io.github.flowersbloom.udp.packet;

import io.netty.buffer.ByteBuf;

import java.util.List;

public interface Transform {
    ByteBuf toNewBuf();
    List<ByteBuf> toNewBufList(long serialNumber);
}
