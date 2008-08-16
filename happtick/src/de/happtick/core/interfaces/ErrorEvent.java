package de.happtick.core.interfaces;

import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Interface for exchange of alarm events.
 * 
 * @author Dirk
 * 
 */
public interface ErrorEvent extends NotEOFEvent {

    public void setId(Long errorId);

    public Long getId();

    public void setLevel(int level);

    public int getLevel();

    public void setDescription(String description);

    public String getDescription();
}
