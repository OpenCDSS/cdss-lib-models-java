// StateCU_Location - class to hold StateCU Location data, compatible with StateCU STR file

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
Class to hold StateCU Location data for StateCU/StateDMI, compatible with the StateCU STR file.
*/
public class StateCU_Location extends StateCU_Data
implements StateCU_ComponentValidator
{

private StateCU_Location_CollectionType __collection_type = null;

/**
Collection part type (see COLLECTION_PART_TYPE_*), used by DMI software.
*/
private StateCU_Location_CollectionPartType __collection_part_type = null;

/**
The identifiers for data that are collected - null if not a collection
location.  This is a List of Lists corresponding to each __collectionYear element.
If the list of identifiers is consistent for the entire period then the
__collectionYear array will have a size of 0 and the __collectionIDList will be a single list.
The items in this list should be consistent in order and number with the __collectionIDTypeList and __collectionIDWDList.
*/
private List<List <String>> __collectionIDList = null;

/**
The identifiers types for data that are collected - null if not a collection location.
This is a List of Lists corresponding to each __collectionYear element.
If the list of identifiers is consistent for the entire period then the
__collectionYear array will have a size of 0 and the __collectionIDTypeList will be a single list.
This list is only used for well collections that use well identifiers (WDID or Receipt) for the parts.
The items in this list should be consistent in order and number with the __collectionIDWDList and __collectionIDList.
*/
private List<List<StateCU_Location_CollectionPartIdType>> __collectionIDTypeList = null;

/**
The WDs for data that are collected - null if not a collection location.
This is a List of Lists corresponding to each __collectionYear element.
If the list of identifiers is consistent for the entire period then the
__collectionYear array will have a size of 0 and the __collectionIDWDList will be a single list.
This list is only used for well collections that use well identifiers (WDID or Receipt) for the parts.
This is needed for receipts in particular because the WD for cached data lookups cannot be determined from WDID.
The items in this list should be consistent in order and number with the __collectionIDTypeList and __collectionIDList.
*/
private List<List<Integer>> __collectionIDWDList = null;

/**
An array of years that correspond to the aggregate/system.  Parcel
collections can have multiple years but ditches currently only have one year.
*/
private int [] __collectionYear = null;

/**
The division that corresponds to the aggregate/system.  Currently
it is expected that the same division number is assigned to all the data.
*/
private int __collection_div = StateCU_Util.MISSING_INT;

/** 
CULocation Elevation.
*/
private double __elevation = StateCU_Util.MISSING_DOUBLE;

/** 
CULocation Latitude.
*/
private double __latitude = StateCU_Util.MISSING_DOUBLE;

/**
Region 1 (e.g., County).
*/
private String __region1 = StateCU_Util.MISSING_STRING;

/**
Region 2 (e.g., HUC).
*/
private String __region2 = StateCU_Util.MISSING_STRING;

/**
Available water content (AWC).
*/
private double __awc = StateCU_Util.MISSING_DOUBLE;

/**
Orographic temperature adjustment (DEGF/1000 FT) - set to 0.0 because this
means no adjustment.  The data will be reset (not filled) if needed.  The size
is the number of climate stations.
*/
private double[] __ota;

/**
Orographic precipitation adjustment (fraction) - set to 1.0 because this
means no adjustment.  The data will be reset (not filled) if needed.  The size
is the number of climate stations.
*/
private double[] __opa;

/**
Number of stations.
*/
private String[] __climate_station_ids = null;
private double[] __precipitation_station_weights = null;
private double[] __temperature_station_weights = null;

/**
The list of StateCU_Parcel observations, as an archive of observations to use with data processing.
These are read from HydroBase by StateDMI ReadCULocationParcelsFromHydroBase command.
*/
private List<StateCU_Parcel> __parcelList = new ArrayList<>();

/**
 * Indicate whether any SetCropPatternTS() commands are used in StateDMI.
 * - used with parcel report output for troubleshooting
 * - a list of years that are set are saved, to allow comparing with irrigated lands assessment years
 */
private List<Integer> __hasSetCropPatternTSCommands = new ArrayList<>();

/**
 * Indicate whether any SetIrrigationPracticeTS() commands are used in StateDMI.
 * - used with parcel report output for troubleshooting
 * - a list of years that are set are saved, to allow comparing with irrigated lands assessment years
 */
private List<Integer> __hasSetIrrigationPracticeTSCommands = new ArrayList<>();

/**
 * Indicate whether any FillCropPatternTS() commands are used in StateDMI.
 * - used with parcel report output for troubleshooting
 * - a list of years that are set are saved, to allow comparing with irrigated lands assessment years
 */
private List<Integer> __hasFillCropPatternTSCommands = new ArrayList<>();

/**
 * Indicate whether any FillIrrigationPracticeTS() commands are used in StateDMI.
 * - used with parcel report output for troubleshooting
 * - a list of years that are set are saved, to allow comparing with irrigated lands assessment years
 */
private List<Integer> __hasFillIrrigationPracticeTSCommands = new ArrayList<>();

/**
 * Location type, initially implemented for use with the Parcel data component.
 */
private StateCU_LocationType __locationType = StateCU_LocationType.UNKNOWN;

/**
Construct a StateCU_Location instance and set to missing and empty data.
*/
public StateCU_Location()
{	super();
}

/**
 * Add a parcel to the parcel list for the location if not already added.
 * Any additions to the supply should be handled elsewhere, such as when creating/updating the parcel object during read.
 */
public void addParcel ( StateCU_Parcel parcelToAdd ) {
	StateCU_Parcel parcelFound = null;
	for ( StateCU_Parcel parcel : this.__parcelList ) {
		if ( (parcel.getYear() == parcelToAdd.getYear()) && parcel.getID().equals(parcelToAdd.getID()) ) {
			// Found an existing parcel.
			parcelFound = parcel;
			break;
		}
	}
	/*
	if ( parcelFound != null ) {
		// Add the supply to the existing found parcel if it has not already been added
		for ( StateCU_Supply supply : parcelToAdd.getSupplyList() ) {
			parcelFound.addSupply(supply);
		}
	}
	else {
	*/
	if ( parcelFound == null ) {
		// Add the new parcel to the list:
		// - add after the same year, if was already added so that year lines up for main model node ID
		boolean sortByYear = true;
		if ( (this.__parcelList.size() == 0) || !sortByYear ) {
			// Just add at the end.
			this.__parcelList.add(parcelToAdd);
		}
		else {
			// Have at least one parcel.
			// Search to insert at end of matched year or before higher year, for example:
			//    1954
			//    1954
			//    1970
			//    1970
			//    1980
			//    1980
			StateCU_Parcel parcel = null;
			int ifound = -1;
			for ( int i = 0; i < this.__parcelList.size(); i++ ) {
				parcel = this.__parcelList.get(i);
				if ( parcel.getYear() > parcelToAdd.getYear() ) {
					ifound = i;
					break;
				}
			}
			if ( ifound >= 0 ) {
				// Found a year that is <= the current year.
				// Add after that year, may be a new slot in the list.
				this.__parcelList.add(ifound,parcelToAdd);
			}
			else {
				// Did not find a year that is > the current year so add at the end.
				this.__parcelList.add(parcelToAdd);
			}
		}
	}
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = clone();
	((StateCU_Location)_original)._isClone = false;
	_isClone = true;
}

/**
 * Get a list of all parcel years.
 * This is used to know which years to zero out when there are no crops in the year,
 * such as with the StateDMI ReadCropPatternTSFromParcels command.
 * @param culocList list of StateCU_Location to process.
 * @param yearStart start year to limit search.
 * @param endStart end year to limit search.
 */
public static List<Integer> getParcelYears ( List<StateCU_Location> culocList, int yearStart, int yearEnd ) {
	List<Integer> parcelYears = new ArrayList<>();
	int parcelYear;
	boolean found = false;
	for ( StateCU_Location culoc : culocList ) {
		List<StateCU_Parcel> parcelList = culoc.getParcelList();
		for ( StateCU_Parcel parcel : parcelList ) {
			parcelYear = parcel.getYear();
			if ( parcelYear <= 0 ) {
				// Missing parcel year.
				continue;
			}
			if ( (yearStart > 0) && (parcelYear < yearStart) ) {
				// Parcel year is before requested start.
				continue;
			}
			if ( (yearEnd > 0) && (parcelYear > yearEnd) ) {
				// Parcel year is after requested end.
				continue;
			}
			// See if the parcel year has already been added
			found = false;
			for ( Integer iparcel : parcelYears ) {
				if ( iparcel.equals(parcelYear) ) {
					found = true;
					break;
				}
			}
			if ( !found ) {
				parcelYears.add ( Integer.valueOf(parcelYear) );
			}
		}
	}
	return parcelYears;
}

/**
Return the AWC.
@return the AWC.
*/
public double getAwc()
{	return __awc;
}

/**
Return the AWC.
@return the AWC.
*/
public double getAWC()
{	return __awc;
}

/**
Return the collection part division the specific year.  Currently it is
expected that the user always uses the same division.
@return the division for the collection, or 0.
*/
public int getCollectionDiv ()
{	return __collection_div;
}

/**
Return the collection part ID list.
This is used with the new design where collection information for ditches and wells
is the same regardless of year.
@return the list of collection part IDS, or null if not defined.
*/
public List<String> getCollectionPartIDs () {
	return getCollectionPartIDsForYear ( 0 );
}

/**
Return the collection part ID list for the specific year.  For ditches, only one
aggregate/system list is currently supported so the same information is returned
regardless of the year value.  For wells, if the collection part type is WELL, the list is the same for all years,
or if PARCEL, for a specific year.
@param year The year of interest, only used for well identifiers when collection is specified with parcels.
@return the list of collection part IDS, or null if not defined.
*/
public List<String> getCollectionPartIDsForYear ( int year )
{	if ( (__collectionIDList == null) || (__collectionIDList.size() == 0) ) {
		return null;
	}
	if ( __collection_part_type == StateCU_Location_CollectionPartType.DITCH ) {
		// The list of part IDs will be the first and only list (same for all years)...
		return __collectionIDList.get(0);
	}
	else if ( __collection_part_type == StateCU_Location_CollectionPartType.WELL ) {
		// The list of part IDs will be the first and only list (same for all years)...
		return __collectionIDList.get(0);
	}
	else if ( __collection_part_type == StateCU_Location_CollectionPartType.PARCEL ) {
		// The list of part IDs needs to match the year.
		for ( int i = 0; i < __collectionYear.length; i++ ) {
			if ( year == __collectionYear[i] ) {
				return __collectionIDList.get(i);
			}
		}
	}
	return null;
}

/**
Return the collection part ID type list.  This is used with well locations when aggregating
by well identifiers (WDIDs and permit receipt numbers).
@return the list of collection part ID types, or null if not defined.
*/
public List<StateCU_Location_CollectionPartIdType> getCollectionPartIDTypes () {
	if (__collectionIDTypeList == null ) {
		return null;
	}
	else {
		return __collectionIDTypeList.get(0); // Currently does not vary by year
	}
}

/**
 * Return the WD for the collection part.
 * This is used when getting the WD for receipt so that data query can occur.
 * @param partID part identifier to search for
 * @return the WD for the requested part ID, or null if not found.
 */
public Integer getCollectionPartIDWD ( String partId ) {
	// Current approach (2020) is to not use year
	List<String> partIds = this.__collectionIDList.get(0);
	for ( int i = 0; i < partIds.size(); i++ ) {
		if ( partId.equals(partIds.get(i)) ) {
			return this.__collectionIDWDList.get(0).get(i);
		}
	}
	return null;
}

/**
Return the collection part ID WD list.  This is used with well locations when aggregating
by well identifiers (WDIDs and permit receipt numbers), to store WD for the parts.
@return the list of collection part WDs, or null if not defined.
*/
public List<Integer> getCollectionPartIDWDs () {
	if (__collectionIDWDList == null ) {
		return null;
	}
	else {
		return __collectionIDWDList.get(0); // Currently does not vary by year
	}
}

/**
Return the collection part type, DITCH, PARCEL, or WELL.
*/
public StateCU_Location_CollectionPartType getCollectionPartType()
{	return __collection_part_type;
}

/**
Return the collection type, "Aggregate", "System", or "MultiStruct".
@return the collection type, "Aggregate", "System", or "MultiStruct".
*/
public StateCU_Location_CollectionType getCollectionType()
{	return __collection_type;
}

/**
Return the array of years for the defined collections.
@return the array of years for the defined collections.
*/
public int [] getCollectionYears ()
{	return __collectionYear;
}

/**
Returns the data column header for the specifically checked data.
@return Data column header.
 */
public static String[] getDataHeader()
{
	// TODO KAT 2007-04-12 
	// When specific checks are added to checkComponentData
	// return the header for that data here
	return new String[] {};
}

/**
Get the climate station identifier.
@return Climate station identifier or "" if not available.
@param pos Station index (relative to zero).
*/
public String getClimateStationID ( int pos )
{	if ( __climate_station_ids == null ) {
		return "";
	}
	if ( (pos >= 0) && (pos < __climate_station_ids.length) ) {
		return __climate_station_ids[pos];
	}
	else {
		return "";
	}
}

/**
Return the full parcel list.
@return the list of StateCU_Parcel
*/
public List<StateCU_Parcel> getParcelList () {
	return this.__parcelList;
}

/**
Return the full parcel list for the requested year.
@param year requested year for parcels
@return the list of StateCU_Parcel for the requested year
*/
public List<StateCU_Parcel> getParcelList ( int year ) {
	List<StateCU_Parcel> parcelList = new ArrayList<>();
	for ( StateCU_Parcel parcel : this.__parcelList ) {
		if ( parcel.getYear() == year ) {
			parcelList.add(parcel);
		}
	}
	return parcelList;
}

/**
Return the list of year for parcels associated with the location, sorted.
@return the list of year for parcels associated with the location, sorted.
*/
public List<Integer> getParcelYearList () {
	List<Integer> parcelYearList = new ArrayList<>();
	boolean found;
	for ( StateCU_Parcel parcel : this.__parcelList ) {
		found = false;
		for ( Integer year : parcelYearList ) {
			if ( year.equals(parcel.getYear()) ) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			parcelYearList.add(Integer.valueOf(parcel.getYear()));
		}
	}
	Collections.sort(parcelYearList);
	return parcelYearList;
}

/**
Return the elevation.
@return the elevation.
*/
public double getElevation()
{	return __elevation;
}

/**
 * Return the list of groundwater supplies for the location.
 * The list of supplies includes only unique instances of the supply.
 * In other words, if parcels associated with the location return the same well more than once,
 * the well will only be added to the returned list once.
 */
public List<StateCU_SupplyFromGW> getGroundwaterSupplies() {
	List<StateCU_SupplyFromGW> supplyFromGWList = new ArrayList<>();
	StateCU_SupplyFromGW supplyFromGW = null;
	for ( StateCU_Parcel parcel : getParcelList() ) {
		for ( StateCU_Supply supply : parcel.getSupplyList() ) {
			// Only get rights for modeled supplies.
			// - handled in StateDMI
			if ( supply.getIsModeled() ) {
				if ( supply instanceof StateCU_SupplyFromGW ) {
					// Groundwater supply.
					// - add if not already in the list
					// - can have situation where sometimes receipt is blank so can't just compare WDID and the receipt.
					supplyFromGW = (StateCU_SupplyFromGW)supply;
					boolean found = false;
					for ( StateCU_SupplyFromGW supply0 : supplyFromGWList ) {
						if ( supply0.getWDID().isEmpty() && !supply0.getReceipt().isEmpty() && supply0.getReceipt().equals(supplyFromGW.getReceipt()) ) {
							// Only have receipt.
							found = true;
							break;
						}
						else if ( !supply0.getWDID().isEmpty() && supply0.getWDID().equals(supplyFromGW.getWDID() ) ) {
							// Have WDID so use it.
							found = true;
							break;
						}
					}
					if ( !found ) {
						// TODO smalers 2021-12-22 Use the following for troubleshooting, found that sometimes receipt is set and sometimes blank for the same WDID.
						//Message.printStatus(2,"xxx","Adding groundwater supply wdid=\"" + supplyFromGW.getWDID() + "\" receipt=\"" + supplyFromGW.getReceipt() + "\"" );
						supplyFromGWList.add(supplyFromGW);
					}
				}
			}
		}
	}
	return supplyFromGWList;
}

/**
Return the latitude.
@return the latitude.
*/
public double getLatitude()
{	return __latitude;
}

/**
Return the location type.
@return the location type.
*/
public StateCU_LocationType getLocationType()
{	return this.__locationType;
}

/**
Return the number of climate stations.
@return the number of climate stations.
*/
public int getNumClimateStations()
{	if ( __climate_station_ids == null ) {
		return 0;
	}
	else {
		return __climate_station_ids.length;
	}
}

/**
Return the orographic precipitation adjustment factor.
@param pos Index (0+) for climate (precipitation) station.
@return the orographic precipitation adjustment factor.
*/
public double getOrographicPrecipitationAdjustment(int pos) {
	if ( (__opa == null) || (pos >= __opa.length) ) {
		return StateCU_Util.MISSING_DOUBLE;
	}
	else {
		return __opa[pos];
	}
}

/**
Return the orographic temperature adjustment factor.
@param pos Index (0+) for climate (temperature) station.
@return the orographic temperature adjustment factor.
*/
public double getOrographicTemperatureAdjustment(int pos) {
	if ( (__ota == null) || (pos >= __ota.length) ) {
		return StateCU_Util.MISSING_DOUBLE;
	}
	else {
		return __ota[pos];
	}
}

/**
 * Return an instance of StateCU_Parcel_Validator, to check StateCU_Parcel instances for
 * a StateCU_Location.
 * This is used with the CheckParcels command to validate parcels for a CU Location.
 * @param culocList a list of all CU Locations for deep check.
 * @param deepCheck whether to perform deep checks, including confirming that parcel only shows up in
 * @param areaPrecision number of digits to perform area comparisons
 * @return StateCU_Parcel_Validator instance that can be used to validate the StateCU_Parcel for the StateCU_Location.
 */
public StateCU_ComponentValidator getParcelValidator ( List<StateCU_Location> culocList, boolean deepCheck, int areaPrecision ) {
	// Return a new instance of the validator since there are not that many CU Locations.
	return new StateCU_Location_ParcelValidator(this, culocList, deepCheck, areaPrecision );
}

/**
Return the precipitation station weight.
@param pos Index (0+) for climate (precipitation) station.
@return the precipitation station weight.
*/
public double getPrecipitationStationWeight(int pos) {
	if ( (__precipitation_station_weights == null) || (pos >= __precipitation_station_weights.length) ) {
		return StateCU_Util.MISSING_DOUBLE;
	}
	else {
		return __precipitation_station_weights[pos];
	}
}

/**
Return the temperature station weight.
@param pos Index (0+) for climate (temperature) station.
@return the temperature station weight.
*/
public double getTemperatureStationWeight(int pos) {
	if ( (__temperature_station_weights == null) || (pos >= __temperature_station_weights.length) ) {
		return StateCU_Util.MISSING_DOUBLE;
	}
	else {
		return __temperature_station_weights[pos];
	}
}

/**
Return region 1.
@return region 1.
*/
public String getRegion1()
{	return __region1;
}

/**
Return region 2.
@return region 2.
*/
public String getRegion2()
{	return __region2;
}

public List<List<String>> getTemp() {
	return __collectionIDList;
}

/**
Indicate whether the CU Location has groundwater only supply.
This will be the case if the location is a collection with part type of "Parcel" or "Well" (WDID or Receipt).
IMPORTANT:  The determination is made based on whether a well collection,
not by checking supply of parcels associated with the model node.
*/
public boolean isGroundwaterOnlySupplyModelNode ()
{	StateCU_Location_CollectionPartType collectionPartType = getCollectionPartType();
	if ( isCollection() && ((collectionPartType == StateCU_Location_CollectionPartType.PARCEL) ||
		(collectionPartType == StateCU_Location_CollectionPartType.WELL)) ) {
		// TODO SAM 2007-05-11 Rectify part types with StateMod
		return true;
	}
	return false;
}

/**
 * Indicate whether the location has any FillCropPatternTS*() commands in StateDMI.
 * @param year the year to check whether a set command was used
 * @return true if FillCropPatternTS*() commands are used in a StateDMI command file
 */
public boolean hasFillCropPatternTSCommands( int year ) {
	for ( Integer year0 : this.__hasFillCropPatternTSCommands ) {
		if ( year0.intValue() == year ) {
			return true;
		}
	}
	return false;
}

/**
 * Indicate whether the location has any FillIrrigationPracticeTS*() commands in StateDMI.
 * @param year the year to check whether a set command was used
 * @return true if FillIrrigationPracticeTS*() commands are used in a StateDMI command file
 */
public boolean hasFillIrrigationPracticeTSCommands( int year ) {
	for ( Integer year0 : this.__hasFillIrrigationPracticeTSCommands ) {
		if ( year0.intValue() == year ) {
			return true;
		}
	}
	return false;
}

/**
 * Indicate whether the location has any SetCropPatternTS*() commands in StateDMI.
 * @param year the year to check whether a set command was used
 * @return true if SetCropPatternTS*() commands are used in a StateDMI command file
 */
public boolean hasSetCropPatternTSCommands( int year ) {
	for ( Integer year0 : this.__hasSetCropPatternTSCommands ) {
		if ( year0.intValue() == year ) {
			return true;
		}
	}
	return false;
}

/**
 * Indicate whether the location has any SetIrrigationPracticeTS*() commands in StateDMI.
 * @param year the year to check whether a set command was used
 * @return true if SetIrrigationPracticeTS*() commands are used in a StateDMI command file
 */
public boolean hasSetIrrigationPracticeTSCommands( int year ) {
	for ( Integer year0 : this.__hasSetIrrigationPracticeTSCommands ) {
		if ( year0.intValue() == year ) {
			return true;
		}
	}
	return false;
}

/**
Indicate whether the CU Location has surface water supply.  This will
be the case if the location is NOT a groundwater only supply location.
This is the opposite of isGroundwaterOnlySupplyModelNode() result.
*/
public boolean hasSurfaceWaterSupplyForModelNode ()
{
	if ( isGroundwaterOnlySupplyModelNode() ) {
		return false;
	}
	return true;
}

/**
Indicate whether the CU Location is a collection (an aggregate or system).
@return true if the CU Location is an aggregate or system.
*/
public boolean isCollection()
{	if ( __collectionIDList == null ) {
		return false;
	}
	else {
		return true;
	}
}

/**
 * Indicate whether the specified WDID identifier matches the CU Location or is in a collection.
 * This assumes that the newer approach is used, NOT parcel aggregation.
 * @param wdid WDID identifier to search for in collection ID list for the location, used with ditches.
 * @return true if the provided identifier is in the list of identifiers in the collection, of any type.
 */
public boolean idIsIn(String wdid) {
	return idIsIn(wdid, null);
}

/**
 * Indicate whether the specified identifier matches the CU Location or is in a collection.
 * This assumes that the newer approach is used, NOT parcel aggregation.
 * The receipt is used if location is a collection and part identifier is receipt.
 * @param wdid WDID identifier to search for in collection ID list for the location, used with wells and ditches.
 * @param receipt receipt identifier to search for in collection ID list for the location, used with wells.
 * @return true if the provided identifier is in the list of identifiers in the collection, of any type.
 */
public boolean idIsIn(String wdid, String receipt) {
	if ( !isCollection() ) {
		// Not a collection, only WDID for surface water supported
		return wdid.equalsIgnoreCase(getID());
	}
	else {
		// Is a collection
		List<String> collectionIdList = getCollectionPartIDsForYear ( 0 );
		if ( collectionIdList != null ) {
			if ( __collection_part_type == StateCU_Location_CollectionPartType.DITCH ) {
				for ( int i = 0; i < collectionIdList.size(); i++ ) {
					if ( collectionIdList.get(i).equalsIgnoreCase(wdid) ) {
						// Provided WDID matches a collection part wdid
						return true;
					}
				}
			}
			else if ( __collection_part_type == StateCU_Location_CollectionPartType.WELL ) {
				List<StateCU_Location_CollectionPartIdType> collectionIdPartTypeList = getCollectionPartIDTypes();
				for ( int i = 0; i < collectionIdList.size(); i++ ) {
					// Well so check the part ID properly
					if ( collectionIdPartTypeList.get(i) == StateCU_Location_CollectionPartIdType.WDID ) {
						if ( !wdid.isEmpty() && collectionIdList.get(i).equalsIgnoreCase(wdid) ) {
							return true;
						}
					}
					else if ( collectionIdPartTypeList.get(i) == StateCU_Location_CollectionPartIdType.RECEIPT ) {
						if ( !receipt.isEmpty() && collectionIdList.get(i).equalsIgnoreCase(receipt) ) {
							return true;
						}
					}
				}
			}
		}
	}
	return false;
}

/**
 * Indicate whether the specified identifier is in a collection.
 * This assumes that the newer approach is used, NOT parcel aggregation.
 * @param id identifier to search for in collection ID list for the location.
 * @return true if the provided identifier is in the list of identifiers in the collection, of any type.
 */
public boolean isIdInCollection(String id) {
	if ( !isCollection() ) {
		// Not a collection
		return false;
	}
	else {
		// Is a collection
		List<String> collectionIdList = getCollectionPartIDsForYear ( 0 );
		for ( String collectionId : collectionIdList ) {
			if ( collectionId.equalsIgnoreCase(id) ) {
				return true;
			}
		}
	}
	return false;
}

/**
 * Lookup a StateCU_Location in a list, using the ID.
 * @param culocList list of StateCU_Location to search.
 * @param id identifier to match
 * @return matching SateCU_Location or null if not matched
 */
public static StateCU_Location lookupForId ( List<StateCU_Location> culocList, String id ) {
	if ( culocList == null ) {
		return null;
	}
	for ( StateCU_Location culoc : culocList ) {
		if ( culoc.getID().equals(id) ) {
			return culoc;
		}
	}
	return null;
}

/**
 * Lookup a StateCU_Location well location that has collection part ID that matches the given IDs,
 * for example WDID and receipt.
 * @param culocList list of StateCU_Location to search.
 * @param wdid first identifier to match
 * @param receipt second identifier to match
 * @return matching SateCU_Location or null if not matched
 */
public static StateCU_Location lookupForWellCollectionPartId ( List<StateCU_Location> culocList, String wdid, String receipt ) {
	if ( culocList == null ) {
		return null;
	}
	for ( StateCU_Location culoc : culocList ) {
		if ( culoc.isCollection() ) {
			if ( culoc.getLocationType() == StateCU_LocationType.WELL ) {
				String partId;
				List<String> partIdList = culoc.getCollectionPartIDs();
				List<StateCU_Location_CollectionPartIdType> partIdTypeList = culoc.getCollectionPartIDTypes();
				StateCU_Location_CollectionPartIdType partIdType;
				for ( int i = 0; i < partIdList.size(); i++ ) {
					partId = partIdList.get(i);
					partIdType = partIdTypeList.get(i);
					if ( (partIdType == StateCU_Location_CollectionPartIdType.WDID) &&
						(wdid != null) && !wdid.isEmpty() && partId.equals(wdid) ) {
						return culoc;
					}
					else if ( (partIdType == StateCU_Location_CollectionPartIdType.RECEIPT) &&
						(receipt != null) && !receipt.isEmpty() && partId.equals(receipt) ) {
						return culoc;
					}
				}
			}
		}
	}
	// Part was not found
	return null;
}

/**
Read the StateCU STR file and return as a list of StateCU_Location.
@param filename filename containing STR data.
*/
public static List<StateCU_Location> readStateCUFile ( String filename )
throws IOException
{	String rtn = "StateCU_Location.readStateCUFile";
	String iline = null;
	List<Object> v = new ArrayList<Object>(8);
	List<StateCU_Location> culoc_List = new ArrayList<StateCU_Location>();
	int i;
	int format_0[] = {
				StringUtil.TYPE_STRING,	// CU Location
				StringUtil.TYPE_STRING,	// Latitude
				StringUtil.TYPE_STRING,	// Elevation
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,	// Region 1
				StringUtil.TYPE_STRING,	// Region 2
				StringUtil.TYPE_SPACE,
				StringUtil.TYPE_STRING,	// CU Location name
				StringUtil.TYPE_STRING,	// Num stations
				StringUtil.TYPE_STRING};// AWC
	int format_0w[] = {
				12,	// CU Location
				6,	// Latitude
				9,	// Elevation
				2,
				20,	// Region1
				8,	// Region2
				2,
				24,	// CU Location name
				4,	// Number of stations
				8 };	// AWC
	int format_1[] = {
				StringUtil.TYPE_STRING,	// Station ID
				StringUtil.TYPE_STRING,	// Temp weight
				StringUtil.TYPE_STRING,	// Precip weight
				StringUtil.TYPE_STRING,	// Orographic temperature adjustment
				StringUtil.TYPE_STRING };// Orographic precipitation adjustment
	int format_1w[] = {
				12,	// Station ID
				6,	// Temp weight
				9,	// Precip weight
				9,	// Orographic temperature adjustment
				9 };// Orographic precipitation adjustment	

	StateCU_Location culoc = null;
	BufferedReader in = null;
	Message.printStatus ( 1, rtn, "Reading StateCU Locations file: " + filename );

	// The following throws an IOException if the file cannot be opened...
	in = new BufferedReader ( new FileReader (filename));
	String latitude, elevation, num_climate_stations, awc, weight, opa, ota;
	int ncli = 0;
	int vsize;	// Size of parsed token list
	while ( (iline = in.readLine()) != null ) {
		// check for comments
		if (iline.startsWith("#") || iline.trim().length()==0 ){
			continue;
		}

		// Allocate new CULocation instance...
		culoc = new StateCU_Location();

		StringUtil.fixedRead ( iline, format_0, format_0w, v );
		culoc.setID ( ((String)v.get(0)).trim() ); 
		latitude = ( ((String)v.get(1)).trim() );
		if ((latitude.length() != 0) && StringUtil.isDouble(latitude)) {
			culoc.setLatitude ( StringUtil.atod(latitude) );
		}
		elevation = ((String)v.get(2)).trim();
		if ( (elevation.length() != 0) && StringUtil.isDouble(elevation)) {
			culoc.setElevation ( StringUtil.atod(elevation) );
		}
		culoc.setRegion1 ( ((String)v.get(3)).trim() ); 
		culoc.setRegion2 ( ((String)v.get(4)).trim() ); 
		culoc.setName ( ((String)v.get(5)).trim() ); 
		num_climate_stations = ( ((String)v.get(6)).trim() );
		if ( (num_climate_stations.length() != 0) && StringUtil.isInteger(num_climate_stations)) {
			culoc.setNumClimateStations ( StringUtil.atoi(num_climate_stations) );
		}
		awc = ((String)v.get(7)).trim();
		if ( (awc.length() != 0) && StringUtil.isDouble(awc)) {
			culoc.setAwc ( StringUtil.atod(awc) );
		}
		ncli = culoc.getNumClimateStations();
		for ( i = 0; i < ncli; i++ ) {
			iline = in.readLine();
			if ( iline == null ) {
				break;
			}
			StringUtil.fixedRead ( iline, format_1, format_1w, v );
			vsize = v.size();
			culoc.setClimateStationID ( ((String)v.get(0)).trim(), i ); 
			weight = ((String)v.get(1)).trim();
			if ( (weight.length() != 0) && StringUtil.isDouble(weight)) {
				culoc.setTemperatureStationWeight ( StringUtil.atod(weight), i );
			}
			weight = ((String)v.get(2)).trim();
			if ( (weight.length() != 0) && StringUtil.isDouble(weight)) {
				culoc.setPrecipitationStationWeight ( StringUtil.atod(weight), i );
			}
			if ( vsize > 3 ) {
				ota = ((String)v.get(3)).trim();
				if ( (ota.length() != 0) && StringUtil.isDouble(ota)) {
					culoc.setOrographicTemperatureAdjustment ( StringUtil.atod(ota), i );
				}
			}
			if ( vsize > 4 ) {
				opa = ((String)v.get(4)).trim();
				if ( (opa.length() != 0) &&	StringUtil.isDouble(opa)) {
					culoc.setOrographicPrecipitationAdjustment ( StringUtil.atod(opa), i );
				}
			}
		}

		// Add the StateCU_Location to the vector...
		culoc_List.add ( culoc );
	}
	if ( in != null ) {
		in.close();
	}
	return culoc_List;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateCU_Location loc = (StateCU_Location)_original;
	super.restoreOriginal();

	__awc = loc.__awc;
	__collection_div = loc.__collection_div;
	__collection_part_type = loc.__collection_part_type;
	__collection_type = loc.__collection_type;
	__elevation = loc.__elevation;
	__latitude = loc.__latitude;
	__region1 = loc.__region1;
	__region2 = loc.__region2;

	_isClone = false;
	_original = null;
}

/**
Set the AWC.
@param awc awc, fraction.
*/
public void setAwc ( double awc )
{	__awc = awc;
}

/**
Set the climate station identifier.
@param id Climate station identifier.
@param pos Station index (relative to zero).
*/
public void setClimateStationID ( String id, int pos )
{	if ( __climate_station_ids == null ) {
		__climate_station_ids = new String[pos + 1];
	}
	else if ( pos >= __climate_station_ids.length ) {
		// Resize the array...
		String [] temp = new String[pos + 1];
		for ( int i = 0; i < __climate_station_ids.length; i++ ) {
			temp[i] = __climate_station_ids[i];
		}
		__climate_station_ids = temp;
	}
	// Finally, assign...
	__climate_station_ids[pos] = id;
}

/**
Set the collection division.  This is needed to uniquely identify the parcels.
@param collection_div The division for the collection.
*/
public void setCollectionDiv ( int collection_div )
{	__collection_div = collection_div;
}

/**
Set the collection list for an aggregate/system.  It is assumed that the
collection applies to all years of data.
@param ids The identifiers indicating the locations to collection.
*/
public void setCollectionPartIDs ( List<String> ids )
{	if ( __collectionIDList == null ) {
		__collectionIDList = new ArrayList<List<String>>();
		__collectionYear = new int[1];
	}
	else {
		// Remove the previous contents...
		__collectionIDList.clear();
	}
	// Now assign...
	__collectionIDList.add ( ids );
	__collectionYear[0] = 0;
}

/**
Set the collection list for an aggregate/system.  It is assumed that the
collection applies to all years of data.
@param partIdList The identifiers indicating the locations in the collection.
@param partIdTypeList the part identifier types, used when well and part ID may be Well or Receipt or Parcel (Parcel is being phased out).
@param partIdWDList the part identifier WD, used in particular when well and part is Receipt.
*/
public void setCollectionPartIDs ( List<String> partIdList,
	List<StateCU_Location_CollectionPartIdType> partIdTypeList,
	List<Integer> partIdWDList )
{	
	__collectionIDList = new ArrayList<List<String>>(1);
	__collectionIDList.add ( partIdList );

	__collectionIDTypeList = new ArrayList<List<StateCU_Location_CollectionPartIdType>>(1);
	__collectionIDTypeList.add ( partIdTypeList );

	__collectionIDWDList = new ArrayList<List<Integer>>(1);
	__collectionIDWDList.add ( partIdWDList );

	__collectionYear = new int[1];
	__collectionYear[0] = 0;
}

/**
Set the collection list for an aggregate/system for a specific year.  It is
assumed that the collection applies to all years of data.
@param year The year to which the collection applies.
@param ids The identifiers indicating the locations in the collection.
*/
public void setCollectionPartIDsForYear ( int year, List<String> ids )
{	int pos = -1;	// Position of year in data lists.
	if ( __collectionIDList == null ) {
		// No previous data so create memory...
		__collectionIDList = new ArrayList<List<String>>();
		__collectionIDList.add ( ids );
		__collectionYear = new int[1];
		__collectionYear[0] = year;
	}
	else {
		// See if the year matches any previous contents...
		for ( int i = 0; i < __collectionYear.length; i++ ) {
			if ( year == __collectionYear[i] ) {
				pos = i;
				break;
			}
		}
		// Now assign...
		if ( pos < 0 ) {
			// Need to add an item...
			pos = __collectionYear.length;
			__collectionIDList.add ( ids );
			int [] temp = new int[__collectionYear.length + 1];
			for ( int i = 0; i < __collectionYear.length; i++ ) {
				temp[i] = __collectionYear[i];
			}
			__collectionYear = temp;
			__collectionYear[pos] = year;
		}
		else {
			// Existing item...
			__collectionIDList.set ( pos, ids );
			__collectionYear[pos] = year;
		}
	}
}

/**
Set the collection part type.
@param collection_part_type The collection part type.
*/
public void setCollectionPartType ( StateCU_Location_CollectionPartType collection_part_type )
{	__collection_part_type = collection_part_type;
}

/**
Set the collection type.
@param collection_type The collection type, either "Aggregate" or "System".
*/
public void setCollectionType ( StateCU_Location_CollectionType collection_type )
{	__collection_type = collection_type;
}

/**
Set the elevation.
@param elevation Elevation, feet.
*/
public void setElevation ( double elevation )
{	__elevation = elevation;
}

/**
 * Set whether the location has a FillCropPatternTS*() command in StateDMI.
 * A true record is only added once for a year.
 * @param year year that a set command is setting data
 */
public void setHasFillCropPatternTSCommands(int year) {
	if ( !hasFillCropPatternTSCommands(year) ) {
		// Add year to the list that has fill command.
		this.__hasFillCropPatternTSCommands.add(Integer.valueOf(year));
		// Also sort in place
		Collections.sort(this.__hasFillCropPatternTSCommands);
	}
}

/**
 * Set whether the location has a FillIrrigationPracticeTS*() command in StateDMI.
 * A true record is only added once for a year.
 * @param year year that a set command is setting data
 */
public void setHasFillIrrigationPracticeTSCommands(int year) {
	if ( !hasFillIrrigationPracticeTSCommands(year) ) {
		// Add year to the list that has fill command.
		this.__hasFillIrrigationPracticeTSCommands.add(Integer.valueOf(year));
		// Also sort in place
		Collections.sort(this.__hasFillIrrigationPracticeTSCommands);
	}
}

/**
 * Set whether the location has a SetCropPatternTS*() command in StateDMI.
 * A true record is only added once for a year.
 * @param year year that a set command is setting data
 */
public void setHasSetCropPatternTSCommands(int year) {
	if ( !hasSetCropPatternTSCommands(year) ) {
		// Add year to the list that has set command.
		this.__hasSetCropPatternTSCommands.add(Integer.valueOf(year));
		// Also sort in place
		Collections.sort(this.__hasSetCropPatternTSCommands);
	}
}

/**
 * Set whether the location has a SetIrrigationPracticeTS*() command in StateDMI.
 * A true record is only added once for a year.
 * @param year year that a set command is setting data
 */
public void setHasSetIrrigationPracticeTSCommands(int year) {
	if ( !hasSetIrrigationPracticeTSCommands(year) ) {
		// Add year to the list that has set command.
		this.__hasSetIrrigationPracticeTSCommands.add(Integer.valueOf(year));
		// Also sort in place
		Collections.sort(this.__hasSetIrrigationPracticeTSCommands);
	}
}

/**
Set the latitude.
@param latitude Latitude, decimal degrees.
*/
public void setLatitude ( double latitude )
{	__latitude = latitude;
}

/**
Set the location type.
@param location type
*/
public void setLocationType ( StateCU_LocationType locationType )
{	__locationType = locationType;
}

/**
Set the number of climate stations.
@param num_climate_stations Number of climate stations.
*/
public void setNumClimateStations ( int num_climate_stations )
{	if ( num_climate_stations == 0 ) {
		// Clear the arrays...
		__climate_station_ids = null;
		__precipitation_station_weights = null;
		__temperature_station_weights = null;
		__ota = null;
		__opa = null;
	}
	else {
		__climate_station_ids = new String[num_climate_stations];
		__precipitation_station_weights =
		new double[num_climate_stations];
		__temperature_station_weights =new double[num_climate_stations];
		__ota = new double[num_climate_stations];
		__opa = new double[num_climate_stations];
	}
}

/**
Set the orographic precipitation adjustment for a station.
@param opa orographic precipitation adjustment.
@param pos Station index (relative to zero).
*/
public void setOrographicPrecipitationAdjustment ( double opa, int pos )
{	if ( __opa == null ) {
		__opa = new double[pos + 1];
	}
	else if ( pos >= __opa.length ) {
		// Resize the array...
		double [] temp = new double[pos + 1];
		for(int i = 0; i < __opa.length; i++){
			temp[i] = __opa[i];
		}
		__opa = temp;
	}
	// Finally, assign...
	__opa[pos] = opa;
}

/**
Set the orographic temperature adjustment for a station.
@param ota orographic temperature adjustment.
@param pos Station index (relative to zero).
*/
public void setOrographicTemperatureAdjustment ( double ota, int pos )
{	if ( __ota == null ) {
		__ota = new double[pos + 1];
	}
	else if ( pos >= __ota.length ) {
		// Resize the array...
		double [] temp = new double[pos + 1];
		for(int i = 0; i < __ota.length; i++){
			temp[i] = __ota[i];
		}
		__ota = temp;
	}
	// Finally, assign...
	__ota[pos] = ota;
}

/**
Set the precipitation station weight.
@param wt precipitation station weight.
@param pos Station index (relative to zero).
*/
public void setPrecipitationStationWeight ( double wt, int pos )
{	if ( __precipitation_station_weights == null ) {
		__precipitation_station_weights = new double[pos + 1];
	}
	else if ( pos >= __precipitation_station_weights.length ) {
		// Resize the array...
		double [] temp = new double[pos + 1];
		for(int i = 0; i < __precipitation_station_weights.length; i++){
			temp[i] = __precipitation_station_weights[i];
		}
		__precipitation_station_weights = temp;
	}
	// Finally, assign...
	__precipitation_station_weights[pos] = wt;
}

/**
Set region 1.
@param region1 Region 1 (e.g., the name of a county).
*/
public void setRegion1 ( String region1 )
{	__region1 = region1;
}

/**
Set region 2.
@param region2 Region 1 (e.g., the name of a HUC).
*/
public void setRegion2 ( String region2 )
{	__region2 = region2;
}

/**
Set the temperature station weight.
@param wt temperature station weight.
@param pos Station index (relative to zero).
*/
public void setTemperatureStationWeight ( double wt, int pos )
{	if ( __temperature_station_weights == null ) {
		__temperature_station_weights = new double[pos + 1];
	}
	else if ( pos >= __temperature_station_weights.length ) {
		// Resize the array...
		double [] temp = new double[pos + 1];
		for ( int i = 0; i < __temperature_station_weights.length; i++){
			temp[i] = __temperature_station_weights[i];
		}
		__temperature_station_weights = temp;
	}
	// Finally, assign...
	__temperature_station_weights[pos] = wt;
}

/**
Performs specific data checks and returns a list of data that failed the data checks.
@param dataset StateCU dataset currently in memory.
@return Validation results.
*/
public StateCU_ComponentValidation validateComponent( StateCU_DataSet dataset )
{
	StateCU_ComponentValidation validation = new StateCU_ComponentValidation();
	String id = getID();
	double latitude = getLatitude();
	if ( !((latitude >= -90.0) && (latitude <= 90.0)) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"CU location \"" + id + "\" latitude (" +
			latitude + ") is invalid.", "Specify a latitude -90 to 90.") );
	}
	double elevation = getElevation();
	if ( !((elevation >= 0.0) && (elevation <= 15000.00)) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"CU location \"" + id + "\" elevation (" +
			elevation + ") is invalid.", "Specify an elevation 0 to 15000 FT (maximum varies by location).") );
	}
	String name = getName();
	if ( (name == null) || name.trim().length() == 0 ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"CU location \"" + id +
			"\" name is blank - may cause confusion.",
			"Specify the station name or use the ID for the name.") );
	}
	String region1 = getRegion1();
	if ( (region1 == null) || region1.trim().length() == 0 ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"CU location \"" + id +
			"\" region1 is blank - may cause region lookups to fail for other data.",
			"Specify as county or other region indicator.") );
	}
	int ncli = getNumClimateStations();
	if ( !(ncli >= 0) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"CU location \"" + id +
			"\" number of climate stations (" + ncli + ") is invalid.",
			"Specify as >= 0.") );
	}
	if ( ncli > 0 ) {
		// Check each climate station and the total...
		double totalPrecipWeight = 0.0;
		double totalTempWeight = 0.0;
		for ( int i = 0; i < ncli; i++ ) {
			String cliStationID = getClimateStationID(i);
			if ( cliStationID.trim().length() == 0 ) {
				validation.add(new StateCU_ComponentValidationProblem(this,"CU location \"" + id +
					"\" climate station ID is invalid (blank).",
					"Specify the climate station ID.") );
			}
			double tempWeight = getTemperatureStationWeight(i);
			if ( !((tempWeight >= 0.0) && (tempWeight <= 1.0)) ) {
				validation.add(new StateCU_ComponentValidationProblem(this,"CU location \"" + id +
					"\" climate station \"" + cliStationID + "\" temperature station weight (" + tempWeight +
					") is invalid.", "Specify as fraction 0 - 1.") );
			}
			double precipWeight = getPrecipitationStationWeight(i);
			if ( !((precipWeight >= 0.0) && (precipWeight <= 1.0)) ) {
				validation.add(new StateCU_ComponentValidationProblem(this,"CU location \"" + id +
					"\" climate station \"" + cliStationID + "\" precipitation station weight (" + precipWeight +
					") is invalid.", "Specify as fraction 0 - 1.") );
			}
			totalPrecipWeight += precipWeight;
			totalTempWeight += tempWeight;
			double oroTemppAdj = getOrographicTemperatureAdjustment(i);
			// Adjustment is always positive
			if ( !((oroTemppAdj >= 0.0) && (oroTemppAdj <= 5.0)) ) {
				validation.add(new StateCU_ComponentValidationProblem(this,"CU location \"" + id +
					"\" climate station \"" + cliStationID + "\" orographic temperature adjustment (" +
					oroTemppAdj + ") is invalid.  May default on output but should be explicitly set.",
					"Specify as DEGF/1000 FT 0 to 5.") );
			}
			double oroPrecipAdj = getOrographicPrecipitationAdjustment(i);
			if ( !((oroPrecipAdj >= 0.0) && (oroPrecipAdj <= 1.0)) ) {
				validation.add(new StateCU_ComponentValidationProblem(this,"CU location \"" + id +
					"\" climate station \"" + cliStationID + "\" orographic precipitation adjustment (" +
					oroPrecipAdj + ") is invalid.  May default on output but should be explicitly set.",
					"Specify as fraction 0 - 1.") );
			}
		}
		if ( !((totalTempWeight >= .9999999) && (totalTempWeight <= 1.0000001)) ) {
			validation.add(new StateCU_ComponentValidationProblem(this,"CU location \"" + id +
				"\" total of temperature station weights (" +
				totalTempWeight + ") is invalid.", "Weights should add up to 1.") );
		}
		if ( !((totalPrecipWeight >= .9999999) && (totalPrecipWeight <= 1.0000001)) ) {
			validation.add(new StateCU_ComponentValidationProblem(this,"CU location \"" + id +
				"\" total of precipitation station weights (" +
				totalPrecipWeight + ") is invalid.", "Weights should add up to 1.") );
		}
	}
	double awc = getAwc();
	if ( !((awc >= 0.0) && (awc <= 1.0)) ) {
		validation.add(new StateCU_ComponentValidationProblem(this,"CU location \"" + id +
			"\" available water content (AWC) (" + awc + ") is invalid.",
			"Specify as fraction 0 - 1.") );
	}
	return validation;
}

