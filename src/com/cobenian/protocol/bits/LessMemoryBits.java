package com.cobenian.protocol.bits;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author brweber2
 */
public class LessMemoryBits implements BitStream<Boolean,LessMemoryBits>, Serializable
{
    public static final int DEFAULT_BUFFER_SIZE = 64;

    private transient List<byte[]> bits = new ArrayList<>();
    private int bitCount = 0;
    private int bufferSize;

    public LessMemoryBits(int bufferSize)
    {
        this.bufferSize = bufferSize;
    }

    public LessMemoryBits()
    {
        this.bufferSize = DEFAULT_BUFFER_SIZE;
    }

    private boolean outOfRange(int position)
    {
        return position >= bitCount;
    }

    private void expandTo(int position)
    {
//        System.err.println("expanding to " + position);
        int bitsNeeded = position - bitCount;
        int numberOfBytesNeeded = bitsNeeded / 8;
        int remainingBits = bitsNeeded % 8;
        if ( remainingBits != 0 )
        {
            numberOfBytesNeeded++;
        }

        int bytesUsed = bitCount / 8;
        int remainingBitsUsed = bitCount % 8;
        if ( remainingBitsUsed != 0 )
        {
            bytesUsed++;
        }
//        System.err.println("number of bytes needed? " + numberOfBytesNeeded);
        for ( int i = bytesUsed; i < bytesUsed + numberOfBytesNeeded; i++ )
        {
            setByteFor(i, (byte)0);
        }
        bitCount = position;
    }

    public void setByteFor(int bytePosition, byte b)
    {
        int listIndex = bytePosition / bufferSize;
        int byteIndex = bytePosition % bufferSize;
        while ( bits.size() <= listIndex )
        {
            bits.add(new byte[bufferSize]);
        }
        byte[] bytes = bits.get(listIndex);
        bytes[byteIndex] = b;
    }

    public byte getByteFor(int bitPosition)
    {
        int bytePosition = bitPosition / 8;
        int remainingBits = bitPosition % 8;
        if ( remainingBits != 0 )
        {
            System.err.println("incrementing");
            bytePosition++;
        }

        System.err.println("byte position is " + bytePosition + " for bit " + bitPosition);

        int listIndex = bytePosition / bufferSize;
        int byteIndex = bytePosition % bufferSize;
        System.err.println("getting byte array " + listIndex + " for bit " + bitPosition);
        byte[] bytes = bits.get(listIndex);
        System.err.println("getting byte index " + byteIndex + " for bit " + bitPosition);
        return bytes[byteIndex];
    }

    public byte getOneMask(int bitInByte)
    {
        byte b = 1;
        return (byte) (b << ((8 - bitInByte - 1)));
    }

    public byte getZeroMask(int bitInByte)
    {
        return (byte) ~ getOneMask(bitInByte);
    }

    public LessMemoryBits set(int position, Boolean isOne)
    {
        if ( isOne == null )
        {
            throw new RuntimeException("Bit cannot be null, must be either 0 or 1.");
        }
        if ( outOfRange(position) )
        {
            expandTo(position);
        }
        int bitInByte = position % 8;
        System.err.println("getting byte at bit position " + position);
        byte b = getByteFor(position);
        System.err.println("before anything: " + byteToString(b));
        if ( isOne ) {
            System.err.println("or mask is " + byteToString(getOneMask(bitInByte)) + " for bit " + bitInByte);
            System.err.println("before byte: " + byteToString(b));
            b = (byte) (b | getOneMask(bitInByte));
            System.err.println("after byte: " + byteToString(b));
        }
        else
        {
            System.err.println("and mask is " + byteToString(getOneMask(bitInByte)) + " for bit " + bitInByte);
            b = (byte) (b & getZeroMask(bitInByte));
        }
        System.err.println("saving " + byteToString(b) + " at " + position);
        int bytePosition = position / 8;
        int remainingBits = position % 8;
        if ( remainingBits != 0 )
        {
            bytePosition++;
        }
        System.err.println("setting byte " + bytePosition + " to " + byteToString(b));
        setByteFor(bytePosition, b);
        return this;
    }

    public Boolean get(int position)
    {
        if ( outOfRange(position) )
        {
            throw new IndexOutOfBoundsException(position + " is out of the range 0:" + length());
        }
        int bitInByte = (position % 8) - 1;
        byte b = getByteFor(position);
        System.err.println("looking at byte: " + byteToString(b));
        System.err.println("OR mask: " + byteToString(getOneMask(bitInByte)));
        System.err.println(byteToString((byte) (b & getOneMask(bitInByte))));
        return ((byte) (b & getOneMask(bitInByte))) != (byte) 0x00;
    }

    public LessMemoryBits clear(int position)
    {
        return set(position, false);
    }

    public LessMemoryBits flip(int position)
    {
        set(position, !get(position));
        return this;
    }

