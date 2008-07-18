package de.notEOF.core.interfaces;

import java.net.Socket;

import de.notEOF.core.exception.ActionFailedException;

public interface Service extends Runnable{

    public abstract Class<?> getCommunicationTagClass();
    public abstract void processMsg(Enum<?> incomingMsgEnum) throws ActionFailedException;
    public void init(Socket socketToClient, String serviceId) throws ActionFailedException;

}
