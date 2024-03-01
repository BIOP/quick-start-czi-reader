package ch.epfl.biop.formats.in.libczi;

import loci.common.Constants;
import loci.common.RandomAccessInputStream;
import ch.epfl.biop.formats.in.ZeissQuickStartCZIReader;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.xml.model.primitives.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * CziStructs.h c++ structures translated to Java
 * And helper static methods
 * <p>
 * Translation:
 * - using Java variable convention (lowercase)
 * - std::int32_t is int
 * - std::int64_t is long
 * - GUID is 16 bytes long (unused)
 * - char arrays are String
 * <p>
 * Timestamps reading inspired by <a href="https://gist.github.com/mutterer/5fbddc293d6c969a9d02778f1551b73f">Jerome's macro</a>
 * <p>
 * See @see <a href="https://zeiss.github.io/">CZI reference documentation</a>
 * <p>
 * Used in {@link ZeissQuickStartCZIReader}
 *
 * @author Nicolas Chiaruttini, EPFL, 2023
 */
public class LibCZI {

    private static final Logger logger = LoggerFactory.getLogger(LibCZI.class);

    // --------------------------- PUBLIC HELPER METHODS

    /**
     * @param id a czi file path
     * @param BUFFER_SIZE the size of the caching buffer in bytes
     * @param isLittleEndian endianness of the data
     * @return the file header segment
     * @throws IOException invalid file, invalid file location, file header not found
     */
    public static FileHeaderSegment getFileHeaderSegment(String id, int BUFFER_SIZE, boolean isLittleEndian) throws IOException {
        try (RandomAccessInputStream in = new RandomAccessInputStream(id, BUFFER_SIZE)) {
            in.order(isLittleEndian);
            int skip =
                    (ALIGNMENT - (int) (in.getFilePointer() % ALIGNMENT)) % ALIGNMENT;
            in.skipBytes(skip);
            long startingPosition = in.getFilePointer();
            String segmentID = in.readString(16).trim();
            if (segmentID.equals(ZISRAWFILE)) {
                // That's correct, it's a CZI fileheader
                FileHeaderSegment fileHeaderSegment = new FileHeaderSegment();
                // read the segment header
                fileHeaderSegment.header.id = segmentID;
                fileHeaderSegment.header.allocatedSize = in.readLong();
                fileHeaderSegment.header.usedSize = in.readLong();

                fileHeaderSegment.data.major = in.readInt();
                fileHeaderSegment.data.minor = in.readInt();
                fileHeaderSegment.data._reserved1 = in.readInt();
                fileHeaderSegment.data._reserved2 = in.readInt();
                in.read(fileHeaderSegment.data.primaryFileGuid.bytes);
                in.read(fileHeaderSegment.data.fileGuid.bytes);
                fileHeaderSegment.data.filePart = in.readInt();
                fileHeaderSegment.data.subBlockDirectoryPosition = in.readLong();
                fileHeaderSegment.data.metadataPosition = in.readLong();
                fileHeaderSegment.data.updatePending = in.readInt();
                fileHeaderSegment.data.attachmentDirectoryPosition = in.readLong();
                return fileHeaderSegment;
            } else {
                throw new IOException(ZISRAWFILE+" segment expected, found "+segmentID+" instead.");
            }
        }
    }

