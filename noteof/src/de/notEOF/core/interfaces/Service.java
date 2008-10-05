package de.notEOF.core.interfaces;

import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.server.Server;

public interface Service {

    public abstract Class<?> getCommunicationTagClass();

    public String getServiceId();

    public Thread getThread();

    public void setThread(Thread serviceThread);

    public boolean isRunning();

    public boolean isLifeSignSystemActive();

    public abstract void processClientMsg(Enum<?> incomingMsgEnum) throws ActionFailedException;

    public Server getServer();
}
