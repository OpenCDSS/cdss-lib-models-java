// StateCU_SupplyFromGW - class to store water basic groundwater supply information

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
public class StateCU_SupplyFromGW extends StateCU_Supply 
implements Cloneable, Comparable<StateCU_Data> {

// Base class has ID and name

/**
Irrigated area for the supply.
*/
private double areaIrrig;

/**
Fraction of parcel irrigated by the well supply,
calculated as the 1 over the number of wells that irrigate the parcel.
If a D&W node, the additional surface water fraction must be
multiplied when computing the irrigated area (see StateCU_Parcel.recompute()).
*/
private double areaIrrigFraction;

/**
Collection part ID type, e.g., "Well" or "Receipt".
*/
private String collectionPartIdType = "";

/**
Collection part type, e.g., "Well" or "Parcel".
*/
private String collectionPartType = "";

/**
 * Indicate whether to include in CDS acreage.
 */
/* TODO smalers 2020-11-06 now hancled in StateCU_Parcel
private boolean includeInCdsArea = true;
*/

/**
WDID for the ditch, either explicit ID or a part.
*/
private String wdid = "";

/**
Receipt for the ditch, either explicit ID or a part.
*/
private String receipt = "";

/**
Constructor.
*/
public StateCU_SupplyFromGW() {
	super();
	initialize();
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateCU_SupplyFromGW supply = (StateCU_SupplyFromGW)super.clone();
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

	StateCU_SupplyFromGW supply = (StateCU_SupplyFromGW)data;

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
	((StateCU_SupplyFromGW)_original)._isClone = false;
	_isClone = true;
}

/**
Tests to see if two parcels equal.  Strings are compared with case sensitivity.
@param supply The supply instance to compare.
@return true if they are equal, false otherwise.
*/
public boolean equals(StateCU_SupplyFromGW supply) {
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
Returns the parcel area irrigated.
@return the parcel area irrigated.
*/
public double getAreaIrrig() {
	return this.areaIrrig;
}

/**
Returns the parcel area irrigated fraction (0 to 1).
@return the parcel area irrigated fraction (0 to 1).
*/
public double getAreaIrrigFraction() {
	return this.areaIrrigFraction;
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
Returns whether to include in CDS area.
@return whether to include in CDS area.
*/
/* TODO smalers 2020-11-06 now handled in StateCU_Parcel
public boolean getIncludeInCdsArea() {
	return this.includeInCdsArea;
}
*/

/**
Returns the WDID for the supply.
@return the WDID for the supply.
*/
public String getWDID() {
	return this.wdid;
}

/**
Returns the Receipt for the supply.
@return the Receipt for the supply.
*/
public String getReceipt() {
	return this.receipt;
}

/**
Initializes member variables.
*/
protected void initialize() {
	super.initialize();
	// TODO smalers 2020-02-17 Set parent class  data, redundant to this class and need to fix later
	super.setIsGroundWater(true);
	super.setIsSurfaceWater(false);
	// Data for this class
	this.areaIrrig = StateCU_Util.MISSING_DOUBLE;
	this.areaIrrigFraction = StateCU_Util.MISSING_DOUBLE;
	this.wdid = "";
}

/**
Indicate whether the supply is a groundwater source.
@return true if a groundwater supply, false if not.
*/
public boolean isGroundWater() {
	return true;
}

/**
Indicate whether the supply is a surface water source.
@return true if a surface water supply, false if not.
*/
public boolean isSurfaceWater() {
	return false;
}

/**
 * Recalculate the internally-computed values such as irrigated acreage fraction.
 * Currently does not do anything since parcel recompute() does the work.
 */
public void recompute () {
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateCU_SupplyFromGW supply = (StateCU_SupplyFromGW)_original;
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
Set the area irrigated.
@param areaIrrig
*/
public void setAreaIrrig ( double areaIrrig ) {
	this.areaIrrig = areaIrrig;
}

/**
Set the area irrigated percent.
@param areaIrrig
*/
public void setAreaIrrigFraction ( double areaIrrigFraction ) {
	this.areaIrrigFraction = areaIrrigFraction;
}

/**
Set the collection part ID type, e.g. "WELL" or "RECEIPT".
@param collectionPartIdType
*/
public void setCollectionPartIdType ( String collectionPartIdType ) {
	this.collectionPartIdType = collectionPartIdType;
}

/**
Set the collection part type, e.g., "Well" or "Parcel".
@param collectionPartType
*/
public void setCollectionPartType ( String collectionPartType ) {
	this.collectionPartType = collectionPartType;
}

/**
Set whether to include in CDS area
@param includeInCdsArea whether to include in CDS area
*/
/** TODO smalers 2020-11-06 now handled in StateCU_Parcel
public void setIncludeInCdsArea ( boolean includeInCdsArea ) {
	this.includeInCdsArea = includeInCdsArea;
}
*/

/**
Set the Receipt for the supply.
@param receipt
*/
public void setReceipt ( String receipt ) {
	this.receipt = receipt;
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