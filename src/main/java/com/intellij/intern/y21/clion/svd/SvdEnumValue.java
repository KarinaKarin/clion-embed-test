package com.intellij.intern.y21.clion.svd;

import org.jetbrains.annotations.NotNull;

public class SvdEnumValue extends SvdNodeBase<SvdNodeBase<?>> {
    private final @NotNull Long value;

    public SvdEnumValue(@NotNull String id, @NotNull String name, @NotNull String description,
                        @NotNull Long value) {
        super(id, name, description);
        this.value = value;
    }

    @Override
    @NotNull
    public String getDisplayValue() {
        return value.toString() + ": " + getName();
    }

    public @NotNull Long getValue() {
        return value;
    }
}
