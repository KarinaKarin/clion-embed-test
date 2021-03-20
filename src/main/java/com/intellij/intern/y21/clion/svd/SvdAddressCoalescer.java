package com.intellij.intern.y21.clion.svd;

import com.intellij.intern.y21.clion.stubs.Address;
import com.intellij.intern.y21.clion.stubs.AddressRange;
import com.intellij.openapi.util.Pair;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SvdAddressCoalescer {
  protected final List<Pair<AddressRange, List<SvdRegister>>> myRanges = new ArrayList<>();

  public void loadFrom(@NotNull Collection<SvdNode<?>> nodes) {

    List<SvdRegister> sortedRegisters = nodes
      .stream()
      .filter(SvdRegister.class::isInstance)
      .map(SvdRegister.class::cast)
      .filter(register -> register.getAccess().isReadable())
      .sorted(Comparator.comparing(SvdRegister::getAddress))
      .collect(Collectors.toList());
    if (sortedRegisters.isEmpty()) {
      return;
    }

    List<SvdRegister> lastList = null;
    Address lastStart = null;
    Address lastEnd = null;
    for (SvdRegister register : sortedRegisters) {
      Address registerEnd = register.getAddress().plus(calcSize(register));
      if (lastStart != null) {
        if (lastStart.compareTo(register.getAddress()) <= 0 && lastEnd.compareTo(register.getAddress()) >= 0) {
          lastList.add(register);
          if (lastEnd.compareTo(registerEnd) < 0) {
            lastEnd = registerEnd;
          }
        }
        else if (lastStart.compareTo(registerEnd) <= 0 && lastEnd.compareTo(registerEnd) >= 0) {
          lastList.add(register);
          if (lastStart.compareTo(register.getAddress()) < 0) {
            lastStart = register.getAddress();
          }
        }
        else {
          myRanges.add(Pair.create(new AddressRange(lastStart, lastEnd.minus(1)), lastList));
          lastStart = register.getAddress();
          lastEnd = registerEnd;
          lastList = new SmartList<>();
          lastList.add(register);
        }
      }
      else {
        lastStart = register.getAddress();
        lastEnd = registerEnd;
        lastList = new SmartList<>();
        lastList.add(register);
      }
    }
    myRanges.add(Pair.create(new AddressRange(lastStart, lastEnd.minus(1)), lastList));
  }

  public static SvdAddressCoalescer create(@NotNull Collection<SvdNode<?>> nodes) {
    SvdAddressCoalescer coalescer = new SvdAddressCoalescer();
    coalescer.loadFrom(nodes);
    return coalescer;
  }

  private static int calcSize(SvdRegister register) {
    return (register.getBitSize() + 7) / 8;
  }

}
