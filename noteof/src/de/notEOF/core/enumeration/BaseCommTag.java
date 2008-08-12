package de.notEOF.core.enumeration;

public enum BaseCommTag {

    // Header for registration at the server
    REQ_REGISTRATION(),
    RESP_REGISTRATION(),

    REQ_TYPE_NAME(),
    RESP_TYPE_NAME(),

    REQ_SERVICE_ID(),
    RESP_SERVICE_ID(),

    REQ_SERVICE(),
    RESP_SERVICE(),

    // Header for global communication acts
    REQ_LIFE_SIGN_ACTIVATE(),
    RESP_LIFE_SIGN_ACTIVATE(),
    REQ_LIFE_SIGN(),
    RESP_LIFE_SIGN(),
    REQ_STOP(),
    RESP_STOP(),

    // Values
    VAL_OK(),
    VAL_TRUE(),
    VAL_FALSE(),
    VAL_STOP();
}
