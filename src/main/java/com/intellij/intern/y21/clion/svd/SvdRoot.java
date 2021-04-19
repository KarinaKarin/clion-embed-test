package com.intellij.intern.y21.clion.svd;

import com.intellij.intern.y21.clion.stubs.Address;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.NlsSafe;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.intellij.intern.y21.clion.svd.RegisterAccess.READ_WRITE;


public class SvdRoot implements SvdNode<SvdFile> {

  private final List<SvdFile> files = new ArrayList<>();
  private Set<SvdNode<?>> activeNodes = new HashSet<>();

  public SvdRoot() {
  }

  @NotNull
  @Override
  public List<SvdFile> getChildren() {
    return files;
  }

  @NotNull
  @Override
  public String getId() {
    return "";
  }

  @Nullable
  public SvdFile addFile(InputStream stream, String shortName, @NlsSafe String fileName) {
    Document document;
    try {
      document = new SAXBuilder().build(stream);
    }
    catch (Exception e) {
//      Messages.showErrorDialog(fileName + ": " + e.getMessage(), EmbeddedBundle.message("svd.root.load.error"));
      return null;
    }
    SvdFile svdFile = new SvdFile(shortName, fileName);
    List<SvdNodeBase<?>> flatList = new ArrayList<>();

    List<Element> endian = selectDomSubNodes(document.getRootElement(), "cpu", "endian");
    boolean bigEndian = !endian.isEmpty() && "big".equals(endian.get(0).getTextTrim());
    Map<String, Element> elements = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    selectDomSubNodes(document.getRootElement(), "peripherals", "peripheral")
      .forEach(element -> elements.put(element.getChildText("name"), element));

    Map<String, SvdNodeBase<SvdPeripheral>> grouped = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (Map.Entry<String, Element> entry : elements.entrySet()) {
      Element element = entry.getValue();
      String name = entry.getKey();
      try {
        String derivedFromName = element.getAttributeValue("derivedFrom");
        Element derivedFrom = derivedFromName == null ? null : elements.get(derivedFromName);
        String description = loadDescription(element, derivedFrom);
        String groupName = getDomSubTagText(element, derivedFrom, "groupName");
        long baseAddress = getDomSubTagValue(element, derivedFrom, "baseAddress", Long::decode, 0L);
        int registerSize = getDomSubTagValue(element, derivedFrom, "size", Integer::decode, 32);
        RegisterAccess registerAccess = getDomSubTagValue(element, derivedFrom, "access", RegisterAccess::parse, READ_WRITE);
        SvdPeripheral peripheral;
        if (groupName.isEmpty()) {
          peripheral = new SvdPeripheral(shortName, name, description, registerAccess, baseAddress, registerSize);
          flatList.add(peripheral);
        }
        else {
          SvdNodeBase<SvdPeripheral> group = grouped.computeIfAbsent(groupName, s -> {
            SvdNodeBase<SvdPeripheral> node = new SvdNodeBase<>(shortName + "|g:" + s, s, "");
            node.setChildren(new ArrayList<>());
            return node;
          });
          peripheral = new SvdPeripheral(shortName, name, description, registerAccess, baseAddress, registerSize);
          group.getChildren().add(peripheral);
        }
        loadContent(peripheral, element, derivedFrom, bigEndian);
      }
      catch (RuntimeException e) {
        Logger.getInstance(SvdFile.class).warn(
          MessageFormat.format("Svd parser error at {0}/{1}", element.getName(), element.getChildText("name")), e);
      }
    }

    for (SvdNodeBase<SvdPeripheral> node : grouped.values()) {
      List<SvdPeripheral> children = node.getChildren();
      switch (children.size()) {
        case 0:
          break;
        case 1:
          flatList.add(children.get(0));
          break;
        default:
          flatList.add(node);
      }
    }
    flatList.sort(NAME_COMPARATOR);

    svdFile.setChildren(flatList);
    Objects.requireNonNull(getChildren()).add(svdFile);
//    getTreeTableModel().notifyFileInserted(svdFile);
    return svdFile;
  }

  public boolean isActive(@NotNull SvdNode<?> child) {
    return activeNodes.contains(child);
  }

  public void setActive(@NotNull Set<SvdNode<?>> nodes) {
    activeNodes = nodes;
  }

  public Set<SvdNode<?>> getActiveNodes() {
    return activeNodes;
  }

