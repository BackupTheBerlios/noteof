package de.notEOF.core.enumeration;

public enum CommTag {

    // Header fuer Nachrichtentypen
    REQ_FINISH_STATE(),
    RESP_FINISH_STATE(),
    REQ_ERROR_CODE(),
    RESP_ERROR_CODE(),
    REQ_LOG_ERROR_CODE(),
    RESP_LOG_ERROR_CODE(),
    REQ_COMM_MODE(),
    RESP_COMM_MODE(),
    REQ_LOG_TYPE(),
    RESP_LOG_TYPE(),
    REQ_LOG_ACTION(),
    RESP_LOG_ACTION(),
    REQ_STATE_ORDINAL(),
    RESP_STATE_ORDINAL(),
    REQ_LOG_DBID,
    RESP_LOG_DBID,
    REQ_CMD_TYPE,
    RESP_CMD_TYPE,
    REQ_CMD_VALUE,
    RESP_CMD_VALUE,
    REQ_CMD_RETVAL,
    RESP_CMD_RETVAL,
    // Vereinbarte Werte
    RECEIVED(),
    RUNNING(),
    CONNECTED(),
    ACTION(),
    DENIED(),
    ALLOWED(),
    STOPPED(),
    ERROR(),
    MODE_EXTERNAL,
    MODE_INTERNAL,
    // Einleitung von komplexen Kommunikationsprozessen
    INFO_CMD(),
    INFO_FINISH(),
    INFO_LOG(),
    INFO_AWAIT_START(),
    INFO_START_ALLOWED(),
    INFO_ERROR(),
    INFO_ERROR_REQUEST(),
    INFO_START_DENIED(),
    INFO_STATE(),
    // Kommandos, um den Paul zu steuern
    CMD_STOP(),
    CMD_UNKNOWN(),
    // Stati, Hinweis auf Fehler
    COMM_LIVE_SIGN,
    COMM_UNEXPECTED_MSG(),
    COMM_IGNORE_MSG();

    private CommTag() {

    }

    /**
     * Ermittelt den Tag anhand des Strings
     */
    public static CommTag retrieveCommTag(String commTag) {
        return CommTag.valueOf(commTag);
    }
}