package com.vivokey.nfcsnoopdecoder;

import java.nio.ByteBuffer;

import org.apache.commons.codec.binary.Hex;

/**
 * Totally not a struct for a NFC packet (since Java doesn't have structs)
 */
public class NFCPacket {
    /**
     * Packet length, in bytes
     */
    private short packetLen;
    /**
     * The delta of the time between the last logged command and this one, in ms. 
     */
    protected int deltaTimems;
    /**
     * Whether or not this command was received (or sent)
     */
    private boolean isReceived;
    /**
     * The actual command included in this packet. Variable length, defined by packetLen.
     */
    private byte[] cmd;
    /**
     * Reads a packet from a buffer.
     * @param buf ByteBuffer to read packet from
     */
    public NFCPacket(ByteBuffer buf) {
        // Get the length first off
        packetLen = buf.getShort();
        deltaTimems = buf.getInt();
        byte isRec = buf.get();
        if(isRec == 0x00) {
            isReceived = false;
        } else {
            isReceived = true;
        }
        cmd = new byte[packetLen];
        buf.get(cmd, 0, packetLen);
    }
    public short getLen() {
        return packetLen;
    }
    public int getDelta() {
        return deltaTimems;
    }
    public boolean wasReceived() {
        return isReceived;
    }
    public byte[] getCmd() {
        return cmd;
    }
    /**
     * Formats the packet in human-readable format, for printing.
     * @return a String-formatted representation of the packet, human-readable.
     */
    public String format() {
        StringBuilder builder = new StringBuilder();
        builder.append(deltaTimems);
        builder.append(", ");
        builder.append(isReceived);
        builder.append(", ");
        builder.append(Hex.encodeHexString(cmd));
        return builder.toString();
    }
    /**
     * Gets a header of the human-readable format, for printing.
     * @return a String-formatted header for a file.
     */
    public static String getHeader() {
        StringBuilder builder = new StringBuilder();
        builder.append("Time offset");
        builder.append(", ");
        builder.append("received?");
        builder.append(", ");
        builder.append("Data");
        return builder.toString();
    }
}
