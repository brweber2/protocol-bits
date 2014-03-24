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
        int a = position / 8;
        if ( position % 8 == 0 )
        {
            return a;
        }
        return a - 1; // zero indexed
    }

    public default byte getMask(int y)
    {
        return (byte) (1 << (8 - y - 1));
    }
}
