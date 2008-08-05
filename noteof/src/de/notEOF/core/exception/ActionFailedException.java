package de.notEOF.core.exception;

/**
 * The standard Exception of this framework. <br>
 * Normally an ActionFailedException should be thrown when something undesirable
 * occured. Advantage of using ActionFailedException: The logging can be done in
 * an uniform way.
 * 
 * @author Dirk
 * 
 */
public class ActionFailedException extends Exception {

    private static final long serialVersionUID = 1L;
    private long errNo;
    private String addInfo;
    private String message;

    // public ActionFailedException(String message, String reason) {
    // super(message);
    // this.reason = reason;
    // }
    //
    public ActionFailedException(long errNo, String addInfo, Exception ex) {
        super(ex);
        initMembers(errNo, addInfo);
    }

    public ActionFailedException(long errNo, String addInfo) {
        this.errNo = errNo;
        initMembers(errNo, addInfo);
    }

    public ActionFailedException(long errNo, Exception ex) {
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
