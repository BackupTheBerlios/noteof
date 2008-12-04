package de.notEOF.core.interfaces;

import de.notEOF.core.communication.TalkLine;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.mail.NotEOFMail;

public interface NotEOFClient {

    public void connect(String ip, int port, TimeOut timeout) throws ActionFailedException;

    public void close() throws ActionFailedException;

    public void sendEvent(NotEOFEvent event) throws ActionFailedException;

    public String getClientNetId();

    public boolean isLinkedToService();

    public void sendMail(NotEOFMail mail) throws ActionFailedException;

    public String getServerAddress();

    public int getServerPort();

    public String serviceForClientByName();

    public TalkLine getTalkLine();

}