    /**
     * @param blockPosition position of the subblockdirectory segment of the referenced czi file
     * @param id a czi file path
     * @param BUFFER_SIZE the size of the caching buffer in bytes
     * @param isLittleEndian endianness of the data
     * @return the subblock directory segment of the czi file
     * @throws IOException invalid file, invalid file location, segment not found
     */
    public static SubBlockDirectorySegment getSubBlockDirectorySegment(long blockPosition, String id, int BUFFER_SIZE, boolean isLittleEndian) throws IOException {
        // TODO (maybe) : increase buffer size to limit the number of IO calls, especially when the file is mounted on a network drive
        try (RandomAccessInputStream in = new RandomAccessInputStream(id, BUFFER_SIZE)) {
            SubBlockDirectorySegment directorySegment = new SubBlockDirectorySegment();

            in.order(isLittleEndian);
            in.seek(blockPosition);

            String segmentID = in.readString(16).trim();
            if (segmentID.equals(ZISRAWDIRECTORY)) {
                directorySegment.header.id = segmentID; // 16
                directorySegment.header.allocatedSize = in.readLong(); // 8
                directorySegment.header.usedSize = in.readLong(); // 8
                directorySegment.data.entryCount = in.readInt(); // 4 -> Sum of bytes = 36
                in.skipBytes(124); // 128 - 4;
                directorySegment.data.entries = new SubBlockDirectorySegment.SubBlockDirectorySegmentData.SubBlockDirectoryEntry[directorySegment.data.entryCount];
                for (int i=0; i<directorySegment.data.entryCount; i++) {
                    directorySegment.data.entries[i] = new SubBlockDirectorySegment.SubBlockDirectorySegmentData.SubBlockDirectoryEntry();
                    String schemaType = in.readString(2);
                    if (schemaType.equals("DV")) {
                        directorySegment.data.entries[i].entryDV = getEntryDV(in);//return new ZeissCZIFastReader.DirectoryEntryDV(s, prestitchedSetter, coreIndex);
                    } else if (schemaType.equals("DE")) {
                        throw new IOException("Unsupported schema type DE for directory entry.");
                        //return new DirectoryEntryDV(s);
                    } else {
                        throw new IOException("Unrecognized directory entry schema type = "+schemaType);
                    }
                }
                return directorySegment;
            } else {
                logger.warn(ZISRAWDIRECTORY+" segment expected, found "+segmentID+" instead.");
                return null; // Some multipart file could be deprived of this segment. The reader needs to deal with null in this case
            }
        }
    }

    /**
     * @param blockPosition  position of the attachment directory segment of the referenced czi file
     * @param id a czi file path
     * @param BUFFER_SIZE the size of the caching buffer in bytes
     * @param isLittleEndian endianness of the data
     * @return the attachment directory segment
     * @throws IOException invalid file, invalid file location, segment not found
     */
    public static AttachmentDirectorySegment getAttachmentDirectorySegment(long blockPosition, String id, int BUFFER_SIZE, boolean isLittleEndian) throws IOException {
        try (RandomAccessInputStream in = new RandomAccessInputStream(id, BUFFER_SIZE)) {
            AttachmentDirectorySegment directorySegment = new AttachmentDirectorySegment();
            in.order(isLittleEndian);
            in.seek(blockPosition);
            String segmentID = in.readString(16).trim();
            if (segmentID.equals(ZISRAWATTDIR)) {
                directorySegment.header.id = segmentID; // 16 bytes
                directorySegment.header.allocatedSize = in.readLong(); // 8 bytes
                directorySegment.header.usedSize = in.readLong(); // 8 bytes
                directorySegment.data.entryCount = in.readInt(); // 4 bytes -> Sum of all bytes so far = 36
                in.skipBytes(252); // 256 - 4;
                directorySegment.data.entries = new AttachmentDirectorySegment.AttachmentDirectorySegmentData.AttachmentEntry[directorySegment.data.entryCount];
                for (int i=0; i<directorySegment.data.entryCount; i++) {
                    directorySegment.data.entries[i] = new AttachmentDirectorySegment.AttachmentDirectorySegmentData.AttachmentEntry();
                    String schemaType = in.readString(2);
                    if (schemaType.equals("A1")) {
                        directorySegment.data.entries[i] = getAttachmentEntryA1(in);
                    } else {
                        throw new IOException("Unrecognized attachment entry schema type = "+schemaType);
                    }
                }
                return directorySegment;
            } else {
                logger.warn("No "+ZISRAWATTDIR+" segment found.");
                return null;
            }
        }
    }

