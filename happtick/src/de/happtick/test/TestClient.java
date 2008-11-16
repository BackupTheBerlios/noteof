package de.happtick.test;

import de.happtick.application.client.HapptickApplication;
import de.happtick.core.events.ActionEvent;
import de.happtick.core.exception.HapptickException;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFEvent;

public class TestClient extends HapptickApplication {

    public TestClient(long applicationId, String serverAddress, int serverPort, String[] args) throws HapptickException {
        super(applicationId, serverAddress, serverPort, args);

        try {
            NotEOFEvent event = new ActionEvent();
            event.addAttribute("information", "---------------- Diese Nachricht kommt vom TestClient --------------------");
            event.addAttribute("eventId", "777");
            sendEvent(event);

            Thread.sleep(10000);

        } catch (Exception e) {
        }
    }

    public static void main(String... args) throws ActionFailedException {
        new TestClient(100, "localhost", 3000, args);
    }

}
