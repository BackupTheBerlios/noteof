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

import de.notEOF.core.enumeration.DataObjectDataTypes;
import de.notEOF.core.enumeration.DataObjectListTypes;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.util.Util;

/**
 * Object to receive or send more complex Data.
 * <p>
 * The DataObject is build to only transfer one type of data. The method
 * getDataType() tells what kind of data the object stores. <br>
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
    private DataObjectDataTypes dataType = DataObjectDataTypes.UNKNOWN;
    private DataObjectListTypes listObjectType = DataObjectListTypes.UNKNOWN;
    private String canonicalFileName;

    public short getShort() {
        return shortValue;
    }

    public void setShort(short shortValue) {
        setDataType(DataObjectDataTypes.SHORT);
        this.shortValue = shortValue;
    }

    public void setConfigurationValue(String value) {
        setDataType(DataObjectDataTypes.CONFIGURATION_VALUE);
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
        setDataType(DataObjectDataTypes.LIST);
        if (null == list)
            return;
        Object obj = list.get(0);
        if (null == obj)
            return;
        this.listValue = list;
        if (obj.getClass().equals(Integer.class)) {
            setListObjectType(DataObjectListTypes.INTEGER);
        }
        if (obj.getClass().equals(Long.class)) {
            setListObjectType(DataObjectListTypes.LONG);
        }
        if (obj.getClass().equals(String.class)) {
            setListObjectType(DataObjectListTypes.STRING);
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
        setDataType(DataObjectDataTypes.INT);
        this.intValue = intValue;
    }

    public long getLong() {
        return longValue;
    }

    public void setLong(long longValue) {
        setDataType(DataObjectDataTypes.LONG);
        this.longValue = longValue;
    }

    public float getFloat() {
        return floatValue;
    }

    public void setFloat(float floatValue) {
        setDataType(DataObjectDataTypes.FLOAT);
        this.floatValue = floatValue;
    }

    public double getDouble() {
        return doubleValue;
    }

    public void setDouble(double doubleValue) {
        setDataType(DataObjectDataTypes.DOUBLE);
        this.doubleValue = doubleValue;
    }

    public char getChar() {
        return charValue;
    }

    public void setChar(char charValue) {
        setDataType(DataObjectDataTypes.CHAR);
        this.charValue = charValue;
    }

    public char[] getCharArray() {
        return charArrayValue;
    }

    public String getCharArrayAsString() {
        return String.copyValueOf(charArrayValue);
    }

    public void setCharArray(char[] charArrayValue) {
        setDataType(DataObjectDataTypes.CHAR_ARRAY);
        this.charArrayValue = charArrayValue;
    }

    public void setCharArray(String characterString) {
        setDataType(DataObjectDataTypes.CHAR_ARRAY);
        charArrayValue = characterString.toCharArray();
        // charArrayValue = new char[characterString.length()];
        // for (int i = 0; i < characterString.length(); i++) {
        // charArrayValue[i] = characterString.charAt(i);
        // }
    }

    public String getLongText() {
        if (!Util.isEmpty(charArrayValue))
            return String.valueOf(charArrayValue);
        return "";
    }

    public void setLongText(String longText) {
        setDataType(DataObjectDataTypes.LONGTEXT);
        if (Util.isEmpty(longText))
            return;
        charArrayValue = longText.toCharArray();
    }

    public String getLine() {
        return lineValue;
    }

    public void setLine(String lineValue) {
        setDataType(DataObjectDataTypes.LINE);
        this.lineValue = lineValue;
    }

    public void setFileName(String fileName) {
        setDataType(DataObjectDataTypes.FILE);
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
        setDataType(DataObjectDataTypes.FILE);
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
        setDataType(DataObjectDataTypes.DATE);
        this.dateValue = dateValue;
    }

    public Date getDate() {
        return dateValue;
    }

    public Map<String, String> getMap() {
        return this.mapValue;
    }

    public void setMap(Map<String, String> mapValue) {
        setDataType(DataObjectDataTypes.MAP_STRING_STRING);
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
     * 13 = Long Text <br>
     */
    public DataObjectDataTypes getDataType() {
        return dataType;
    }

    private void setDataType(DataObjectDataTypes dataType) {
        if (DataObjectDataTypes.UNKNOWN == this.dataType) {
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
    public DataObjectListTypes getListObjectType() {
        return listObjectType;
    }

    private void setListObjectType(DataObjectListTypes listObjectType) {
        this.listObjectType = listObjectType;
    }
}
