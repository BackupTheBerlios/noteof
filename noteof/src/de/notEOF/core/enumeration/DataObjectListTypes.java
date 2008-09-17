package de.notEOF.core.enumeration;

import de.notEOF.core.communication.DataObject;

/**
 * The DataObject can store Data of type List<?>. <br>
 * The method DataObject.getListObjectType() delivers int values representing
 * the data type in the list. <br>
 * The constant values here can be helpfull to map the objectTypes.
 * 
 * @author Dirk
 * @see DataObject
 * @see DataObjectDataTypes
 */
public enum DataObjectListTypes {

    UNKNOWN(-1), //
    INTEGER(0),
    LONG(1),
    STRING(7);

    private int objectType = -1;

    private DataObjectListTypes(int objectType) {
        this.objectType = objectType;
    }

    public int getObjectType() {
        return this.objectType;
    }

}
