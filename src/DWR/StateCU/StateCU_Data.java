package DWR.StateCU;

/**
This StateCU_Data class can be used as a base class for StateCU data objects.
Common data like identifier and name is maintained here to simplify access and remove redundant code.
*/
public abstract class StateCU_Data 
implements Cloneable, Comparable<StateCU_Data> {

protected String _id = StateCU_Util.MISSING_STRING;
protected String _name = StateCU_Util.MISSING_STRING;
protected boolean _is_dirty = false;

/**
Whether this object is a clone (i.e., data that can be cancelled out of).
*/
protected boolean _isClone = false;

/**
For screens that can cancel changes, this stores the original values.
*/
protected Object _original;

/**
Construct the object and set values to empty strings.
*/
public StateCU_Data ()
{
}

/**
Clones this object.  
@return a clone of this object.
*/
public Object clone() {
	StateCU_Data data = null;
	try {
		data = (StateCU_Data)super.clone();
	}
	catch (CloneNotSupportedException e) {
		// should never happen.
	}

	// dataset is not cloned -- the same reference is used.
	data._isClone = true;

	return data;
}

/**
Compares this object to another StateCU_Data object based on _id and _name, in that order.
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other object, or -1 if it is less.
*/
public int compareTo (StateCU_Data data) {
	String name = data.getName();
	String id = data.getID();

	int res = _id.compareTo(id);
	if (res != 0) {
		return res;
	}
	
	res = _name.compareTo(name);
	if (res != 0) {
		return res;
	}
	// The same...
	return 0;
}

/**
Finalize before garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize()
throws Throwable
{	_id = null;
	_name = null;
	super.finalize();
}

/**
Return the identifier.
@return the identifier.
*/
public String getID ()
{	return _id;
}

/**
Return the name.
@return the name.
*/
public String getName ()
{	return _name;
}

/**
Indicates whether the data object is dirty (has been modified).
@return true if the data object is dirty (has been modified).
*/
public boolean isDirty ()
{	return _is_dirty;
}

/**
Set whether the data object is dirty (has been modified).
@param is_dirty true if the object is being marked as dirty.
@return true if the data object is dirty (has been modified).
*/
public boolean isDirty ( boolean is_dirty )
{	_is_dirty = is_dirty;
	return _is_dirty;
}

/**
Restores the values from the _original object into the current object and sets _original to null.
*/
public void restoreOriginal() {
	StateCU_Data d = (StateCU_Data)_original;
	_id = d._id;
	_name = d._name;
}

/**
Set the identifier.
@param id Identifier for object.
*/
public void setID ( String id )
{	_id = id;
}

/**
Set the name.
@param name Name for object.
*/
public void setName ( String name )
{	_name = name;
}

/**
Print information about the object.
*/
public String toString ()
{	return _id + "," + _name;
}

}