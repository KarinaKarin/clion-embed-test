package com.intellij.intern.y21.clion.svd;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SvdNodeBase<CHILD_TYPE extends SvdNode<?>> implements SvdNode<CHILD_TYPE> {
  private final String myDescription;
  private final String name;
  private final String myId;
  private List<CHILD_TYPE> children = Collections.emptyList();

  public SvdNodeBase(
    @NotNull String id,
    @NotNull String name,
    @NotNull String description) {
    myId = id.intern();
    this.name = name;
    this.myDescription = description;
  }

  @Override
  @NotNull
  public String getDescription() {
    return myDescription;
  }

  @NotNull
  @Override
  public List<CHILD_TYPE> getChildren() {
    return children;
  }

  public void setChildren(@NotNull List<CHILD_TYPE> children) {
    this.children = children;
  }

  @Override
  @NotNull
  public String getName() {
    return name;
  }

  @Override
  @NotNull
  public String toString() {
    return getName();
  }

  @NotNull
  @Override
  public String getId() {
    return myId;
  }
}
