package com.intellij.intern.y21.clion.svd;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SvdFile implements SvdNode<SvdNodeBase<?>> {

  private final String myName;
  private final String myLocation;
  private List<SvdNodeBase<?>> children = Collections.emptyList();

  public SvdFile(@NotNull String name, @NotNull String location) {
    this.myName = name;
    this.myLocation = location;
  }

  @Override
  public String toString() {
    return getName();
  }

  @NotNull
  @Override
  public String getName() {
    return myName;
  }

  @NotNull
  @Override
  public List<SvdNodeBase<?>> getChildren() {
    return children;
  }

  public void setChildren(@NotNull List<SvdNodeBase<?>> children) {
    this.children = children;
  }

  @NotNull
  @Override
  public String getId() {
    return getName();
  }

  @NotNull
  public String getLocation() {
    return myLocation;
  }
}
