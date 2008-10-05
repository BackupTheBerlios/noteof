package de.notEOF.mail.enumeration;

public enum MailTag {

    // Header for registration at the server
    REQ_READY_FOR_MAIL, //
    RESP_READY_FOR_MAIL, //

    REQ_MAIL_ENVELOPE,
    RESP_MAIL_ENVELOPE,

    REQ_BODY_TEXT,
    RESP_BODY_TEXT,

    REQ_BODY_DATA_EXISTS,
    RESP_BODY_DATA_EXISTS,

    REQ_BODY_DATA,
    RESP_BODY_DATA;
}
