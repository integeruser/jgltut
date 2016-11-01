package integeruser.jgltut.framework;

import org.lwjgl.BufferUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.*;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
 * Original: https://bitbucket.org/alfonse/gltut/src/default/framework/Mesh.cpp
 */
public class Mesh {
    public Mesh(String filename) {
        ArrayList<Attribute> attribs = new ArrayList<>(16);
        ArrayList<IndexData> indexData = new ArrayList<>();
        ArrayList<NamedVAO> namedVaoList = new ArrayList<>();

        Document doc = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            String meshPath = Framework.findFileOrThrow(filename);
            doc = dBuilder.parse(ClassLoader.class.getResourceAsStream(meshPath));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Element meshElement = doc.getDocumentElement();
        NodeList attrs = meshElement.getElementsByTagName("attribute");
        for (int i = 0; i < attrs.getLength(); i++) {
            // crea un Attribute e lo aggiunge a attribs
            Attribute a = new Attribute(attrs.item(i));
            attribs.add(a);
        }

        NodeList vaos = meshElement.getElementsByTagName("vao");
        for (int i = 0; i < vaos.getLength(); i++) {
            // crea un NamedVAO con ProcessVAO e lo aggiunge a namedVaoList
            NamedVAO namedVao = new NamedVAO(vaos.item(i));
            namedVaoList.add(namedVao);
        }

        NodeList cmds = meshElement.getElementsByTagName("indices");
        for (int i = 0; i < cmds.getLength(); i++) {
            // aggiunge a primitives il risultato di ProcessRenderCmd
            RenderCmd r = new RenderCmd(cmds.item(i));
            primitives.add(r);
            // aggiunge a indexData il risultato di IndexData
            IndexData in = new IndexData(cmds.item(i));
            indexData.add(in);
        }

        NodeList arrays = meshElement.getElementsByTagName("arrays");
        for (int i = 0; i < arrays.getLength(); i++) {
            // aggiunge a primitives il risultato di ProcessRenderCmd
            RenderCmd r = new RenderCmd(arrays.item(i));
            primitives.add(r);
        }

        // calcola la lunghezza del buffer controllando che tutti gli array di
        // attributi abbiano la stessa lunghezza
        int iAttrbBufferSize = 0;
        ArrayList<Integer> attribStartLocs = new ArrayList<>(attribs.size());
        int iNumElements = 0;

        for (int i = 0; i < attribs.size(); i++) {
            iAttrbBufferSize = iAttrbBufferSize % 16 != 0 ?
                    (iAttrbBufferSize + (16 - iAttrbBufferSize % 16))
                    : iAttrbBufferSize;
            attribStartLocs.add(iAttrbBufferSize);
            Attribute attrib = attribs.get(i);

            iAttrbBufferSize += attrib.calcByteSize();

            if (iNumElements != 0) {
                if (iNumElements != attrib.numElements()) {
                    throw new RuntimeException("Some of the attribute arrays have different element counts.");
                }
            } else {
                iNumElements = attrib.numElements();
            }
        }

        // crea e binda il VAO
        oVAO = glGenVertexArrays();
        glBindVertexArray(oVAO);

        // Crea i buffer object
        oAttribArraysBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, oAttribArraysBuffer);
        glBufferData(GL_ARRAY_BUFFER, iAttrbBufferSize, GL_STATIC_DRAW);

        // Inserisce i dati degli attributi nel buffer object
        for (int i = 0; i < attribs.size(); i++) {
            Attribute attrib = attribs.get(i);
            attrib.fillBoundBufferObject(attribStartLocs.get(i));
            attrib.setupAttributeArray(attribStartLocs.get(i));
        }

        // riempie i vari VAOs
        for (int i = 0; i < namedVaoList.size(); i++) {
            NamedVAO namedVao = namedVaoList.get(i);

            int vao = glGenVertexArrays();
            glBindVertexArray(vao);

            List<Integer> attributeArray = namedVao.attributes;
            for (int j = 0; j < attributeArray.size(); j++) {
                int idAttrib = attributeArray.get(j);
                int iAttribOffset = -1;
                for (int iCount = 0; iCount < attribs.size(); iCount++) {
                    if (attribs.get(iCount).attribIndex == idAttrib) {
                        iAttribOffset = iCount;
                        break;
                    }
                }

                Attribute attrib = attribs.get(iAttribOffset);
                attrib.setupAttributeArray(attribStartLocs.get(iAttribOffset));
            }

            namedVAOs.put(namedVao.name, vao);
        }

