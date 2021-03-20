package com.intellij.intern.y21.clion.svd;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.intern.y21.clion.svd.Format.BIN;

public class SvdField extends SvdValue<SvdNodeBase<?>> {

  private static final Format DEFAULT_FORMAT = BIN;
  private final int myBitOffset;
  private final SvdRegister myParent;

  private long value = 0;

  public SvdField(@NotNull SvdRegister parent,
                  @NotNull String name,
                  @NotNull String description,
                  @NotNull RegisterAccess access,
                  @Nullable RegisterReadAction readAction,
                  int bitOffset, int size) {
    super(parent.getId() + "|" + name,
          name, description, access, readAction, size, DEFAULT_FORMAT);
    this.myBitOffset = bitOffset;
    this.myParent = parent;
  }

  public int getBitOffset() {
    return myBitOffset;
  }

  public void updateFromValue(long registerValue) {
    long newValue = registerValue >> myBitOffset;
    newValue &= ~(0xFFFFFFFFFFFFFFFFL << getBitSize());
    changed = value != newValue;
    value = newValue;
  }

  @Override
  @NotNull
  public String getDisplayValue() {
    if (getAccess().isReadable() && !myParent.isFailed()) {
      return getFormat().format(value, getBitSize());
    }
    else {
      return "-";
    }
  }

  @NotNull
  @Override
  public Format getDefaultFormat() {
    return DEFAULT_FORMAT;
  }

  public void markStalled() {
    changed = false;
  }
}
