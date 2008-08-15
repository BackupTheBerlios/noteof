package de.happtick.core.interfaces;

public interface ClientObserver {
    
    public void startAllowanceEvent(boolean startAllowed);
    public boolean observe();

}
