package de.happtick.core.interfaces;

import de.notEOF.core.interfaces.NotEOFEvent;

public interface LogEvent extends NotEOFEvent {

    public void setText(String logText);

    public String getText();
}
