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
import java.nio.CharBuffer;

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
        // 7 = String

        System.out.println("In receiveDAtaObject... 1");
        DataObject dataObject = new DataObject();
        try {
            DataInputStream inputStream = new DataInputStream(socketToPartner.getInputStream());
            // den Datentyp ermitteln
            int dataType = inputStream.readInt();
            System.out.println("inputStream gelesen.");
            System.out.println("dataType = " + dataType);

            switch (dataType) {
            case 0:
                // short
                dataObject.setShortValue(inputStream.readShort());
                break;

            case 1:
                // int
                dataObject.setIntValue(inputStream.readInt());
                break;

            case 2:
                // long
                dataObject.setLongValue(inputStream.readLong());
                break;

            case 3:
                // float
                dataObject.setFloatValue(inputStream.readFloat());
                break;

            case 4:
                // double
                dataObject.setDoubleValue(inputStream.readDouble());
                break;

            case 5:
                // char
                dataObject.setCharValue(inputStream.readChar());
                break;

            case 6:
                // char array
                // size block 1 and count of blocks for 1
                int sizeBlock1 = inputStream.readInt();
                int countBlock1 = inputStream.readInt();
                // size block 2 and count of blocks for 2
                int sizeBlock2 = inputStream.readInt();
                int countBlock2 = inputStream.readInt();

                char[] charArray = new char[(sizeBlock1 * countBlock1) + (sizeBlock2 * countBlock2)];

                int pos = 0;
                for (int i = 0; i < countBlock1; i++) {
                    bufferedReader.read(charArray, pos += sizeBlock1, sizeBlock1);
                }
                for (int i = 0; i < countBlock2; i++) {
                    bufferedReader.read(charArray, pos += sizeBlock2, sizeBlock2);
                }
                dataObject.setCharArrayValue(charArray);
                break;

            case 7:
                // String
                dataObject.setStringValue(bufferedReader.readLine());
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
        // 7 = String

        try {
            DataOutputStream outputStream = new DataOutputStream(socketToPartner.getOutputStream());
            // den Datentyp ermitteln
            int dataType = dataObject.getDataType();
            outputStream.writeInt(dataType);

            switch (dataType) {
            case 0:
                // short
                outputStream.writeShort(dataObject.getShortValue());
                break;

            case 1:
                // int
                outputStream.writeInt(dataObject.getIntValue());
                break;

            case 2:
                // long
                outputStream.writeLong(dataObject.getLongValue());
                break;

            case 3:
                // float
                outputStream.writeFloat(dataObject.getFloatValue());
                break;

            case 4:
                // double
                outputStream.writeDouble(dataObject.getDoubleValue());
                break;

            case 5:
                // char
                outputStream.writeChar(dataObject.getCharValue());
                break;

            case 6:
                // char array
                int arrayLength = dataObject.getCharArrayValue().length;
                System.out.println("arrayLength = " + arrayLength);
                int sizeBlock1 = 255;
                int countBlock1 = arrayLength / sizeBlock1;
                int sizeBlock2 = arrayLength % sizeBlock1;
                int countBlock2 = 1;

                PrintWriter printWriterChar = new PrintWriter(new OutputStreamWriter(socketToPartner.getOutputStream()));
                int pos = 0;
                for (int i = 0; i < countBlock1; i++) {
                    System.out.println("Write 1; i = " + i + "; pos = " + pos);
                    printWriterChar.write(dataObject.getCharArrayValue(), pos, sizeBlock1);
                    pos += sizeBlock1;
                }
                System.out.println("countBlock2 = " + countBlock2);
                System.out.println("sizeBlock2 = " + sizeBlock2);
                for (int i = 0; i < countBlock2; i++) {
                    System.out.println("Write 2; i = " + i + "; pos = " + pos);
                    printWriterChar.write(dataObject.getCharArrayValue(), pos, sizeBlock2);
                    pos += sizeBlock2;

                }
                System.out.println("String..." + String.valueOf(dataObject.getCharArrayValue()));
                CharBuffer cbuf = CharBuffer.allocate(18);
                cbuf.put(dataObject.getCharArrayValue());
                System.out.println("cbuf = " + cbuf.toString());
                printWriterChar.flush();
                break;

            case 7:
                // String
                PrintWriter printWriterString = new PrintWriter(new OutputStreamWriter(socketToPartner.getOutputStream()));
                printWriterString.write(dataObject.getStringValue());
                printWriterString.flush();
                break;

            }
        } catch (SocketTimeoutException ex) {
            throw new ActionFailedException(26L, ex);
        } catch (IOException ex) {
            throw new ActionFailedException(25L, ex);
        }
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
