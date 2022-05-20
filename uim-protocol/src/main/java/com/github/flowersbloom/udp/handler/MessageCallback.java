package com.github.flowersbloom.udp.handler;

import com.github.flowersbloom.udp.packet.BasePacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 消息回调
 */
public interface MessageCallback {
    List<MessageListener> listenerList = Collections.synchronizedList(new ArrayList<>());

    public static void subscribe(MessageListener listener) {
        listenerList.add(listener);
        System.out.println(listener + " subscribe");
    }

    public static void unsubscribe(MessageListener listener) {
        listenerList.remove(listener);
        System.out.println(listener + " unsubscribe");
    }

    public default void notice(BasePacket basePacket) {
        for (MessageListener listener : listenerList) {
            listener.handle(basePacket);
        }
    }
}
