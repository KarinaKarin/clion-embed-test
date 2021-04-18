package com.intellij.intern.y21.clion.svd;

import org.jetbrains.annotations.NotNull;

public interface RegisterPropsHolder {
    int getRegisterBitSize();

    long getBaseAddress();

    @NotNull
    RegisterAccess getRegisterAccess();

    @NotNull
    String getId();
}
