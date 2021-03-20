package com.intellij.intern.y21.clion.svd;

import com.intellij.intern.y21.clion.stubs.Address;
import com.intellij.intern.y21.clion.stubs.LLMemoryHunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SvdRegisterBigEndian extends SvdRegister {
  public SvdRegisterBigEndian(@NotNull String peripheralName,
                              @NotNull String name,
                              @NotNull String description,
                              Address address,
                              int bitSize,
                              @NotNull RegisterAccess access,
                              @Nullable RegisterReadAction readAction) {
    super(peripheralName, name, description, address, bitSize, access, readAction);
  }

  @Override
  protected long getValue(LLMemoryHunk hunk, int offset, int byteSize) {
    long value = 0;
    for (int i = 0; i < byteSize; i++) {
      value = (value << 8) | (0xFFL & hunk.getBytes().get(offset + i));
    }
    return value;
  }
}