    /**
     * @param blockPosition position  of th file header segment of the referenced czi file
     * @param id a czi file path
     * @param BUFFER_SIZE the size of the caching buffer in bytes
     * @param isLittleEndian endianness of the data
     * @return the raw metadata segment
     * @throws IOException invalid file, invalid file location, segment not found
     */
    public static MetaDataSegment getMetaDataSegment(long blockPosition, String id, int BUFFER_SIZE, boolean isLittleEndian, long lastBlockPosition) throws IOException {
        try (RandomAccessInputStream in = new RandomAccessInputStream(id, BUFFER_SIZE)) {
            in.order(isLittleEndian);
            in.seek(blockPosition);
            String segmentID = in.readString(16).trim();
            boolean metadataSegmentFound = false;

            // ------------ Handling of potentially deleted metadata segement
            if (!segmentID.equals(ZISRAWMETADATA)) {
                if (segmentID.equals(DELETED)) {
                    // The metadata segment has been deleted, let's walk at the end of the file and attempt to find the missing metadata segment
                    long newPosition = LibCZI.findOrphanBlock(id, BUFFER_SIZE, isLittleEndian, lastBlockPosition, ZISRAWMETADATA);
                    if (newPosition != -1) {
                        return getMetaDataSegment(newPosition, id, BUFFER_SIZE, isLittleEndian, -1);
                    } else {
                        throw new IOException(ZISRAWMETADATA+" segment expected, found "+segmentID+" instead.");
                    }
                }
            } else {
                metadataSegmentFound = true;
            }

            if (metadataSegmentFound) {
                MetaDataSegment metaDataSegment = new MetaDataSegment();
                // read the segment header
                metaDataSegment.header.id = segmentID;
                metaDataSegment.header.allocatedSize = in.readLong();
                metaDataSegment.header.usedSize = in.readLong();
                int xmlSize = in.readInt();
                int attachmentSize = in.readInt();

                in.skipBytes(248); // Hum hum why ?

                metaDataSegment.data.xml = in.readString(xmlSize);
                metaDataSegment.data.attachment = new byte[attachmentSize];
                in.read(metaDataSegment.data.attachment);
                return metaDataSegment;
            } else {
                throw new IOException(ZISRAWMETADATA+" segment expected, found "+segmentID+" instead.");
            }
        }
    }
    private static AttachmentDirectorySegment.AttachmentDirectorySegmentData.AttachmentEntry getAttachmentEntryA1(RandomAccessInputStream in) throws IOException{
        AttachmentDirectorySegment.AttachmentDirectorySegmentData.AttachmentEntry entry = new AttachmentDirectorySegment.AttachmentDirectorySegmentData.AttachmentEntry();
        in.skipBytes(10); //unsigned char _spare[10];
        entry.filePosition = in.readLong();
        entry.filePart = in.readInt();
        in.skipBytes(16); // GUID
        entry.contentFileType = in.readString(8).trim();
        entry.name = in.readString(80).trim();
        return entry;
    }

    // If blocks have been deleted, they are appended at the end of the file. This method looks for the location of the last known block and
    // iterates over the next blocks until the end of the file. This method returns whether some orphan blocks have been found
    static long findOrphanBlock(String id, int BUFFER_SIZE, boolean isLittleEndian, long positionOfLastKnownBlock, String blockType) throws IOException {
        try (RandomAccessInputStream in = new RandomAccessInputStream(id, BUFFER_SIZE)) {
            long eof = in.length();
            in.order(isLittleEndian);
            long positionBlockStart = positionOfLastKnownBlock;
            in.seek(positionBlockStart);
            String segmentID = in.readString(16).trim();

            long allocatedSize = in.readLong();
            long usedSize = in.readLong();
            while (positionBlockStart + allocatedSize + 32 <eof) {
                positionBlockStart += allocatedSize + 32;
                in.seek(positionBlockStart);
                segmentID = in.readString(16).trim();
                if (segmentID.equals(blockType)) {
                    return positionBlockStart;
                }
                allocatedSize = in.readLong();
                usedSize = in.readLong();
            }
            return -1;
        }
    }

