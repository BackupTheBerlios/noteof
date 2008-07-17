package de.notEOF.core.communication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import de.notEOF.core.constants.NotEOFConstants;
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

    public SocketLayer(Socket socketToPartner) {
        this.socketToPartner = socketToPartner;
        lifeTimer = new LifeTimer();
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
    protected synchronized String readMsg() throws ActionFailedException {
        String msg = BaseCommTag.REQ_LIFE_SIGN.name();
        while (BaseCommTag.REQ_LIFE_SIGN.name().equals(msg)) {
            msg = readUnqualifiedMsg();
            if (BaseCommTag.REQ_LIFE_SIGN.name().equals(msg)) {
                responseToPartner(BaseCommTag.RESP_LIFE_SIGN.name(), BaseCommTag.VAL_OK.name());
            }
        }
        return msg;
    }

    private synchronized String readUnqualifiedMsg() throws ActionFailedException {
        String msg = "";
        try {
            if (null == bufferedReader)
                bufferedReader = new BufferedReader(new InputStreamReader(socketToPartner.getInputStream()));
            msg = bufferedReader.readLine();
            if (!Util.isEmpty(msg)) {
                if (msg.startsWith("#")) {
                    msg = msg.substring(1);
                }
                // if (msg.indexOf("#") >= 0) {
                // msg = msg.substring(0, msg.indexOf("#"));
                // }
            }
        } catch (Exception ex) {
            throw new ActionFailedException(7520, ex);
        }
        if (Util.isEmpty(msg))
            msg = "";

        // show lifetimer that the connection is ok
        lifeTimer.lifeSignReceived();

        return msg;
    }

    protected synchronized boolean isConnected() {
        if (null == socketToPartner)
            return false;
        return socketToPartner.isConnected();
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
        } catch (Exception ex) {
            throw new ActionFailedException(7521, ex);
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

    /**
     * Activates the integrated system to send periodical signs to the
     * communication partner. Normally this should used only by client
     * implementations, not by a service.
     */
    public void activateLifeSignSystem(Boolean asClient) {
        if (null != asClient && asClient)
            lifeTimer.useAsClient();
        Thread threadLifeTimer = new Thread(lifeTimer);
        threadLifeTimer.start();
    }

    /**
     * Marks if the lifesigns between client or service are not be sent just in
     * time or if one of them has sent a negative response.
     * 
     * @return false if the service don't wants to stop the client.
     */
    public boolean stoppedByService() {
        return lifeTimer.stoppedByService();
    }

    /*
     * Frequently the SocketLayer class sends or awaits lifesigns from or to the
     * partner.
     */
    private class LifeTimer implements Runnable {

        // timeout for using as client
        private long nextLifeSignToSend;
        private boolean stoppedByService = false;
        private boolean thisIsAClient = false;

        // timeout for using as service
        private long nextLifeSignAwaited = 0;
        private boolean stopped = false;

        // Marks if the communication partner told something like 'stop' when
        // receiving the lifesign or whether the partner didn't answer.
        public boolean stoppedByService() {
            return stoppedByService;
        }

        protected boolean isRunning() {
            return !stopped;
        }

        protected void stop() {
            stopped = true;
        }

        // LifeTimer used for client implementation
        private void updateNextLifeSign() {
            nextLifeSignToSend = System.currentTimeMillis() + NotEOFConstants.LIFE_TIME_INTERVAL_CLIENT;
        }

        private void useAsClient() {
            thisIsAClient = true;
        }

        // LifeTimer used for service implementation
        private void checkLastLifeSign() {
            // > 0 means that client has already connected and meanwhile sent
            // one or more messages
            if (nextLifeSignAwaited > 0 && nextLifeSignAwaited < System.currentTimeMillis()) {
                // lifesign not in time
                stopped = true;
                try {
                    socketToPartner.close();
                } catch (Exception ex) {
                    // nothing to do
                }
            }
        }

        protected void lifeSignReceived() {
            nextLifeSignAwaited = System.currentTimeMillis() + NotEOFConstants.LIFE_TIME_INTERVAL_SERVICE;
        }

        public void run() {
            while (!stopped) {
                try {
                    Thread.sleep(1000);
                    if (thisIsAClient) {
                        sendLifeSign();
                    } else {
                        checkLastLifeSign();
                    }
                } catch (InterruptedException iex) {
                    stopped = true;
                } catch (Exception ix) {
                }
            }
        }

        /*
         * Send a special request to server. This messages is a hint for the
         * service that it's client is alive. The response from the service also
         * shows that the service is alive to. Additional this communication act
         * is used to find out if the service wants the client to stop.
         */
        protected synchronized void sendLifeSign() {
            if (isConnected() && nextLifeSignToSend < System.currentTimeMillis()) {
                int oldTimeOut = 0;
                try {
                    oldTimeOut = socketToPartner.getSoTimeout();
                    socketToPartner.setSoTimeout(5000);
                    writeMsg(BaseCommTag.REQ_LIFE_SIGN.name());
                    String msg = readUnqualifiedMsg();
                    if (BaseCommTag.REQ_LIFE_SIGN.name().indexOf(msg) >= 0) {
                        // OK - awaited response received
                        lifeSignReceived();
                    } else {
                        // partner doesn't send the expected message
                        stopped = true;
                    }
                } catch (Exception ex) {
                    stopped = true;
                }

                try {
                    if (stopped) {
                        socketToPartner.close();
                    } else {
                        socketToPartner.setSoTimeout(oldTimeOut);
                    }
                } catch (Exception e) {
                    // nothing to do
                }
            }
        }
    }
}
