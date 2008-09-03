package de.notEOF.core.communication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.util.Util;

/**
 * Object to receive or send more complex Data.
 * <p>
 * The method getDataType() tells what kind of data the object stores. <br>
 * Depend to the dataType the matching function must be called. <br>
 * E.g. if dataType = 0 use the method getShort().
 * 
 * @see getDataType()
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
    private Date dateValue;
    private String lineValue;
    private String fileName;
    private Map<String, String> mapValue;
    private List<?> listValue;
    private int dataType = -1;
    private int listObjectType = -1;
    private String canonicalFileName;

    public short getShort() {
        return shortValue;
    }

    public void setShort(short shortValue) {
        setDataType(0);
        this.shortValue = shortValue;
    }

    public void setConfigurationValue(String value) {
        setDataType(9);
        setShort(Util.parseShort(value, (short) 0));
        setFloat(Util.parseFloat(value, 0));
        setLong(Util.parseLong(value, 0));
        setDouble(Util.parseDouble(value, 0));
        char[] charArray = new char[value.length()];
        for (int i = 0; i < value.length(); i++) {
            charArray[i] = value.charAt(i);
        }
        setCharArray(charArray);
        setLine(value);
    }

    /**
     * 
     * @param list
     *            List with Objects. Actual there are only Strings, Integers or
     *            Longs supported.
     */
    public void setList(List<?> list) {
        setDataType(12);
        if (null == list)
            return;
        Object obj = list.get(0);
        if (null == obj)
            return;
        this.listValue = list;
        if (obj.getClass().equals(Integer.class)) {
            setListObjectType(1);
        }
        if (obj.getClass().equals(Long.class)) {
            setListObjectType(2);
        }
        if (obj.getClass().equals(String.class)) {
            setListObjectType(7);
        }
    }

    /**
     * Returns the value as a list. <br>
     * Which object type the list elements are shows the function
     * getListObjectType().
     * 
     * @see getListObjectType().
     * 
     * @return A list containing objects.
     */
    public List<?> getList() {
        return this.listValue;
    }

    public String getConfigurationValue() {
        if (null != lineValue) {
            return lineValue;
        }
        return null;
    }

    public int getInt() {
        return intValue;
    }

    public void setInt(int intValue) {
        setDataType(1);
        this.intValue = intValue;
    }

    public long getLong() {
        return longValue;
    }

    public void setLong(long longValue) {
        setDataType(2);
        this.longValue = longValue;
    }

    public float getFloat() {
        return floatValue;
    }

    public void setFloat(float floatValue) {
        setDataType(3);
        this.floatValue = floatValue;
    }

    public double getDouble() {
        return doubleValue;
    }

    public void setDouble(double doubleValue) {
        setDataType(4);
        this.doubleValue = doubleValue;
    }

    public char getChar() {
        return charValue;
    }

    public void setChar(char charValue) {
        setDataType(5);
        this.charValue = charValue;
    }

    public char[] getCharArray() {
        return charArrayValue;
    }

    public String getCharArrayAsString() {
        return String.copyValueOf(charArrayValue);
    }

    public void setCharArray(char[] charArrayValue) {
        setDataType(6);
        this.charArrayValue = charArrayValue;
    }

    public void setCharArray(String characterString) {
        charArrayValue = new char[characterString.length()];
        for (int i = 0; i < characterString.length(); i++) {
            charArrayValue[i] = characterString.charAt(i);
        }
    }

    public String getLine() {
        return lineValue;
    }

    public void setLine(String lineValue) {
        setDataType(7);
        this.lineValue = lineValue;
    }

    public void setFileName(String fileName) {
        setDataType(8);
        this.fileName = fileName;
    }

    public void setCanonicalFileName(String canonicalFileName) {
        this.canonicalFileName = canonicalFileName;
    }

    public String getCanonicalFileName() {
        return this.canonicalFileName;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFile(String fileName) throws ActionFailedException {
        setFile(new File(fileName));
    }

    public void setFile(File inputFile) throws ActionFailedException {
        setDataType(8);
        try {
            String canonicalFileName = inputFile.getCanonicalPath();

            setFileName(inputFile.getName());
            setCanonicalFileName(canonicalFileName);

            int fileSize = (int) inputFile.length();
            if (0 == fileSize)
                throw new ActionFailedException(13L, "File: " + canonicalFileName);
            charArrayValue = new char[fileSize];
            BufferedReader bReader = new BufferedReader(new FileReader(inputFile));
            bReader.read(charArrayValue);
            bReader.close();

        } catch (IOException e) {
            throw new ActionFailedException(13L, "File: " + inputFile.getName(), e);
        }
    }

    public int getFile(String fileName, boolean overwrite) throws ActionFailedException {
        return getFile(new File(fileName), overwrite);
    }

    public int getFile(File outputFile, boolean overwrite) throws ActionFailedException {
        try {
            String canonicalFileName = outputFile.getCanonicalPath();
            if (outputFile.exists() && !overwrite)
                throw new ActionFailedException(15L, "File: " + canonicalFileName);

            if (outputFile.exists()) {
                outputFile.delete();
            }

            outputFile.createNewFile();
            BufferedWriter bWriter = new BufferedWriter(new FileWriter(outputFile));
            bWriter.write(this.charArrayValue);
            bWriter.flush();
            bWriter.close();

        } catch (IOException e) {
            throw new ActionFailedException(14L, "File: " + outputFile.getName(), e);
        }

        return (int) outputFile.length();
    }

    public void setDate(Date dateValue) {
        setDataType(10);
        this.dateValue = dateValue;
    }

    public Date getDate() {
        return dateValue;
    }

    public Map<String, String> getMap() {
        return this.mapValue;
    }

    public void setMap(Map<String, String> mapValue) {
        setDataType(11);
        this.mapValue = mapValue;
    }

    /**
     * dataTypes: <br>
     * 00 = short <br>
     * 01 = int <br>
     * 02 = long <br>
     * 03 = float <br>
     * 04 = double <br>
     * 05 = char <br>
     * 06 = char[] <br>
     * 07 = line (terminated by \n) <br>
     * 08 = file <br>
     * 09 = configurationValue <br>
     * 10 = Date <br>
     * 11 = Map<String, String> <br>
     * 12 = List<?> <br>
     */
    public int getDataType() {
        return dataType;
    }

    private void setDataType(int dataType) {
        if (-1 == this.dataType) {
            this.dataType = dataType;
        }
    }

    /**
     * object Types in list: <br>
     * 01 = Integer <br>
     * 02 = Long <br>
     * 07 = String <br>
     * 
     * @return
     */
    public int getListObjectType() {
        return listObjectType;
    }

    private void setListObjectType(int listObjectType) {
        this.listObjectType = listObjectType;
    }
}
