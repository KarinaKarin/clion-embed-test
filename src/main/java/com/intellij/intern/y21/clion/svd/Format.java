package com.intellij.intern.y21.clion.svd;

import com.intellij.intern.y21.clion.stubs.EmbeddedBundle;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum Format {

  DEC(EmbeddedBundle.messagePointer("format.decimal"), 'D') {
    @NotNull
    @Override
    public String format(long value, int bits) {
      return Long.toUnsignedString(value, 10);
    }
  },
  HEX(EmbeddedBundle.messagePointer("format.hex"), 'H') {
    @NotNull
    @Override
    public String format(long value, int bits) {
      return bitGroupFormat(value, bits, 4, "0123456789ABCDEF", "0x");
    }
  },
  OCT(EmbeddedBundle.messagePointer("format.octal"), 'O') {
    @NotNull
    @Override
    public String format(long value, int bits) {
      return bitGroupFormat(value, bits, 3, "01234567", "0");
    }
  },
  BIN(EmbeddedBundle.messagePointer("format.binary"), 'B') {
    @NotNull
    @Override
    public String format(long value, int bits) {
      return bitGroupFormat(value, bits, 1, "01", "0b");
    }
  };

  @NotNull
  private final Supplier<@NotNull String> readableName;
  private final char sign;

  Format(@NotNull Supplier<@NotNull String> readableName, char sign) {
    this.readableName = readableName;
    this.sign = sign;
  }

  char getSign() {
    return sign;
  }

  public @NotNull Supplier<@NotNull String> getReadableName() {
    return readableName;
  }

  @NotNull
  public abstract String format(long value, int bits);

  private static String bitGroupFormat(long value, int bits, int bitsPerSymbol, String alphabet, String prefix) {
    StringBuilder result = new StringBuilder();
    int symCount = alphabet.length();
    for (; bits > 0; bits -= bitsPerSymbol) {
      result.append(alphabet.charAt((int)Long.remainderUnsigned(value, symCount)));
      value = Long.divideUnsigned(value, symCount);
    }
    return result.reverse().insert(0, prefix).toString();
  }
}
