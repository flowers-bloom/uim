package io.github.flowersbloom.packet;

import com.alibaba.fastjson.JSON;
import io.github.flowersbloom.command.BizCommand;
import io.github.flowersbloom.udp.packet.BasePacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class AddressDataPacket extends BasePacket {
    String ip;
    int port;

    public AddressDataPacket() {
        this.command = BizCommand.ADDRESS_DATA_PACKET;
    }

    @Override
    public ByteBuf toNewBuf(long serialNumber) {
        ByteBuf byteBuf = super.toNewBuf(serialNumber);
        byteBuf.writeBytes(JSON.toJSONString(this).getBytes());
        return byteBuf;
    }
}
