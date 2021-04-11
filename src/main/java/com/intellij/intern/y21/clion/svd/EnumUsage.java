package com.intellij.intern.y21.clion.svd;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public enum EnumUsage {
    READ,
    WRITE,
    READ_WRITE;

    @Contract("null->null")
    @Nullable
    public static EnumUsage parse(@Nullable String s) {
        if (s != null && !s.isEmpty()) {
            switch (s) {
                case "read-write":
                    return READ_WRITE;
                case "read":
                    return READ;
                case "write":
                    return WRITE;
            }
        }
        return null;
    }
}
