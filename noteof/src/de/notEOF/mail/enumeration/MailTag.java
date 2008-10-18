package de.notEOF.mail.enumeration;

public enum MailTag {

    // Header for registration at the server
    REQ_READY_FOR_MAIL, //
    RESP_READY_FOR_MAIL, //

    REQ_AWAITING_MAIL,
    RESP_AWAITING_MAIL,

    REQ_READY_FOR_ACTION,
    RESP_READY_FOR_ACTION,

    REQ_ACTION_TYPE,
    RESP_ACTION_TYPE,

    REQ_READY_FOR_EXPRESSIONS,
    RESP_READY_FOR_EXPRESSIONS,

    REQ_READY_FOR_EVENT,
    RESP_READY_FOR_EVENT,

    REQ_EXPRESSION_TYPE,
    RESP_EXPRESSION_TYPE,

    VAL_EXPRESSION_TYPE_DESTINATIONS,
    VAL_EXPRESSION_TYPE_HEADERS,

    REQ_MAIL_ENVELOPE,
    RESP_MAIL_ENVELOPE,

    REQ_BODY_TEXT,
    RESP_BODY_TEXT,

    REQ_BODY_DATA_EXISTS,
    RESP_BODY_DATA_EXISTS,

    REQ_BODY_DATA,
    RESP_BODY_DATA,

    VAL_OK,
    VAL_FAULT,

    VAL_ACTION_MAIL,
    VAL_ACTION_EVENT;
}