  public void export(PrintWriter writer) {
    writer.write("Peripheral, Register, Value, Fields" + System.lineSeparator());
    boolean fileNamePrefix = getChildren().size() > 1;
    for (SvdFile file : files) {
      if (file.getChildren().stream().noneMatch(this::isActive)) continue;
      String prefix = fileNamePrefix ? file.getName() + ": " : "";
      file.exportCsv(writer, prefix, this::isActive);
    }
  }

  private static void loadContent(@NotNull SvdPeripheral peripheral,
                                  @NotNull Element peripheralElement,
                                  @Nullable Element peripheralDerivedFrom,
                                  boolean bigEndian) {

    Stream<Element> registersStream = Stream.concat(selectDomSubNodes(peripheralElement, "registers", "register").stream(),
                  selectDomSubNodes(peripheralDerivedFrom, "registers", "register").stream());
    List<SvdRegister> registers = getRegisters(registersStream, peripheral, bigEndian);

    Stream<Element> clustersStream = Stream.concat(selectDomSubNodes(peripheralElement, "registers", "cluster").stream(),
            selectDomSubNodes(peripheralDerivedFrom, "registers", "cluster").stream());
    List<SvdCluster> clusters = getClusters(clustersStream, peripheral, bigEndian);

    List<SvdRegisterLevel<?>> result = new ArrayList<>(registers);
    result.addAll(clusters);
    peripheral.setChildren(result);
  }

  private static void loadContent(@NotNull SvdCluster cluster,
                                  @NotNull Element clusterElement,
                                  @Nullable Element clusterDerivedFrom,
                                  boolean bigEndian) {

    Stream<Element> registersStream = Stream.concat(selectDomSubNodes(clusterElement, "register").stream(),
                                                    selectDomSubNodes(clusterDerivedFrom, "register").stream());
    List<SvdRegister> registers = getRegisters(registersStream, cluster, bigEndian);

    // cmsis-svd V1.3: nesting of cluster is supported
    Stream<Element> clusterStream = Stream.concat(selectDomSubNodes(clusterElement, "cluster").stream(),
                                                  selectDomSubNodes(clusterDerivedFrom, "cluster").stream());
    List<SvdCluster> clusters = getClusters(clusterStream, cluster, bigEndian);

    List<SvdRegisterLevel<?>> result = new ArrayList<>(registers);
    result.addAll(clusters);
    cluster.setChildren(result);
  }

  private static List<SvdCluster> getClusters(@NotNull Stream<Element> clusterStream,
                                              @NotNull RegisterPropsHolder parent,
                                              boolean bigEndian) {

    Map<String, Element> elements = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    clusterStream.forEach(element -> elements.put(element.getChildText("name"), element));

    List<SvdCluster> clusters = new ArrayList<>();
    for (Map.Entry<String, Element> entry : elements.entrySet()) {
      Element element = entry.getValue();
      String name = entry.getKey();
      String derivedFromName = element.getAttributeValue("derivedFrom");
      Element derivedFrom = derivedFromName == null ? null : elements.get(derivedFromName);
      Address address = Address.fromUnsignedLong(
              parent.getBaseAddress() + getDomSubTagValue(element, derivedFrom, "addressOffset", Long::decode, 0L)
      );
      String description = loadDescription(element, derivedFrom) + " (" + addressToString(address) + ")";
      int registerSize = getDomSubTagValue(element, derivedFrom, "size", Integer::decode, parent.getRegisterBitSize());
      RegisterAccess registerAccess =
              getDomSubTagValue(element, derivedFrom, "access", RegisterAccess::parse, parent.getRegisterAccess());
      String headerStructName = getDomSubTagText(element, derivedFrom, "headerStructName");
      String parentId = parent.getId();
      if (!headerStructName.isBlank()) {
        parentId += "|" + headerStructName;
      }
      SvdCluster cluster = new SvdCluster(parentId, name, description, address, registerAccess, registerSize);
      loadContent(cluster, element, derivedFrom, bigEndian);
      clusters.add(cluster);
    }
    clusters.sort(NAME_COMPARATOR);
    return clusters;
  }

