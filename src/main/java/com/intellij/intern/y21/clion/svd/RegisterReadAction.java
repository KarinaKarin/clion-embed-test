package com.intellij.intern.y21.clion.svd;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public enum RegisterReadAction {
  CLEAR,
  SET,
  MODIFY,
  MODIFY_EXTERNAL;

  @Contract("null->null")
  @Nullable
  public static RegisterReadAction parse(@Nullable String s) {
    if (s != null && !s.isEmpty()) {
      switch (s) {
        case "clear":
          return CLEAR;
        case "set":
          return SET;
        case "modify":
          return MODIFY;
        case "modifyExternal":
          return MODIFY_EXTERNAL;
      }
    }
    return null;
  }
}