    /**
     * @param attachmentDirectorySegment attachment directory segment of the referenced czi file
     * @param id a czi file path
     * @param BUFFER_SIZE the size of the caching buffer in bytes
     * @param isLittleEndian endianness of the data
     * @return the bytes of the label image (a czi file), null if no label is found
     * @throws IOException invalid file, invalid file location
     */
    public static byte[] getLabelBytes(AttachmentDirectorySegment attachmentDirectorySegment, String id, int BUFFER_SIZE, boolean isLittleEndian) throws IOException {
        if (attachmentDirectorySegment==null) return null;

        AttachmentDirectorySegment.AttachmentDirectorySegmentData.AttachmentEntry labelEntry = null;
        for (AttachmentDirectorySegment.AttachmentDirectorySegmentData.AttachmentEntry entry: attachmentDirectorySegment.data.entries) {
            if (entry.contentFileType.equals("CZI") && (entry.name.equals("Label"))) {
                labelEntry = entry;
            }
        }

        if (labelEntry==null) return null;

        try (RandomAccessInputStream in = new RandomAccessInputStream(id, BUFFER_SIZE)) {
            in.order(isLittleEndian);
            in.seek(labelEntry.filePosition);
            String segmentID = in.readString(16).trim();
            if (segmentID.equals(ZISRAWATTACH)) {
                long usedSize = in.readLong();
                in.skipBytes(256+8); // Hum hum why ? 16 (String) + 8 + 8
                byte[] bytes = new byte[(int) usedSize-256];
                in.read(bytes);
                return bytes;
            } else {
                logger.warn("Label not found, "+ZISRAWATTACH+" segment expected, "+segmentID+" found instead.");
            }
        }
        return null;
    }
    /**
     * @param attachmentDirectorySegment attachment directory segment of the referenced czi file
     * @param id a czi file path
     * @param BUFFER_SIZE the size of the caching buffer in bytes
     * @param isLittleEndian endianness of the data
     * @return the bytes of the preview image (a czi file), null if no preview is found
     * @throws IOException invalid file, invalid file location
     */
    public static byte[] getPreviewBytes(AttachmentDirectorySegment attachmentDirectorySegment, String id, int BUFFER_SIZE, boolean isLittleEndian) throws IOException {
        if (attachmentDirectorySegment==null) return null;
        AttachmentDirectorySegment.AttachmentDirectorySegmentData.AttachmentEntry previewEntry = null;
        for (AttachmentDirectorySegment.AttachmentDirectorySegmentData.AttachmentEntry entry: attachmentDirectorySegment.data.entries) {
            if (entry.contentFileType.equals("CZI") && (entry.name.equals("SlidePreview"))) {
                previewEntry = entry;
            }
        }

        if (previewEntry==null) return null;

        try (RandomAccessInputStream in = new RandomAccessInputStream(id, BUFFER_SIZE)) {
            in.order(isLittleEndian);
            in.seek(previewEntry.filePosition);
            String segmentID = in.readString(16).trim();
            if (segmentID.equals(ZISRAWATTACH)) {
                long usedSize = in.readLong();
                in.skipBytes(264); // Hum hum why ? 16 (String) + 8 + 8
                byte[] bytes = new byte[(int) usedSize];
                in.read(bytes);
                return bytes;
            } else {
                logger.warn("Preview not found, "+ZISRAWATTACH+" segment expected, "+segmentID+" found instead.");
            }
        }
        return null;
    }

