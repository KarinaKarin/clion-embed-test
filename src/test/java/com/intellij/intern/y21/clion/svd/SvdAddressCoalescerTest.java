package com.intellij.intern.y21.clion.svd;

import com.intellij.intern.y21.clion.stubs.AddressRange;
import com.intellij.openapi.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SvdAddressCoalescerTest {

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
    public void testClusterWithNestedOne() {
        validateCluster("DPLL", 1, 1, Arrays.asList("DPLLCTRLA", "CTRLA"));
    }

    @Test
    public void testClusterWithoutNesting() {
        validateCluster("USART_EXT", 0, 1, Collections.singletonList("CTRLA"));
    }

    private void validateCluster(String name, int expectedClusterChildCount,
                             int expectedRegisterChildCount, List<String> allRegistersNames
    ) {
        SvdCluster cluster = clusterByName(name);
        List<String> children = cluster
                .getAllRegisters()
                .stream()
                .map(SvdNodeBase::getName)
                .collect(Collectors.toList());

        assertTrue(children.size() == allRegistersNames.size() &&
                children.containsAll(allRegistersNames) && allRegistersNames.containsAll(children));

        long actualClusterChildCount = cluster
                .getChildren()
                .stream()
                .filter(SvdCluster.class::isInstance)
                .count();

        assertEquals(expectedClusterChildCount, actualClusterChildCount);

        long actualRegisterChildCount = cluster
                .getChildren()
                .stream()
                .filter(SvdRegister.class::isInstance)
                .count();

        assertEquals(expectedRegisterChildCount, actualRegisterChildCount);
    }

    private SvdCluster clusterByName(String name) {
        return (SvdCluster) myAllNodes.stream()
                .filter(node -> name.equals(node.getName()))
                .findAny()
                .orElseThrow(AssertionError::new);
    }

    @Test
    public void testEnumWithZeroValue() {
        validateEnum("PAD0", 0L, null, "SERCOM PAD[0] is used as data input");
    }

    @Test
    public void testEnumWithNonZeroValue() {
        validateEnum("PAD2", 2L, null, "SERCOM PAD[2] is used as data input");
    }

    @Test
    public void testEnumIsDefault() {
        validateEnum("PAD3", null, true, "SERCOM PAD[3] is used as data input");
    }

    private void validateEnum(String enumName, Long expectedValue, Boolean expectedIsDefault, String expectedDescription) {
        SvdEnumValue svdEnum = (SvdEnumValue) myAllNodes.stream()
                .filter(node -> enumName.equals(node.getName()))
                .findAny()
                .orElseThrow(AssertionError::new);

        assertEquals(expectedValue, svdEnum.getValue());
        assertEquals(expectedIsDefault, svdEnum.getDefault());
        assertEquals(expectedDescription, svdEnum.getDescription());
    }

    @Test
    public void testOneBit() {
        testField("ONE_BIT", 11, 1);
    }

    @Test
    public void testTwoBitsReversedDefinition() {
        testField("TWO_BITS", 7, 2);
    }

    @Test
    public void testThreeBits() {
        testField("THREE_BITS", 1, 3);
    }

    @Test
    public void testFourBits() {
        testField("FOUR_BITS", 12, 4);
    }

    @Test
    public void testFiveBits() {
        testField("FIVE_BITS", 16, 5);
    }

    private void testField(String name, int expectedBitOffset, int expectedBitSize) {
        SvdField field = nodeByName(name);
        assertEquals(expectedBitOffset, field.getBitOffset());
        assertEquals(expectedBitSize, field.getBitSize());
    }

    private SvdField nodeByName(String name) {
        return (SvdField) myAllNodes.stream()
                .filter(node -> name.equals(node.getName()))
                .findAny()
                .orElseThrow(AssertionError::new);
    }

    @Test
    public void testSmoke() {

        SvdCoalescerForTest coalescer = new SvdCoalescerForTest();
        coalescer.loadFrom(myAllNodes);

        assertEquals(3, coalescer.size());
        Pair<AddressRange, List<SvdRegister>> pair1 = coalescer.get(0);
        assertEquals(0x1002, pair1.first.getStart().getUnsignedLongValue());
        assertEquals(0x1003, pair1.first.getEnd().getUnsignedLongValue());
        assertEquals("R3", pair1.second.stream().map(SvdNodeBase::getName).collect(Collectors.joining("|")));

        Pair<AddressRange, List<SvdRegister>> pair2 = coalescer.get(1);
        assertEquals(0x1013, pair2.first.getStart().getUnsignedLongValue());
        assertEquals(0x1019, pair2.first.getEnd().getUnsignedLongValue());
        assertEquals("R0|R1|R2", pair2.second.stream().map(SvdNodeBase::getName).collect(Collectors.joining("|")));
    }

    private static class SvdCoalescerForTest extends SvdAddressCoalescer {

        public int size() {
            return myRanges.size();
        }

        public Pair<AddressRange, List<SvdRegister>> get(int i) {
            return myRanges.get(i);
        }
    }
}
