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

import de.notEOF.core.enumeration.BaseCommTag;
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

    private String readUnqualifiedMsg() throws ActionFailedException {
        String msg = "";
        try {
            if (null == bufferedReader)
                bufferedReader = new BufferedReader(new InputStreamReader(socketToPartner.getInputStream()));
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

    protected DataObject receiveDataObject() throws ActionFailedException {
        // dataTypes:
        // 0 = short
        // 1 = int
        // 2 = long
        // 3 = float
        // 4 = double
        // 5 = char
        // 6 = char[]
        // 7 = line (terminated by \n)

        DataObject dataObject = new DataObject();
        try {
            DataInputStream inputStream = new DataInputStream(socketToPartner.getInputStream());
            // den Datentyp ermitteln
            int dataType = inputStream.readInt();

            switch (dataType) {
            case 0:
                // short
                dataObject.setShort(inputStream.readShort());
                break;

            case 1:
                // int
                dataObject.setInt(inputStream.readInt());
                break;

            case 2:
                // long
                dataObject.setLong(inputStream.readLong());
                break;

            case 3:
                // float
                dataObject.setFloat(inputStream.readFloat());
                break;

            case 4:
                // double
                dataObject.setDouble(inputStream.readDouble());
                break;

            case 5:
                // char
                dataObject.setChar(inputStream.readChar());
                break;

            case 6:
                // char array
                receiveDataObjectCharArray(dataObject, inputStream);
                break;

            case 7:
                // String
                dataObject.setLine(bufferedReader.readLine());
                break;

            case 8:
                // file
                // first step: get file name and canonical file name
                dataObject.setFileName(bufferedReader.readLine());
                dataObject.setCanonicalFileName(bufferedReader.readLine());

                // second step: receive FileData
                receiveDataObjectCharArray(dataObject, inputStream);
                break;

            case 9:
                // conf value
                dataObject.setConfigurationValue(bufferedReader.readLine());
                break;

            case 10:
                // TODO Datum empfangen
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

        try {
            DataOutputStream outputStream = new DataOutputStream(socketToPartner.getOutputStream());
            // den Datentyp ermitteln
            int dataType = dataObject.getDataType();
            outputStream.writeInt(dataType);

            switch (dataType) {
            case 0:
                // short
                outputStream.writeShort(dataObject.getShort());
                break;

            case 1:
                // int
                outputStream.writeInt(dataObject.getInt());
                break;

            case 2:
                // long
                outputStream.writeLong(dataObject.getLong());
                break;

            case 3:
                // float
                outputStream.writeFloat(dataObject.getFloat());
                break;

            case 4:
                // double
                outputStream.writeDouble(dataObject.getDouble());
                break;

            case 5:
                // char
                outputStream.writeChar(dataObject.getChar());
                break;

            case 6:
                // char array
                sendDataObjectCharArray(dataObject, outputStream);
                break;

            case 7:
                // line
                PrintWriter printWriterString = new PrintWriter(new OutputStreamWriter(socketToPartner.getOutputStream()));
                printWriterString.println(dataObject.getLine());
                printWriterString.flush();
                break;

            case 8:
                // file
                // at first send fileName and canonicalFileName
                PrintWriter printWriterFileName = new PrintWriter(new OutputStreamWriter(socketToPartner.getOutputStream()));
                printWriterFileName.println(dataObject.getFileName());
                printWriterFileName.println(dataObject.getCanonicalFileName());
                printWriterFileName.flush();

                // then send fileData
                sendDataObjectCharArray(dataObject, outputStream);
                break;

            case 9:
                // configuration value
                PrintWriter printWriterConf = new PrintWriter(new OutputStreamWriter(socketToPartner.getOutputStream()));
                printWriterConf.println(dataObject.getLine());
                printWriterConf.flush();
                break;

            case 10:
                // TODO Datum senden und empfangen...
                break;

            }
        } catch (SocketTimeoutException ex) {
            throw new ActionFailedException(26L, ex);
        } catch (IOException ex) {
            throw new ActionFailedException(25L, ex);
        }
    }

    private void receiveDataObjectCharArray(DataObject dataObject, DataInputStream inputStream) throws IOException {
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