    /**
     * @param attachmentDirectorySegment attachment directory segment of the referenced czi file
     * @param id a czi file path
     * @param BUFFER_SIZE the size of the caching buffer in bytes
     * @param isLittleEndian endianness of the data
     * @return timestamps as a double array
     * @throws IOException invalid file, invalid file location
     */
    public static double[] getTimeStamps(AttachmentDirectorySegment attachmentDirectorySegment, String id, int BUFFER_SIZE, boolean isLittleEndian) throws IOException {
        AttachmentDirectorySegment.AttachmentDirectorySegmentData.AttachmentEntry timeStampEntry = null;
        for (AttachmentDirectorySegment.AttachmentDirectorySegmentData.AttachmentEntry entry: attachmentDirectorySegment.data.entries) {
            if (entry.contentFileType.equals("CZTIMS")) {
                timeStampEntry = entry;
            }
        }

        if (timeStampEntry==null) {
            return new double[0];
        }

        double[] timeStamps;
        try (RandomAccessInputStream in = new RandomAccessInputStream(id, BUFFER_SIZE)) {
            in.order(isLittleEndian);
            in.seek(timeStampEntry.filePosition);

            String segmentId = in.readString(16).trim(); // in.skipBytes(16);
            int dataSize = in.readInt(); // in.skipBytes(4)
            in.skip(12); // spare
            //int xmlSize = in.readInt();
            //int attachSize = in.readInt();
            in.skipBytes(256); // spare

            int size = in.readInt(); // size
            int nTimeStamps = in.readInt();

            timeStamps = new double[nTimeStamps];
            for (int i = 0; i<nTimeStamps; i++) {
                timeStamps[i] = in.readDouble();
            }
        }
        return timeStamps;
    }
    private static SubBlockSegment.SubBlockSegmentData.SubBlockDirectoryEntryDV getEntryDV(RandomAccessInputStream in) throws IOException{
        SubBlockSegment.SubBlockSegmentData.SubBlockDirectoryEntryDV entryDV = new SubBlockSegment.SubBlockSegmentData.SubBlockDirectoryEntryDV();
        //entryDV.schemaType = "DV", Removed to save space and because we already know this
        entryDV.pixelType = in.readInt();
        entryDV.filePosition = in.readLong();
        entryDV.filePart = in.readInt();
        entryDV.compression = in.readInt();
        in.skipBytes(6); //entryDV._spare, Removed to save space
        entryDV.dimensionCount = in.readInt();
        entryDV.dimensionEntries =
                new SubBlockSegment.SubBlockSegmentData.SubBlockDirectoryEntryDV.DimensionEntry[entryDV.dimensionCount];
        for (int i=0; i<entryDV.dimensionEntries.length; i++) {
            entryDV.dimensionEntries[i] = new SubBlockSegment.SubBlockSegmentData.SubBlockDirectoryEntryDV.DimensionEntry();
            entryDV.dimensionEntries[i].dimension = in.readString(4).trim();
            entryDV.dimensionEntries[i].start = in.readInt();
            entryDV.dimensionEntries[i].size = in.readInt();
            entryDV.dimensionEntries[i].startCoordinate = in.readFloat();
            entryDV.dimensionEntries[i].storedSize = in.readInt();
        }
        return entryDV;
    }

    private static void skipEntryDV(RandomAccessInputStream in) throws IOException{
        in.skipBytes(28);
        int dimensionCount = in.readInt();
        in.skipBytes(20*dimensionCount);
    }

    /**
     * The stream should be already initialized, its endianness set properly. No raw data is read,
     * but the {@link SubBlockSegment} object has its fields initialized properly
     * @param in stream over a czi file
     * @param filePosition starting position of the block to fetch
     * @return the referenced sub block segment
     * @throws IOException io exception
     */
    public static SubBlockSegment getBlock(RandomAccessInputStream in, long filePosition) throws IOException {
        SubBlockSegment subBlock = new SubBlockSegment();

        in.seek(filePosition
                // Jumps 16 bytes to avoid reading the id, which should be ZISRAWSUBBLOCK anyway
                // Jumps 8 bytes for used size
                // Jumps 8 bytes for allocated size
                +HEADER_SIZE);

        long fp = in.getFilePointer();
        subBlock.data.metadataSize = in.readInt();
        subBlock.data.attachmentSize = in.readInt();
        subBlock.data.dataSize = in.readLong();
        // Here is the directory entry (DE, or DV, but we already read it, so
        // here we just skip it
        skipEntryDV(in);
        in.skipBytes((int) Math.max(256 - (in.getFilePointer() - fp), 0));
        subBlock.data.metadataOffset = in.getFilePointer();
        in.skipBytes(subBlock.data.metadataSize);
        subBlock.dataOffset = in.getFilePointer();
        return subBlock;
    }

