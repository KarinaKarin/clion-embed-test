package com.intellij.intern.y21.clion.svd;

import com.intellij.intern.y21.clion.stubs.Address;
import com.intellij.intern.y21.clion.stubs.LLMemoryHunk;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.intern.y21.clion.stubs.EmbeddedBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.function.Predicate;

public class SvdRegister extends SvdRegisterLevel<SvdField> {

  private volatile long value = 0;
  @Nullable
  private volatile String failReason = "-";

  public SvdRegister(@NotNull String peripheralName,
                     @NotNull String name,
                     @NotNull String description,
                     Address address,
                     int bitSize,
                     @NotNull RegisterAccess access,
                     @Nullable RegisterReadAction readAction) {
    super(peripheralName + "|" + name, name, description, address, access, readAction, bitSize);
    if (!getAccess().isReadable()) {
      markNoValue(EmbeddedBundle.message("svd.write.only"), false);
    }
  }

  public void processValue(@Nullable List<LLMemoryHunk> hunks, @Nullable Throwable e) {
    if (e != null) {
      markNoValue(e.getLocalizedMessage(), !(e instanceof CancellationException || e instanceof ProcessCanceledException));
    }
    else if (hunks == null) {
      markNoValue("-", true);
    }
    else {
      for (LLMemoryHunk hunk : hunks) {
        if (hunk.getRange().contains(getAddress())) {
          int byteSize = (getBitSize() + 7) / 8;
          int offset = getAddress().minus(hunk.getRange().getStart());
          long value = getValue(hunk, offset, byteSize);
          changed = this.value != value;
          if (changed) {
            this.value = value;
            for (SvdField field : getChildren()) {
              field.updateFromValue(value);
            }
          }
          else {
            getChildren().forEach(SvdField::markStalled);
          }
          failReason = null;
          return;
        }
      }
      markNoValue(EmbeddedBundle.message("non.readable"), true);
    }
  }

  protected long getValue(LLMemoryHunk hunk, int offset, int byteSize) {
    long value = 0;
    for (int i = byteSize - 1; i >= 0; i--) {
      value = (value << 8) | (0xFFL & hunk.getBytes().get(offset + i));
    }
    return value;
  }

  public void markNoValue(@NotNull String reason, boolean valueChange) {
    this.changed = valueChange && !reason.equals(failReason);
    this.failReason = reason;
  }

  @NotNull
  @Override
  public String getDisplayValue() {
    //noinspection ConstantConditions
    return isFailed() ? failReason : getFormat().format(value, getBitSize());
  }

  public boolean isFailed() {
    return failReason != null;
  }

  @Override
  public void exportCsv(@NotNull PrintWriter writer, @NotNull String prefix, @NotNull Predicate<SvdNode<?>> predicateActive) {
    writer.print(prefix);
    writer.print(", ");
    writer.print(getName());
    writer.print(", ");
    writer.print(getDisplayValue());
    if (!getChildren().isEmpty()) {
      writer.print(", ");
      for (int i = 0; i < getChildren().size(); i++) {
        SvdField field = getChildren().get(i);
        if (i > 0) {
          writer.print("; ");
        }
        writer.print(field.getName());
        writer.print(": ");
        writer.print(field.getDisplayValue());
      }
    }
    writer.println();
  }
}
