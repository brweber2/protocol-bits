package com.cobenian.protocol.bits;

/**
 * @author brweber2
 */
public enum Bit
{
    ZERO(false),
    ONE(true);

    private final boolean isOne;

    Bit(boolean isOne)
    {
        this.isOne = isOne;
    }

    public static Bit fromBoolean(boolean isOne)
    {
        if (isOne)
        {
            return ONE;
        }
        return ZERO;
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
        return Bit.fromBoolean(isOne && other.isOne);
    }

    public Bit or(Bit other)
    {
        return Bit.fromBoolean(isOne || other.isOne);
    }

    public Bit xor(Bit other)
    {
        return Bit.fromBoolean(!matches(other));
    }

    public Bit not()
    {
        return Bit.fromBoolean(!isOne);
    }

    public boolean matches(Bit other)
    {
        return isOne == other.isOne;
    }

    @Override
    public String toString()
    {
        return (isOne) ? "1" : "0";
    }
}
