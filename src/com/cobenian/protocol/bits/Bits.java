package com.cobenian.protocol.bits;

/**
 * @author brweber2
 */
public interface Bits<BitType>
{
    BitType get(int position);

    int length();

    byte[] toByteArray();
}