/**
 * Check whether any locations have no set commands and have parcel/supplies with CDS:UNK.
 * This is called by StateDMI CheckCropPatternTS and CheckIrrigationPracticeTS to point out
 * locations that have not been processed and don't have set commands.
 */
public static StateCU_ComponentValidation validateStateCULocationSupplies(List<StateCU_Location> culocList) {
		StateCU_ComponentValidation validation = new StateCU_ComponentValidation();
		return validation;
}

/**
Write a list of StateCU_Location to a file using default properties.  The filename is adjusted to the
working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filenamePrev The name of the previous version of the file (for
processing headers).  Specify as null if no previous file is available.
@param filename The name of the file to write.
@param dataList A list of StateCU_Location to write.
@param newComments Comments to add to the top of the file.  Specify as null if no comments are available.
@exception IOException if there is an error writing the file.
*/
public static void writeStateCUFile ( String filenamePrev, String filename,
					List<StateCU_Location> dataList, List<String> newComments )
throws IOException
{	writeStateCUFile ( filenamePrev, filename, dataList, newComments, null );
}

/**
Write a list of StateCU_Location to a file.  The filename is adjusted to the
working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filenamePrev The name of the previous version of the file (for
processing headers).  Specify as null if no previous file is available.
@param filename The name of the file to write.
@param dataList A list of StateCU_Location to write.
@param newComments Comments to add to the top of the file.  Specify as null if no comments are available.
@param props Properties to control the write.  Currently only the following
property is supported:  Version=True|False.  If the version is "10", then the
file format will match that for version 10.  Otherwise, the newest format is
used.  This is useful for comparing with or regenerating old data sets.
@exception IOException if there is an error writing the file.
*/
public static void writeStateCUFile ( String filenamePrev, String filename,
					List<StateCU_Location> dataList, List<String> newComments, PropList props )
