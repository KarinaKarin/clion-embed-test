package com.intellij.intern.y21.clion.svd;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SvdPeripheral extends SvdNodeBase<SvdRegisterLevel<?>> implements RegisterPropsHolder {

  private final int myRegisterBitSize;
  private final RegisterAccess myRegisterAccess;
  private final long myBaseAddress;

  public SvdPeripheral(@NotNull String fileName,
                       @NotNull String name,
                       @NotNull String description,
                       @Nullable RegisterAccess registerAccess,
                       long baseAddress,
                       int registerBitSize
  ) {
    super(fileName + "|p:" + name, name, description);
    this.myRegisterBitSize = registerBitSize;
    this.myBaseAddress = baseAddress;
    this.myRegisterAccess = registerAccess;
  }

  @Override
  public void exportCsv(@NotNull PrintWriter writer, @NotNull String prefix, @NotNull Predicate<SvdNode<?>> predicateActive) {
    super.exportCsv(writer, prefix + getName(), predicateActive);
  }

  @Override
  public int getRegisterBitSize() {
    return myRegisterBitSize;
  }

  @Override
  public long getBaseAddress() {
    return myBaseAddress;
  }

  @Override
  @NotNull
  public RegisterAccess getRegisterAccess() {
    return myRegisterAccess == null ? RegisterAccess.READ_WRITE : myRegisterAccess;
  }

  @NotNull
  public <T extends SvdRegisterLevel<?>> List<T> getChildren(Class<T> childType) {
    return getChildren()
            .stream()
            .filter(childType::isInstance)
            .map(childType::cast)
            .collect(Collectors.toList());
  }
}
