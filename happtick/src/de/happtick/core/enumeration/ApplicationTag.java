package de.happtick.core.enumeration;

public enum ApplicationTag {

    PROCESS_APPLICATION_ID, //
    PROCESS_START_ID,
    PROCESS_START_ALLOWANCE,
    PROCESS_NEW_LOG,
    PROCESS_NEW_ALARM,
    PROCESS_NEW_EVENT,
    PROCESS_NEW_ERROR,
    PROCESS_STOP_EVENT,
    PROCESS_START_WORK_EVENT,

    REQ_APPLICATION_ID,
    RESP_APPLICATION_ID,

    REQ_START_ID,
    RESP_START_ID,

    REQ_START_ALLOWED,
    RESP_START_ALLOWED,

    REQ_ERROR_ID,
    RESP_ERROR_ID,
    REQ_ERROR_LEVEL,
    RESP_ERROR_LEVEL,
    REQ_ERROR_TEXT,
    RESP_ERROR_TEXT,

    REQ_EVENT_ID,
    RESP_EVENT_ID,
    REQ_EVENT_TEXT,
    RESP_EVENT_TEXT,

    REQ_ALARM_TYPE,
    RESP_ALARM_TYPE,
    REQ_ALARM_LEVEL,
    RESP_ALARM_LEVEL,
    REQ_ALARM_TEXT,
    RESP_ALARM_TEXT,

    REQ_LOG_TEXT,
    RESP_LOG_TEXT,

    REQ_EXIT_CODE,
    RESP_EXIT_CODE,

    INFO_TRUE,
    INFO_FALSE,

    REQ_SAMPLE_TAG,
    RESP_SAMPLE_TAG;

}
