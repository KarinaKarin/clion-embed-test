package com.intellij.intern.y21.clion.stubs;

public class AddressRange {

    private final Address start;
    private final Address end;

    public AddressRange(Address start, Address end) {
        this.start = start;
        this.end = end;
    }

    public boolean contains(Address myAddress) {
        return false;
    }

    public Address getStart() {
        return start;
    }

    public Address getEnd() {
        return end;
    }

}