  private static List<SvdRegister> getRegisters(@NotNull Stream<Element> elementStream,
                                                @NotNull RegisterPropsHolder parent,
                                                boolean bigEndian) {

    Map<String, Element> elements = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    elementStream.forEach(element -> elements.put(element.getChildText("name"), element));

    List<SvdRegister> registers = new ArrayList<>();
    for (Map.Entry<String, Element> entry : elements.entrySet()) {
      Element element = entry.getValue();
      String name = entry.getKey();
      String derivedFromName = element.getAttributeValue("derivedFrom");
      Element derivedFrom = derivedFromName == null ? null : elements.get(derivedFromName);
      Address address = Address.fromUnsignedLong(
              parent.getBaseAddress() + getDomSubTagValue(element, derivedFrom, "addressOffset", Long::decode, 0L)
      );
      String description = loadDescription(element, derivedFrom) + " (" + addressToString(address) + ")";
      int registerSize = getDomSubTagValue(element, derivedFrom, "size", Integer::decode, parent.getRegisterBitSize());
      RegisterAccess registerAccess =
              getDomSubTagValue(element, derivedFrom, "access", RegisterAccess::parse, parent.getRegisterAccess());
      RegisterReadAction registerReadAction = getDomSubTagValue(element, derivedFrom, "readAction", RegisterReadAction::parse, null);
      String parentId = parent.getId();
      SvdRegister register = bigEndian ?
              new SvdRegisterBigEndian(parentId, name, description, address, registerSize, registerAccess,
                      registerReadAction) :
              new SvdRegister(parentId, name, description, address, registerSize, registerAccess,
                      registerReadAction);
      loadContent(register, element, derivedFrom);
      registers.add(register);
    }
    registers.sort(Comparator.comparing(SvdRegister::getAddress));
    return registers;
  }

  @NotNull
  public static String addressToString(Address address) {
    long value = address.getUnsignedLongValue();
    if ((value & 0xFFFFFFFF00000000L) == 0L) return "0x" + /*StringsKt.padStart(*/Integer.toHexString((int)value)/*, 8, '0')*/;
    return address.toString();
  }

  private static String loadDescription(@NotNull Element element, @Nullable Element derivedFrom) {
    return Text.normalizeString(getDomSubTagText(element, derivedFrom, "description"));
  }

  @NotNull
  private static String getDomSubTagText(@NotNull Element element,
                                         @Nullable Element derivedFrom,
                                         @NotNull String tagName) {
    return getDomSubTagValue(element, derivedFrom, tagName, String::valueOf, "");
  }

  @Nullable
  @Contract("_,_,_,_,!null->!null")
  private static <T> T getDomSubTagValue(@NotNull Element element,
                                         @Nullable Element derivedFrom,
                                         @NotNull String tagName,
                                         Function<String, T> convert,
                                         @Nullable T defaultValue) {
    String text = element.getChildText(tagName);
    if (text != null) return convert.apply(text);
    if (derivedFrom != null) {
      text = derivedFrom.getChildText(tagName);
    }
    return text == null ? defaultValue : convert.apply(text);
  }

  @NotNull
  private static List<Element> selectDomSubNodes(@Nullable Element parent, String... selectors) {
    if (parent == null) return Collections.emptyList();
    for (int i = 0; i < selectors.length - 1; i++) {
      parent = parent.getChild(selectors[i]);
      if (parent == null) return Collections.emptyList();
    }
    return parent.getChildren(selectors[selectors.length - 1]);
  }

