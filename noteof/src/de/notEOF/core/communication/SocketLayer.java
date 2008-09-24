package de.notEOF.core.communication;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.notEOF.core.enumeration.BaseCommTag;
import de.notEOF.core.enumeration.DataObjectDataTypes;
import de.notEOF.core.enumeration.DataObjectListTypes;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.Util;

/**
 * Class for simple communication.<br>
 * Near to the basic socket communication. <br>
 * Additional when sending or receiving messages this class puts the msg-headers
 * at the beginning of the messages.
 * 
 */
public class SocketLayer {

    private BufferedReader bufferedReader;
    private Socket socketToPartner;
    private LifeTimer lifeTimer;
    private boolean lifeSignSystemActivated = false;

    public SocketLayer(Socket socketToPartner) {
        this.socketToPartner = socketToPartner;
        lifeTimer = new LifeTimer(this);
    }

    protected synchronized void responseToPartner(String respHeader, String value) throws ActionFailedException {
        if (null == value)
            value = "";
        writeMsg(respHeader + "=" + value);
    }

    protected synchronized String requestToPartner(String requestString, String expectedRespHeader) throws ActionFailedException {
        expectedRespHeader += "=";
        writeMsg(requestString);
        String clientMsg = readMsg();
        if (clientMsg.length() < expectedRespHeader.length() || //
                !expectedRespHeader.equalsIgnoreCase(clientMsg.substring(0, expectedRespHeader.length()))) {
            throw new ActionFailedException(20L, "Erwartet: " + expectedRespHeader + "; Empfangen: " + clientMsg);
        }
        return clientMsg.substring(expectedRespHeader.length());
    }

    protected synchronized void awaitPartnerRequest(String expectedRequest) throws ActionFailedException {
        String msg = readMsg();
        if (!msg.equalsIgnoreCase(expectedRequest)) {
            throw new ActionFailedException(21, "Erwartet: " + expectedRequest + "; Empfangen: " + msg);
        }
    }

