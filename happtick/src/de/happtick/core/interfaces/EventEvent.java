package de.happtick.core.interfaces;

import de.notEOF.core.interfaces.NotEOFEvent;

public interface EventEvent extends NotEOFEvent {

    public void setId(Long eventId);

    public Long getId();

    public void setInfomation(String information);

    public String getInformation();

}
