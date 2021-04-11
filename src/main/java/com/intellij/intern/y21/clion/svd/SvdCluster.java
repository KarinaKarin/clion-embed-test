package com.intellij.intern.y21.clion.svd;

import com.intellij.intern.y21.clion.stubs.Address;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class SvdCluster extends SvdRegisterLevel<SvdRegisterLevel<?>> {

    public SvdCluster(@NotNull String parentId, @NotNull String name, @NotNull String description, Address address,
                      @NotNull RegisterAccess access, int bitSize) {
        super(parentId + "|c:" + name, name, description, address, access, null, bitSize);
    }

    @NotNull
    public List<SvdRegister> getRegisters() {
        List<SvdRegister> registers = getChildren()
                .stream()
                .filter(n -> n instanceof SvdRegister)
                .map(n -> (SvdRegister) n)
                .collect(Collectors.toList());

        getChildren()
                .stream()
                .filter(n -> n instanceof SvdCluster)
                .map(n -> (SvdCluster) n)
                .forEach(c -> registers.addAll(c.getRegisters()));

        return registers;
    }
}
