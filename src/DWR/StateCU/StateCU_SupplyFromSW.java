// StateCU_SupplyFromSW - class to store water supply information

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

package DWR.StateCU;

/**
This class is not part of the core StateCU classes.  Instead, it is used with
StateDMI to track the water supply for a parcel.  The data may ultimately be
useful in StateCU and is definitely useful for data checks in StateDMI.  For
groundwater only lands, the supply consists of well right/permit identifiers
and decree/yield for amount.
*/
public class StateCU_SupplyFromSW extends StateCU_Supply 
implements Cloneable, Comparable<StateCU_Data> {

// Base class has ID and name

/**
Irrigated area for the supply, after accounting for fraction.
*/
private double areaIrrig;

/**
Irrigated area for the supply, after accounting for fraction, from HydroBase.
*/
private double areaIrrigHydroBase;

/**
Fraction of parcel irrigated by the supply, calculated as 1/(number of surface supplies).
This is a fraction 0 to 1.
*/
private double areaIrrigFraction;

/**
Fraction of parcel irrigated by the supply, from HydroBase.
This is a fraction 0 to 1.
*/
private double areaIrrigFractionHydroBase;

/**
Whether the fraction computed by number of surface supplies is different from HydroBase fraction.
"ERROR" if different or "" if the same.
*/
private String areaIrrigFractionHydroBaseError = "";

/**
Collection part ID type, e.g., always "WDID".
*/
private String collectionPartIdType = "WDID";

/**
Collection part type, e.g., always "Ditch".
*/
private String collectionPartType = "Ditch";

/**
WDID for the ditch, either explicit ID or a part.
*/
private String wdid;

/**
Constructor.
*/
public StateCU_SupplyFromSW() {
	super();
	initialize();
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateCU_SupplyFromSW supply = (StateCU_SupplyFromSW)super.clone();
	supply._isClone = true;
	return supply;
}

/**
Compares this object to another StateCU_Supply object based on the sorted
order from the StateCU_Data variables, and then by amount.
@param data the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(StateCU_Data data) {
	int res = super.compareTo(data);
	if (res != 0) {
		return res;
	}

	//StateCU_SupplyFromSW supply = (StateCU_SupplyFromSW)data;

	/*
	if (this.__supplyAmount < supply.__supplyAmount) {
		return -1;
	}
	else if (this.__supplyAmount > supply.__supplyAmount) {
		return 1;
	}
	*/

	return 0;
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateCU_SupplyFromSW)_original)._isClone = false;
	_isClone = true;
}

/**
Tests to see if two parcels equal.  Strings are compared with case sensitivity.
@param supply The supply instance to compare.
@return true if they are equal, false otherwise.
*/
public boolean equals(StateCU_SupplyFromSW supply) {
	if (!super.equals(supply)) {
	 	return false;
	}

	/*
	if ( (this.__supplyAmount == supply.__supplyAmount) &&
			(__is_ground == supply.__is_ground) &&
			(__is_surface == supply.__is_surface) ) {
		return true;
	}
	*/
	return false;
}

/**
Returns the parcel area irrigated, calculated using the fraction.
@return the parcel area irrigated, calculated using the fraction.
*/
public double getAreaIrrig() {
	return this.areaIrrig;
}

/**
Returns the parcel area irrigated, calculated using the fraction from HydroBase.
@return the parcel area irrigated, calculated using the fraction from HydroBase.
*/
public double getAreaIrrigHydroBase() {
	return this.areaIrrigHydroBase;
}

/**
Returns the parcel area irrigated fraction.
@return the parcel area irrigated fraction.
*/
public double getAreaIrrigFraction() {
	return this.areaIrrigFraction;
}

/**
Returns the parcel area irrigated fraction, from HydroBase.
@return the parcel area irrigated fraction, from HydroBase.
*/
public double getAreaIrrigFractionHydroBase() {
	return this.areaIrrigFractionHydroBase;
}

/**
Returns "ERROR" if calculated irrigated area fraction differs from HydroBase value.
This is used by code that prints parcel reports.
@return "ERROR" if different, "" if the same.
*/
public String getAreaIrrigFractionHydroBaseError() {
	// Should be calculated in the parcel recompute.
	//if ( this.isDirty() ) {
	//	recompute();
	//}
	return this.areaIrrigFractionHydroBaseError;
}

/**
Returns the collection part ID type.
@return the collection part  IDtype.
*/
public String getCollectionPartIdType() {
	return this.collectionPartIdType;
}

/**
Returns the collection part type.
@return the collection part type.
*/
public String getCollectionPartType() {
	return this.collectionPartType;
}

/**
Returns the WDID for the supply.
@return the WDID for the supply.
*/
public String getWDID() {
	return this.wdid;
}


/**
Initializes member variables.
*/
protected void initialize() {
	super.initialize();
	// TODO smalers 2020-02-17 Set parent class  data, redundant to this class and need to fix later
	super.setIsGroundWater(false);
	super.setIsSurfaceWater(true);
	// Class data
	this.areaIrrig = StateCU_Util.MISSING_DOUBLE;
	this.areaIrrigHydroBase = StateCU_Util.MISSING_DOUBLE;
	this.areaIrrigFraction = StateCU_Util.MISSING_DOUBLE;
	this.areaIrrigFractionHydroBase = StateCU_Util.MISSING_DOUBLE;
	this.wdid = "";
}

/**
Indicate whether the supply is a groundwater source.
@return true if a groundwater supply, false if not.
*/
public boolean isGroundWater() {
	return false;
}

/**
Indicate whether the supply is a surface water source.
@return true if a surface water supply, false if not.
*/
public boolean isSurfaceWater() {
	return true;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	//StateCU_SupplyFromSW supply = (StateCU_SupplyFromSW)_original;
	super.restoreOriginal();

	/*
	this.__supplyAmount = supply.__supplyAmount;
	__is_ground = supply.__is_ground;
	__is_surface = supply.__is_surface;
	*/
	_isClone = false;
	_original = null;
}

/**
Set the area irrigated, calculated using the fraction.
@param areaIrrig
*/
public void setAreaIrrig ( double areaIrrig ) {
	this.areaIrrig = areaIrrig;
}

/**
Set the area irrigated, calculated using the fraction from HydroBase.
@param areaIrrigHydroBase
*/
public void setAreaIrrigHydroBase ( double areaIrrigHydroBase ) {
	this.areaIrrigHydroBase = areaIrrigHydroBase;
}

/**
Set the area irrigated fraction, calculated.
@param areaIrrigFraction
*/
public void setAreaIrrigFraction ( double areaIrrigFraction ) {
	this.areaIrrigFraction = areaIrrigFraction;
}

/**
Set the area irrigated fraction, from HydroBase.
@param areaIrrigFractionHydroBase
*/
public void setAreaIrrigFractionHydroBase ( double areaIrrigFractionHydroBase ) {
	this.areaIrrigFractionHydroBase = areaIrrigFractionHydroBase;
}

/**
Set the area irrigated fraction, from HydroBase, error indicator.
@param areaIrrigFractionHydroBase
*/
public void setAreaIrrigFractionHydroBaseError ( String error ) {
	this.areaIrrigFractionHydroBaseError = error;
}

/**
Set the WDID for the supply.
@param wdid
*/
public void setWDID ( String wdid ) {
	this.wdid = wdid;
}

/**
Returns a String representation of this object.
@return a String representation of this object.
*/
public String toString() {
	return super.toString();
	/*
	return super.toString() + ", " + _id + ", " + this.__supplyAmount + ", " + __is_ground +
	", " + __is_surface;
	*/
}

}