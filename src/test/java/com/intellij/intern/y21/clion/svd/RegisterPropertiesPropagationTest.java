package com.intellij.intern.y21.clion.svd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static com.intellij.intern.y21.clion.svd.RegisterAccess.READ_ONLY;
import static com.intellij.intern.y21.clion.svd.RegisterAccess.READ_WRITE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegisterPropertiesPropagationTest {

    private ArrayList<SvdNode<?>> myAllNodes;

    @BeforeEach
    public void setUp() throws Exception {
        SvdRoot mySvdRoot = new SvdRoot();
        mySvdRoot.addFile(SvdAddressCoalescerTest.class.getResourceAsStream("coalesce.svd"), "coalesce.svd", "coalesce.svd");
        myAllNodes = new ArrayList<>();
        myAllNodes.add(mySvdRoot);
        for (int i = 0; i < myAllNodes.size(); i++) {
            myAllNodes.addAll(myAllNodes.get(i).getChildren());
        }
    }

    @Test
    public void testPeriphClusterPropagation() {
        testPeripheralCluster("P1", "DPLL", 0x1030, 32, READ_WRITE);
    }

    @Test
    public void testNestedClusterPropagation() {
        testClusterCluster("DPLL", "USART_EXT", 0x1040, 32, READ_WRITE);
    }

    @Test
    public void testClusterRegisterPropagation() {
        testClusterRegister("DPLL", "DPLLRATIO", 0x1031, 32, READ_WRITE);
        testClusterRegister("USART_EXT", "CTRLA", 0x1040, 32, READ_WRITE);
    }

    @Test
    public void testRegisterPropsPriority() {
        testClusterRegister("DPLL", "DPLLCTRLA", 0x1030, 8, READ_WRITE);
        testClusterRegister("USART_EXT", "CTRLB", 0x1044, 32, READ_ONLY);
    }

    private void testPeripheralCluster(String peripheralName, String clusterName,
                                       long expectedAddress, int expectedBitSize,
                                       RegisterAccess expectedAccess) {
        SvdPeripheral peripheral = nodeByName(SvdPeripheral.class, peripheralName);

        SvdCluster cluster = peripheral.getChildren(SvdCluster.class)
                .stream()
                .filter(c -> c.getName().equals(clusterName))
                .findAny()
                .orElseThrow(AssertionError::new);

        assertEquals(expectedAddress, cluster.getAddress().getUnsignedLongValue());
        assertEquals(expectedBitSize, cluster.getBitSize());
        assertEquals(expectedAccess, cluster.getAccess());
    }

    private void testClusterCluster(String clusterName, String nestedClusterName,
                                    long expectedAddress, int expectedBitSize,
                                    RegisterAccess expectedAccess) {
        SvdCluster cluster = nodeByName(SvdCluster.class, clusterName);

        SvdCluster nestedCluster = cluster.getChildren(SvdCluster.class)
                .stream()
                .filter(c -> c.getName().equals(nestedClusterName))
                .findAny()
                .orElseThrow(AssertionError::new);

        assertEquals(expectedAddress, nestedCluster.getAddress().getUnsignedLongValue());
        assertEquals(expectedBitSize, nestedCluster.getBitSize());
        assertEquals(expectedAccess, nestedCluster.getAccess());
    }

    private void testClusterRegister(String clusterName, String registerName,
                                     long expectedAddress, int expectedBitSize,
                                     RegisterAccess expectedAccess) {
        SvdCluster cluster = nodeByName(SvdCluster.class, clusterName);

        SvdRegister register = cluster.getClusterRegisters()
                .stream()
                .filter(c -> c.getName().equals(registerName))
                .findAny()
                .orElseThrow(AssertionError::new);

        assertEquals(expectedAddress, register.getAddress().getUnsignedLongValue());
        assertEquals(expectedBitSize, register.getBitSize());
        assertEquals(expectedAccess, register.getAccess());
    }

    private <T extends SvdNode<?>> T nodeByName(Class<T> clazz, String name) {
        return myAllNodes.stream()
                .filter(node -> name.equals(node.getName()))
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .findAny()
                .orElseThrow(AssertionError::new);
    }
}
