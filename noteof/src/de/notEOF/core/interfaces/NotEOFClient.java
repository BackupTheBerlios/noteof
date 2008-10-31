package de.notEOF.core.interfaces;

import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.mail.NotEOFMail;

public interface NotEOFClient {

    public void connect(String ip, int port, TimeOut timeout) throws ActionFailedException;

    public void sendEvent(NotEOFEvent event) throws ActionFailedException;

    public String getClientNetId();

    public void sendMail(NotEOFMail mail) throws ActionFailedException;

    public void stop() throws ActionFailedException;

    public void stop(int exitCode) throws ActionFailedException;
}
