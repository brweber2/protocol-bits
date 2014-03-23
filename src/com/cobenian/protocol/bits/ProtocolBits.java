package com.cobenian.protocol.bits;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author brweber2
 *         <p>
 *         This class is a serlization efficient, but memory inefficient implementation of BitStream
 */
public class ProtocolBits extends AbstractCollection<Bit> implements Collection<Bit>, BitStream<Bit,ProtocolBits>, Serializable
{
    private transient List<Bit> bits;

    public ProtocolBits()
    {
        this.bits = new ArrayList<>();
    }

    public ProtocolBits set(int position, Bit bit)
    {
        if (position >= bits.size())
        {
            bits.add(position, bit);
        } else
        {
            bits.set(position, bit);
        }
        return this;
    }

    public Bit get(int position)
    {
        return bits.get(position);
    }

    public ProtocolBits clear(int position)
    {
        return set(position, Bit.ZERO);
    }

    public ProtocolBits flip(int position)
    {
        set(position, get(position).not());
        return this;
    }

    public int length()
    {
        return bits.size();
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
        out.writeInt(bits.size());
        int bytesToWrite = bits.size() / 8;
        int remainingBits = bits.size() % 8;
        for (int i = 0; i < bytesToWrite; i++)
        {
            byte b = 0;
            if (bits.get(8 * i + 0).isOne())
            {
                b = (byte) (b | 0x80);
            }
            if (bits.get(8 * i + 1).isOne())
            {
                b = (byte) (b | 0x40);
            }
            if (bits.get(8 * i + 2).isOne())
            {
                b = (byte) (b | 0x20);
            }
            if (bits.get(8 * i + 3).isOne())
            {
                b = (byte) (b | 0x10);
            }
            if (bits.get(8 * i + 4).isOne())
            {
                b = (byte) (b | 0x08);
            }
            if (bits.get(8 * i + 5).isOne())
            {
                b = (byte) (b | 0x04);
            }
            if (bits.get(8 * i + 6).isOne())
            {
                b = (byte) (b | 0x02);
            }
            if (bits.get(8 * i + 7).isOne())
            {
                b = (byte) (b | 0x01);
            }
            out.writeByte(b);
        }
        for (int y = 0; y < remainingBits; y++)
        {
            byte b = 0;
            if (bits.get(8 * bytesToWrite + y).isOne())
            {
                b = (byte) (b | getMask(y));
            }
            out.writeByte(b);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        int numberOfBits = in.readInt();
        int numberOfBytesToRead = numberOfBits / 8;
        int numberOfRemainingBits = numberOfBits % 8;
        ProtocolBits bits = this;
        bits.bits = new ArrayList<>();
        for (int i = 0; i < numberOfBytesToRead; i++)
        {
            byte b = in.readByte();
            bits.set(8 * i + 0, new Bit((b & 0x80) > 0));
            bits.set(8 * i + 1, new Bit((b & 0x40) > 0));
            bits.set(8 * i + 2, new Bit((b & 0x20) > 0));
            bits.set(8 * i + 3, new Bit((b & 0x10) > 0));
            bits.set(8 * i + 4, new Bit((b & 0x08) > 0));
            bits.set(8 * i + 5, new Bit((b & 0x04) > 0));
            bits.set(8 * i + 6, new Bit((b & 0x02) > 0));
            bits.set(8 * i + 7, new Bit((b & 0x01) > 0));
        }
        if (numberOfRemainingBits > 0)
        {
            byte b = in.readByte();
            for (int y = 0; y < numberOfRemainingBits; y++)
            {
                bits.set(8 * numberOfBytesToRead + y, new Bit((b & getMask(y)) > 0));
            }
        }
    }

    private byte getMask(int y)
    {
        return (byte) (1 << (8 - y - 1));
    }

    @Override
    public Iterator<Bit> iterator()
    {
        return bits.iterator();
    }

    @Override
    public int size()
    {
        return bits.size();
    }

    public byte[] toByteArray()
    {
        int numberOfBytesNeeded = bits.size() / 8;
        int numberOfRemainingBits = bits.size() % 8;

        byte[] bytes = new byte[(numberOfBytesNeeded + ((numberOfRemainingBits == 0) ? 0 : 1))];
        for (int i = 0; i < numberOfBytesNeeded; i++)
        {
            byte b = 0;
            if (bits.get(8 * i + 0).isOne())
            {
                b = (byte) (b | (byte) 0x80);
            }
            if (bits.get(8 * i + 1).isOne())
            {
                b = (byte) (b | (byte) 0x40);
            }
            if (bits.get(8 * i + 2).isOne())
            {
                b = (byte) (b | (byte) 0x20);
            }
            if (bits.get(8 * i + 3).isOne())
            {
                b = (byte) (b | (byte) 0x10);
            }
            if (bits.get(8 * i + 4).isOne())
            {
                b = (byte) (b | (byte) 0x08);
            }
            if (bits.get(8 * i + 5).isOne())
            {
                b = (byte) (b | (byte) 0x04);
            }
            if (bits.get(8 * i + 6).isOne())
            {
                b = (byte) (b | (byte) 0x02);
            }
            if (bits.get(8 * i + 7).isOne())
            {
                b = (byte) (b | (byte) 0x01);
            }
            bytes[i] = b;
        }
        if (numberOfRemainingBits > 0)
        {
            byte b = 0;
            for (int y = 0; y < numberOfRemainingBits; y++)
            {
                if (bits.get(8 * numberOfBytesNeeded + y).isOne())
                {
                    b = (byte) (b | getMask(y));
                }
            }
            bytes[numberOfBytesNeeded] = b;
        }
        return bytes;
    }

    public static ProtocolBits read(InputStream inputStream) throws IOException
    {
        int i;
        int index = 0;
        ProtocolBits bits = new ProtocolBits();
        while ((i = inputStream.read()) != -1)
        {
            byte b = (byte) i;
            bits.set(index++, new Bit((b & 0x80) > 0));
            bits.set(index++, new Bit((b & 0x40) > 0));
            bits.set(index++, new Bit((b & 0x20) > 0));
            bits.set(index++, new Bit((b & 0x10) > 0));
            bits.set(index++, new Bit((b & 0x08) > 0));
            bits.set(index++, new Bit((b & 0x04) > 0));
            bits.set(index++, new Bit((b & 0x02) > 0));
            bits.set(index++, new Bit((b & 0x01) > 0));
        }
        return bits;
    }

    @Override
    public String toString()
    {
        return "ProtocolBits{" +
                "bits=" + bits +
                '}';
    }
}
