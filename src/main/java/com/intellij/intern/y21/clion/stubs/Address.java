package com.intellij.intern.y21.clion.stubs;

import org.jetbrains.annotations.NotNull;

public class Address implements Comparable<Address> {
    private final long address;

    public Address(long address) {
        this.address = address;
    }

    public static Address fromUnsignedLong(long addressOffset) {
        return new Address(addressOffset);
    }

    public int minus(Address addr) {
        return (int) (address - addr.address);
    }

    public Address minus(int addr) {
        return new Address(address - addr);

    }

    public long getUnsignedLongValue() {
        return address;
    }

    public Address plus(int calcSize) {
        return new Address(address + calcSize);
    }

    @Override
    public int compareTo(@NotNull Address other) {
        return Long.compare(address, other.address);
    }
}
