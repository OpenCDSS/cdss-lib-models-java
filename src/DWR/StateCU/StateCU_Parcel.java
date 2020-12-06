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

import DWR.StateMod.StateMod_Data;
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
 * Location ID - this is redundant with the identifier of the object that has a list of these objects,
 * but is necessary when the parcel list is formed for many locations.
 * For example, this can be set to StateCU or StateMod location.
 */
//private String locationId = "";
/**
 * List of model locations used with parcels.
 * This is a list of StateCU_Location (StateCU_Data) when processing a StateCU dataset or
 * a list of StateMod_Diversion or StateMod_Well (StateMod_Data) when processing a StateMod dataset.
 * A dataset will have one or the other.
 */
private List<StateCU_Location> cuLocList = new ArrayList<>();
private List<StateMod_Data> smLocList = new ArrayList<>();

/**
 * The location identifier for CDS file that contains the parcel.
*/
// TODO smalers 2020-11-08 replaced with cuLocForCds;
//private String cdsLocationId = "";

/**
Year for the data.
*/
private int year;

/**
 * Water division for the parcel.
 */
private int div;

/**
 * Water district for the parcel.
 */
private int wd;

/**
 * Integer identifier, used to speed performance.
 * Value is consistent with base class ID.
 */
private int idInt = -1; // -1 for getter causes recompute of value from getID()

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
 * Source of the parcel data, for example "HB-PUTS" for parcel use (ditch) time series and "HB-WTP" for well to parcel.
 * The first source used for the parcel will be assigned.
 */
// TODO smalers 2020-11-05 move to StateCU_Supply since the source is derived from diversion or well relationship.
//private String dataSource = "";

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
 *  General error string, for example no supplies.
 *  The setError() method should be used to set as one or more phrases, each ending in period.
 */
