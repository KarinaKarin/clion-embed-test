package com.intellij.intern.y21.clion.svd;

import org.jetbrains.annotations.NotNull;

public class SvdEnum extends SvdNodeBase<SvdEnumValue> {
    private final RegisterAccess usage;

    public SvdEnum(@NotNull String parentId, @NotNull String name, @NotNull RegisterAccess usage) {
        super(parentId + "|" + name, name, "");
        this.usage = usage;
    }

    public SvdEnumValue getEnumValue(long value) {
        return getChildren()
                .stream()
                .filter(e -> e.getValue() == value)
                .findAny().orElse(null);
    }

    public RegisterAccess getUsage() {
        return usage;
    }
}
