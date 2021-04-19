package com.intellij.intern.y21.clion.svd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import static com.intellij.intern.y21.clion.svd.RegisterAccess.READ_ONLY;
import static com.intellij.intern.y21.clion.svd.RegisterAccess.READ_WRITE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegisterPropertiesPropagationTest {

    private ArrayList<SvdNode<?>> myAllNodes;

    @BeforeEach
    public void setUp() throws Exception {
        SvdRoot mySvdRoot = new SvdRoot();
        mySvdRoot.addFile(RegisterPropertiesPropagationTest.class.getResourceAsStream("coalesce.svd"), "coalesce.svd", "coalesce.svd");
        myAllNodes = new ArrayList<>();
        myAllNodes.add(mySvdRoot);
        for (int i = 0; i < myAllNodes.size(); i++) {
            myAllNodes.addAll(myAllNodes.get(i).getChildren());
        }
    }

    @Test
    public void testPeriphClusterPropagation() {
        testPeripheralClusterPropagation("P1", "DPLL", 0x1030, 32, READ_WRITE);
    }

    @Test
    public void testNestedClusterPropagation() {
        testClusterClusterPropagation("DPLL", "USART_EXT", 0x1040, 32, READ_WRITE);
    }

    @Test
    public void testClusterRegisterPropagation() {
        testClusterRegisterPropagation("DPLL", "DPLLRATIO", 0x1031, 32, READ_WRITE);
        testClusterRegisterPropagation("USART_EXT", "CTRLA", 0x1040, 32, READ_WRITE);
    }

    @Test
    public void testRegisterPropsPriority() {
        testClusterRegisterPropagation("DPLL", "DPLLCTRLA", 0x1030, 8, READ_WRITE);
        testClusterRegisterPropagation("USART_EXT", "CTRLB", 0x1044, 32, READ_ONLY);
    }

    @Test
    public void testClusterArrayAddressCalcAndPropagation() {
        testClusterArrayCluster("CLS1[]", Map.of("CLS1[0]",0x1130L, "CLS1[1]", 0x1132L));
        testClusterRegisterPropagation("CLS1[0]", "R1", 0x1130, 8, READ_WRITE);
        testClusterRegisterPropagation("CLS1[0]", "R11", 0x1131, 8, READ_WRITE);
        testClusterRegisterPropagation("CLS1[1]", "R1", 0x1132, 8, READ_WRITE);
        testClusterRegisterPropagation("CLS1[1]", "R11", 0x1133, 8, READ_WRITE);
    }

    @Test
    public void testClusterArrayNamesWithChars() {
        testClusterArrayCluster("CLS2_", Map.of("CLS2_B",0x1140L, "CLS2_C", 0x1142L, "CLS2_D", 0x1144L));
    }

    @Test
    public void testClusterArrayNamesWithDigits() {
        testClusterArrayCluster("CLS3_", Map.of("CLS3_21",0x1150L, "CLS3_22", 0x1152L));
    }

    @Test
    public void testClusterArrayNamesWithList() {
        testClusterArrayCluster("CLS4_", Map.of("CLS4_A",0x1160L, "CLS4_B", 0x1162L, "CLS4_C", 0x1164L));
    }

    private void testPeripheralClusterPropagation(String peripheralName, String clusterName,
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

    private void testClusterArrayCluster(String clusterArrayName,
                                         Map<String, Long> clusters) {
        SvdClusterArray clusterArray = nodeByName(SvdClusterArray.class, clusterArrayName);

        Map<String, Long> children = clusterArray
                .getChildren()
                .stream()
                .collect(Collectors.toMap(
                        SvdNodeBase::getName,
                        c -> c.getAddress().getUnsignedLongValue())
                );

        assertEquals(clusters, children);
    }

    private void testClusterClusterPropagation(String clusterName, String nestedClusterName,
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

    private void testClusterRegisterPropagation(String clusterName, String registerName,
                                                long expectedAddress, int expectedBitSize,
                                                RegisterAccess expectedAccess) {
        SvdCluster cluster = nodeByName(SvdCluster.class, clusterName);

        SvdRegister register = cluster.getChildren(SvdRegister.class)
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
