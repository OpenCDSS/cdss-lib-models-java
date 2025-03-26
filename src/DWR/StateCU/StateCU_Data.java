// StateCU_Data - this class can be used as a base class for StateCU data objects.

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

CDSS Models Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

CDSS Models Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with CDSS Models Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package DWR.StateCU;

/**
This StateCU_Data class can be used as a base class for StateCU data objects.
Common data like identifier and name is maintained here to simplify access and remove redundant code.
*/
public abstract class StateCU_Data 
implements Cloneable, Comparable<StateCU_Data> {

protected String _id = StateCU_Util.MISSING_STRING;
protected String _name = StateCU_Util.MISSING_STRING;
protected boolean _isDirty = false;

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
{	return _isDirty;
}

/**
Set whether the data object is dirty (has been modified).
@param is_dirty true if the object is being marked as dirty.
@return true if the data object is dirty (has been modified).
*/
public boolean isDirty ( boolean isDirty )
{	_isDirty = isDirty;
	return _isDirty;
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
Sets whether the data is dirty or not,
meaning something has been modified that may impact derived values.
The 'recompute()' method can be called in lazy fashion to update those derived values.
Set whether the object data are dirty or not.
*/
public void setDirty(boolean dirty) {
	this._isDirty = dirty;
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