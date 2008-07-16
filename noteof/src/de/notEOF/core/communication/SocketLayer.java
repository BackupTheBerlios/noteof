package de.notEOF.core.communication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

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

    public SocketLayer(Socket socketToPartner) {
        this.socketToPartner = socketToPartner;
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

    protected synchronized String readMsg() throws ActionFailedException {
        String msg = "";
        try {
            if (null == bufferedReader)
                bufferedReader = new BufferedReader(new InputStreamReader(socketToPartner.getInputStream()));
            msg = bufferedReader.readLine();
            if (!Util.isEmpty(msg)) {
                if (msg.startsWith("#")) {
                    msg = msg.substring(1);
                }
                if (msg.indexOf("#") >= 0) {
                    msg = msg.substring(0, msg.indexOf("#"));
                }
            }
        } catch (Exception ex) {
            throw new ActionFailedException(7520, ex);
        }
        if (Util.isEmpty(msg))
            msg = "";
        return msg;
    }

    protected synchronized boolean isConnected() {
        if (null == socketToPartner)
            return false;
        return socketToPartner.isConnected();
    }

    protected synchronized void writeMsg(String msg) throws ActionFailedException {
        if (null == msg)
            msg = "";
        msg = "#" + msg;
        try {
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socketToPartner.getOutputStream()));
            printWriter.println(msg);
            printWriter.flush();
        } catch (Exception ex) {
            throw new ActionFailedException(7521, ex);
        }
    }

    protected void close() {
        if (null == socketToPartner)
            return;
        try {
            if (socketToPartner.isConnected())
                socketToPartner.close();
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
}
