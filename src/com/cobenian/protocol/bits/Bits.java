package com.cobenian.protocol.bits;

/**
 * @author brweber2
 */
public interface Bits<BitType>
{
    BitType get(int position);

    int length();

    byte[] toByteArray();

    public default int getByteForBit(int position)
    {
        return position / 8;
    }

    public default byte getOneMask(int bitInByte)
    {
        return (byte) (1 << ((8 - bitInByte - 1)));
    }

    public default byte getZeroMask(int bitInByte)
    {
        return (byte) ~getOneMask(bitInByte);
    }

    public default String byteToString(byte b)
    {
        String s = "";
        s += ((b & 0x80) != 0) ? "1" : "0";
        s += ((b & 0x40) != 0) ? "1" : "0";
        s += ((b & 0x20) != 0) ? "1" : "0";
        s += ((b & 0x10) != 0) ? "1" : "0";
        s += ((b & 0x08) != 0) ? "1" : "0";
        s += ((b & 0x04) != 0) ? "1" : "0";
        s += ((b & 0x02) != 0) ? "1" : "0";
        s += ((b & 0x01) != 0) ? "1" : "0";
        return s;
    }
}
