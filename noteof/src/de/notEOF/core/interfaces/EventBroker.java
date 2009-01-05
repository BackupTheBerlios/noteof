package de.notEOF.core.interfaces;

public interface EventBroker {

    public abstract void postEvent(Service service, NotEOFEvent event);
}
