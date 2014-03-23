package com.cobenian.protocol.bits;

import junit.framework.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author brweber2
 */
public class ProtocolBitsTest
{
    @Test
    public void testStream() throws IOException
    {
        ProtocolBits bits = ProtocolBits.read(new ByteArrayInputStream("abc".getBytes()));
        bits.stream().forEach(System.err::print);
        System.err.println();
    }

    @Test
    public void testStream2() throws IOException
    {
        ProtocolBits bits = ProtocolBits.read(new ByteArrayInputStream(new byte[] {1, 2, 3}));
        bits.stream().forEach(System.err::print);
        System.err.println();
    }

    @Test
    public void testStream3() throws IOException, ClassNotFoundException
    {
        ProtocolBits bits = ProtocolBits.read(new FileInputStream("/tmp/dns.cap"));
        bits.stream().forEach(System.err::print);
        System.err.println();

        FileOutputStream fos = new FileOutputStream("/tmp/dns2.cap");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(bits);
        oos.close();
        fos.close();

        FileInputStream fis = new FileInputStream("/tmp/dns2.cap");
        ObjectInputStream ois = new ObjectInputStream(fis);
        ProtocolBits sameBits = (ProtocolBits) ois.readObject();
        ois.close();
        fis.close();

        System.err.println("same bits>>>>");
        System.err.println(sameBits);

        FileOutputStream fw = new FileOutputStream("/tmp/dns3.cap");
        fw.write(sameBits.toByteArray());
        fw.close();
    }
}
