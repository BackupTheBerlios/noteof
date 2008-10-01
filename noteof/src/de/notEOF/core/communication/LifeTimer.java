package de.notEOF.core.communication;

import java.net.SocketException;

import de.notEOF.core.constant.NotEOFConstants;
import de.notEOF.core.enumeration.BaseCommTag;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.logging.LocalLog;

/*
 * Frequently the SocketLayer class sends or awaits lifesigns from or to the partner.
 */
public class LifeTimer implements Runnable {

    // timeout for using as client
    private SocketLayer socketLayer;
    private long nextLifeSignToSend;
    private boolean thisIsAClient = false;
    private boolean lifeSignSucceeded = false;
    private boolean lifeSignSystemActivated = false;
    private boolean closedByLifeSignSystem = false;

    // timeout for using as service
    private long nextLifeSignAwaited = 0;
    private boolean stopped = false;

    protected LifeTimer(SocketLayer socketLayer) {
        this.socketLayer = socketLayer;
    }

    public boolean lifeSignSucceeded() {
        return lifeSignSucceeded;
    }

    protected boolean closedByLifeSignSystem() {
        return closedByLifeSignSystem;
    }

    protected boolean isRunning() {
        return !stopped;
    }

    protected void stop() {
        stopped = true;
    }

    protected boolean lifeSignSystemActivated() {
        return lifeSignSystemActivated;
    }

    // LifeTimer used for client implementation
    protected void updateNextLifeSign() {
        nextLifeSignToSend = System.currentTimeMillis() + NotEOFConstants.LIFE_TIME_INTERVAL_CLIENT;
    }

    protected void useAsClient() {
        thisIsAClient = true;
    }

    // LifeTimer used for service implementation
    private void checkLastLifeSign() {
        // > 0 means that client has already connected and meanwhile sent
        // one or more messages and the lifesign system is activated
        if (nextLifeSignAwaited > 0 && nextLifeSignAwaited < System.currentTimeMillis()) {
            // lifesign not in time
            System.out.println("Kein lifesign erhalten");
            lifeSignSucceeded = true;
            stopped = true;
            try {
                socketLayer.close();
            } catch (Exception ex) {
                // nothing to do
            }
        }
    }

    protected void lifeSignReceived() {
        nextLifeSignAwaited = System.currentTimeMillis() + NotEOFConstants.LIFE_TIME_INTERVAL_SERVICE;
    }

    public void run() {
        lifeSignSystemActivated = true;

        while (!stopped) {
            try {
                Thread.sleep(1000);
                if (thisIsAClient) {
                    sendLifeSign();
                } else {
                    checkLastLifeSign();
                }
            } catch (InterruptedException iex) {
                System.out.println("Fehler bei lifesignüberwachung: " + iex);
                stopped = true;
            }
        }
    }

    /*
     * Send a special request to server. This messages is a hint for the service
     * that it's client is alive. The response from the service also shows that
     * the service is alive to. Additional this communication act in a future
     * version can be used to find out if the service wants the client to stop.
     */
    protected synchronized void sendLifeSign() {
        if (socketLayer.isConnected() && nextLifeSignToSend < System.currentTimeMillis()) {
            int oldTimeOut = 0;
            try {
                oldTimeOut = socketLayer.getSocketToPartner().getSoTimeout();
                socketLayer.getSocketToPartner().setSoTimeout(5000);
                socketLayer.writeMsg(BaseCommTag.REQ_LIFE_SIGN.name());
                // exactly one read - the response should be
                // BaseCommTag.RESP_LIFE_SIGN
                // if not the server has ignored the REQ_LIFE_SIGN message
                // in this version this circumstance is ignored
                LocalLog.info("LifeTimer sendLifeSign");
                socketLayer.readMsg(true);
            } catch (ActionFailedException afx) {
                LocalLog.error("Senden des LifeSigns. Verbindung zu Service wird unterbrochen.", afx);
                stopped = true;
            } catch (SocketException sx) {
                LocalLog.error("Senden des LifeSigns. Verbindung zu Service wird unterbrochen.", sx);
                stopped = true;
            }

            try {
                // error case
                if (stopped) {
                    closedByLifeSignSystem = true;
                    socketLayer.close();
                } else {
                    // ok - timeout reset to original value
                    socketLayer.getSocketToPartner().setSoTimeout(oldTimeOut);
                }
            } catch (Exception e) {
                // nothing to do
            }
        }
    }
}
