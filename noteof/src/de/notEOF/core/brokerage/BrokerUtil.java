package de.notEOF.core.brokerage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.Util;

public class BrokerUtil {
    private static String queuePath = "";
    private static int maxFiles = 1000;

    private static final String FILE_PREFIX = "e_";
    private static final String FILE_SUFFIX = ".xml";
    private static final String FILE_QUEUEID = "qi_";
    private static final String FILE_EVENT_TIMESTAMP = "es_";

    static {
        try {
            NotEOFConfiguration conf = new LocalConfiguration();
            maxFiles = conf.getAttributeInt("brokerage.Queue", "maxFiles", 1000);
        } catch (ActionFailedException e) {
            LocalLog.warn("Fehler bei Ermittlung der max. Anzahl Speicherelemente im EventQueReader.", e);
        }
        new Thread(new FileCleaner()).start();
    }

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

    private static class FileCleaner implements Runnable {

        /*
         * Kill too much files
         */
        private void reduceFiles() {
            // not check too often - costs time
            if (null == getQueueFileNames()) {
                return;
            }
            List<String> fileNames = getQueueFileNames();
            Collections.sort(fileNames);
            int performanceCounter = 0;
            // immer nur 10 stck loeschen wg. performance
            while (fileNames.size() > maxFiles && performanceCounter++ <= 50) {
                File file = new File(getQueuePath() + "/" + fileNames.remove(0));
                if (file.isFile()) {
                    file.delete();
                }
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // cleaning is not as important as performance
                    Thread.sleep(5000);
                    reduceFiles();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private static List<String> getQueueFileNames() {
        List<String> fileNames = new ArrayList<String>();
        try {
            File pathFiles = new File(getQueuePath());
            String[] files = pathFiles.list();
            for (String file : files) {
                fileNames.add(file);
            }
            Collections.sort(fileNames);
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
                if (eventFile.isFile())
                    files.add(eventFile);
            }
        }
        return files;
    }

    protected static String createFileName(NotEOFEvent event) {
        return FILE_PREFIX + FILE_QUEUEID + event.getQueueId() + FILE_EVENT_TIMESTAMP + event.getTimeStampSend() + FILE_SUFFIX;
    }

    /**
     * Delivers the queue id of the file name.
     * 
     * @param fileName
     * @return
     */
    protected static long extractFileQueueId(String fileName) {
        String timeStamp = extractFilePart(fileName, FILE_QUEUEID, FILE_EVENT_TIMESTAMP);
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
