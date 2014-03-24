package com.cobenian.protocol.bits;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author brweber2
 */
public class LessMemoryBits extends AbstractCollection<byte[]> implements Collection<byte[]>, BitStream<Boolean, LessMemoryBits>, Serializable
{
    public static final int DEFAULT_BUFFER_SIZE = 64;

    private transient List<byte[]> bits = new ArrayList<>();
    private int bitCount = 0;
    private int bufferSize;

    // **** STATIC METHODS ****

    public static LessMemoryBits read(InputStream inputStream) throws IOException
    {
        LessMemoryBits lessMemoryBits = new LessMemoryBits();
        int i;
        while ((i = inputStream.read()) >= 0)
        {
            lessMemoryBits.addByte((byte)i);
        }
        return lessMemoryBits;
    }

    // **** CONSTRUCTORS ****

    public LessMemoryBits(int bufferSize)
    {
        this.bufferSize = bufferSize;
    }

    public LessMemoryBits()
    {
        this.bufferSize = DEFAULT_BUFFER_SIZE;
    }

    // **** COLLECTION METHODS ****

    @Override
    public Iterator<byte[]> iterator()
    {
        return Arrays.asList(toByteArray()).iterator();
    }

    @Override
    public int size()
    {
        return length();
    }

    @Override
    public boolean add(byte[] bytes)
    {
        for (byte aByte : bytes)
        {
            addByte(aByte);
        }
        return true;
    }

    // **** BIT RELATED METHODS ****

    public LessMemoryBits set(int position, Boolean isOne)
    {
        if (isOne == null)
        {
            throw new RuntimeException("Bit cannot be null, must be either 0 or 1.");
        }
        if (outOfRange(position))
        {
            expandTo(position);
        }
        int bitInByte = position % 8;
        byte b = getByteFor(position);
        if (isOne)
        {
            b = (byte) (b | getOneMask(bitInByte));
        }
        else
        {
            b = (byte) (b & getZeroMask(bitInByte));
        }
        int bytePosition = getByteForBit(position);
        setByteFor(bytePosition, b);
        return this;
    }

    public Boolean get(int position)
    {
        if (outOfRange(position))
        {
            throw new IndexOutOfBoundsException(position + " is out of the range 0:" + length());
        }
        int bitInByte = position % 8;
        byte b = getByteFor(position);
        return ((byte) (b & getOneMask(bitInByte))) != 0;
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
        if (end < start)
        {
            throw new RuntimeException("avoiding infinite loop");
        }
        LessMemoryBits bits = new LessMemoryBits();
        for (int i = start; i <= end; i++)
        {
            bits.addBit(this.get(i));
        }
        return bits;
    }

    @Override
    public LessMemoryBits getExclusive(int start, int end)
    {
        if (end < start)
        {
            throw new RuntimeException("avoiding infinite loop");
        }
        LessMemoryBits bits = new LessMemoryBits();
        for (int i = start; i < end; i++)
        {
            bits.addBit(this.get(i));
        }
        return bits;
    }

    public int length()
    {
        return bitCount;
    }

    public byte[] toByteArray()
    {
        int bytesNeeded = getByteForBit(bitCount);

        byte[] bytes = new byte[bytesNeeded];
        int index = 0;
        for (byte[] bit : bits)
        {
            int bytesToCopy = Math.min(bit.length, bytesNeeded - index);
            System.arraycopy(bit, 0, bytes, index, bytesToCopy);
            index += bytesToCopy;
            if (index >= bytesNeeded)
            {
                break;
            }
        }
        return bytes;
    }

    // **** SERIALIZATION ****

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.writeInt(bitCount);
        out.writeInt(bufferSize);
        for (byte b : toByteArray())
        {
            out.writeByte(b);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        int bitCount = in.readInt();
        int bufferSize = in.readInt();
        LessMemoryBits lessMemoryBits = this;
        this.bitCount = 0;
        this.bits = new ArrayList<>();
        this.bufferSize = bufferSize;
        int byteCount = getByteForBit(bitCount);
        int remainingBits = bitCount % 8;
        for (int i = 0; i < byteCount; i++)
        {
            lessMemoryBits.addByte(in.readByte());
        }
        if (remainingBits != 0)
        {
            lessMemoryBits.addByte(in.readByte());
        }
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

    // **** IMPL SPECIFIC HELPERS ****

    private boolean outOfRange(int position)
    {
        return position >= bitCount;
    }

    private void expandTo(int position)
    {
        int bitsNeeded = position - bitCount;
        int numberOfBytesNeeded = getByteForBit(bitsNeeded);
        int bytesUsed = getByteForBit(bitCount);

        for (int i = bytesUsed; i < bytesUsed + numberOfBytesNeeded; i++)
        {
            setByteFor(i, (byte) 0);
        }
        bitCount = position;
    }

    public void setByteFor(int bytePosition, byte b)
    {
        int listIndex = getArrayForByte(bytePosition);
        int byteIndex = bytePosition % bufferSize;
        while (bits.size() <= listIndex)
        {
            bits.add(new byte[bufferSize]);
        }
        byte[] bytes = bits.get(listIndex);
        bytes[byteIndex] = b;
    }

    public byte getByteFor(int bitPosition)
    {
        int bytePosition = getByteForBit(bitPosition);
        int listIndex = getArrayForByte(bytePosition);
        int byteIndex = bytePosition % bufferSize;
        byte[] bytes = bits.get(listIndex);
        return bytes[byteIndex];
    }

    public void addBit(boolean isOne)
    {
        int size = length();
        expandTo(size + 1);
        set(size, isOne);
    }

    public void addByte(byte b)
    {
        int byteIndex = getByteForBit(bitCount);
        if (byteIndex % bufferSize != 0)
        {
            byte[] bytes = bits.get(getArrayForByte(byteIndex));
            bytes[byteIndex % bufferSize] = b;
        }
        else
        {
            byte[] bytes = new byte[bufferSize];
            bytes[0] = b;
            bits.add(bytes);
        }
        bitCount += 8;
    }

    public int getArrayForByte(int byteIndex)
    {
       return byteIndex / bufferSize;
    }

}