    /**
     * The stream should be already initialized, its endianness set properly. No raw data is read,
     * but the {@link SubBlockSegment} object has its fields initialized properly
     * @param in stream over a czi file
     * @param subBlock a properly initialized subblock object
     * @param parser an object that can read xml
     * @return the referenced sub block metadata of the input sub block segment
     * @throws IOException io exception
     */
    public static SubBlockMeta readSubBlockMeta(RandomAccessInputStream in, SubBlockSegment subBlock, DocumentBuilder parser) throws IOException {
        SubBlockMeta subBlockMeta = new SubBlockMeta();
        if (subBlock.dataOffset + subBlock.data.dataSize + subBlock.data.attachmentSize < in.length()) {
            in.seek(subBlock.data.metadataOffset);

            String metadata = in.readString(subBlock.data.metadataSize).trim();
            if (metadata.length() <= 16) {
                return subBlockMeta;
            }

            Element root;
            try {
                ByteArrayInputStream s =
                        new ByteArrayInputStream(metadata.getBytes(Constants.ENCODING));
                root = parser.parse(s).getDocumentElement();
                s.close();
            }
            catch (SAXException e) {
                return subBlockMeta;
            }

            if (root == null) {
                return subBlockMeta;
            }

            NodeList children = root.getChildNodes();

            for (int i=0; i<children.getLength(); i++) {
                if (!(children.item(i) instanceof Element)) {
                    continue;
                }
                Element child = (Element) children.item(i);

                if (child.getNodeName().equals("Tags")) {
                    NodeList tags = child.getChildNodes();

                    for (int tag=0; tag<tags.getLength(); tag++) {
                        if (!(tags.item(tag) instanceof Element)) {
                            continue;
                        }
                        Element tagNode = (Element) tags.item(tag);
                        String text = tagNode.getTextContent();
                        if (text != null) {
                            switch (tagNode.getNodeName()) {
                                case "StageXPosition": {
                                    final Double number = Double.valueOf(text);
                                    subBlockMeta.stageX = new Length(number, UNITS.MICROMETER);
                                    break;
                                }
                                case "StageYPosition": {
                                    final Double number = Double.valueOf(text);
                                    subBlockMeta.stageY = new Length(number, UNITS.MICROMETER);
                                    break;
                                }
                                case "FocusPosition": {
                                    final Double number = Double.valueOf(text);
                                    subBlockMeta.stageZ = new Length(number, UNITS.MICROMETER);
                                    break;
                                }
                                case "AcquisitionTime":
                                    Timestamp t = Timestamp.valueOf(text);
                                    if (t != null)
                                        subBlockMeta.timestamp = t.asInstant().getMillis() / 1000d;
                                    break;
                                case "ExposureTime":
                                    subBlockMeta.exposureTime = Double.parseDouble(text);
                                    break;
                            }
                        }
                    }
                }
            }
        }
        return subBlockMeta;
    }

    // --------------------------- PUBLIC TRANSLATED CZI STRUCTS TO JAVA CLASSES
    public static class SubBlockMeta {
        public double exposureTime = Double.NaN;
        public double timestamp = Double.NaN;
        public Length stageX, stageY, stageZ;
    }
    public static class SegmentHeader {
        public String id;
        public long allocatedSize;
        public long usedSize;
    }
    public static class GUID {
        final byte[] bytes = new byte[16]; // 128 bits identifier = 16 bytes, or 2 longs
    }

