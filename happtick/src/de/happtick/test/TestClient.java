package de.happtick.test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import de.happtick.core.exception.HapptickException;
import de.notEOF.core.exception.ActionFailedException;

public class TestClient {// extends HapptickApplication {

    public TestClient(long applicationId, String serverAddress, int serverPort, String[] args) throws HapptickException {
        // super(applicationId, serverAddress, serverPort, args);

        // try {
        // NotEOFEvent event = new ActionEvent();
        // event.addAttribute("information",
        // "---------------- Diese Nachricht kommt vom TestClient --------------------"
        // );
        // event.addAttribute("eventId", "777");
        // sendEvent(event);
        //
        // Thread.sleep(10000);
        //
        // } catch (Exception e) {
        // }

        Calendar cal = new GregorianCalendar();
        System.out.println("Heute: " + formatCal(cal));

        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        System.out.println("Gesetzt auf Montag: " + formatCal(cal));

        cal.set(Calendar.DAY_OF_MONTH, 18);
        System.out.println("Gesetzt auf den 18.: " + formatCal(cal));

    }

    private String formatCal(Calendar cal) {
        String format = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        format += "." + cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.GERMAN);
        format += "." + String.valueOf(cal.get(Calendar.YEAR));
        format += " " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
        format += ":" + String.valueOf(cal.get(Calendar.MINUTE));
        format += ":" + String.valueOf(cal.get(Calendar.SECOND));
        return format;
    }

    public static void main(String... args) throws ActionFailedException {
        new TestClient(100, "localhost", 3000, args);
    }

}