        glBindVertexArray(0);

        // Calcola la lunghezza dell'index buffer
        int iIndexBufferSize = 0;
        ArrayList<Integer> indexStartLocs = new ArrayList<>(indexData.size());
        for (int i = 0; i < indexData.size(); i++) {
            iIndexBufferSize = iIndexBufferSize % 16 != 0 ?
                    (iIndexBufferSize + (16 - iIndexBufferSize % 16))
                    : iIndexBufferSize;

            indexStartLocs.add(iIndexBufferSize);
            IndexData currData = indexData.get(i);

            iIndexBufferSize += currData.calcByteSize();
        }

        // Crea l'index buffer object
        if (iIndexBufferSize > 0) {
            glBindVertexArray(oVAO);

            oIndexBuffer = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, oIndexBuffer);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, iIndexBufferSize,
                    GL_STATIC_DRAW);

            // gli inserisce i dati
            for (int i = 0; i < indexData.size(); i++) {
                IndexData currData = indexData.get(i);
                currData.fillBoundBufferObject(indexStartLocs.get(i));
            }

            // crea i RenderCmd
            int iCurrIndexed = 0;
            for (int i = 0; i < primitives.size(); i++) {
                RenderCmd prim = primitives.get(i);
                if (prim.isIndexedCmd) {
                    prim.start = indexStartLocs.get(iCurrIndexed);
                    prim.elemCount = indexData.get(iCurrIndexed)
                            .getDataNumElem();
                    prim.eIndexDataType = indexData.get(iCurrIndexed).attribType.glType;
                    iCurrIndexed++;
                }
            }

            for (Integer idVAO : namedVAOs.values()) {
                glBindVertexArray(idVAO);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, oIndexBuffer);
            }

            glBindVertexArray(0);
        }
    }

    public void render() {
        if (oVAO == 0) {
            return;
        }

        glBindVertexArray(oVAO);

        for (RenderCmd cmd : primitives) {
            cmd.render();
        }

        glBindVertexArray(0);
    }

    public void render(String meshName) {
        Integer vao = namedVAOs.get(meshName);
        if (vao == null) {
            return;
        }

        glBindVertexArray(vao);

        for (RenderCmd cmd : primitives) {
            cmd.render();
        }

        glBindVertexArray(0);
    }

    ////////////////////////////////
    private int oAttribArraysBuffer = 0;
    private int oIndexBuffer = 0;
    private int oVAO = 0;

    private ArrayList<RenderCmd> primitives = new ArrayList<>();
    private Map<String, Integer> namedVAOs = new HashMap<>();

    ////////////////////////////////
    public abstract static class ParseFunc {
        abstract public Buffer parse(String strToParse);
    }

    public abstract static class WriteFunc {
        abstract public void writeToBuffer(int eBuffer, Buffer theData, int iOffset);
    }


    private static class Attribute {
        Attribute(Node attributeNode) {
            NamedNodeMap attrs = attributeNode.getAttributes();

            {
                Node indexNode = attrs.getNamedItem("index");
                if (indexNode == null) {
                    throw new RuntimeException("Missing 'index' attribute in an 'attribute' element.");
                }

                int index = Integer.parseInt(indexNode.getNodeValue());
                if (!((0 <= index) && (index < 16))) {
                    throw new RuntimeException("Attribute index must be between 0 and 16.");
                }

                attribIndex = index;
            }

            {
                Node sizeNode = attrs.getNamedItem("size");
                if (sizeNode == null) {
                    throw new RuntimeException("Missing 'size' attribute in an 'attribute' element.");
                }

                int size = Integer.parseInt(sizeNode.getNodeValue());
                if (!((1 <= size) && (size < 5))) {
                    throw new RuntimeException("Attribute size must be between 1 and 4.");
                }

                this.size = size;
            }

            {
                String strType;
                Node typeNode = attrs.getNamedItem("type");
                if (typeNode == null) {
                    throw new RuntimeException("Missing 'type' attribute in an 'attribute' element.");
                }

                strType = typeNode.getNodeValue();
                attribType = AttribType.get(strType);
            }

            {
                Node integralNode = attrs.getNamedItem("integral");
                if (integralNode == null) {
                    isIntegral = false;
                } else {
                    String strIntegral = integralNode.getNodeValue();
                    switch (strIntegral) {
                        case "true":
                            isIntegral = true;
                            break;
                        case "false":
                            isIntegral = false;
                            break;
                        default:
                            throw new RuntimeException("Incorrect 'integral' value for the 'attribute'.");
                    }

                    if (attribType.normalized) {
                        throw new RuntimeException("Attribute cannot be both 'integral' and a normalized 'type'.");
                    }

                    if (attribType.glType == GL_FLOAT
                            || attribType.glType == GL_HALF_FLOAT
                            || attribType.glType == GL_DOUBLE) {
                        throw new RuntimeException("Attribute cannot be both 'integral' and a floating-point 'type'.");
                    }
                }
            }

            {
                String strData = attributeNode.getChildNodes().item(0).getNodeValue();
                dataArray = attribType.parse(strData);
            }
        }


        int numElements() {
            return getDataNumElem() / size;
        }

        int calcByteSize() {
            return getDataNumElem() * attribType.numBytes;
        }

        void fillBoundBufferObject(int offset) {
            attribType.writeToBuffer(GL_ARRAY_BUFFER, dataArray, offset);
        }

        void setupAttributeArray(int offset) {
            glEnableVertexAttribArray(attribIndex);
            if (isIntegral) {
                glVertexAttribIPointer(attribIndex, size, attribType.glType, 0, offset);
            } else {
                glVertexAttribPointer(attribIndex, size, attribType.glType,
                        attribType.normalized, 0, offset);
            }
        }

        ////////////////////////////////

        private int attribIndex = 0xFFFFFFFF;
        private AttribType attribType = null;
        private int size = -1;
        private boolean isIntegral = false;
        private Buffer dataArray;


        private int getDataNumElem() {
            return dataArray.limit();
        }
    }

    private static class AttribType {
        AttribType(boolean normalized, int glType, int numBytes, ParseFunc parseFunc, WriteFunc writeFunc) {
            this.normalized = normalized;
            this.glType = glType;
            this.numBytes = numBytes;
            this.parseFunc = parseFunc;
            this.writeFunc = writeFunc;
        }


        Buffer parse(String strData) {
            return parseFunc.parse(strData);
        }

        void writeToBuffer(int eBuffer, Buffer buffer, int offset) {
            writeFunc.writeToBuffer(eBuffer, buffer, offset);
        }


        static AttribType get(String type) {
            AttribType attType = allAttribType.get(type);
            if (attType == null) {
                throw new RuntimeException("Unknown 'type' field.");
            }

            return attType;
        }

        ////////////////////////////////

        private static final Map<String, AttribType> allAttribType = new HashMap<>();
        private static final Map<String, Integer> allPrimitiveType = new HashMap<>();

        private boolean normalized;
        private int glType;
        private int numBytes;

        private ParseFunc parseFunc;
        private WriteFunc writeFunc;

        private static ParseFunc parseFloats = new ParseFunc() {
            public Buffer parse(String strToParse) {
                Scanner scn = new Scanner(strToParse);
                scn.useDelimiter("\\s+");
                ArrayList<Float> array = new ArrayList<>();

                while (scn.hasNext()) {
                    array.add(Float.parseFloat(scn.next()));
                }

                FloatBuffer buff = BufferUtils.createFloatBuffer(array.size());
                for (Float data : array) {
                    buff.put(data);
                }
                buff.flip();

                scn.close();
                return buff;
            }
        };
        private static ParseFunc parseInts = new ParseFunc() {
            public Buffer parse(String strToParse) {
                Scanner scn = new Scanner(strToParse);
                scn.useDelimiter("\\s+");
                ArrayList<Integer> array = new ArrayList<>();

                while (scn.hasNext()) {
                    array.add((int) Long.parseLong(scn.next()));
                }

                IntBuffer buff = BufferUtils.createIntBuffer(array.size());
                for (Integer data : array) {
                    buff.put(data);
                }
                buff.flip();

                scn.close();
                return buff;
            }
        };
        private static ParseFunc parseShorts = new ParseFunc() {
            public Buffer parse(String strToParse) {
                Scanner scn = new Scanner(strToParse);
                scn.useDelimiter("\\s+");
                ArrayList<Short> array = new ArrayList<>();

                while (scn.hasNext()) {
                    array.add((short) Integer.parseInt(scn.next()));
                }

                ShortBuffer buff = BufferUtils.createShortBuffer(array.size());
                for (Short data : array) {
                    buff.put(data);
                }
                buff.flip();

                scn.close();
                return buff;
            }
        };
        private static ParseFunc parseBytes = new ParseFunc() {
            public Buffer parse(String strToParse) {
                Scanner scn = new Scanner(strToParse);
                scn.useDelimiter("\\s+");
                ArrayList<Byte> array = new ArrayList<>();

                while (scn.hasNext()) {
                    array.add((byte) Short.parseShort(scn.next()));
                }

                ByteBuffer buff = BufferUtils.createByteBuffer(array.size());
                for (Byte data : array) {
                    buff.put(data);
                }
                buff.flip();

                scn.close();
                return buff;
            }
        };

        private static WriteFunc writeFloats = new WriteFunc() {
            public void writeToBuffer(int eBuffer, Buffer theData, int iOffset) {
                glBufferSubData(eBuffer, iOffset, (FloatBuffer) theData);
            }
        };
        private static WriteFunc writeInts = new WriteFunc() {
            public void writeToBuffer(int eBuffer, Buffer theData, int iOffset) {
                glBufferSubData(eBuffer, iOffset, (IntBuffer) theData);
            }
        };
        private static WriteFunc writeShorts = new WriteFunc() {
            public void writeToBuffer(int eBuffer, Buffer theData, int iOffset) {
                glBufferSubData(eBuffer, iOffset, (ShortBuffer) theData);
            }
        };
        private static WriteFunc writeBytes = new WriteFunc() {
            public void writeToBuffer(int eBuffer, Buffer theData, int iOffset) {
                glBufferSubData(eBuffer, iOffset, (ByteBuffer) theData);
            }
        };

        static {
            allAttribType.put("float", new AttribType(false, GL_FLOAT,
                    Float.SIZE / 8, parseFloats, writeFloats));
            // {"half", false, GL_HALF_FLOAT, sizeof(GLhalfARB), ParseFloats,
            // WriteFloats},
            allAttribType.put("int", new AttribType(false, GL_INT,
                    Integer.SIZE / 8, parseInts, writeInts));
            allAttribType.put("uint", new AttribType(false, GL_UNSIGNED_INT,
                    Integer.SIZE / 8, parseInts, writeInts));
            // {"norm-int", true, GL_INT, sizeof(GLint), ParseInts, WriteInts},
            // {"norm-uint", true, GL_UNSIGNED_INT, sizeof(GLuint), ParseUInts,
            // WriteUInts},
            allAttribType.put("short", new AttribType(false, GL_SHORT,
                    Short.SIZE / 8, parseShorts, writeShorts));
            allAttribType.put("ushort",
                    new AttribType(false, GL_UNSIGNED_SHORT, Short.SIZE / 8,
                            parseShorts, writeShorts));
            // {"norm-short", true, GL_SHORT, sizeof(GLshort), ParseShorts,
            // WriteShorts},
            // {"norm-ushort", true, GL_UNSIGNED_SHORT, sizeof(GLushort),
            // ParseUShorts, WriteUShorts},
            allAttribType.put("byte", new AttribType(false, GL_BYTE,
                    Byte.SIZE / 8, parseBytes, writeBytes));
            allAttribType.put("ubyte", new AttribType(false, GL_UNSIGNED_BYTE,
                    Byte.SIZE / 8, parseBytes, writeBytes));
            // {"norm-byte", true, GL_BYTE, sizeof(GLbyte), ParseBytes,
            // WriteBytes},
            // {"norm-ubyte", true, GL_UNSIGNED_BYTE, sizeof(GLubyte),
            // ParseUBytes, WriteUBytes},

            allPrimitiveType.put("triangles", GL_TRIANGLES);
            allPrimitiveType.put("tri-strip", GL_TRIANGLE_STRIP);
            allPrimitiveType.put("tri-fan", GL_TRIANGLE_FAN);
            allPrimitiveType.put("lines", GL_LINES);
            allPrimitiveType.put("line-strip", GL_LINE_STRIP);
            allPrimitiveType.put("line-loop", GL_LINE_LOOP);
            allPrimitiveType.put("points", GL_POINTS);
        }
    }

    ////////////////////////////////
    private static class RenderCmd {
        private boolean isIndexedCmd;
        private int primType;
        private int start;
        private int elemCount;
        private int eIndexDataType;  // Only if isIndexedCmd is true.


        RenderCmd(Node cmdNode) {
            NamedNodeMap attrs = cmdNode.getAttributes();

            {
                Node cmdAttr = attrs.getNamedItem("cmd");
                if (cmdAttr == null) {
                    throw new RuntimeException("Missing 'cmd' attribute in an 'arrays' or 'indices' element.");
                }

                String strCmd = cmdAttr.getNodeValue();
                Integer primitive = AttribType.allPrimitiveType.get(strCmd);
                if (primitive == null) {
                    throw new RuntimeException("Unknown 'cmd' field.");
                }

                primType = primitive;
            }

            if (cmdNode.getNodeName().equals("indices")) {
                isIndexedCmd = true;
            } else if (cmdNode.getNodeName().equals("arrays")) {
                isIndexedCmd = false;

                {
                    Node nodeStart = cmdNode.getAttributes().getNamedItem("start");
                    if (nodeStart == null) {
                        throw new RuntimeException("Missing 'start' attribute in an 'arrays' element.");
                    }

                    int iStart = Integer.parseInt(nodeStart.getNodeValue());
                    if (iStart < 0) {
                        throw new RuntimeException("Attribute 'start' must be between 0 or greater.");
                    }

                    start = iStart;
                }
                {
                    Node nodeCount = cmdNode.getAttributes().getNamedItem("count");
                    if (nodeCount == null) {
                        throw new RuntimeException("Missing 'count' attribute in an 'arrays' element.");
                    }

                    int iCount = Integer.parseInt(nodeCount.getNodeValue());
                    if (iCount <= 0) {
                        throw new RuntimeException("Attribute 'count' must be greater than 0.");
                    }

                    elemCount = iCount;
                }
            } else {
                throw new RuntimeException("Bad element. Must be 'indices' or 'arrays'.");
            }
        }


        void render() {
            if (isIndexedCmd) {
                glDrawElements(primType, elemCount, eIndexDataType, start);
            } else {
                glDrawArrays(primType, start, elemCount);
            }
        }
    }

    private static class IndexData {
        private AttribType attribType;
        private Buffer dataArray;


        IndexData(Node indexElem) {
            NamedNodeMap attrs = indexElem.getAttributes();
            // controlla che type sia valido
            {
                Node typeAttr = attrs.getNamedItem("type");
                if (typeAttr == null) {
                    throw new RuntimeException("Missing 'type' attribute in an 'index' element.");
                }

                String strType = typeAttr.getNodeValue();
                if (!(strType.equals("uint") || strType.equals("ushort") || strType.equals("ubyte"))) {
                    throw new RuntimeException("Improper 'type' attribute value on 'index' element.");
                }

                attribType = AttribType.get(strType);
            }

            // legge gli indici
            {
                String strIndex = indexElem.getChildNodes().item(0).getNodeValue();
                dataArray = attribType.parse(strIndex);
                if (dataArray.limit() == 0) {
                    throw new RuntimeException("The index element must have an array of values.");
                }
            }
        }


        void fillBoundBufferObject(int iOffset) {
            attribType.writeToBuffer(GL_ELEMENT_ARRAY_BUFFER, dataArray,
                    iOffset);
        }

        int getDataNumElem() {
            return dataArray.limit();
        }

        int calcByteSize() {
            return getDataNumElem() * attribType.numBytes;
        }
    }

    private static class NamedVAO {
        private String name;
        private ArrayList<Integer> attributes;


        NamedVAO(Node itemVao) {
            attributes = new ArrayList<>();

            NamedNodeMap attrs = itemVao.getAttributes();
            {
                Node nameAttr = attrs.getNamedItem("name");
                if (nameAttr == null) {
                    throw new RuntimeException("Missing 'name' attribute in an 'vao' element.");
                }

                name = nameAttr.getNodeValue();
            }

            Element elemVao = (Element) itemVao;
            NodeList sources = elemVao.getElementsByTagName("source");
            for (int sourceIndex = 0; sourceIndex < sources.getLength(); sourceIndex++) {
                Node attrib = sources.item(sourceIndex).getAttributes().getNamedItem("attrib");
                if (attrib == null) {
                    throw new RuntimeException("Missing 'attrib' attribute in an 'source' element.");
                }

                attributes.add(Integer.parseInt(attrib.getNodeValue()));
            }
        }
    }
}