    public static long getPositionLastBlock(SubBlockDirectorySegment segment) {
        long lastBlockPosition = -1;

        for (SubBlockDirectorySegment.SubBlockDirectorySegmentData.SubBlockDirectoryEntry entry: segment.data.entries) {
            lastBlockPosition = Math.max(lastBlockPosition, entry.getFilePosition());
        }

        return lastBlockPosition;
    }

    public static class SubBlockDirectorySegment {
        public final SegmentHeader header = new SegmentHeader();

        public final SubBlockDirectorySegmentData data = new SubBlockDirectorySegmentData();
        public static class SubBlockDirectorySegmentData {
            public int entryCount;
            public SubBlockDirectoryEntry[] entries;

            public static class SubBlockDirectoryEntry {
                @Override
                public String toString() {
                    if (entryDV!=null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("pixelType "+this.getPixelType()+" compression = "+getCompression()+"\n");
                        for (SubBlockSegment.SubBlockSegmentData.SubBlockDirectoryEntryDV.DimensionEntry entry: getDimensionEntries()) {
                            sb.append(entry+"\n");
                        }
                        return sb.toString();
                    } else return "entryDE not supported";
                }

                public SubBlockSegment.SubBlockSegmentData.SubBlockDirectoryEntryDV entryDV;

                public SubBlockSegment.SubBlockSegmentData.SubBlockDirectoryEntryDV.DimensionEntry getDimension(String dimension) {
                    if (entryDV!=null) {
                        int i;
                        for (i = 0; i<entryDV.dimensionEntries.length; i++) {
                            if (entryDV.dimensionEntries[i].dimension.equals(dimension)) break;
                        }
                        if (i==entryDV.dimensionEntries.length) {
                            SubBlockSegment.SubBlockSegmentData.SubBlockDirectoryEntryDV.DimensionEntry dummyEntry = new SubBlockSegment.SubBlockSegmentData.SubBlockDirectoryEntryDV.DimensionEntry();
                            dummyEntry.dimension = dimension;
                            dummyEntry.start = 0;
                            dummyEntry.size = 1;
                            dummyEntry.storedSize = 1;
                            dummyEntry.startCoordinate = 0;
                            return dummyEntry;
                        }
                        return entryDV.dimensionEntries[i];
                    } else {
                        throw new UnsupportedOperationException("entryDE not supported");
                    }
                }

                public SubBlockSegment.SubBlockSegmentData.SubBlockDirectoryEntryDV.DimensionEntry[] getDimensionEntries() {
                    if (entryDV!=null) {
                        return entryDV.dimensionEntries;
                    } else {
                        throw new UnsupportedOperationException("entryDE not supported");
                    }
                }

                public int getPixelType() {
                    if (entryDV!=null) {
                        return entryDV.pixelType;
                    } else {
                        return -1;
                    }
                }

                public int getCompression() {
                    if (entryDV!=null) {
                        return entryDV.compression;
                    } else {
                        return -1;
                    }
                }

                public long getFilePosition() {
                    if (entryDV!=null) {
                        return entryDV.filePosition;
                    } else {
                        return -1;
                    }
                }
            }
        }

    }
    public static class FileHeaderSegment {
        public final SegmentHeader header = new SegmentHeader();

        public final FileHeaderSegmentData data = new FileHeaderSegmentData();

        // FileHeader
        public static class FileHeaderSegmentData {
            public int major;
            public int minor;
            public int _reserved1;
            public int _reserved2;
            public GUID primaryFileGuid = new GUID();
            public GUID fileGuid = new GUID();
            public int filePart;
            public long subBlockDirectoryPosition;
            public long metadataPosition;
            public int updatePending;
            public long attachmentDirectoryPosition;
        }
    }
    public static class MetaDataSegment {
        public final SegmentHeader header = new SegmentHeader();
        public final MetaDataSegmentData data = new MetaDataSegmentData();
        public static class MetaDataSegmentData {
            public String xml;
            public byte[] attachment;
        }
    }
    public static class SubBlockSegment {

