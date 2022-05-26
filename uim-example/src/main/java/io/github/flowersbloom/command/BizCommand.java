package io.github.flowersbloom.command;

import io.github.flowersbloom.udp.Command;

public class BizCommand extends Command {
    /**
     * 从20开始
     */
    public static final byte P2P_DATA_PACKET = 20;
    public static final byte BROADCAST_DATA_PACKET = 21;
    public static final byte VIDEO_DATA_PACKET = 22;
    public static final byte VIDEO_HEADER_PACKET = 23;
    public static final byte ACTIVE_QUERY_PACKET = 24;
    public static final byte ACTIVE_DATA_PACKET = 25;
}
