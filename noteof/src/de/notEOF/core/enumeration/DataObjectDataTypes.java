package de.notEOF.core.enumeration;

import de.notEOF.core.communication.DataObject;

/**
 * The DataObject can store data of some different data types.
 * <p>
 * This Enum is helpful to map betwenn constants and real data types.
 * 
 * @see DataObject
 * @see DataObjectListTypes
 * @author Dirk
 * 
 */
public enum DataObjectDataTypes {

    UNKNOWN(-1), //
    SHORT(0),
    INT(1),
    LONG(2),
    FLOAT(3),
    DOUBLE(4),
    CHAR(5),
    CHAR_ARRAY(6),
    /** 7 = line (terminated by \n) */
    LINE(7),
    FILE(8),
    CONFIGURATION_VALUE(9),
    DATE(10),
    /** 11 = Map < String, String > */
    MAP_STRING_STRING(11),
    /** 12 = List < ? > */
    LIST(12);

    private int typeValue = -1;

    private DataObjectDataTypes(int typeValue) {
        this.typeValue = typeValue;
    }

    public int getTypeValue() {
        return this.typeValue;
    }

}