        public SubBlockSegmentData data = new SubBlockSegmentData();
        public long dataOffset; // Pure Java field, maybe not a good idea

        public static class SubBlockSegmentData {
            public int metadataSize;
            public int attachmentSize;
            public long dataSize;
            public long metadataOffset; // Pure Java field, out of convenience

            public static class SubBlockDirectoryEntryDV {
                public int pixelType;
                public long filePosition;
                public int filePart;
                public int compression;
                public int dimensionCount;
                public DimensionEntry[] dimensionEntries; // offset 32

                public static class DimensionEntry {

                    public String dimension;

                    public int start;

                    public int size; // real physical size
                    public float startCoordinate; // TODO : remove ?
                    public int storedSize; // number of pixels in this block
                    @Override
                    public String toString() {
                        return "dimension=" + dimension + ", start=" + start + ", size=" + size +
                                ", startCoordinate=" + startCoordinate + ", storedSize=" + storedSize;
                    }


                }

                @Override
                public String toString() {
                    String s = "schemaType = DV, pixelType = " + pixelType + ", filePosition = " +
                            filePosition + ", filePart = " + filePart + ", compression = " + compression +
                            ", pyramidType = TODO" /*+ pyramidType*/ + ", dimensionCount = " + dimensionCount;
                    if (dimensionCount > 0) {
                        StringBuilder sb = new StringBuilder(s);
                        sb.append(", dimensions = [");
                        for (int i=0; i<dimensionCount; i++) {
                            sb.append(dimensionEntries[i]);
                            if (i < dimensionCount - 1) {
                                sb.append("; ");
                            }
                        }
                        sb.append(']');
                        s = sb.toString();
                    }
                    return s;
                }
            }

        }

    }
    public static class AttachmentDirectorySegment {
        public final SegmentHeader header = new SegmentHeader();

        public final AttachmentDirectorySegmentData data = new AttachmentDirectorySegmentData();
        public static class AttachmentDirectorySegmentData {
            public int entryCount;
            public String _spare; // _spare[SIZE_ATTACHMENTDIRECTORY_DATA - 4];
            // followed by any sequence of SubBlockDirectoryEntryDE or SubBlockDirectoryEntryDV records;
            public AttachmentEntry[] entries;

            public static class AttachmentEntry {

                public String schemaType;
                // Spare : 10 bytes
                public long filePosition;
                public int filePart;
                // GUID : 16 bytes
                public int compression;
                public String contentFileType;

                public String name;  //dimensionCount;

            }
        }
    }

    // --------------------------- CONSTANTS

    public final static String
            ZISRAWFILE = "ZISRAWFILE",
            ZISRAWDIRECTORY = "ZISRAWDIRECTORY",
            ZISRAWATTDIR = "ZISRAWATTDIR",
            ZISRAWMETADATA = "ZISRAWMETADATA",
            ZISRAWATTACH = "ZISRAWATTACH",
            DELETED = "DELETED";

    /** Pixel type constants. See CziUtils.cpp */
    public static final int GRAY8 = 0;
    public static final int GRAY16 = 1;
    public static final int GRAY_FLOAT = 2;
    public static final int BGR_24 = 3;
    public static final int BGR_48 = 4;
    public static final int BGR_FLOAT = 8;
    public static final int BGRA_8 = 9;
    public static final int COMPLEX = 10;
    public static final int COMPLEX_FLOAT = 11;
    public static final int GRAY32 = 12;
    public static final int GRAY_DOUBLE = 13;

    /** Compression constants. See CziUtils.cpp */
    public static final int UNCOMPRESSED = 0;
    public static final int JPEG = 1;
    public static final int LZW = 2;
    public static final int JPEGXR = 4;
    public static final int ZSTD_0 = 5;
    public static final int ZSTD_1 = 6;
    public static final int ALIGNMENT = 32; // all segments are aligned on 32 bytes increments
    public static final int HEADER_SIZE = 32; // SubBlock header size

}
