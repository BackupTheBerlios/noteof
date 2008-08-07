package de.notEOF.dispatch;

import de.notEOF.core.util.Util;

/**
 * Simple Object to store connection data to another communication partner.
 * 
 * @author Dirk
 */
public class SimpleSocketData {

    private int port = 0;
    private String ip = "";

    public SimpleSocketData(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public SimpleSocketData(String ip, String port) {
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
