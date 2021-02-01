// StateCU_Supply - class to store water supply information

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
This class is not part of the core StateCU classes.
It is used with StateDMI to track the water supply for a parcel when processing
data files that derive from parcel/supply data.
The StateCU_Data parent class has ID and name.
The child classes store WDID and receipt identifiers to allow look back to the StateCU_Location and StateMod_Data.
*/
public class StateCU_Supply extends StateCU_Data 
implements Cloneable, Comparable<StateCU_Data> {

/**
TODO smalers 2020-11-08 is this not currently used?
- It may not work for rights since multiple rights per supply ID.
Supply amount (rate) associated with the supply, CFS for wells.
This is associated with water rights or permits.
*/
private double	__supplyAmount;

// Indicate if a groundwater and/or surface water supply (likely only one but not both)
private boolean __is_ground = false;
private boolean __is_surface = false;

/**
 * Whether to include the parcel in the *.cds file.
 * If set to YES, the cuLocForCds value contains the StateCU_Location that includes the parcel.
 * The parcel recompute() method indicates the status.
 */
private IncludeParcelInCdsType includeInCdsType = IncludeParcelInCdsType.UNKNOWN;

/**
 * CU Location that the supply is related to.
 * This is assigned in readCropPatternTSFromHydrobase().
 */
private StateCU_Location cuLocForCds = null;

/**
 * Error message when this.includeInCdsType=IncludeParcelInCdsType.ERROR.
 * TODO smalers 2021-01-31 need to enable to provide more information.
 */
private String includeInCdsError = "";

/**
 * Source of the parcel data, for example "HB-PUTS" for parcel use time series and "HB-WTP" for well to parcel.
 */
private String dataSource = "";

/**
 *  General error string, for example supply ID has an error in GIS.
 *  The setError() method should be used to set as one or more phrases, each ending in period.
 */
private String error = "";

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
@param data the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(StateCU_Data data) {
	int res = super.compareTo(data);
	if (res != 0) {
		return res;
	}

	StateCU_Supply supply = (StateCU_Supply)data;

	if (this.__supplyAmount < supply.__supplyAmount) {
		return -1;
	}
	else if (this.__supplyAmount > supply.__supplyAmount) {
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
@param v1 the first Vector of StateMod_ReservoirAreaCap s to check.  Can not be null.
@param v2 the second Vector of StateMod_ReservoirAreaCap s to check.  Can not be null.
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

	if ( (this.__supplyAmount == supply.__supplyAmount) &&
			(__is_ground == supply.__is_ground) &&
			(__is_surface == supply.__is_surface) ) {
		return true;
	}
	return false;
}

/**
Returns the data source.
@return the data source.
*/
public String getDataSource() {
	return this.dataSource;
}

/**
Returns the general error.
@return the general error.
*/
public String getError() {
	return this.error;
}

/**
Returns the integer ID for the parcel.
@return the integer ID for the parcel.
*/
public IncludeParcelInCdsType getIncludeParcelInCdsType() {
	return this.includeInCdsType;
}

/**
Returns the StateCU_Location that includes the parcel.
@return the StateCU_Location that includes the parcel, null if no location has been set for CDS.
*/
public StateCU_Location getStateCULocationForCds() {
	return this.cuLocForCds;
}

/**
Returns the supply amount.
@return the supply amount (CFS for wells).
*/
public double getSupplyAmount() {
	return this.__supplyAmount;
}

/**
Initializes member variables.
*/
protected void initialize() {
	this.__supplyAmount = StateCU_Util.MISSING_DOUBLE;
	// Will be set in child classes
	this.__is_ground = false;
	this.__is_surface = false;
}

/**
Indicate whether the supply is a groundwater source.
@return true if a groundwater supply, false if not.
*/
public boolean isGroundWater() {
	return this.__is_ground;
}

/**
Indicate whether the supply is a surface water source.
@return true if a surface water supply, false if not.
*/
public boolean isSurfaceWater() {
	return this.__is_surface;
}

/**
 * Recalculate the internally-computed values such as irrigated acreage fraction.
 * This should be overridden in the derived classes.
 */
public void recompute () {
	throw new RuntimeException ( "StateCU_Supply.recompute() should be implemented in child class.");
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateCU_Supply supply = (StateCU_Supply)_original;
	super.restoreOriginal();

	this.__supplyAmount = supply.__supplyAmount;
	__is_ground = supply.__is_ground;
	__is_surface = supply.__is_surface;
	_isClone = false;
	_original = null;
}

/**
Set the data source.
@param dataSource Data source to set.
*/
public void setDataSource(String dataSource ) {
	this.dataSource = dataSource;
}

/**
Set the general error.
@param error general error for the supply data
*/
public void setError(String error) {
	this.setError(error, false);
}

/**
Set the general error.
@param error general error for the supply data
@param append whether to append the error to the existing error string (true) or replace (false)
*/
public void setError(String error, boolean append) {
	if ( append ) {
		if ( this.error.isEmpty() ) {
			// Just set.
			this.error = error;
		}
		else {
			// Append by adding a space.  Assume that each error is a sentence with trailing period.
			this.error = this.error + " " + error;
		}
	}
	else {
		// Just set.
		this.error = error;
	}
}

/**
Set the error for including in CDS.
@param includeInCdsError error if whether to include in CDS could not be determined.
*/
public void setIncludeInCdsError(String includeInCdsError ) {
	this.includeInCdsError  = includeInCdsError;
}

/**
Set whether the parcel is included in the CDS.
@param includeInCdsType indicates how included in the CDS
*/
public void setIncludeInCdsType(IncludeParcelInCdsType includeInCdsType ) {
	this.includeInCdsType  = includeInCdsType;
}

/**
Set the StateCU_Location that includes the parcel supply.
Necessary because parcel may be related to D&W and WEL but
the area for a supply is only assigned to one CU Location depending
on whether surface supply is used for the parcel.
If yes, then assign to D&W location.  If no, then assign to WEL location.
@param culoc the StateCU_Location that includes the parcel.
*/
public void setStateCULocationForCds( StateCU_Location culoc) {
	this.cuLocForCds = culoc;
}

/**
Set the supply amount.
@param supplyAmount the supply amount.
*/
public void setSupplyAmount ( double supplyAmount ) {
	if (supplyAmount != this.__supplyAmount) {
		/* TODO SAM 2006-04-09
		Supply is not currently part of the StateCU data set, just used by StateDMI.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		*/
		this.__supplyAmount = supplyAmount;
	}
}

/**
Set whether it is a groundwater supply.
@param whether a groundwater supply.
*/
public void setIsGroundWater ( boolean is_ground ) {
	if (is_ground != __is_ground) {
		/* TODO SAM 2006-04-09
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
		/* TODO SAM 2006-04-09
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
	return super.toString() + ", " + _id + ", " + this.__supplyAmount + ", " + __is_ground +
	", " + __is_surface;
}

}