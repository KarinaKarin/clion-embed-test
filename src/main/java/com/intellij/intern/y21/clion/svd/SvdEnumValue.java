package com.intellij.intern.y21.clion.svd;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SvdEnumValue extends SvdNodeBase<SvdNodeBase<?>> {
    private final Long value;
    private final Boolean isDefault;

    public SvdEnumValue(@NotNull String id, @NotNull String name, @NotNull String description,
                        @Nullable Long value, @Nullable Boolean isDefault) {
        super(id, name, description);
        this.value = value;
        this.isDefault = isDefault;
    }

    @Override
    @NotNull
    public String getDisplayValue() {
        if (value != null) {
            return value.toString();
        } else if (isDefault != null){
            return isDefault.toString();
        }
        return "";
    }

    public Long getValue() {
        return value;
    }

    public Boolean getDefault() {
        return isDefault;
    }
}
