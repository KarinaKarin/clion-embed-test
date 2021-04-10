package com.intellij.intern.y21.clion.svd;

import com.intellij.intern.y21.clion.stubs.Address;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SvdCluster extends SvdRegisterLevel<SvdRegisterLevel<?>> {

    public SvdCluster(@NotNull String id, @NotNull String name, @NotNull String description, Address address,
                      @NotNull RegisterAccess access, @Nullable RegisterReadAction readAction, int bitSize) {
        super(id, name, description, address, access, readAction, bitSize);
    }
}