throws IOException
{	List<String> commentStr = new ArrayList<String>(1);
	commentStr.add ( "#" );
	List<String> ignoreCommentStr = new ArrayList<String>(1);
	ignoreCommentStr.add ( "#>" );
	PrintWriter out = null;
	String fullFilenamePrev = IOUtil.getPathUsingWorkingDir ( filenamePrev );
	String fullFilename = IOUtil.getPathUsingWorkingDir ( filename );
	out = IOUtil.processFileHeaders ( fullFilenamePrev, fullFilename, 
		newComments, commentStr, ignoreCommentStr, 0 );
	if ( out == null ) {
		throw new IOException ( "Error writing to \"" + fullFilename + "\"" );
	}
	writeStateCUFile ( dataList, out, props );
	out.flush();
	out.close();
}

/**
Write a list of StateCU_Location to an opened file.
@param dataList A list of StateCU_Location to write.
@param out output PrintWriter.
@param props Properties to control the write.  See the writeStateCUFile() method for a description.
@exception IOException if an error occurs.
*/
private static void writeStateCUFile ( List<StateCU_Location> dataList, PrintWriter out, PropList props )
throws IOException
{	int i,j;
	String cmnt = "#>";
	// Missing data handled by formatting as a string...
	// The following format works for both lines.
	String format = "%-12.12s%6.6s%9.9s  %-20.20s%-8.8s  %-24.24s%4.4s%8.8s";
	String format_version10 = "%-12.12s%6.6s%9.9s  %-20.20s%-8.8s  %-24.24s";
	String format2 = "%-12.12s%6.6s%9.9s%9.9s%9.9s";
	// Not used but indicates format before orographic adjustments were added
	//String format2_version10 = "%-12.12s%6.6s%9.9s";
	StateCU_Location cu_loc = null;
	List<Object> v = new ArrayList<Object>(8);	// Reuse for all output lines.

	if ( props == null ) {
		props = new PropList ( "StateCU_Location" );
	}
	String Version = props.getValue ( "Version" );
	boolean Version_10 = false;
	if ( (Version != null) && Version.equalsIgnoreCase("10") ) {
		Version_10 = true;
	}

	out.println ( cmnt );
	out.println ( cmnt + "  StateCU CU Locations (STR) File" );
	out.println ( cmnt );
	out.println ( cmnt + "  Record 1 format (a12,f6.2,11x,a10,10x,i8,2x,a24,i4,f8.4)");
	out.println ( cmnt );
	out.println ( cmnt + "  ID       base_id:  CU Location identifier" );
	out.println ( cmnt + "  Latitude    blat:  Latitude (decimal degrees)" );
	out.println ( cmnt + "  Elevation   elev:  Elevation (feet)" );
	out.println ( cmnt + "  Region1  ttcount:  Region1 (e.g., County)" );
	out.println ( cmnt + "  Region2    tthuc:  Region2 (e.g., Hydrologic Unit)");
	out.println ( cmnt + "                     Optional");
	out.println ( cmnt + "  Name     base_id:  CU Location name" );
	if ( !Version_10 ) {
		out.println ( cmnt + "  NCli            :  Number of climate stations" );
		out.println ( cmnt + "  AWC             :  Available water content (fraction)" );
		out.println ( cmnt );
		out.println ( cmnt + "  Record 2+ format (a12,f6.2,3f9.2)");
		out.println ( cmnt );
		out.println ( cmnt + "  ClimID          :  Climate station identifier" );
		out.println ( cmnt + "  TmpWT           :  Temperature station weight (fraction)" );
		out.println ( cmnt + "  PptWT           :  Precipitation station weight (fraction)");
		out.println ( cmnt + "                     Weights for each type should add to 1.0");
		out.println ( cmnt + "  OroTmpAdj       :  Orographic temperature station adjustment (DEGF/1000 FT)" );
		out.println ( cmnt + "  OroPptAdj       :  Orographic precipitation station adjustment (fraction)");
		out.println ( cmnt );
	}
	if ( Version_10 ) {
		out.println ( cmnt + "    ID     Lat  Elevation   Region1             Region2       Name" );
		out.println ( cmnt + "---------eb----eb-------bxxb--------exxxxxxxxxxb------exxb----------------------e" );
	}
	else {
		// Full output...
		out.println ( cmnt + "    ID     Lat  Elevation   Region1             Region2       Name               NCli  AWC" );
		out.println ( cmnt + "---------eb----eb-------bxxb--------exxxxxxxxxxb------exxb----------------------eb--eb------e" );
		out.println ( cmnt );
		out.println ( cmnt + " ClimID    TmpWT  PptWt  OroTmpAdj OroPptAdj" );
		out.println ( cmnt + "---------eb----eb-------eb-------eb-------e" );
	}
	out.println ( cmnt + "EndHeader" );

	int num = 0;
	if ( dataList != null ) {
		num = dataList.size();
	}
	int numclimate = 0;
	double val;	// Generic value
	for ( i=0; i<num; i++ ) {
		cu_loc = dataList.get(i);
		if ( cu_loc == null ) {
			continue;
		}

		v.clear();
		v.add(cu_loc._id);
		if ( StateCU_Util.isMissing(cu_loc.__latitude) ) {
			v.add("");
		}
		else {
			v.add(StringUtil.formatString(cu_loc.__latitude,"%6.2f"));
		}
		if ( StateCU_Util.isMissing(cu_loc.__elevation) ) {
			v.add("");
		}
		else {
			v.add( StringUtil.formatString(cu_loc.__elevation,"%9.2f"));
		}
		v.add(cu_loc.__region1);
		v.add(cu_loc.__region2);
		v.add(cu_loc._name);
		if ( !Version_10 ) {
			numclimate = cu_loc.getNumClimateStations();
			v.add( StringUtil.formatString(numclimate,"%4d"));
			if ( StateCU_Util.isMissing(cu_loc.__awc) ) {
				v.add("");
			}
			else {
				v.add( StringUtil.formatString(cu_loc.__awc,"%8.4f"));
			}
		}

		if ( Version_10 ) {
			out.println ( StringUtil.formatString ( v, format_version10) );
		}
		else {
			out.println ( StringUtil.formatString ( v, format) );
		}
		if ( !Version_10 ) {
			// Print the climate station weights.
			// If values are missing, assign reasonable defaults.
			for ( j = 0; j < numclimate; j++ ) {
				v.clear();
				v.add(StringUtil.formatString(cu_loc.getClimateStationID(j),"%-12.12s"));
				val = cu_loc.getTemperatureStationWeight(j);
				if ( StateCU_Util.isMissing(val) ) {
					val = 0.0;
				}
				v.add(StringUtil.formatString(val,"%.2f"));
				val = cu_loc.getPrecipitationStationWeight(j);
				if ( StateCU_Util.isMissing(val) ) {
					val = 0.0;
				}
				v.add(StringUtil.formatString(val,"%.2f"));
				val = cu_loc.getOrographicTemperatureAdjustment(j);
				if ( StateCU_Util.isMissing(val) ) {
					val = 0.0;
				}
				v.add(StringUtil.formatString(val,"%.2f"));
				val = cu_loc.getOrographicPrecipitationAdjustment(j);
				if ( StateCU_Util.isMissing(val) ) {
					val = 1.0;
				}
				v.add(StringUtil.formatString(val,"%.2f"));
				out.println ( StringUtil.formatString (v, format2) );
			}
		}
	}
}

