package io.github.flowersbloom.udp.packet;

import io.netty.buffer.ByteBuf;

import java.util.List;

public interface Transform {
    /**
     * 生成新的缓冲池
     * @param serialNumber use the serialNumber if it's value != 0 else generate a new serialNumber
     * @return
     */
    ByteBuf toNewBuf(long serialNumber);

    /**
     * 生成新的缓冲池列表
     * @param serialNumber use the serialNumber if it's value != 0 else generate a new serialNumber
     * @return
     */
    List<ByteBuf> toNewBufList(long serialNumber);
}
