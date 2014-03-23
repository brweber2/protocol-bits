package com.cobenian.protocol.bits;

/**
 * @author brweber2
 */
public interface BitStream<BitType, BitRange extends Bits<BitType>> extends Bits<BitType>
{
    BitType get(int position);

    BitRange set(int position, BitType bit);

    BitRange clear(int position);

    BitRange flip(int position);

    int length();

    byte[] toByteArray();
}
