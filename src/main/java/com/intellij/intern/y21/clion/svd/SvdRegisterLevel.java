package com.intellij.intern.y21.clion.svd;

import com.intellij.intern.y21.clion.stubs.Address;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.intern.y21.clion.svd.Format.HEX;

public abstract class SvdRegisterLevel<T extends SvdNodeBase<?>> extends SvdValue<T> {
    private static final Format DEFAULT_FORMAT = HEX;
    private final Address myAddress;

    protected SvdRegisterLevel(@NotNull String id, @NotNull String name, @NotNull String description,  Address address,
                               @NotNull RegisterAccess access, @Nullable RegisterReadAction readAction, int bitSize) {
        super(id, name, description, access, readAction, bitSize, DEFAULT_FORMAT);
        this.myAddress = address;

    }

    public Address getAddress() {
        return myAddress;
    }

    @NotNull
    @Override
    public Format getDefaultFormat() {
        return DEFAULT_FORMAT;
    }

    @NotNull
    public <CHILD_TYPE extends SvdRegisterLevel<?>> List<CHILD_TYPE> getChildren(Class<CHILD_TYPE> childType) {
        return getChildren()
                .stream()
                .filter(childType::isInstance)
                .map(childType::cast)
                .collect(Collectors.toList());
    }
}
