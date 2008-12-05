//------------------------------------------------------------------------------
// StateCU_Parcel - class to store parcel information
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 2006-04-09  Steven A. Malers, RTi   Initial version.  Copy from
//                                     StateMod_Parcel.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateCU;

import java.util.List;
import java.util.Vector;

/**
This class is not part of the core StateCU classes.  Instead, it is used with
StateDMI to track whether a CU Location has parcels.  The data may ultimately be
useful in StateCU and is definitely useful for data checks in StateDMI.
*/
public class StateCU_Parcel extends StateCU_Data 
implements Cloneable, Comparable {

// Base class has ID and name (not useful and same as ID)

/**
Crop name.
*/
private String	__crop;

/**
Area associated with the parcel, acres (should reflect percent_irrig from HydroBase).
*/
private double	__area;

/**
Area units.
*/
private String	__area_units;

/**
Year for the data.
*/
private int		__year;

/**
Irrigation method.
*/
private String	__irrigation_method;

/**
Water supply sources - initialize so non-null.
*/
private List __supply_Vector = new Vector();

/**
Constructor.
*/
public StateCU_Parcel() {
	super();
	initialize();
}

/**
Add a supply object.
*/
public void addSupply ( StateCU_Supply supply )
{	__supply_Vector.add ( supply );
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateCU_Parcel parcel = (StateCU_Parcel)super.clone();
	parcel._isClone = true;
	return parcel;
}

/**
Compares this object to another StateCU_Data object based on the sorted
order from the StateCU_Data variables, and then by crop, irrigation method,
area, and year,in that order.
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(Object o) {
	int res = super.compareTo(o);
	if (res != 0) {
		return res;
	}

	StateCU_Parcel parcel = (StateCU_Parcel)o;

	res = __crop.compareTo(parcel.getCrop());
	if ( res != 0) {
		return res;
	}
	
	res = __irrigation_method.compareTo(parcel.getIrrigationMethod());
	if ( res != 0) {
		return res;
	}

	if (__area < parcel.__area) {
		return -1;
	}
	else if (__area > parcel.__area) {
		return 1;
	}

	if (__year < parcel.__year) {
		return -1;
	}
	else if (__year > parcel.__year) {
		return 1;
	}

	return 0;
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateCU_Parcel)_original)._isClone = false;
	_isClone = true;
}

// TODO SAM 2007-05-15
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
public boolean equals(StateCU_Parcel parcel) {
	if (!super.equals(parcel)) {
	 	return false;
	}

	if (	__crop.equals(parcel.__crop) &&
			__irrigation_method.equalsIgnoreCase(parcel.__irrigation_method) &&
		(__area == parcel.__area) && __area_units.equalsIgnoreCase(parcel.__area_units) &&
		(__year == parcel.__year) ) {
		
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
	__crop = null;
	__irrigation_method = null;
}

/**
Returns the area for the crop (acres).
@return the area for the crop (acres).
*/
public double getArea() {
	return __area;
}


/**
Returns the area units for the crop.
@return the area units for the crop.
*/
public String getAreaUnits() {
	return __area_units;
}

/**
Returns the crop.
@return the crop.
*/
public String getCrop() {
	return __crop;
}

/**
Returns the irrigation method.
@return the irrigation method.
*/
public String getIrrigationMethod() {
	return __irrigation_method;
}

/**
Return the list of StateCU_Supply for the parcel.
@return the list of StateCU_Supply for the parcel.
*/
public List getSupplyList() {
	return __supply_Vector;
}

/**
Returns the number of wells associated with the parcel.
@return the number of wells associated with the parcel.
*/
/*
public int getWellCount() {
	return _well_count;
}
*/

/**
Returns the year for the crop.
@return the year for the crop.
*/
public int getYear() {
	return __year;
}

/**
Indicate whether the parcel has groundwater supply.  This will be true if
any of the StateCU_Supply associated with the parcel return isGroundWater as
true.
*/
public boolean hasGroundWaterSupply ()
{	int size = __supply_Vector.size();
	StateCU_Supply supply = null;
	for ( int i = 0; i < size; i++ ) {
		supply = (StateCU_Supply)__supply_Vector.get(i);
		if ( supply.isGroundWater() ) {
			return true;
		}
	}
	return false;
}

/**
Initializes member variables.
*/
private void initialize() {
	__crop = "";
	__irrigation_method = "";
	__area = StateCU_Util.MISSING_DOUBLE;
	__area_units = "";
	__year = StateCU_Util.MISSING_INT;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateCU_Parcel parcel = (StateCU_Parcel)_original;
	super.restoreOriginal();

	__crop = parcel.__crop;
	__irrigation_method = parcel.__irrigation_method;
	__area = parcel.__area;
	__area_units = parcel.__area_units;
	__year = parcel.__year;
	_isClone = false;
	_original = null;
}

/**
Set the crop area.
@param area area to set.
*/
public void setArea(double area) {
	if (area != __area) {
		/* REVISIT SAM 2006-04-09
		Parcels are not currently part of the data set.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		*/
		__area = area;
	}
}

/**
Set the area units.
@param area_units Area units to set.
*/
public void setAreaUnits(String area_units ) {
	if ( !area_units.equalsIgnoreCase(__area_units) ) {
		/* REVISIT SAM 2006-04-09
		parcels are not currently part of StateMod data set.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		*/
		__area_units = area_units;
	}
}

/**
Set the crop.
@param crop Crop to set.
*/
public void setCrop(String crop ) {
	if ( !crop.equalsIgnoreCase(__crop) ) {
		/* REVISIT SAM 2006-04-09
		parcels are not currently part of StateMod data set.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		*/
		__crop = crop;
	}
}

/**
Set the irrigation method.
@param irrigation_method Irrigation method to set.
*/
public void setIrrigationMethod(String irrigation_method ) {
	if ( !irrigation_method.equalsIgnoreCase(__irrigation_method) ) {
		/* REVISIT SAM 2006-04-09
		parcels are not currently part of StateMod data set.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		*/
		__irrigation_method = irrigation_method;
	}
}

/**
Set the number of wells assoiated with the parcel.
@param well_count Number of wells associated with the parcel.
*/
/*
public void setWellCount(int well_count) {
	if ( well_count != _well_count) {
		/ * REVISIT SAM 2006-04-09
		parcels are not currently part of StateMod data set.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		* /
		_well_count = well_count;
	}
}
*/

/**
Set the year associated with the crop.
@param year Year to set.
*/
public void setYear(int year) {
	if ( year != __year) {
		/* REVISIT SAM 2006-04-09
		parcels are not currently part of StateMod data set.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		*/
		__year = year;
	}
}

/**
Returns a String representation of this object.
@return a String representation of this object.
*/
public String toString() {
	return super.toString() + ", " + __crop + ", " + __irrigation_method +
	__area + ", " + __area_units + ", " + __year;
}

}