    @Override
    public LessMemoryBits getInclusive(int start, int end)
    {
        if ( end < start )
        {
            throw new RuntimeException("avoiding infinite loop");
        }
        LessMemoryBits bits = new LessMemoryBits();
        for ( int i = start; i <= end; i++ )
        {
            boolean isOne = this.get(i);
            System.err.println("adding bit: " + ((isOne)?"1":"0"));
            bits.addBit(isOne);
        }
        return bits;
    }

    @Override
    public LessMemoryBits getExclusive(int start, int end)
    {
        if ( end < start )
        {
            throw new RuntimeException("avoiding infinite loop");
        }
        LessMemoryBits bits = new LessMemoryBits();
        for ( int i = start; i < end; i++ )
        {
            bits.addBit(this.get(i));
        }
        return bits;
    }

    public int length()
    {
        return bitCount;
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
//        out.defaultWriteObject();
        out.writeInt(bitCount);
        out.writeInt(bufferSize);
        for ( byte b : toByteArray() )
        {
            out.writeByte(b);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
//        in.defaultReadObject();
        int bitCount = in.readInt();
        int bufferSize = in.readInt();
        LessMemoryBits lessMemoryBits = this;
        this.bits = new ArrayList<>();
//        this.bitCount = bitCount;
        this.bufferSize = bufferSize;
        int byteCount = bitCount / 8;
        int remainingBits = bitCount % 8;
        System.err.println("byte count is: " + byteCount);
        for ( int i = 0; i < byteCount; i++ )
        {
            System.err.println("\tadding byte " + i);
            lessMemoryBits.addByte(in.readByte());
        }
        if ( remainingBits != 0 )
        {
            lessMemoryBits.addByte(in.readByte());
        }
    }

    public Iterator<byte[]> iterator()
    {
        return Arrays.asList(toByteArray()).iterator();
    }

    public int size()
    {
        return length();
    }

    public byte[] toByteArray()
    {
        int bytesFilled = bitCount / 8;
        int remainingBits = bitCount % 8;
        int bytesNeeded = bytesFilled;
        if ( remainingBits != 0 ) {
            bytesNeeded++;
        }
        byte[] bytes = new byte[bytesNeeded];
        int index = 0;
        for (byte[] bit : bits)
        {
//            System.err.println("index: " + index + " bytesneeded " + bytesNeeded);
//            System.err.println("\tbit.length " + bit.length + " and " + (bytesNeeded-index));
            int bytesToCopy = Math.min(bit.length, bytesNeeded - index);
            System.arraycopy(bit, 0, bytes, index, bytesToCopy);
            index += bytesToCopy;
            if ( index >= bytesNeeded )
            {
//                System.err.println("all done");
                break;
            }
        }
        return bytes;
    }

    public static LessMemoryBits read(InputStream inputStream) throws IOException
    {
        LessMemoryBits lessMemoryBits = new LessMemoryBits();
        int i;
        while ( (i = inputStream.read()) >= 0 )
        {
            byte b = (byte) i;
            lessMemoryBits.addByte(b);
        }
        return lessMemoryBits;
    }

    public void addBit(boolean isOne)
    {
        int size = length();
        expandTo(size + 1);
        System.err.println("expanded to " + length());
        set(size, isOne);
    }

    public void addByte(byte b)
    {
//        System.err.println("buffer size: " + bufferSize);
        if ( bitCount / 8 % bufferSize != 0 )
        {
            int byteIndex = bitCount / 8 / bufferSize;
//            System.err.println("for bitCount " + bitCount + " the byte index is " + byteIndex);
            byte[] bytes = bits.get(byteIndex);
            bytes[(bitCount/8) % bufferSize] = b;
        }
        else
        {
//            System.err.println("adding new byte array at " + bitCount);
            byte[] bytes = new byte[bufferSize];
            bytes[0] = b;
            bits.add(bytes);
        }
        bitCount += 8;
    }

    @Override
    public String toString()
    {
        return "LessMemoryBits{bits=" +
            bytesToString(toByteArray())
            + "}";
    }

    private String bytesToString(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes)
        {
            sb.append(byteToString(aByte));
        }
        return sb.toString();
    }

    private String byteToString(byte b)
    {
        String s = "";
        s += ( (b & 0x80) != 0 ) ? "1":"0";
        s += ( (b & 0x40) != 0 ) ? "1":"0";
        s += ( (b & 0x20) != 0 ) ? "1":"0";
        s += ( (b & 0x10) != 0 ) ? "1":"0";
        s += ( (b & 0x08) != 0 ) ? "1":"0";
        s += ( (b & 0x04) != 0 ) ? "1":"0";
        s += ( (b & 0x02) != 0 ) ? "1":"0";
        s += ( (b & 0x01) != 0 ) ? "1":"0";
        return s;
    }
}
