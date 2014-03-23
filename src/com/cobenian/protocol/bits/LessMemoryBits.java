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
        byte[] bytes = bits.get(listIndex);
        bytes[byteIndex] = b;
    }

    public byte getByteFor(int bitPosition)
    {
        int bytePosition = bitPosition / 8;
        int remainingBits = bitPosition % 8;
        if ( remainingBits != 0 )
        {
            bytePosition++;
        }

        int listIndex = bytePosition / bufferSize;
        int byteIndex = bytePosition % bufferSize;
        byte[] bytes = bits.get(listIndex);
        return bytes[byteIndex];
    }

    public byte getOneMask(int bitInByte)
    {
        byte b = 1;
        return (byte) (b << ((8 - bitInByte) - 1));
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
        byte b = getByteFor(position);
        if ( isOne ) {
            b = (byte) (b | getOneMask(bitInByte));
        }
        else
        {
            b = (byte) (b & getZeroMask(bitInByte));
        }
        setByteFor(position, b);
        return this;
    }

    public Boolean get(int position)
    {
        if ( outOfRange(position) )
        {
            throw new IndexOutOfBoundsException(position + " is out of the range 0:" + length());
        }
        int bitInByte = position % 8;
        byte b = getByteFor(position);
        return (b | getOneMask(bitInByte)) > 0;
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
            System.err.println("adding new byte array at " + bitCount);
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
        s += ( (b & 0x80) > 0 ) ? "1":"0";
        s += ( (b & 0x40) > 0 ) ? "1":"0";
        s += ( (b & 0x20) > 0 ) ? "1":"0";
        s += ( (b & 0x10) > 0 ) ? "1":"0";
        s += ( (b & 0x08) > 0 ) ? "1":"0";
        s += ( (b & 0x04) > 0 ) ? "1":"0";
        s += ( (b & 0x02) > 0 ) ? "1":"0";
        s += ( (b & 0x01) > 0 ) ? "1":"0";
        return s;
    }
}