/**
Writes a list of StateCU_Location objects to a list file.  A header is 
printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter 
will be wrapped in "...".  <p>
This method also writes Climate Station and Collection data to
so if this method is called with a filename parameter of "locations.txt", 
three files may be generated:
- locations.txt
- locations_ClimateStations.txt
- locations_Collections.txt
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@return a list of files that were actually written, because this method controls all the secondary
filenames.
@param data the list of objects to write.
@param newComments comments to add to the top of the file (e.g., command file and HydroBase version). 
@throws Exception if an error occurs.
*/
public static List<File> writeListFile(String filename, String delimiter, boolean update, List<StateCU_Location> data,
		List<String> newComments) 
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List<String> fields = new ArrayList<String>();
	fields.add("ID");
	fields.add("Name");
	fields.add("Latitude");
	fields.add("Elevation");
	fields.add("Region1");
	fields.add("Region2");
	fields.add("NumClimateStations");
	fields.add("AWC");
	int fieldCount = fields.size();

	List<String> names = new ArrayList<String>(fieldCount);
	List<String> formats = new ArrayList<String>(fieldCount); 
	int comp = StateCU_DataSet.COMP_CU_LOCATIONS;
	String s = null;
	for (int i = 0; i < fieldCount; i++) {
		s = fields.get(i);
		names.add(StateCU_Util.lookupPropValue(comp, "FieldName", s));
		formats.add(StateCU_Util.lookupPropValue(comp, "Format", s));
	}

	String oldFile = null;	
	if (update) {
		oldFile = IOUtil.getPathUsingWorkingDir(filename);
	}
	
	int j = 0;
	PrintWriter out = null;
	StateCU_Location loc = null;
	List<String> commentString = new ArrayList<String>(1);
	commentString.add ( "#" );
	List<String> ignoreCommentString = new ArrayList<String>(1);
	ignoreCommentString.add ("#>");
	String[] line = new String[fieldCount];
	StringBuffer buffer = new StringBuffer();
	
	String filenameFull = IOUtil.getPathUsingWorkingDir(filename);
	try {
		// Add some basic comments at the top of the file.  However, do this to a copy of the
		// incoming comments so that they are not modified in the calling code.
		List<String> newComments2 = null;
		if ( newComments == null ) {
			newComments2 = new ArrayList<String>();
		}
		else {
			newComments2 = new ArrayList<String>(newComments);
		}
		newComments2.add(0,"");
		newComments2.add(1,"StateCU location information as delimited list file.");
		newComments2.add(2,"See also the associated climate station assignment and collection files.");
		newComments2.add(3,"");
		out = IOUtil.processFileHeaders( oldFile, filenameFull, 
			newComments2, commentString, ignoreCommentString, 0);

		for (int i = 0; i < fieldCount; i++) {
			if ( i > 0 ) {
				buffer.append(delimiter);
			}
			buffer.append("\"" + names.get(i) + "\"");
		}

		out.println(buffer.toString());
		
		for (int i = 0; i < size; i++) {
			loc = data.get(i);
			
			line[0] = StringUtil.formatString(loc.getID(), ((String)formats.get(0))).trim();
			line[1] = StringUtil.formatString(loc.getName(), ((String)formats.get(1))).trim();
			line[2] = StringUtil.formatString(loc.getLatitude(), ((String)formats.get(2))).trim();				
			line[3] = StringUtil.formatString(loc.getElevation(), ((String)formats.get(3))).trim();
			line[4] = StringUtil.formatString(loc.getRegion1(), ((String)formats.get(4))).trim();
			line[5] = StringUtil.formatString(loc.getRegion2(), ((String)formats.get(5))).trim();
			line[6] = StringUtil.formatString(loc.getNumClimateStations(), ((String)formats.get(6))).trim();
			line[7] = StringUtil.formatString(loc.getAwc(), ((String)formats.get(7))).trim();

			buffer = new StringBuffer();	
			for (j = 0; j < fieldCount; j++) {
				if (line[j].indexOf(delimiter) > -1) {
					line[j] = "\"" + line[j] + "\"";
				}
				if (j > 0) {
					buffer.append(delimiter);
				}
				buffer.append(line[j]);
			}

			out.println(buffer.toString());
		}
	}
	finally {
		if (out != null) {
			out.flush();
			out.close();
		}
		out = null;
	}

	int lastIndex = filename.lastIndexOf(".");
	String front = filename.substring(0, lastIndex);
	String end = filename.substring((lastIndex + 1), filename.length());
	
	String climateFilename = front + "_ClimateStations." + end;
	writeClimateStationListFile(climateFilename, delimiter, update, data, newComments );

	String collectionFilename = front + "_Collections." + end;
	writeCollectionListFile(collectionFilename, delimiter, update, data, newComments );
	
	List<File> filesWritten = new ArrayList<File>();
	filesWritten.add ( new File(filenameFull) );
	filesWritten.add ( new File(climateFilename) );
	filesWritten.add ( new File(collectionFilename) );
	return filesWritten;
}

