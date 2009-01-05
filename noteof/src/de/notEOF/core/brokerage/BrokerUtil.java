package de.notEOF.core.brokerage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.Util;

public class BrokerUtil {
    private static String queuePath = "";
    private static Long fileCounter;

    private static final String FILE_PREFIX = "e_";
    private static final String FILE_SUFFIX = ".xml";
    private static final String FILE_TIMESTAMP = "ts_";
    private static final String FILE_EVENT_TIMESTAMP = "es_";
    private static final String FILE_COUNTER = "c_";

    protected static String getQueuePath() {
        if (Util.isEmpty(queuePath)) {
            NotEOFConfiguration conf = new LocalConfiguration();
            try {
                File thePath = new File(conf.getAttribute("brokerage.Queue", "path"));
                if (thePath.isDirectory()) {
                    queuePath = thePath.getCanonicalPath();
                } else {
                    LocalLog.error("Achtung! Queue Pfad ist nicht gueltig oder es handelt sich um eine Datei.");
                }
            } catch (ActionFailedException e) {
                LocalLog.error("Achtung! Queue Pfad konnte nicht ermittelt werden. Events werden nicht in die Queue geschrieben.", e);
            } catch (IOException e) {
                LocalLog.error("Achtung! Queue Pfad konnte nicht ermittelt werden. Events werden nicht in die Queue geschrieben.", e);
            }
        }
        return queuePath;
    }

    protected static long getLastFileCounter() {
        long lastCounter = 0;
        if (null == fileCounter) {
            fileCounter = new Long(0);

            // filenames: e_ts_<timestamp>es_<event timestamp>c_<counter>.xml
            try {
                List<String> files = getQueueFileNames();
                if (!Util.isEmpty(files)) {
                    for (String fileName : files) {
                        if (fileName.startsWith("e_ts_") && fileName.contains("c_") && fileName.endsWith("xml")) {
                            // ok - queued event file
                            long counter = extractFileCounter(fileName);
                            if (counter > lastCounter) {
                                lastCounter = counter;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LocalLog.warn("FileCounter konnte nicht ermittelt werden. Beginne mit 0");
            }
        }
        return fileCounter;
    }

    // TODO noch offen
    public void removeEventFromQueue(String fileName) {

    }

    protected static List<String> getQueueFileNames() {
        List<String> fileNames = new ArrayList<String>();
        try {
            File pathFiles = new File(getQueuePath());
            String[] files = pathFiles.list();
            for (String file : files) {
                fileNames.add(file);
            }
            return fileNames;
        } catch (Exception e) {
            LocalLog.warn("FileCounter konnte nicht ermittelt werden. Beginne mit 0");
            return null;
        }
    }

    protected static List<File> getQueueFiles() {
        List<File> files = new ArrayList<File>();
        List<String> fileNames = getQueueFileNames();
        if (!Util.isEmpty(fileNames)) {
            for (String fileName : fileNames) {
                File eventFile = new File(getQueuePath() + "/" + fileName);
                files.add(eventFile);
            }
        }
        return files;
    }

    protected static long getNextFileCounter() {
        long counter = getLastFileCounter();
        fileCounter = counter;
        return fileCounter++;
    }

    protected static String createFileName(NotEOFEvent event) {
        return FILE_PREFIX + FILE_TIMESTAMP + new Date().getTime() + FILE_EVENT_TIMESTAMP + event.getTimeStampSend() + FILE_COUNTER + getNextFileCounter()
                + FILE_SUFFIX;
    }

    /**
     * Delivers the counter within the file name.
     * 
     * @param fileName
     * @return
     */
    protected static long extractFileCounter(String fileName) {
        String counter = extractFilePart(fileName, FILE_COUNTER, FILE_SUFFIX);
        return Util.parseLong(counter, 0);
    }

    /**
     * Delivers the file timestamp of the file name.
     * 
     * @param fileName
     * @return
     */
    protected static long extractFileTimeStamp(String fileName) {
        String timeStamp = extractFilePart(fileName, FILE_TIMESTAMP, FILE_EVENT_TIMESTAMP);
        return Util.parseLong(timeStamp, 0);
    }

    /*
     * Liefert den zwischen den beiden Zeichenketten befindlichen Teil
     */
    private static String extractFilePart(String fileName, String beginStr, String endStr) {
        int pos = fileName.indexOf(endStr);
        fileName = fileName.substring(0, pos);
        pos = fileName.indexOf(beginStr);
        return fileName.substring(pos + beginStr.length());
    }

}
