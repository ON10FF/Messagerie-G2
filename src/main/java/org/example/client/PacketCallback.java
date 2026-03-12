package org.example.client;

import org.example.util.Packet;

public interface PacketCallback {

    void onPacketReceived(Packet packet);
}
