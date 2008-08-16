package de.notEOF.core.interfaces;

public interface ServiceChangeEvent extends NotEOFEvent {

    public void setService(Service service);

    public Service getService();
}
