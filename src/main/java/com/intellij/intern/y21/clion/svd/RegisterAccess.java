package com.intellij.intern.y21.clion.svd;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public enum RegisterAccess {
  READ_ONLY,
  WRITE_ONLY,
  READ_WRITE,
  WRITEONCE,
  READ_WRITEONCE;

  public boolean isReadable() {
    switch (this) {
      case WRITE_ONLY:
      case WRITEONCE:
        return false;
      default:
        return true;
    }
  }

  @NotNull
  public static RegisterAccess parse(@Nullable String s) {
    switch (Objects.toString(s, "")) {
      case "read-only":
        return READ_ONLY;
      case "write-only":
        return WRITE_ONLY;
      case "writeOnce":
        return WRITEONCE;
      case "read-writeOnce":
        return READ_WRITEONCE;
      default:
        return READ_WRITE;
    }
  }
}
