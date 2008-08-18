package de.happtick.core.exception;

public class HapptickException extends Exception{
    

private static final long serialVersionUID = 3414816098348131479L;
private long errNo;
private String addInfo;
private String message;

public HapptickException(Exception ex) {
    super(ex);
}

public HapptickException(long errNo, String addInfo, Exception ex) {
    super(ex);
    initMembers(errNo, addInfo);
}

public HapptickException(long errNo, String addInfo) {
    this.errNo = errNo;
    initMembers(errNo, addInfo);
}

public HapptickException(long errNo, Exception ex) {
    super(ex);
    initMembers(errNo, null);
}

private void initMembers(long errNo, String addInfo) {
    this.errNo = errNo;
    String msg = Errors.getMsg(errNo);
    if (null == msg || msg.trim().length() == 0) {
        msg = "Message not defined in Errors.class. Index of msg in code: " + errNo;
    }
    this.message = msg;
    if (null != addInfo)
        this.addInfo = addInfo;
}

/**
 * Message is a description what happened, when the Exception was thrown.
 * 
 * @return The message which is stored in class Errors. Attention - message
 *         can be null.
 */
public String getMessage() {
    return message + ": " + addInfo;
}

/**
 * Reason tells why something didn't work correctly.
 * 
 * @return
 */
public String getAddInfo() {
    return addInfo;
}

/**
 * Returns the errNo when this Object was instanciated.
 * 
 * @return A number which should be stored in the class Errors.
 */
public long getErrNo() {
    return errNo;
}
}
