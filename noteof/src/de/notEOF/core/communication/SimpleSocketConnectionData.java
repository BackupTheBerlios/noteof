package de.notEOF.core.communication;

import de.notEOF.core.util.Util;

/**
 * Simple Object to store connection data.
 * 
 * @author Dirk
 */
public class SimpleSocketConnectionData {

    private int port = 0;
    private String ip = "";

    public SimpleSocketConnectionData(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public SimpleSocketConnectionData(String ip, String port) {
        this(ip, Util.parseInt(port, 0));
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getPortString() {
        return String.valueOf(port);
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }
}
