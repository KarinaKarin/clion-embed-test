package com.intellij.intern.y21.clion.svd;

import com.intellij.intern.y21.clion.stubs.Address;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class SvdCluster extends SvdRegisterLevel<SvdRegisterLevel<?>> implements RegisterPropsHolder {

    public SvdCluster(@NotNull String parentId, @NotNull String name, @NotNull String description, Address address,
                      @NotNull RegisterAccess access, int bitSize) {
        super(parentId + "|c:" + name, name, description, address, access, null, bitSize);
    }

    /**
     * @return all registers including nested
     */
    @NotNull
    public List<SvdRegister> getAllRegisters() {
        List<SvdRegister> registers = getChildren()
                .stream()
                .filter(SvdRegister.class::isInstance)
                .map(SvdRegister.class::cast)
                .collect(Collectors.toList());

        getChildren()
                .stream()
                .filter(SvdCluster.class::isInstance)
                .map(SvdCluster.class::cast)
                .forEach(c -> registers.addAll(c.getAllRegisters()));

        return registers;
    }

    @NotNull
    public List<SvdRegister> getClusterRegisters() {
        return getChildren()
                .stream()
                .filter(SvdRegister.class::isInstance)
                .map(SvdRegister.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public int getRegisterBitSize() {
        return getBitSize();
    }

    @Override
    public long getBaseAddress() {
        return getAddress().getUnsignedLongValue();
    }

    @Override
    @NotNull
    public RegisterAccess getRegisterAccess() {
        return getAccess();
    }
}