  private static void loadContent(@NotNull SvdRegister register,
                                  @NotNull Element peripheralElement,
                                  @Nullable Element peripheralDerivedFrom) {
    Map<String, Element> elements = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    Stream.concat(selectDomSubNodes(peripheralElement, "fields", "field").stream(),
                  selectDomSubNodes(peripheralDerivedFrom, "fields", "field").stream())
      .forEach(element -> elements.put(element.getChildText("name"), element));
    List<SvdField> fields = new ArrayList<>();
    for (Map.Entry<String, Element> entry : elements.entrySet()) {
      Element element = entry.getValue();
      String name = entry.getKey();
      String derivedFromName = element.getAttributeValue("derivedFrom");
      Element derivedFrom = derivedFromName == null ? null : elements.get(derivedFromName);
      RegisterAccess registerAccess = getDomSubTagValue(element, derivedFrom, "access", RegisterAccess::parse, register.getAccess());
      RegisterReadAction registerReadAction = getDomSubTagValue(element, derivedFrom, "readAction", RegisterReadAction::parse, null);
      int bitOffset = getDomSubTagValue(element, derivedFrom, "bitOffset", Integer::parseInt, -1);
      int bitSize;
      if (bitOffset >= 0) {
        bitSize = getDomSubTagValue(element, derivedFrom, "bitWidth", Integer::parseInt, register.getBitSize() - bitOffset);
      }
      else {
        bitOffset = getDomSubTagValue(element, derivedFrom, "lsb", Integer::parseInt, -1);
        if (bitOffset >= 0) {
          bitSize = getDomSubTagValue(element, derivedFrom, "msb", Integer::parseInt, register.getBitSize()) - bitOffset + 1;
        }
        else {
          String bitRange = getDomSubTagText(element, derivedFrom, "bitRange");
          bitRange = bitRange.replaceAll("\\s|\\[|]", "");
          int columnIndex = bitRange.indexOf(':');
          if (columnIndex < 1) {
            bitOffset = 0;
            bitSize = register.getBitSize();
          }
          else {
            int bitEnd = Integer.parseUnsignedInt(bitRange.substring(0, columnIndex), 10);
            int bitStart = Integer.parseUnsignedInt(bitRange.substring(columnIndex + 1), 10);
            bitOffset = Math.min(bitEnd, bitStart);
            bitSize = Math.abs(bitEnd - bitStart) + 1;
          }
        }
      }
      String description = loadDescription(element, derivedFrom);
      if (bitSize == 1) {
        description += " [" + bitOffset + "]";
      }
      else {
        description += " [" + bitOffset + ":" + (bitOffset + bitSize - 1) + "]";
      }
      SvdField f = new SvdField(register, name, description, registerAccess, registerReadAction, bitOffset, bitSize);
      loadContent(f, element, derivedFrom);
      fields.add(f);
    }
    fields.sort(Comparator.comparingInt(SvdField::getBitOffset));
    register.setChildren(fields);
  }

  private static void loadContent(@NotNull SvdField field,
                                  @NotNull Element fieldElement,
                                  @Nullable Element fieldDerivedFrom) {

//  <xs:element name="enumeratedValues" type="enumerationType" minOccurs="0" maxOccurs="2">
    Map<String, Element> elements = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    Stream.concat(selectDomSubNodes(fieldElement, "enumeratedValues").stream(),
            selectDomSubNodes(fieldDerivedFrom, "enumeratedValues").stream())
            .forEach(element -> elements.put(element.getChildText("name"), element));

    List<SvdEnum> enumValues = new ArrayList<>();
    for (Map.Entry<String, Element> entry : elements.entrySet()) {
      Element element = entry.getValue();
      String name = entry.getKey();
      String derivedFromName = element.getAttributeValue("derivedFrom");
      Element derivedFrom = derivedFromName == null ? null : elements.get(derivedFromName);
      RegisterAccess enumUsage = getDomSubTagValue(element, derivedFrom, "usage", RegisterAccess::parseUsage, RegisterAccess.READ_WRITE);
      String parentId = field.getId();
      String headerStructName = getDomSubTagText(element, derivedFrom, "headerStructName");
      if (!headerStructName.isBlank()) {
        parentId += "|" + headerStructName;
      }
      SvdEnum svdEnum = new SvdEnum(parentId, name, enumUsage);
      loadContent(svdEnum, element, derivedFrom);
      enumValues.add(svdEnum);
    }

    enumValues.sort(NAME_COMPARATOR);
    field.setChildren(enumValues);
    if (!enumValues.isEmpty()) {
      field.setFormat(Format.ENUM);
    }
  }

  private static void loadContent(@NotNull SvdEnum svdEnum,
                                  @NotNull Element enumElement,
                                  @Nullable Element enumDerivedFrom) {

    Map<String, Element> elements = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    Stream.concat(selectDomSubNodes(enumElement, "enumeratedValue").stream(),
            selectDomSubNodes(enumDerivedFrom, "enumeratedValue").stream())
            .forEach(element -> elements.put(element.getChildText("name"), element));

    List<SvdEnumValue> enumValues = new ArrayList<>();
    for (Map.Entry<String, Element> entry : elements.entrySet()) {
      Element element = entry.getValue();
      String name = entry.getKey();
      Long value = getDomSubTagValue(element, null, "value", Long::decode, null);
      Boolean isDefault = getDomSubTagValue(element, null, "isDefault", Boolean::parseBoolean, null);
      String description = loadDescription(element, null);
      enumValues.add(new SvdEnumValue(svdEnum.getId() + "|" + name, name, description, value, isDefault));
    }
    svdEnum.setChildren(enumValues);
  }
}
