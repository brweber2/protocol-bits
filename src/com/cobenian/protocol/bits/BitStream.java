package com.cobenian.protocol.bits;

import java.util.stream.Stream;

/**
 * @author brweber2
 */
public interface BitStream<BitType,BitRange extends Bits<BitType>> extends Bits<BitType>
{
    BitType get(int position);

    BitRange set(int position, BitType bit);
}
