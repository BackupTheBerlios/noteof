package de.notEOF.core.interfaces;

import de.notEOF.core.exception.ActionFailedException;

public interface Service {

    public abstract Class<?> getCommunicationTagClass();

    public String getServiceId();

    public Thread getThread();

    public void setThread(Thread serviceThread);

    public boolean isRunning();
    
    public boolean isLifeSignSystemActive();

    public abstract void processMsg(Enum<?> incomingMsgEnum) throws ActionFailedException;

    // public void initializeConnection(Socket socketToClient, String serviceId)
    // throws ActionFailedException;

}