/**
Writes the climate station data from a list of StateCU_Location objects to a 
list file.  A header is printed to the top of the file, containing the commands 
used to generate the file.  Any strings in the body of the file that contain 
the field delimiter will be wrapped in "...". 
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the list of StateCU_Location objects to write (climate station assignments will
be extracted). 
@param newComments comments to add at the top of the file (e.g., commands, HydroBase information).
@throws Exception if an error occurs.
*/
public static void writeClimateStationListFile(String filename, String delimiter, boolean update,
	List<StateCU_Location> data, List<String> newComments ) 
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List<String> fields = new ArrayList<String>();
	fields.add("LocationID");
	fields.add("StationID");
	fields.add("TempWeight");
	fields.add("PrecipWeight");
	fields.add("OrographicTempAdj");
	fields.add("OrographicPrecipAdj");
	int fieldCount = fields.size();

	List<String> names = new ArrayList<String>(fieldCount);
	List<String> formats = new ArrayList<String>(fieldCount); 
	int comp = StateCU_DataSet.COMP_CU_LOCATION_CLIMATE_STATIONS;
	String s = null;
	for (int i = 0; i < fieldCount; i++) {
		s = fields.get(i);
		names.add( StateCU_Util.lookupPropValue(comp, "FieldName", s));
		formats.add(StateCU_Util.lookupPropValue(comp, "Format", s));
	}

	String oldFile = null;	
	if (update) {
		oldFile = IOUtil.getPathUsingWorkingDir(filename);
	}
	
	int j = 0;
	int k = 0;
	int num = 0;
	PrintWriter out = null;
	StateCU_Location loc = null;
	List<String> commentString = new ArrayList<String>(1);
	commentString.add ( "#" );
	List<String> ignoreCommentString = new ArrayList<String>(1);
	ignoreCommentString.add ( "#>" );
	String[] line = new String[fieldCount];
	String id = null;
	StringBuffer buffer = new StringBuffer();
	
	try {
		// Add some basic comments at the top of the file.  However, do this to a copy of the
		// incoming comments so that they are not modified in the calling code.
		List<String> newComments2 = null;
		if ( newComments == null ) {
			newComments2 = new ArrayList<String>();
		}
		else {
			newComments2 = new ArrayList<String>(newComments);
		}
		newComments2.add(0,"");
		newComments2.add(1,"StateCU location climate station assignment information as delimited list file.");
		newComments2.add(2,"See also the associated location and collection files.");
		newComments2.add(3,"");
		out = IOUtil.processFileHeaders(
			oldFile, IOUtil.getPathUsingWorkingDir(filename), 
			newComments2, commentString, ignoreCommentString, 0);

		for (int i = 0; i < fieldCount; i++) {
			buffer.append("\"" + names.get(i) + "\"");
			if (i < (fieldCount - 1)) {
				buffer.append(delimiter);
			}
		}

		out.println(buffer.toString());
		
		for (int i = 0; i < size; i++) {
			loc = (StateCU_Location)data.get(i);
			id = loc.getID();
			num = loc.getNumClimateStations();
			
			for (j = 0; j < num; j++) {
				line[0] = StringUtil.formatString(id, ((String)formats.get(0))).trim();
				line[1] = StringUtil.formatString( loc.getClimateStationID(j), ((String)formats.get(1))).trim();
				line[2] = StringUtil.formatString( loc.getTemperatureStationWeight(j), ((String)formats.get(2))).trim();
				line[3] = StringUtil.formatString( loc.getPrecipitationStationWeight(j), ((String)formats.get(3))).trim();
				line[4] = StringUtil.formatString( loc.getOrographicTemperatureAdjustment(j), ((String)formats.get(4))).trim();
				line[5] = StringUtil.formatString( loc.getOrographicPrecipitationAdjustment(j), ((String)formats.get(5))).trim();
				
				buffer = new StringBuffer();	
				for (k = 0; k < fieldCount; k++) {
					if (line[k].indexOf(delimiter) > -1) {
						line[k] = "\"" + line[k] + "\"";
					}
					buffer.append(line[k]);
					if (k < (fieldCount - 1)) {
						buffer.append(delimiter);
					}
				}
	
				out.println(buffer.toString());
			}
		}
	}
	finally {
		if (out != null) {
			out.flush();
			out.close();
		}
	}
}

