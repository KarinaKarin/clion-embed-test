package com.intellij.intern.y21.clion.svd;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public interface SvdNode<CHILD_TYPE extends SvdNode<?>> {

  Comparator<SvdNode<?>> NAME_COMPARATOR =
    (a, b) -> a.getName().compareToIgnoreCase(b.getName());

  @NotNull
  default String getName() {
    return "";
  }

  @NotNull
  default String getDescription() {
    return "";
  }

  @NotNull
  List<CHILD_TYPE> getChildren();

  @NotNull
  default String getDisplayValue() {
    return "";
  }

  @NotNull
  String getId();

  default void setFormatFromSign(char sign) {}

  default void exportCsv(@NotNull PrintWriter writer, @NotNull String prefix, @NotNull Predicate<SvdNode<?>> predicateActive) {
    for (CHILD_TYPE child : getChildren()) {
      if (predicateActive.test(child)) {
        child.exportCsv(writer, prefix, predicateActive);
      }
    }
  }
}


