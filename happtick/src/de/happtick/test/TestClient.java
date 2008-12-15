package de.happtick.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.util.Util;

public class TestClient {// extends HapptickApplication {

    public TestClient(long applicationId, String serverAddress, int serverPort, String[] args) throws ActionFailedException {

        List<Integer> integers = new ArrayList<Integer>();
        integers.add(7);
        integers.add(2);
        integers.add(8);

        Collections.sort(integers);

        for (int zahl : integers) {
            System.out.println("Sortiert? " + zahl);
        }

        if (true)
            System.exit(0);

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

        System.out.println("Montag     = " + Calendar.MONDAY);
        System.out.println("Dienstag   = " + Calendar.TUESDAY);
        System.out.println("Mittwoch   = " + Calendar.WEDNESDAY);
        System.out.println("Donnerstag = " + Calendar.THURSDAY);
        System.out.println("Freitag    = " + Calendar.FRIDAY);
        System.out.println("Samstag    = " + Calendar.SATURDAY);
        System.out.println("Sonntag    = " + Calendar.SUNDAY);

        List<Integer> timePlanMonthdays = new ArrayList<Integer>();
        List<Integer> timePlanWeekdays = new ArrayList<Integer>();
        List<Integer> timePlanSeconds = new ArrayList<Integer>();
        List<Integer> timePlanMinutes = new ArrayList<Integer>();
        List<Integer> timePlanHours = new ArrayList<Integer>();

        timePlanSeconds.add(10);
        timePlanSeconds.add(20);
        timePlanSeconds.add(30);
        timePlanSeconds.add(40);
        timePlanMinutes.add(0);

        for (int i = 0; i < 24; i++) {
            timePlanHours.add(i);
        }

        // timePlanMonthdays.add(18);
        // timePlanMonthdays.add(16);
        // timePlanWeekdays.add(2);
        // timePlanWeekdays.add(6);
        //
        // for (int md : timePlanMonthdays) {
        // System.out.println("Monthday... " + md);
        // }
        // for (int wd : timePlanWeekdays) {
        // System.out.println("Weedday... " + wd);
        // }

        Calendar calcDate = new GregorianCalendar();
        calcDate.set(Calendar.HOUR_OF_DAY, 23);
        calcDate.set(Calendar.MINUTE, 59);
        calcDate.set(Calendar.SECOND, 15);

        formatCal("Start                                             ", calcDate);

        // ermittle den ersten gueltigen Tag, ohne die Wochentage zu
        // beruecksichtigen
        boolean timeValueFound = true;
        if (!Util.isEmpty(timePlanMonthdays)) {
            timeValueFound = timePlanMonthdays.contains(calcDate.get(Calendar.DAY_OF_MONTH));

            if (!timeValueFound) {
                for (Integer day : timePlanMonthdays) {
                    // Tag kommt noch in diesem Monat
                    if (calcDate.get(Calendar.DAY_OF_MONTH) < day) {
                        calcDate.set(Calendar.DAY_OF_MONTH, day);
                        calcDate.set(Calendar.HOUR_OF_DAY, 0);
                        calcDate.set(Calendar.MINUTE, 0);
                        calcDate.set(Calendar.SECOND, 0);
                        timeValueFound = true;
                        break;
                    }
                }
            }
            // Tag folgt in diesem Monat nicht mehr, also auf den kleinsten des
            // naechsten Monats setzen.
            if (!timeValueFound) {
                calcDate.set(Calendar.DAY_OF_MONTH, timePlanMonthdays.get(0));
                calcDate.add(Calendar.MONTH, 1);
                calcDate.set(Calendar.HOUR_OF_DAY, 0);
                calcDate.set(Calendar.MINUTE, 0);
                calcDate.set(Calendar.SECOND, 0);
            }
        }

        // pruefe, ob Wochentag passt
        if (!Util.isEmpty(timePlanWeekdays)) {
            timeValueFound = timePlanWeekdays.contains(calcDate.get(Calendar.DAY_OF_WEEK));

            // Tag passt nicht... Zeit erst mal auf 0
            if (!timeValueFound) {
                // ein anderer Tag ist es
                calcDate.set(Calendar.HOUR_OF_DAY, 0);
                calcDate.set(Calendar.MINUTE, 0);
                calcDate.set(Calendar.SECOND, 0);
            }
        }

        // Solange suchen, bis Wochentag und Tag im Monat passen...
        while (!timeValueFound) {
            calcDate.add(Calendar.DATE, 1);

            // vergleiche Wochentage
            // wenn Wochentag nicht passt direkt naechsten Tag
            if (!Util.isEmpty(timePlanWeekdays) && //
                    !timePlanWeekdays.contains(calcDate.get(Calendar.DAY_OF_WEEK)))
                continue;

            // vergleiche Tag des Monats
            timeValueFound = !Util.isEmpty(timePlanMonthdays) && //
                    timePlanMonthdays.contains(calcDate.get(Calendar.DAY_OF_MONTH));
        }

        formatCal("Pr�fung f�r Tag abgeschlossen:                " + timeValueFound, calcDate);

        // Jetzt auf Uhrzeit pruefen
        // Sekunden
        timeValueFound = false;
        for (int sec = calcDate.get(Calendar.SECOND); sec < 60; sec++) {
            if (timePlanSeconds.contains(sec)) {
                calcDate.set(Calendar.SECOND, sec);
                timeValueFound = true;
                break;
            }
        }
        if (!timeValueFound) {
            calcDate.add(Calendar.MINUTE, 1);
            calcDate.set(Calendar.SECOND, timePlanSeconds.get(0));
        }
        formatCal("Pr�fung f�r Sekunden abgeschlossen:           " + timeValueFound, calcDate);

        // Minuten
        timeValueFound = false;
        for (int minute = calcDate.get(Calendar.MINUTE); minute < 60; minute++) {
            if (timePlanMinutes.contains(minute)) {
                calcDate.set(Calendar.MINUTE, minute);
                timeValueFound = true;
                break;
            }
        }
        if (!timeValueFound) {
            calcDate.add(Calendar.HOUR_OF_DAY, 1);
            calcDate.set(Calendar.MINUTE, timePlanMinutes.get(0));
            calcDate.set(Calendar.SECOND, timePlanSeconds.get(0));
        }
        formatCal("Pr�fung f�r Minuten abgeschlossen:            " + timeValueFound, calcDate);

        // Stunden
        timeValueFound = false;
        for (int hour = calcDate.get(Calendar.HOUR_OF_DAY); hour < 24; hour++) {
            if (timePlanHours.contains(hour)) {
                calcDate.set(Calendar.HOUR_OF_DAY, hour);
                timeValueFound = true;
                break;
            }
        }
        if (!timeValueFound) {
            calcDate.add(Calendar.DATE, 1);
            calcDate.set(Calendar.HOUR_OF_DAY, timePlanHours.get(0));
            calcDate.set(Calendar.MINUTE, timePlanMinutes.get(0));
            calcDate.set(Calendar.SECOND, timePlanSeconds.get(0));
        }
        formatCal("Pr�fung f�r Stunden abgeschlossen:            " + timeValueFound, calcDate);

    }

    private void formatCal(String bla, Calendar cal) {

        String format = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.GERMAN);
        format += " der " + String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        format += "." + cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.GERMAN);
        format += "." + String.valueOf(cal.get(Calendar.YEAR));
        format += " " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
        format += ":" + String.valueOf(cal.get(Calendar.MINUTE));
        format += ":" + String.valueOf(cal.get(Calendar.SECOND));

        System.out.println(bla + "... " + format);
    }

    public static void main(String... args) throws ActionFailedException {
        new TestClient(100, "localhost", 3000, args);
    }

}
