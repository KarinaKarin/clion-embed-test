package com.intellij.intern.y21.clion.svd;

import org.jetbrains.annotations.NotNull;

public class SvdEnum extends SvdNodeBase<SvdEnumValue> {
    private final EnumUsage usage;

    public SvdEnum(@NotNull String parentId, @NotNull String name, @NotNull EnumUsage usage) {
        super(parentId + "|" + name, name, "");
        this.usage = usage;
    }

    public EnumUsage getUsage() {
        return usage;
    }
}
