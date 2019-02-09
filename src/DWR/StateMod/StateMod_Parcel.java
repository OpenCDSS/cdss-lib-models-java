// StateMod_Parcel - class to store parcel information

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

//------------------------------------------------------------------------------
// StateMod_Parcel - class to store parcel information
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 2006-04-09	Steven A. Malers, RTi	Initial version.
// 2006-04-12	SAM, RTi		Add well count and area data.
// 2007-03-01	SAM, RTi		Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

/**
This class is not part of the core StateMod classes.  Instead, it is used with
StateDMI to track whether a well or diversion station have parcels.  This
approach will be evaluated.
*/
public class StateMod_Parcel extends StateMod_Data 
implements Cloneable, Comparable<StateMod_Data> {

// Base class has ID import and name (not important)

/**
Content in area cap table.
*/
protected String	_crop;

/**
Area associated with the parcel, acres.
*/
protected double	_area;

/**
Year for the data.
*/
protected int		_year;

/**
Count of wells on the parcel.
*/
protected int		_well_count;

/**
Constructor.
*/
public StateMod_Parcel() {
	super();
	initialize();
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateMod_Parcel parcel = (StateMod_Parcel)super.clone();
	parcel._isClone = true;
	return parcel;
}

/**
Compares this object to another StateMod_Data object based on the sorted
order from the StateMod_Data variables, and then by crop, area, and year,
in that order.
@param data the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(StateMod_Data data) {
	int res = super.compareTo(data);
	if (res != 0) {
		return res;
	}

	StateMod_Parcel parcel = (StateMod_Parcel)data;

	res = _crop.compareTo(parcel.getCrop());
	if ( res != 0) {
		return res;
	}

	if (_area < parcel._area) {
		return -1;
	}
	else if (_area > parcel._area) {
		return 1;
	}

	if (_year < parcel._year) {
		return -1;
	}
	else if (_year > parcel._year) {
		return 1;
	}

	return 0;
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = (StateMod_Parcel)clone();
	((StateMod_Parcel)_original)._isClone = false;
	_isClone = true;
}

// REVISIT SAM 2006-04-09
// Not sure if something like this is needed.
/**
Compare two rights Vectors and see if they are the same.
@param v1 the first Vector of StateMod_ReservoirAreaCap s to check.  Can not
be null.
@param v2 the second Vector of StateMod_ReservoirAreaCap s to check.  Can not
be null.
@return true if they are the same, false if not.
*/
/*
public static boolean equals(Vector v1, Vector v2) {
	String routine = "StateMod_ReservoirAreaCap.equals(Vector, Vector)";
	StateMod_ReservoirAreaCap r1;	
	StateMod_ReservoirAreaCap r2;	
	if (v1.size() != v2.size()) {
		Message.printStatus(1, routine, "Vectors are different sizes");
		return false;
	}
	else {
		// sort the Vectors and compare item-by-item.  Any differences
		// and data will need to be saved back into the dataset.
		int size = v1.size();
		Message.printStatus(1, routine, "Vectors are of size: " + size);
		Vector v1Sort = StateMod_Util.sortStateMod_DataVector(v1);
		Vector v2Sort = StateMod_Util.sortStateMod_DataVector(v2);
		Message.printStatus(1, routine, "Vectors have been sorted");
	
		for (int i = 0; i < size; i++) {			
			r1 = (StateMod_ReservoirAreaCap)v1Sort.elementAt(i);	
			r2 = (StateMod_ReservoirAreaCap)v2Sort.elementAt(i);	
			Message.printStatus(1, routine, r1.toString());
			Message.printStatus(1, routine, r2.toString());
			Message.printStatus(1, routine, "Element " + i 
				+ " comparison: " + r1.compareTo(r2));
			if (r1.compareTo(r2) != 0) {
				return false;
			}
		}
	}	
	return true;
}
*/

/**
Tests to see if two parcels equal.  Strings are compared with case sensitivity.
@param ac the ac to compare.
@return true if they are equal, false otherwise.
*/
public boolean equals(StateMod_Parcel parcel) {
	if (!super.equals(parcel)) {
	 	return false;
	}

	if (	_crop.equals(parcel._crop) &&
		(_area == parcel._area) &&
		(_year == parcel._year) ) {
		
		return true;
	}
	return false;
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	super.finalize();
	_crop = null;
}

/**
Returns the area for the crop (acres).
@return the area for the crop (acres).
*/
public double getArea() {
	return _area;
}

/**
Returns the crop.
@return the crop.
*/
public String getCrop() {
	return _crop;
}

/**
Returns the number of wells associated with the parcel.
@return the number of wells associated with the parcel.
*/
public int getWellCount() {
	return _well_count;
}
/**
Returns the year for the crop.
@return the year for the crop.
*/
public int getYear() {
	return _year;
}

/**
Initializes member variables.
*/
private void initialize() {
	_crop = "";
	_area = StateMod_Util.MISSING_DOUBLE;
	_year = StateMod_Util.MISSING_INT;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_Parcel parcel = (StateMod_Parcel)_original;
	super.restoreOriginal();

	_crop = parcel._crop;
	_area = parcel._area;
	_year = parcel._year;
	_isClone = false;
	_original = null;
}

/**
Set the crop area.
@param area area to set.
*/
public void setArea(double area) {
	if (area != _area) {
		/* REVISIT SAM 2006-04-09
		Parcels are not currently part of the data set.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		*/
		_area = area;
	}
}

/**
Set the crop.
@param crop Crop to set.
*/
public void setCrop(String crop ) {
	if ( !crop.equalsIgnoreCase(_crop) ) {
		/* REVISIT SAM 2006-04-09
		parcels are not currently part of StateMod data set.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		*/
		_crop = crop;
	}
}

/**
Set the number of wells assoiated with the parcel.
@param well_count Number of wells associated with the parcel.
*/
public void setWellCount(int well_count) {
	if ( well_count != _well_count) {
		/* REVISIT SAM 2006-04-09
		parcels are not currently part of StateMod data set.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		*/
		_well_count = well_count;
	}
}

/**
Set the year associated with the crop.
@param year Year to set.
*/
public void setYear(int year) {
	if ( year != _year) {
		/* REVISIT SAM 2006-04-09
		parcels are not currently part of StateMod data set.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		*/
		_year = year;
	}
}

/**
Returns a String representation of this object.
@return a String representation of this object.
*/
public String toString() {
	return super.toString() + ", " + _crop + ", " + _area + ", "
		+ _year;
}
 
}
