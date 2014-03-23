package com.cobenian.protocol.bits;

/**
 * @author brweber2
 */
public interface BitStream<BitType, BitRange extends Bits<BitType>> extends Bits<BitType>
{
    BitRange set(int position, BitType bit);

    BitRange clear(int position);

    BitRange flip(int position);

    BitRange getInclusive(int start, int end);

    BitRange getExclusive(int start, int end);
}
