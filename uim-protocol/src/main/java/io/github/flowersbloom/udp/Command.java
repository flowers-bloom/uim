package io.github.flowersbloom.udp;


public class Command {
    public static final byte ACK_PACKET = 1;
    public static final byte HEARTBEAT_PACKET = 2;
    public static final byte P2P_DATA_PACKET = 3;
    public static final byte CONFIRM_PACKET = 4;
    public static final byte BROADCAST_DATA_PACKET = 5;
    public static final byte VIDEO_DATA_PACKET = 6;
    public static final byte VIDEO_HEADER_PACKET = 7;
}
