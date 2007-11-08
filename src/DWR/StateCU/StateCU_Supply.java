//------------------------------------------------------------------------------
// StateCU_Supply - class to store water supply information
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 2007-05-15  Steven A. Malers, RTi   Initial version.  Copy from
//                                     StateCU_Parcel.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateCU;

/**
This class is not part of the core StateCU classes.  Instead, it is used with
StateDMI to track the water supply for a parcel.  The data may ultimately be
useful in StateCU and is definitely useful for data checks in StateDMI.  For
groundwater only lands, the supply consists of well right/permit identifiers
and decree/yield for amount.
*/
public class StateCU_Supply extends StateCU_Data 
implements Cloneable, Comparable {

// Base class has ID and name

/**
Supply amount (rate) associated with the supply, CFS for wells.
*/
private double	__amount;

// Indicate if a groundwater and/or surface water supply (likely only one but not both)
private boolean __is_ground = false;
private boolean __is_surface = false;

/**
Constructor.
*/
public StateCU_Supply() {
	super();
	initialize();
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateCU_Supply supply = (StateCU_Supply)super.clone();
	supply._isClone = true;
	return supply;
}

/**
Compares this object to another StateCU_Supply object based on the sorted
order from the StateCU_Data variables, and then by amount.
@param o the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(Object o) {
	int res = super.compareTo(o);
	if (res != 0) {
		return res;
	}

	StateCU_Supply supply = (StateCU_Supply)o;

	if (__amount < supply.__amount) {
		return -1;
	}
	else if (__amount > supply.__amount) {
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
@param supply The supply instance to compare.
@return true if they are equal, false otherwise.
*/
public boolean equals(StateCU_Supply supply) {
	if (!super.equals(supply)) {
	 	return false;
	}

	if ( (__amount == supply.__amount) &&
			(__is_ground == supply.__is_ground) &&
			(__is_surface == supply.__is_surface) ) {
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
}

/**
Returns the supply amount.
@return the supply amount (CFS for wells).
*/
public double getAmount() {
	return __amount;
}

/**
Initializes member variables.
*/
private void initialize() {
	__amount = StateCU_Util.MISSING_DOUBLE;
}

/**
Indicate whether the supply is a groundwater source.
@return true if a groundwater supply, false if not.
*/
public boolean isGroundWater() {
	return __is_ground;
}

/**
Indicate whether the supply is a surface water source.
@return true if a surface water supply, false if not.
*/
public boolean isSurfaceWater() {
	return __is_surface;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateCU_Supply supply = (StateCU_Supply)_original;
	super.restoreOriginal();

	__amount = supply.__amount;
	__is_ground = supply.__is_ground;
	__is_surface = supply.__is_surface;
	_isClone = false;
	_original = null;
}

/**
Set the supply amount.
@param amount the supply amount.
*/
public void setAmount ( double amount ) {
	if (amount != __amount) {
		/* REVISIT SAM 2006-04-09
		Supply is not currently part of the data set.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		*/
		__amount = amount;
	}
}

/**
Set whether it is a groundwater supply.
@param whether a groundwater supply.
*/
public void setIsGroundWater ( boolean is_ground ) {
	if (is_ground != __is_ground) {
		/* REVISIT SAM 2006-04-09
		Supply is not currently part of the data set.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		*/
		__is_ground = is_ground;
	}
}

/**
Set whether it is a surface water supply.
@param whether a surface water supply.
*/
public void setIsSurfaceWater ( boolean is_surface ) {
	if (is_surface != __is_surface ) {
		/* REVISIT SAM 2006-04-09
		Supply is not currently part of the data set.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		*/
		__is_surface = is_surface;
	}
}

/**
Returns a String representation of this object.
@return a String representation of this object.
*/
public String toString() {
	return super.toString() + ", " + _id + ", " + __amount + ", " + __is_ground +
	", " + __is_surface;
}

}
