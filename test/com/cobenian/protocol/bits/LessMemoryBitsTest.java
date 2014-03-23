package com.cobenian.protocol.bits;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

/**
 * @author brweber2
 */
public class LessMemoryBitsTest
{
    @Test
    public void testStream() throws IOException
    {
        LessMemoryBits bits = LessMemoryBits.read(new ByteArrayInputStream("abc".getBytes()));
        System.err.println(bits.toString());
    }

    @Test
    public void testStream2() throws IOException
    {
        LessMemoryBits bits = LessMemoryBits.read(new ByteArrayInputStream(new byte[] {1,2,3}));
        System.err.println(bits.toString());
    }

    @Test
    public void testStream3() throws IOException, ClassNotFoundException
    {
        LessMemoryBits bits = LessMemoryBits.read(new FileInputStream("/tmp/dns.cap"));
        System.err.println("kong");
//        Arrays.asList(bits.toByteArray()).stream().forEach(System.err::print);
//        for (byte b : bits.toByteArray() ) {
//            System.err.println(b);
//        }
        System.err.println("donks");
        System.err.println();

        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        FileOutputStream fos = new FileOutputStream("/tmp/dns22.cap");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(bits);
        oos.close();
        fos.close();

        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        FileInputStream fis = new FileInputStream("/tmp/dns22.cap");
        ObjectInputStream ois = new ObjectInputStream(fis);
        LessMemoryBits sameBits = (LessMemoryBits) ois.readObject();
        ois.close();
        fis.close();

        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        System.err.println("same bits>>>>");
        System.err.println(sameBits);

        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        FileOutputStream fw = new FileOutputStream("/tmp/dns33.cap");
        fw.write(sameBits.toByteArray());
        fw.close();
    }
}
