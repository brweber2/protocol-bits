package com.cobenian.protocol.bits;

/**
 * @author brweber2
 */
public class Bit
{
    // enum better here?
    public static final Bit ZERO = new Bit(false);
    public static final Bit ONE = new Bit(true);

    private boolean isOne;

    public Bit(boolean isOne)
    {
        this.isOne = isOne;
    }

    public boolean isZero()
    {
        return !isOne;
    }

    public boolean isOne()
    {
        return isOne;
    }

    public Bit and(Bit other)
    {
        return new Bit(isOne && other.isOne);
    }

    public Bit or(Bit other)
    {
        return new Bit(isOne || other.isOne);
    }

    public Bit xor(Bit other)
    {
        return new Bit(!matches(other));
    }

    public Bit not()
    {
        return new Bit(!isOne);
    }

    // todo bad idea to have mutation in this class?
    public void flip() {
        isOne = !isOne;
    }

    public boolean matches(Bit other)
    {
        return isOne == other.isOne;
    }

    @Override
    public String toString()
    {
        return (isOne)? "1":"0";
    }
}
