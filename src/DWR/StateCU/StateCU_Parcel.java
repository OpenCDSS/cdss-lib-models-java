// StateCU_Parcel - used with StateDMI to track whether a CU Location has parcels

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

import java.util.ArrayList;
import java.util.List;

import RTi.Util.Message.Message;

/**
This class is not part of the core StateCU model classes.
Instead, it is used with StateDMI to track whether a CU Location has parcels.
The data may ultimately be useful in StateCU and is definitely useful for data checks in StateDMI,
for example to make sure a parcel is not counted more than once in acreage totals for the location.
*/
public class StateCU_Parcel extends StateCU_Data 
implements Cloneable, Comparable<StateCU_Data> {

// Base class has ID (parcel_id) and name (name is not not useful and same as CULocation ID)
	
/**
 * Location ID - this is redundant with the identifier of the object hat has a list of these objects,
 * but is necessary when the parcel list is formed for many locations.
 * For example, this can be set to StateCU or StateMod location.
 */
private String locationId = "";

/**
Year for the data.
*/
private int year;

/**
Crop name.
*/
private String crop;

/**
Parcel total area - see supply information for what was actually irrigated.
*/
private double area;

/**
Area units.
*/
private String areaUnits;

/**
Irrigation method.
*/
private String irrigationMethod;

/**
 * Source of the parcel data, for example "HB-PUTS" for parcel use time series and "HB-WTP" for well to parcel.
 */
private String dataSource = "";

/**
 * Count of groundwater supplies for the parcel for the year,
 * used to prorate the parcel area to each of those wells.
 * Call refreshSupplyCount to update.
 */
private int supplyFromGWCount = 0;

/**
 * Count of surface water supplies for the parcel for the year,
 * used to prorate the parcel area to each of those ditches.
 * Call refreshSupplyCount() to update.
 */
private int supplyFromSWCount = 0;

/**
Water supply sources - initialize so non-null.
These map to irrigated lands GIS data.
*/
private List<StateCU_Supply> supplyList = new ArrayList<>();

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
{	this.supplyList.add ( supply );
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
Compares this object to another StateCU_Data object based on the sorted order from the StateCU_Data
variables, and then by crop, irrigation method, area, and year,in that order.
@param data the object to compare against (should be a StateCU_Parcel).
@return 0 if they are the same, 1 if this object is greater than the other object, or -1 if it is less.
*/
public int compareTo(StateCU_Data data) {
	int res = super.compareTo(data);
	if (res != 0) {
		return res;
	}
	
	StateCU_Parcel parcel = (StateCU_Parcel)data;
	res = this.crop.compareTo(parcel.getCrop());
	if ( res != 0) {
		return res;
	}
	
	res = this.irrigationMethod.compareTo(parcel.getIrrigationMethod());
	if ( res != 0) {
		return res;
	}

	if (this.area < parcel.area) {
		return -1;
	}
	else if (this.area > parcel.area) {
		return 1;
	}

	if (this.year < parcel.year) {
		return -1;
	}
	else if (this.year > parcel.year) {
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
@param v1 the first Vector of StateMod_ReservoirAreaCap s to check.  Cannot be null.
@param v2 the second Vector of StateMod_ReservoirAreaCap s to check.  Cannot be null.
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

	if ( this.crop.equals(parcel.crop) && this.irrigationMethod.equalsIgnoreCase(parcel.irrigationMethod) &&
		(this.area == parcel.area) && this.areaUnits.equalsIgnoreCase(parcel.areaUnits) &&
		(this.year == parcel.year) ) {
		
		return true;
	}
	return false;
}

/**
Returns the area for the crop (acres).
@return the area for the crop (acres).
*/
public double getArea() {
	return this.area;
}

/**
Returns the area units for the crop.
@return the area units for the crop.
*/
public String getAreaUnits() {
	return this.areaUnits;
}

/**
Returns the crop.
@return the crop.
*/
public String getCrop() {
	return this.crop;
}

/**
Returns the data source.
@return the data source.
*/
public String getDataSource() {
	return this.dataSource;
}

/**
Returns the irrigation method.
@return the irrigation method.
*/
public String getIrrigationMethod() {
	return this.irrigationMethod;
}

/**
Returns the location identifier.
@return the location identifier.
*/
public String getLocationId() {
	return this.locationId;
}

/**
Returns the count of groundwater supply.
@return the count of groundwater supply.
*/
public int getSupplyFromGWCount() {
	return this.supplyFromGWCount;
}

/**
Returns the count of surface water supply.
@return the count of surface water supply.
*/
public int getSupplyFromSWCount() {
	return this.supplyFromSWCount;
}

/**
Return the list of StateCU_Supply for the parcel.
@return the list of StateCU_Supply for the parcel.
*/
public List<StateCU_Supply> getSupplyList() {
	return this.supplyList;
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
	return this.year;
}

/**
Indicate whether the parcel has groundwater supply.  This will be true if
any of the StateCU_Supply associated with the parcel return isGroundWater as true.
*/
public boolean hasGroundWaterSupply ()
{	int size = supplyList.size();
	StateCU_Supply supply = null;
	for ( int i = 0; i < size; i++ ) {
		supply = this.supplyList.get(i);
		if ( supply.isGroundWater() ) {
			return true;
		}
	}
	return false;
}

/**
Indicate whether the parcel has surface water supply.  This will be true if
any of the StateCU_Supply associated with the parcel return isSurfaceWater as true.
*/
public boolean hasSurfaceWaterSupply ()
{	int size = supplyList.size();
	StateCU_Supply supply = null;
	for ( int i = 0; i < size; i++ ) {
		supply = this.supplyList.get(i);
		if ( supply.isSurfaceWater() ) {
			return true;
		}
	}
	return false;
}

/**
Initializes member variables.
*/
private void initialize() {
	// Parent class
	this._id = "";
	// This class
	this.area = StateCU_Util.MISSING_DOUBLE;
	this.areaUnits = "";
	this.crop = "";
	this.irrigationMethod = "";
	this.locationId = "";
	this.supplyFromGWCount = 0;
	this.supplyFromSWCount = 0;
	this.year = StateCU_Util.MISSING_INT;
}

/**
 * Refresh the counts of well and ditch supply.
 * This must be called after parcel data are read from HydroBase or other data.
 */
public void refreshSupplyCount () {
	// Count the number of groundwater supplies (wells) and surface water supplies (ditches)
	int countGW = 0;
	int countSW = 0;
	for ( StateCU_Supply supply : this.supplyList ) {
		if ( supply.isGroundWater() ) {
			++countGW;
		}
		if ( supply.isSurfaceWater() ) {
			++countSW;
		}
	}
	this.supplyFromGWCount = countGW;
	this.supplyFromSWCount = countSW;
	// Loop through the well supply parcels and update the areaIrrig based on count
	// - divide the parcel area by the number of wells
	for ( StateCU_Supply supply : this.supplyList ) {
		if ( supply instanceof StateCU_SupplyFromGW ) {
			if ( this.supplyFromGWCount == 0 ) {
				((StateCU_SupplyFromGW) supply).setAreaIrrig(0.0);
			}
			else {
				((StateCU_SupplyFromGW) supply).setAreaIrrig(this.area/this.supplyFromGWCount);
			}
		}
		else if ( supply instanceof StateCU_SupplyFromSW ) {
			// TODO smalers 2020-02-17 this is currently handled via HydroBase data when read
			// - percent irrig
			// - recalculate and see if the number is accurate
			StateCU_SupplyFromSW swSupply = (StateCU_SupplyFromSW)supply;
			double swIrrigAreaCalc = this.getArea() * swSupply.getAreaIrrigPercent();
			double swIrrigAreaCalcMin = swIrrigAreaCalc*.99999; 
			double swIrrigAreaCalcMax = swIrrigAreaCalc*1.00001; 
			if ( (swSupply.getAreaIrrig() < swIrrigAreaCalcMin) || (swSupply.getAreaIrrig() > swIrrigAreaCalcMax) ) {
				// The calculated area does not match that for the parcel - HydroBase load error?
				// - TODO smalers 2020-10-12 need to evaluate whether needs to result in command warning
				Message.printWarning(2, "", "Input data surface supply area (" + swSupply.getAreaIrrig() +
					") does not equal calculated area from total area and percent_irrig (" + swIrrigAreaCalc +
					") for location \"" + getLocationId() + "\" parcel " + getID() + " year " + getYear() );
			}
		}
	}
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateCU_Parcel parcel = (StateCU_Parcel)_original;
	super.restoreOriginal();

	this.area = parcel.area;
	this.areaUnits = parcel.areaUnits;
	this.crop = parcel.crop;
	this.dataSource = parcel.dataSource;
	this.irrigationMethod = parcel.irrigationMethod;
	this.locationId = parcel.locationId;
	this.supplyFromGWCount = parcel.supplyFromGWCount;
	this.supplyFromSWCount = parcel.supplyFromSWCount;
	this.year = parcel.year;
	_isClone = false;
	_original = null;
}

/**
Set the crop area.
@param area area to set.
*/
public void setArea(double area) {
	if (area != this.area) {
		/* TODO SAM 2006-04-09 Parcels are not currently part of the data set.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		*/
		this.area = area;
	}
}

/**
Set the area units.
@param area_units Area units to set.
*/
public void setAreaUnits(String area_units ) {
	if ( !area_units.equalsIgnoreCase(this.areaUnits) ) {
		/* TODO SAM 2006-04-09 parcels are not currently part of StateMod data set.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		*/
		this.areaUnits = area_units;
	}
}

/**
Set the crop.
@param crop Crop to set.
*/
public void setCrop(String crop ) {
	if ( !crop.equalsIgnoreCase(this.crop) ) {
		/* TODO SAM 2006-04-09 parcels are not currently part of StateMod data set.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		*/
		this.crop = crop;
	}
}

/**
Set the data source.
@param dataSource Data source to set.
*/
public void setDataSource(String dataSource ) {
	this.dataSource = dataSource;
}

/**
Set the irrigation method.
@param irrigationMethod Irrigation method to set.
*/
public void setIrrigationMethod(String irrigationMethod ) {
	if ( !irrigationMethod.equalsIgnoreCase(this.irrigationMethod) ) {
		/* TODO SAM 2006-04-09 parcels are not currently part of StateMod data set.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		*/
		this.irrigationMethod = irrigationMethod;
	}
}

/**
Set the location identifier (StateCU Location ID or StateMod node ID).
@param locationId Location ID.
*/
public void setLocationId(String locationId ) {
	this.locationId = locationId;
}

/**
Set the number of wells associated with the parcel.
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
	if ( year != this.year) {
		/* TODO SAM 2006-04-09 parcels are not currently part of StateMod data set.
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(_dataset.COMP_RESERVOIR_STATIONS,
			true);
		}
		*/
		this.year = year;
	}
}

/**
Returns a String representation of this object.
@return a String representation of this object.
*/
public String toString() {
	return super.toString() + ", " + this.crop + ", " + this.irrigationMethod + ", " +
	this.area + ", " + this.areaUnits + ", " + this.year;
}

}