private String error = "";

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
Append to after the same year.
*/
public void addSupply ( StateCU_Supply supply ) {
	// Only add the supply if not already added
	// - don't compare blank strings
	boolean found = false;
	StateCU_SupplyFromGW supplyFromGW, supplyFromGW0;
	StateCU_SupplyFromSW supplyFromSW, supplyFromSW0;
	String supplyType = "";
	for ( StateCU_Supply supply0 : this.supplyList ) {
		if ( (supply instanceof StateCU_SupplyFromSW) && (supply0 instanceof StateCU_SupplyFromSW)) {
			supplyFromSW = (StateCU_SupplyFromSW)supply;
			supplyFromSW0 = (StateCU_SupplyFromSW)supply0;
			supplyType = "SW";
			if ( !supplyFromSW.getWDID().isEmpty() && supplyFromSW.getWDID().equals(supplyFromSW0.getWDID()) ) {
				// Supply can be well via ditch relationship and separate well-only lands.
				// Don't re-add the supply.
				found = true;
				break;
			}
		}
		else if ( (supply instanceof StateCU_SupplyFromGW) && (supply0 instanceof StateCU_SupplyFromGW) ) {
			supplyFromGW = (StateCU_SupplyFromGW)supply;
			supplyFromGW0 = (StateCU_SupplyFromGW)supply0;
			supplyType = "GW";
			if ( (!supplyFromGW.getWDID().isEmpty() && (supplyFromGW.getWDID().equals(supplyFromGW0.getWDID()))) ||
				(!supplyFromGW.getReceipt().isEmpty() && (supplyFromGW.getReceipt().equals(supplyFromGW0.getReceipt()))) ) { 
				// Supply can be well via ditch relationship and separate well-only lands.
				// Don't re-add the supply.
				// - TODO smalers 2020-11-08 this could be an issue if WDID matches a receipt,
				//   but unlikely to occur at the same parcel.
				found = true;
				break;
			}
		}
	}
	if ( !found ) {
		this.supplyList.add ( supply );
		boolean debug = Message.isDebugOn;
		debug = true;
		if ( debug ) {
			Message.printDebug(2,"addSupply", "        Adding " + supplyType + " supply for year " +
				this.getYear() + " parcel ID " + getID() + " supply ID " + supply.getID() );
		}
		// Assume that this will require a recompute of calculated values.
		this.setDirty(true);
	}
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
Returns the area for the parcel (acres).
@return the area for the parcel (acres).
*/
public double getArea() {
	return this.area;
}

/**
Returns the area units for the parcel.
@return the area units for the parcel.
*/
public String getAreaUnits() {
	return this.areaUnits;
}

/**
Returns the CDS location identifier.
@return the CDS location identifier.
*/
/* TODO smalers 2020-11-08 replace with culocForCds
public String getCdsLocationId() {
	return this.cdsLocationId;
}
*/

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
/* TODO smalers 2020-11-05 moved to StateCU_Supply
public String getDataSource() {
	return this.dataSource;
}
*/

/**
Returns the water division for the parcel.
@return the water division for the parcel.
*/
public int getDiv() {
	return this.div;
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
public int getIdInt() {
	if ( this.idInt < 0 ) {
		this.idInt = Integer.parseInt(getID());
	}
	return this.idInt;
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
/* TODO smalers 2020-11-05 change to list of model objects.
public String getLocationId() {
	return this.locationId;
}
*/

/**
 * Return a formatted string containing the list of model identifiers.
 * All model identifiers are included, separated by commas.
 */
public String getModelIdListString () {
	StringBuilder b = new StringBuilder();
	if ( this.cuLocList.size() > 0 ) {
		for ( int i = 0; i < this.cuLocList.size(); i++ ) {
			if ( i > 0 ) {
				b.append(", ");
			}
			b.append(this.cuLocList.get(i).getID());
		}
	}
	else {
		for ( int i = 0; i < this.smLocList.size(); i++ ) {
			if ( i > 0 ) {
				b.append(", ");
			}
			b.append(this.smLocList.get(i).getID());
		}
	}
	return b.toString();
}

/**
 * Returns the StateCU_Location at an index.
 * @return the StateCU_Location at an index.
 */
public StateCU_Location getStateCULocation ( int index ) {
	return this.cuLocList.get(index);
}

/**
 * Returns the StateMod_Data at an index.
 * @return the model location at an index.
 */
public StateMod_Data getStateModStation ( int index ) {
	return this.smLocList.get(index);
}

/**
 * Returns the model location list size.
 * @return the model location list size.
 */
public int getModelLocListSize () {
	// Return the size of the largest list:
	// - only one should be non-zero
	if ( this.cuLocList.size() > 0 ) {
		return cuLocList.size();
	}
	else {
		return this.smLocList.size();
	}
}

/**
Returns the count of groundwater supply.
@return the count of groundwater supply.
*/
public int getSupplyFromGWCount() {
	if ( this.isDirty() ) {
		// Need to recompute because something derived data are not current.
		recompute();
	}
	return this.supplyFromGWCount;
}

/**
Returns the count of surface water supply.
@return the count of surface water supply.
*/
public int getSupplyFromSWCount() {
	if ( this.isDirty() ) {
		// Need to recompute because something derived data are not current.
		recompute();
	}
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
Returns the water district for the parcel.
@return the water district for the parcel.
*/
public int getWD() {
	return this.wd;
}

/**
Returns the year for the parcel.
@return the year for the parcel.
*/
public int getYear() {
	return this.year;
}

/**
Indicate whether the parcel has groundwater supply.  This will be true if
any of the StateCU_Supply associated with the parcel return isGroundWater as true.
*/
public boolean hasGroundWaterSupply () {
	// The following checks whether recompute is needed.
	if ( getSupplyFromGWCount() > 0 ) {
		return true;
	}
	else {
		return false;
	}
}

/**
Indicate whether the parcel has surface water supply.  This will be true if
any of the StateCU_Supply associated with the parcel return isSurfaceWater as true.
*/
public boolean hasSurfaceWaterSupply () {
	// The following checks whether recompute is needed.
	if ( getSupplyFromSWCount() > 0 ) {
		return true;
	}
	else {
		return false;
	}
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
	// TODO smalers 2020-11-05 
	//this.locationId = "";
	this.supplyFromGWCount = 0;
	this.supplyFromSWCount = 0;
	this.year = StateCU_Util.MISSING_INT;
	// Dirty until a data request is made
	this.setDirty(true);
}

/**
 * Recalculate derived data.
 * This should be called in lazy fashion when retrieving derived values and the object is dirty.
 * <ul>
 * <li>counts of well and ditch supply - to avoid looping in repeated calls and </li>
 * This method is called if the object is detected to be in a dirty state,
 * meaning data have been set but derived values have not been updated.
 */
public void recompute () {
	if ( !this.isDirty() ) {
		// No reason to recompute
		return;
	}
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

	// Loop through the well supply parcels and update the irrigAreaFraction and irrigArea based on count
	// - divide the parcel area by the number of wells
	// - divide the diversion area by the number of diversions
	for ( StateCU_Supply supply : this.supplyList ) {
		if ( supply instanceof StateCU_SupplyFromGW ) {
			StateCU_SupplyFromGW supplyFromGW = (StateCU_SupplyFromGW)supply;
			if ( this.supplyFromGWCount == 0 ) {
				supplyFromGW.setAreaIrrigFraction(0.0);
			}
			else {
				supplyFromGW.setAreaIrrigFraction(1.0/this.supplyFromGWCount);
			}
			double areaIrrigFractionDW = 1.0;
			if ( this.supplyFromSWCount > 1 ) {
				areaIrrigFractionDW = 1.0/this.supplyFromSWCount;
			}
			supplyFromGW.setAreaIrrig(this.area*supplyFromGW.getAreaIrrigFraction()*areaIrrigFractionDW);
		}
		else if ( supply instanceof StateCU_SupplyFromSW ) {
			// TODO smalers 2020-02-17 this is currently handled via HydroBase data when read
			// - percent irrig
			// - recalculate and see if the number is accurate
			StateCU_SupplyFromSW supplyFromSW = (StateCU_SupplyFromSW)supply;

			if ( this.supplyFromSWCount == 0 ) {
				// This should never happen because the list has at least one item
				supplyFromSW.setAreaIrrigFraction(0.0);
			}
			else {
				supplyFromSW.setAreaIrrigFraction(1.0/this.supplyFromSWCount);
			}
			supplyFromSW.setAreaIrrig(this.area*supplyFromSW.getAreaIrrigFraction());

			// Verify that the calculated area for the supply matches the HydroBase value
			// - this is in StateCU_Location validation
			// - also set a value in the supply to allow output in parcel report
			// - the tolerance on the comparison is important - making too tight results in errors comparing numbers
			
			// Precision of 1 avoids warnings except for "large" differences
			// - this should the same as the default for CheckParcels(AreaFormat) parameter. 
			int areaPrecision = 1;
			String areaFormat = "%." + areaPrecision + "f";
			if ( !String.format(areaFormat, supplyFromSW.getAreaIrrigFraction()).equals(
				String.format(areaFormat, supplyFromSW.getAreaIrrigFractionHydroBase())) ) {
				//Message.printWarning(3, "", "Calculated supply fraction is " + supplyFromSW.getAreaIrrigFraction() +
				//	" HydroBase fraction is " + supplyFromSW.getAreaIrrigFractionHydroBase() );
				supplyFromSW.setAreaIrrigFractionHydroBaseError("ERROR");
			}
			else {
				supplyFromSW.setAreaIrrigFractionHydroBaseError("");
			}
		}
	}
	
	// If parcel has a surface supply, add to the CDS
	/* TODO smalers 2020-11-06 fix this
	for ( StateCU_Supply supply : this.supplyList ) {
		if ( supply instanceof StateCU_SupplyFromSW ) {
			StateCU_SupplyFromSW supplyFromSW = (StateCU_SupplyFromSW)supply;
			supplyFromSW.setIncludeInCdsArea(false);
		}
		else if ( supply instanceof StateCU_SupplyFromGW ) {
			if ( this.getSupplyFromSWCount() > 0 ) {
				supplyFromGW.setIncludeInCdsArea(false);
			}
		}
	}
	*/

	// Set not dirty
	this.setDirty(false);
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
	// TODO smalers 2020-11-05 moved to StateCU_Supply
	// this.dataSource = parcel.dataSource;
	this.irrigationMethod = parcel.irrigationMethod;
	// TODO smalers 2020-11-05 change from single string since multiple locations can be associated with the parcel
	//this.locationId = parcel.locationId;
	this.cuLocList = parcel.cuLocList;
	this.smLocList = parcel.smLocList;
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
Set the CDS location identifier (StateCU Location) where the parcel is counted.
@param locationId Location ID.
*/
//public void setCdsLocationId ( String cdsLocationId ) {
	//this.cdsLocationId = cdsLocationId;
//}

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
/* TODO smalers 2020-11-05 moved to StateCU_Supply
public void setDataSource(String dataSource ) {
	this.dataSource = dataSource;
}
*/

/**
Set the water division associated with the crop.
@param wd water division to set.
*/
public void setDiv(int div) {
	if ( div != this.div) {
		this.div = div;
	}
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
			this.error  = error;
		}
		else {
			// Append by adding a space.  Assume that each error is a sentence with trailing period.
			this.error = this.error + " " + error;
		}
	}
	else {
		// Just set.
		this.error  = error;
	}
}

/**
Set the water district associated with the crop.
@param wd water district to set.
*/
public void setIdInt(int idInt) {
	this.idInt = idInt;
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
/*
public void setLocationId(String locationId ) {
	this.locationId = locationId;
}
*/

/**
Set the StateCU_Location associated with the parcel.
If already in the list it won't be added again.
@param culoc StateCU_Location
*/
public void setStateCULocation(StateCU_Location culoc) {
	boolean found = false;
	String culocId = culoc.getID();
	for ( StateCU_Location culoc2 : this.cuLocList ) {
		if ( culocId.equals(culoc2.getID())) {
			found = true;
			break;
		}
	}
	if ( !found ) {
		this.cuLocList.add(culoc);
	}
}

/**
Set the water district from the parcel ID.
Parcel ID is of the form 21011060, where first digit is division, digits 2-3 are the water district.
@param parcelID containing the water district.
*/
public void setWDFromParcelID( int parcelID ) {
	if ( parcelID > 100 ) {
		String s = "" + parcelID;
		this.wd = Integer.parseInt(s.substring(1,3));
	}
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
Set the water district associated with the crop.
@param wd water district to set.
*/
public void setWD(int wd) {
	if ( wd != this.wd) {
		this.wd = wd;
	}
}

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