/**
Writes the collection data from a list of StateCU_Location objects to a 
list file.  A header is printed to the top of the file, containing the commands 
used to generate the file.  Any strings in the body of the file that contain 
the field delimiter will be wrapped in "...". 
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the list of StateCU_Location objects to write, from which collection information will
be extracted.
@param newComments comments to add at the top of the file (e.g., commands, HydroBase information).
@throws Exception if an error occurs.
*/
public static void writeCollectionListFile(String filename, String delimiter, boolean update,
	List<StateCU_Location> data, List<String> newComments ) 
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List<String> fields = new ArrayList<String>();
	fields.add("LocationID");
	fields.add("Division");
	fields.add("Year");
	fields.add("CollectionType");
	fields.add("PartType");
	fields.add("PartID");
	fields.add("PartIDType");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateCU_DataSet.COMP_CU_LOCATION_COLLECTIONS;
	
	String s = null;
	for (int i = 0; i < fieldCount; i++) {
		s = fields.get(i);
		names[i] = StateCU_Util.lookupPropValue(comp, "FieldName", s);
		formats[i] = StateCU_Util.lookupPropValue(comp, "Format", s);
	}

	String oldFile = null;	
	if (update) {
		oldFile = IOUtil.getPathUsingWorkingDir(filename);
	}
	
	int[] years = null;
	int div = 0;
	int j = 0;
	int k = 0;
	int numYears = 0;
	PrintWriter out = null;
	StateCU_Location loc = null;
	List<String> commentString = new ArrayList<String>(1);
	commentString.add ( "#" );
	List<String> ignoreCommentString = new ArrayList<String>(1);
	ignoreCommentString.add ( "#>" );
	String[] field = new String[fieldCount];
	StateCU_Location_CollectionType colType = null;
	String id = null;
	StateCU_Location_CollectionPartType partType = null;	
	StringBuffer buffer = new StringBuffer();
	List<String> ids = null;
	List<StateCU_Location_CollectionPartIdType> idTypes = null;

	try {
		// Add some basic comments at the top of the file.  However, do this to a copy of the
		// incoming comments so that they are not modified in the calling code.
		List<String> newComments2 = null;
		if ( newComments == null ) {
			newComments2 = new ArrayList<String>();
		}
		else {
			newComments2 = new ArrayList<String>(newComments);
		}
		newComments2.add(0,"");
		newComments2.add(1,"StateCU location collection information as delimited list file.");
		newComments2.add(2,"See also the associated location and climate station assignment files.");
		newComments2.add(3,"Division and year are only used with well parcel aggregates/systems.");
		newComments2.add(4,"ParcelIdType are only used with well aggregates/systems where the part ID is Well.");
		newComments2.add(5,"");
		out = IOUtil.processFileHeaders( oldFile, IOUtil.getPathUsingWorkingDir(filename), 
			newComments2, commentString, ignoreCommentString, 0);

		for (int i = 0; i < fieldCount; i++) {
			if ( i != 0 ) {
				buffer.append(delimiter);
			}
			buffer.append("\"" + names[i] + "\"");
		}

		out.println(buffer.toString());
		
		for (int i = 0; i < size; i++) {
			loc = data.get(i);
			id = loc.getID();
			div = loc.getCollectionDiv();
			years = loc.getCollectionYears();
			if (years == null) {
				numYears = 0; // By this point, collections that span the full period will use 1 year = 0
			}
			else {
				numYears = years.length;
			}
			colType = loc.getCollectionType();
			partType = loc.getCollectionPartType();
			// Loop through the number of years of collection data
			idTypes = loc.getCollectionPartIDTypes(); // Currently crosses all years
			int numIdTypes = 0;
			if ( idTypes != null ) {
				numIdTypes = idTypes.size();
			}
			for (j = 0; j < numYears; j++) {
				ids = loc.getCollectionPartIDsForYear(years[j]);
				// Loop through the identifiers for the specific year
				for ( k = 0; k < ids.size(); k++ ) {
					field[0] = StringUtil.formatString(id,formats[0]).trim();
					field[1] = StringUtil.formatString(div,formats[1]).trim();
					field[2] = StringUtil.formatString(years[j],formats[2]).trim();
					field[3] = StringUtil.formatString(colType.toString(),formats[3]).trim();
					field[4] = StringUtil.formatString(partType.toString(),formats[4]).trim();
					field[5] = StringUtil.formatString(ids.get(k),formats[5]).trim();
					field[6] = "";
					if ( numIdTypes > k ) {
						// Have data to output
						field[6] = StringUtil.formatString(idTypes.get(k),formats[6]).trim();
					}
	
					buffer = new StringBuffer();	
					for (int ifield = 0; ifield < fieldCount; ifield++) {
						if (ifield > 0) {
							buffer.append(delimiter);
						}
						if (field[ifield].indexOf(delimiter) > -1) {
							// Wrap delimiter in quoted field
							field[ifield] = "\"" + field[ifield] + "\"";
						}
						buffer.append(field[ifield]);
					}
		
					out.println(buffer.toString());
				}
			}
		}
	}
	finally {
		if (out != null) {
			out.flush();
			out.close();
		}
	}
}

}
