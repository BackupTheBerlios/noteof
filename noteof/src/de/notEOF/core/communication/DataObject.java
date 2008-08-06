package de.notEOF.core.communication;

/**
 * Object to receive or send more complex Data.
 * <p>
 * The method getDataType() tells what kind of data the object stores. <br>
 * Depend to the dataType the matching function must be called. <br>
 * E.g. if dataType = 0 use the method getShort().
 * <p>
 * dataTypes: <br>
 * 0 = short <br>
 * 1 = int <br>
 * 2 = long <br>
 * 3 = float <br>
 * 4 = double <br>
 * 5 = char <br>
 * 6 = char[] <br>
 * 7 = String <br>
 * 
 * @author Dirk
 * 
 */
public class DataObject {
    private short shortValue;
    private int intValue;
    private long longValue;
    private float floatValue;
    private double doubleValue;
    private char charValue;
    private char[] charArrayValue;
    private String stringValue;
    private int dataType = -1;

    public short getShortValue() {
        return shortValue;
    }

    public void setShortValue(short shortValue) {
        this.dataType = 0;
        this.shortValue = shortValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.dataType = 1;
        this.intValue = intValue;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.dataType = 2;
        this.longValue = longValue;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(float floatValue) {
        this.dataType = 3;
        this.floatValue = floatValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(double doubleValue) {
        this.dataType = 4;
        this.doubleValue = doubleValue;
    }

    public char getCharValue() {
        return charValue;
    }

    public void setCharValue(char charValue) {
        this.dataType = 5;
        this.charValue = charValue;
    }

    public char[] getCharArrayValue() {
        return charArrayValue;
    }

    public void setCharArrayValue(char[] charArrayValue) {
        this.dataType = 6;
        this.charArrayValue = charArrayValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.dataType = 7;
        this.stringValue = stringValue;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public int getDataType() {
        return dataType;
    }

}