    protected int readInt() throws ActionFailedException {
        String value = readMsg();
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            throw new ActionFailedException(200, "Integer String: " + value, e);
        }
    }

    protected long readLong() throws ActionFailedException {
        String value = readMsg();
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            throw new ActionFailedException(200, "Long String: " + value, e);
        }
    }

    protected float readFloat() throws ActionFailedException {
        String value = readMsg();
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            throw new ActionFailedException(200, "Float String: " + value, e);
        }
    }

    protected double readDouble() throws ActionFailedException {
        String value = readMsg();
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            throw new ActionFailedException(200, "Double String: " + value, e);
        }
    }

    protected short readShort() throws ActionFailedException {
        String value = readMsg();
        try {
            return Short.parseShort(value);
        } catch (Exception e) {
            throw new ActionFailedException(200, "Short String: " + value, e);
        }
    }

    protected void writeInt(int value) throws ActionFailedException {
        try {
            String msg = String.valueOf(value);
            writeMsg(msg);
        } catch (Exception e) {
            throw new ActionFailedException(201, "Int Value: " + value, e);
        }
    }

    protected void writeLong(long value) throws ActionFailedException {
        try {
            String msg = String.valueOf(value);
            writeMsg(msg);
        } catch (Exception e) {
            throw new ActionFailedException(201, "Long Value: " + value, e);
        }
    }

    protected void writeDouble(double value) throws ActionFailedException {
        try {
            String msg = String.valueOf(value);
            writeMsg(msg);
        } catch (Exception e) {
            throw new ActionFailedException(201, "Double Value: " + value, e);
        }
    }

    protected void writeFloat(float value) throws ActionFailedException {
        try {
            String msg = String.valueOf(value);
            writeMsg(msg);
        } catch (Exception e) {
            throw new ActionFailedException(200, "Float Value: " + value, e);
        }
    }

    protected void writeShort(short value) throws ActionFailedException {
        try {
            String msg = String.valueOf(value);
            writeMsg(msg);
        } catch (Exception e) {
            throw new ActionFailedException(200, "Short Value: " + value, e);
        }
    }

    /*
     * If a message contains lifesigns, a read is required once more. Additional
     * the last time when a lifesign came in will be updated. This method
     * 'clears' the messages from the lifesign by rereading.
     */
    protected String readMsg() throws ActionFailedException {
        String msg = BaseCommTag.REQ_LIFE_SIGN.name();
        while (BaseCommTag.REQ_LIFE_SIGN.name().equals(msg)) {
            msg = readUnqualifiedMsg();
            if (BaseCommTag.REQ_LIFE_SIGN.name().equals(msg)) {
                responseToPartner(BaseCommTag.RESP_LIFE_SIGN.name(), BaseCommTag.VAL_OK.name());
            }
            // no more read in this case
            // the next msg in the buffer can be sent independent to the
            // lifesign system
            if (BaseCommTag.RESP_LIFE_SIGN.name().equals(msg)) {
                lifeTimer.lifeSignReceived();
            }
        }
        return msg;
    }

    /*
     * Perhaps the reader is yet null.
     */
    private void initBufferedReader() throws ActionFailedException {
        try {
            if (null == bufferedReader)
                bufferedReader = new BufferedReader(new InputStreamReader(socketToPartner.getInputStream()));
        } catch (IOException ex) {
            throw new ActionFailedException(23L, ex);
        }
    }

    private String readUnqualifiedMsg() throws ActionFailedException {
        String msg = "";
        initBufferedReader();
        try {
            msg = bufferedReader.readLine();
            if (!Util.isEmpty(msg)) {
                if (msg.startsWith("#")) {
                    msg = msg.substring(1);
                }
            }
        } catch (SocketTimeoutException ex) {
            throw new ActionFailedException(24L, ex);
        } catch (IOException ex) {
            throw new ActionFailedException(23L, ex);
        }
        if (Util.isEmpty(msg))
            msg = "";

        // show lifetimer that the connection is ok
        lifeTimer.lifeSignReceived();

        return msg;
    }

    @SuppressWarnings("unchecked")
    protected DataObject receiveDataObject() throws ActionFailedException {
        // TODO Listen verarbeiten
        // dataTypes:
        // 0 = short
        // 1 = int
        // 2 = long
        // 3 = float
        // 4 = double
        // 5 = char
        // 6 = char[]
        // 7 = line (terminated by \n)
        // 8 = file
        // 9 = configuration value
        // 10 = date
        // 11 = Map <String, String>
        // 12 = List<?>

        initBufferedReader();
        DataObject dataObject = new DataObject();
        try {
            DataInputStream inputStream = new DataInputStream(socketToPartner.getInputStream());
            // den Datentyp ermitteln
            int dataTypeInt = inputStream.readInt();
            DataObjectDataTypes dataType = DataObjectDataTypes.values()[dataTypeInt];

            switch (dataType) {
            case SHORT:
                dataObject.setShort(readShort());
                break;

            case INT:
                dataObject.setInt(readInt());
                break;

            case LONG:
                dataObject.setLong(readLong());
                break;

            case FLOAT:
                dataObject.setFloat(readFloat());
                break;

            case DOUBLE:
                dataObject.setDouble(readDouble());
                break;

            case CHAR:
                dataObject.setChar(inputStream.readChar());
                break;

            case CHAR_ARRAY:
                receiveDataObjectCharArray(dataObject, inputStream);
                break;

            case LINE:
                dataObject.setLine(readMsg());
                break;

            case FILE:
                // first step: get file name and canonical file name
                dataObject.setFileName(readMsg());
                dataObject.setCanonicalFileName(readMsg());

                // second step: receive FileData
                receiveDataObjectCharArray(dataObject, inputStream);
                break;

            case CONFIGURATION_VALUE:
                dataObject.setConfigurationValue(readMsg());
                break;

            case DATE:
                // TODO Datum empfangen
                break;

            case MAP_STRING_STRING:
                int mapSize = readInt();
                if (0 != mapSize) {
                    Map<String, String> map = new HashMap<String, String>();
                    for (int i = 0; i < mapSize; i++) {
                        String key = readMsg();
                        String value = readMsg();
                        map.put(key, value);
                    }
                    dataObject.setMap(map);
                }
                break;

            case LIST:
                int listSize = readInt();
                int listTypeInt = readInt();
                DataObjectListTypes listType = DataObjectListTypes.values()[listTypeInt];

                List list = new ArrayList();
                if (0 != listSize) {
                    for (int i = 0; i < listSize; i++) {
                        String line = readMsg();
                        switch (listType) {
                        case INTEGER:
                            list.add(Integer.valueOf(line));
                            break;

                        case LONG:
                            list.add(Long.valueOf(line));
                            break;

                        case STRING:
                            list.add(line);
                            break;
                        }
                    }
                    dataObject.setList(list);
                }
                break;

            }

        } catch (SocketTimeoutException ex) {
            throw new ActionFailedException(24L, ex);
        } catch (IOException ex) {
            throw new ActionFailedException(23L, ex);
        }
        return dataObject;
    }

    protected void sendDataObject(DataObject dataObject) throws ActionFailedException {
        // dataTypes:
        // 0 = short
        // 1 = int
        // 2 = long
        // 3 = float
        // 4 = double
        // 5 = char
        // 6 = char[]
        // 7 = line (terminated by \n)
        // 8 = file
        // 9 = configuration value
        // 10 = date
        // 11 = Map <String, String>
        // 12 = List<?>

        try {
            DataOutputStream outputStream = new DataOutputStream(socketToPartner.getOutputStream());
            // den Datentyp ermitteln
            DataObjectDataTypes dataType = dataObject.getDataType();
            outputStream.writeInt(dataType.ordinal());
            outputStream.flush();

            switch (dataType) {
            case SHORT:
                writeShort(dataObject.getShort());
                break;

            case INT:
                writeInt(dataObject.getInt());
                break;

            case LONG:
                writeLong(dataObject.getLong());
                break;

            case FLOAT:
                writeFloat(dataObject.getFloat());
                break;

            case DOUBLE:
                writeDouble(dataObject.getDouble());
                break;

            case CHAR:
                outputStream.writeChar(dataObject.getChar());
                break;

            case CHAR_ARRAY:
                sendDataObjectCharArray(dataObject, outputStream);
                break;

            case LINE:
                writeMsg(dataObject.getLine());
                break;

            case FILE:
                // at first send fileName and canonicalFileName
                writeMsg(dataObject.getFileName());
                writeMsg(dataObject.getCanonicalFileName());

                // then send fileData
                sendDataObjectCharArray(dataObject, outputStream);
                break;

            case CONFIGURATION_VALUE:
                writeMsg(dataObject.getLine());
                break;

            case DATE:
                // TODO Datum senden und empfangen...
                break;

            case MAP_STRING_STRING:
                if (null != dataObject.getMap()) {
                    Map<String, String> map = dataObject.getMap();
                    Set<Map.Entry<String, String>> mapSet = map.entrySet();
                    writeInt(mapSet.size());
                    for (Map.Entry<String, String> mapEntry : mapSet) {
                        // send key
                        writeMsg(mapEntry.getKey());
                        // send value
                        writeMsg(mapEntry.getValue());
                    }
                } else {
                    // send size of map is 0
                    writeInt(0);
                }
                break;

            case LIST:
                if (null != dataObject.getList()) {
                    List<?> list = dataObject.getList();

                    writeInt(list.size());
                    String value = "";
                    writeInt(dataObject.getListObjectType().ordinal());
                    for (Object obj : list) {
                        switch (dataObject.getListObjectType()) {
                        case INTEGER:
                        case LONG:
                            value = String.valueOf(obj);
                            break;
                        case STRING:
                            value = (String) obj;
                            break;
                        }
                        writeMsg(value);
                    }
                } else {
                    // send that size of list is 0
                    writeInt(0);
                }
                break;

            }
        } catch (SocketTimeoutException ex) {
            throw new ActionFailedException(26L, ex);
        } catch (IOException ex) {
            throw new ActionFailedException(25L, ex);
        }
    }

    private void receiveDataObjectCharArray(DataObject dataObject, DataInputStream inputStream) throws ActionFailedException {
        initBufferedReader();

        try {
            // size block 1 and count of blocks for 1
            int sizeBlock1 = inputStream.readInt();
            int countBlock1 = inputStream.readInt();
            // size block 2 and count of blocks for 2
            int sizeBlock2 = inputStream.readInt();
            int countBlock2 = inputStream.readInt();
            char[] charArray = new char[(sizeBlock1 * countBlock1) + (sizeBlock2 * countBlock2)];

            int pos = 0;
            for (int i = 0; i < countBlock1; i++) {
                bufferedReader.read(charArray, pos, sizeBlock1);
                pos += sizeBlock1;
            }
            for (int i = 0; i < countBlock2; i++) {
                bufferedReader.read(charArray, pos, sizeBlock2);
                pos += sizeBlock2;
            }

            dataObject.setCharArray(charArray);
        } catch (IOException ex) {
            throw new ActionFailedException(23L, ex);
        }

    }

    private void sendDataObjectCharArray(DataObject dataObject, DataOutputStream outputStream) throws IOException {
        // char array
        int arrayLength = dataObject.getCharArray().length;
        int sizeBlock1 = 255;
        int countBlock1 = arrayLength / sizeBlock1;
        int sizeBlock2 = arrayLength % sizeBlock1;
        int countBlock2 = 1;

        outputStream.writeInt(sizeBlock1);
        outputStream.writeInt(countBlock1);
        outputStream.writeInt(sizeBlock2);
        outputStream.writeInt(1);

        PrintWriter printWriterChar = new PrintWriter(new OutputStreamWriter(socketToPartner.getOutputStream()));
        int pos = 0;
        for (int i = 0; i < countBlock1; i++) {
            printWriterChar.write(dataObject.getCharArray(), pos, sizeBlock1);
            pos += sizeBlock1;
        }
        for (int i = 0; i < countBlock2; i++) {
            printWriterChar.write(dataObject.getCharArray(), pos, sizeBlock2);
            pos += sizeBlock2;

        }
        printWriterChar.flush();
    }

    protected synchronized boolean isConnected() {
        if (null == socketToPartner)
            return false;
        return socketToPartner.isConnected() && socketToPartner.isBound();
    }

    protected synchronized boolean lifeSignSucceeded() {
        return lifeTimer.lifeSignSucceeded();
    }

    protected synchronized void writeMsg(String msg) throws ActionFailedException {
        // avoid that the lifetimer sends a signal during awaiting a response
        // from partner
        lifeTimer.updateNextLifeSign();

        if (null == msg)
            msg = "";
        msg = "#" + msg;
        try {
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socketToPartner.getOutputStream()));
            printWriter.println(msg);
            printWriter.flush();
        } catch (IOException ex) {
            throw new ActionFailedException(25L, ex);
        }
    }

    protected void close() {
        if (null != lifeTimer)
            lifeTimer.stop();
        if (null == socketToPartner)
            return;
        try {
            if (socketToPartner.isConnected())
                socketToPartner.close();
            socketToPartner = null;
        } catch (Exception ex) {
            LocalLog.error("Problem bei Schliessen der Verbindung zum Paul-Server.", ex);
        }
    }

    protected void setTimeOut(int timeOutMillis) throws ActionFailedException {
        try {
            socketToPartner.setSoTimeout(timeOutMillis);
        } catch (SocketException ex) {
            throw new ActionFailedException(11L, ex);
        }
    }

    protected int getTimeOut() throws ActionFailedException {
        try {
            return socketToPartner.getSoTimeout();
        } catch (SocketException ex) {
            throw new ActionFailedException(12L, ex);
        }
    }

    public boolean isLifeSignSystemActive() {
        return lifeSignSystemActivated;
    }

    /**
     * Activates the integrated system to send periodical signs to the
     * communication partner.<br>
     * When the lifesign system is activated for the service part, it must be
     * activated for the client also.<br>
     * If not the service awaits lifesigns which not are sent by the client.
     * This is interpreted as 'client not alive' by the service and he cuts the
     * connection to the client.
     * 
     * @param asClient
     *            Must be true when the lifesign system is activated by a client
     *            implementation, otherwise it must be false.
     */
    public void activateLifeSignSystem(Boolean asClient) {
        if (null != asClient && asClient) {
            lifeTimer.useAsClient();
        }
        Thread threadLifeTimer = new Thread(lifeTimer);
        threadLifeTimer.start();
    }

    public boolean closedByLifeSignSystem() {
        if (isConnected())
            return false;
        return lifeTimer.closedByLifeSignSystem();
    }

    protected Socket getSocketToPartner() {
        return socketToPartner;
    }

}
