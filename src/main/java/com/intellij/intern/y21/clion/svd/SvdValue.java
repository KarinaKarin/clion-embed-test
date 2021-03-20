package com.intellij.intern.y21.clion.svd;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SvdValue<T extends SvdNodeBase<?>> extends SvdNodeBase<T> {

  private static final Map<Character, Format> ourFormatBySign =
    Stream.of(Format.values())
      .collect(Collectors.toMap(Format::getSign, t -> t));
  private final RegisterAccess myAccess;
  private final RegisterReadAction myReadAction;
  private final int myBitSize;
  protected volatile boolean changed;
  private Format myFormat;

  protected SvdValue(@NotNull String id,
                     @NotNull String name,
                     @NotNull String description,
                     @NotNull RegisterAccess access,
                     @Nullable RegisterReadAction readAction,
                     int bitSize,
                     Format format) {
    super(id, name, description);
    myAccess = access;
    myReadAction = readAction;
    myBitSize = bitSize;
    myFormat = format;
  }

  @Nullable
  protected RegisterReadAction getReadAction() {
    return myReadAction;
  }

  @NotNull
  protected RegisterAccess getAccess() {
    return myAccess;
  }

  @NotNull
  protected Format getFormat() {
    return myFormat;
  }

  protected void setFormat(@NotNull Format format) {
    myFormat = format;
  }

  protected int getBitSize() {
    return myBitSize;
  }

  protected boolean isChanged() {
    return changed;
  }

  @NotNull
  public abstract Format getDefaultFormat();

  @Override
  public void setFormatFromSign(char sign) {
    setFormat(ourFormatBySign.get(sign));
  }
}
