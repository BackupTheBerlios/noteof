package de.notEOF.core.communication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import de.notEOF.core.exception.ActionFailedException;

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
    private String lineValue;
    private String fileName;
    private int dataType = -1;
    private String canonicalFileName;

    public short getShort() {
        return shortValue;
    }

    public void setShort(short shortValue) {
        setDataType(0);
        this.shortValue = shortValue;
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

    /**
     * dataTypes: <br>
     * 0 = short <br>
     * 1 = int <br>
     * 2 = long <br>
     * 3 = float <br>
     * 4 = double <br>
     * 5 = char <br>
     * 6 = char[] <br>
     * 7 = line (terminated by \n) <br>
     * 8 = file
     */
    public int getDataType() {
        return dataType;
    }

    private void setDataType(int dataType) {
        if (-1 == this.dataType)
            this.dataType = dataType;
    }
}
