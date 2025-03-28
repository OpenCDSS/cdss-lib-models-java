// StateMod_WellRight - class to hold well right data

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

package DWR.StateMod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeZoneDefaultType;

/**
This class provides stores all the information associated with a well right.
*/
public class StateMod_WellRight extends StateMod_Data
implements Cloneable, Comparable<StateMod_Data>, StateMod_ComponentValidator, StateMod_Right {

/**
Administration number.
*/
private String _irtem;

/**
Decreed amount.
*/
private double _dcrdivw;

/**
 * Decree amount formatted as as string.
 * This is used to compare strings to ensure that only one is added per station.
 */
private String decreeString = "";

// TODO SAM 2016-05-18 Evaluate how to make this more generic, but is tough due to complexity.
// The following data are not part of the official StateMod specification but are useful
// to output in order to understand how well rights are determined.
// The data are specific to the State of Colorado due to its complex data model.

/**
Year for parcels.
*/
private int __parcelYear;
/**
Identifier for parcel.
*/
private String __parcelId;
/**
Well to parcel matching "class".
*/
private int __parcelMatchClass;

/**
 * Collection type (if a collection), StateMod_Well.COLLECTION_TYPE_*.
 */
private StateMod_Well_CollectionType __collectionType = null;

/**
 * Collection part type (if a collection), StateMod_Well.COLLECTION_PART_TYPE_*.
 */
private StateMod_Well_CollectionPartType __collectionPartType = null;

/**
 * Collection part ID (if a collection), ID corresponding to the part type.
 */
private String __collectionPartId = "";

/**
 * Collection part ID type (if a collection).
 */
private StateMod_Well_CollectionPartIdType __collectionPartIdType = null;

/**
 * Well WDID.	
 */
private String __xWDID = "";

/**
 * Well permit receipt.	
 */
private String __xPermitReceipt = "";

/**
 * Well yield GPM
 */
private double __xYieldGPM = Double.NaN;

/**
 * Well yield alternate point/exchange (APEX) GPM
 */
private double __xYieldApexGPM = Double.NaN;

/**
 * Well permit date.
 */
private Date __xPermitDate = null;

/**
 * Well permit date as an administration number.
 */
private String __xPermitDateAdminNumber = "";

/**
 * Well right appropriation date.
 */
private Date __xApproDate = null;

/**
 * Well right appropriation date as an administration number.
 */
private String __xApproDateAdminNumber = "";

/**
 * Well right use corresponding to three-digit concatenated codes.
 */
private String __xUse = "";

/**
 * Prorated yield based on parcel area
 */
private double __xProratedYield = Double.NaN;

/**
 * Fraction of yield attributed to ditch (based on area of parcel served by ditch)
 */
private double __xDitchFraction = Double.NaN;

/**
 * Fraction of yield (percent_yield in HydroBase)
 */
private double __xFractionYield = Double.NaN;

/**
Constructor
*/
public StateMod_WellRight() {
	super();
	initialize();
}

/**
Clones the data object.
@return a cloned object.
*/
public Object clone() {
	StateMod_WellRight right = (StateMod_WellRight)super.clone();
	right._irtem = new String(_irtem);
	right._dcrdivw = _dcrdivw;
	right._isClone = true;

	return right;
}

/**
Compares this object to another StateMod_Data object based on the sorted
order from the StateMod_Data variables, and then by rtem, dcrres, iresco,
ityrstr, n2fill and copid, in that order.
@param data the object to compare against.
@return 0 if they are the same, 1 if this object is greater than the other
object, or -1 if it is less.
*/
public int compareTo(StateMod_Data data) {
	int res = super.compareTo(data);
	if (res != 0) {
		return res;
	}

	StateMod_WellRight right = (StateMod_WellRight)data;

	res = _irtem.compareTo(right._irtem);
	if (res != 0) {
		return res;
	}

	if (_dcrdivw < right._dcrdivw) {
		return -1;
	}
	else if (_dcrdivw > right._dcrdivw) {
		return 1;
	}

	return 0;
}

/**
Creates a backup of the current data object and stores it in _original,
for use in determining if an object was changed inside of a GUI.
*/
public void createBackup() {
	_original = (StateMod_WellRight)clone();
	((StateMod_WellRight)_original)._isClone = false;
	_isClone = true;
}

/**
Compare two rights lists and see if they are the same.
@param v1 the first list of StateMod_WellRight to check.  Cannot be null.
@param v2 the second list of StateMod_WellRight to check.  Cannot be null.
@return true if they are the same, false if not.
*/
public static boolean equals(List<StateMod_WellRight> v1, List<StateMod_WellRight> v2) {
	String routine = "StateMod_WellRight.equals(Vector, Vector)";
	StateMod_WellRight r1;	
	StateMod_WellRight r2;	
	if (v1.size() != v2.size()) {
		Message.printStatus(1, routine, "Lists are different sizes");
		return false;
	}
	else {
		// sort the Vectors and compare item-by-item.  Any differences
		// and data will need to be saved back into the dataset.
		int size = v1.size();
		Message.printStatus(2, routine, "Lists are of size: " + size);
		List<StateMod_WellRight> v1Sort = StateMod_Util.sortStateMod_DataVector(v1);
		List<StateMod_WellRight> v2Sort = StateMod_Util.sortStateMod_DataVector(v2);
		Message.printStatus(2, routine, "Vectors have been sorted");
	
		for (int i = 0; i < size; i++) {			
			r1 = v1Sort.get(i);	
			r2 = v2Sort.get(i);	
			Message.printStatus(2, routine, r1.toString());
			Message.printStatus(2, routine, r2.toString());
			Message.printStatus(2, routine, "Element " + i + " comparison: " + r1.compareTo(r2));
			if (r1.compareTo(r2) != 0) {
				return false;
			}
		}
	}	
	return true;
}

/**
Tests to see if two well rights are equal.  Strings are compared with case sensitivity.
@param right the right to compare.
@return true if they are equal, false otherwise.
*/
public boolean equals(StateMod_WellRight right) {
	 if (!super.equals(right)) {
	 	return false;
	}
	
	if ( right._irtem.equals(_irtem) && right._dcrdivw == _dcrdivw) {
		return true;
	}
	return false;
}

/**
Tests to see if two well rights are equal, to avoid duplicate rights for output.
The location ID must be the same since could be comparing one locations rights to
a larger list.
If the location ID is the same, the following are compared:
<ul>
<li>do care if a collection because this indicates how the location identifier is compared
<li>do not care what the collection part type is because well rights can be associated multiple ways
<li>administration number as string</li>
<li>decree as string formatted to 2 digits after decimal</li>
<li>If a collection:
	<ul>
	<li>collection part type</li>
	<li>collection part ID</li>
	</ul>
</li>
<li>If not a collection:
	<ul>
	<li>location ID</li>
	</ul>
</li>
</ul>
Strings are compared with case sensitivity.
@param right the right to compare.
@return true if they are equal, false otherwise.
*/
public boolean equalsForOutput(StateMod_WellRight right) {
	String routine = "";
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".equalsForOutput";
	}
	// Use for troubleshooting.
	/*
	if ( this.getLocationIdentifier().equals("1300975") ) {
		String message =
			"Water right 1 for location=" + this.getLocationIdentifier() +
			" admin #=" + this.getAdministrationNumber() +
			" decree=" + this.getDecreeString() +
			" collection type=" + this.getCollectionType() +
			" collection part ID type=" + this.getCollectionPartIdType() +
			" collection part ID=" + this.getCollectionPartId();
		Message.printStatus(2, routine, message);
		message =
			"Water right 2 for location=" + right.getLocationIdentifier() +
			" admin #=" + right.getAdministrationNumber() +
			" decree=" + right.getDecreeString() +
			" collection type=" + right.getCollectionType() +
			" collection part ID type=" + right.getCollectionPartIdType() +
			" collection part ID=" + right.getCollectionPartId();
		Message.printStatus(2, routine, message);
	}
	*/
	// Location identifier must be the same.
	if ( !getLocationIdentifier().equals(right.getLocationIdentifier()) ) {
		// Should not normally happen because should only be called for the same station.
		Message.printWarning(3, routine, "Right location ID different. Should not happen - check code logic to make sure calling for one station's rights.");
		return false;
	}
	if ( !getIrtem().equals(right.getIrtem()) ) {
		// Administration numbers don't match.
		return false;
	}
	if ( !getDecreeString().equals(right.getDecreeString()) ) {
		// Decree does not match.
		return false;
	}
	// Must check the collection type because the well right is typically
	// not the same as the 
	// Count whether collection type is set for either right.
	int collectionTypeCount = 0;
	if ( this.__collectionType != null ) {
		++collectionTypeCount;
	}
	if ( right.__collectionType != null ) {
		++collectionTypeCount;
	}
	if ( collectionTypeCount == 1 ) {
		// Collection type is different because only one is null.
		// Should not normally happen because should only be called for the same station.
		Message.printWarning(3, routine, "Right is for collection different.  Should not happen - check code logic to make sure calling for one station's rights.");
		return false;
	}
	else if ( collectionTypeCount == 2 ) {
		// Also check the values of collection type since both are set.
		if ( this.__collectionType != right.__collectionType ) {
			// Should not normally happen because should only be called for the same station.
			Message.printWarning(3, routine, "Right collection type different.  Should not happen - check code logic to make sure calling for one station's rights.");
			return false;
		}
		else {
			// Collection type is the same for both so check the part ID type and value:
			// - part type will be null if not set
			int collectionPartTypeCount = 0;
			if ( this.__collectionPartType != null ) {
				++collectionPartTypeCount;
			}
			if ( right.__collectionPartType != null ) {
				++collectionPartTypeCount;
			}
			if ( collectionPartTypeCount == 1 ) {
				// Collection part type is different because only one is null.
				return false;
			}
			else if ( collectionPartTypeCount == 2 ) {
				// Collection part type is set for both.
				if ( this.__collectionPartIdType != right.__collectionPartIdType ) {
					// Collection part type is different, for example trying to compare water right and permit.
					return false;
				}
				else {
					// Part ID type is the same so compare the ID.
					// - compare right ID to right ID
					// - or compare permit ID to permit ID
					if ( !this.__collectionPartId.equals(right.__collectionPartId) ) {
						return false;
					}
				}
			}
		}
	}
	else if ( collectionTypeCount == 0 ) {
		// Not a collection so check the location ID, which will be an explicit diversion WDID:
		// - check for location identifier was done at the top so don't need to recheck here
		// - main collection type is not used but the part type for Well WDID or permit is used
		// - part type will be null if not set
		// - this code is similar to the collectionTypeCount==2 code
		int collectionPartTypeCount = 0;
		if ( this.__collectionPartType != null ) {
			++collectionPartTypeCount;
		}
		if ( right.__collectionPartType != null ) {
			++collectionPartTypeCount;
		}
		if ( collectionPartTypeCount == 1 ) {
			// Collection part type is different because only one is null.
			return false;
		}
		else if ( collectionPartTypeCount == 2 ) {
			// Collection part type is set for both.
			if ( this.__collectionPartIdType != right.__collectionPartIdType ) {
				// Collection part type is different, for example trying to compare water right and permit.
				return false;
			}
			else {
				// Part ID type is the same so compare the ID:
				// - compare right ID to right ID
				// - or compare permit ID to permit ID
				if ( !this.__collectionPartId.equals(right.__collectionPartId) ) {
					return false;
				}
			}
		}
	}
	// Rights are the same.
	return true;
}

/**
This version was used with 5.1.1 and 5.1.2 but does not correctly remove all duplicates, maybe too complex.
Tests to see if two well rights are equal, to avoid duplicate rights for output.
The location ID must be the same since could be comparing one locations rights to
a larger list.
If the location ID is the same, the following are compared:
<ul>
<li>do care if a collection because this indicates how the location identifier is compared
<li>do not care what the collection part type is because well rights can be associated multiple ways
<li>administration number as string</li>
<li>decree as string formatted to 2 digits after decimal</li>
<li>If a collection:
	<ul>
	<li>collection part type</li>
	<li>collection part ID</li>
	</ul>
</li>
<li>If not a collection:
	<ul>
	<li>location ID</li>
	</ul>
</li>
</ul>
Strings are compared with case sensitivity.
@param right the right to compare.
@return true if they are equal, false otherwise.
*/
public boolean x_equalsForOutput(StateMod_WellRight right) {
	//String routine = "";
	if ( Message.isDebugOn ) {
		//routine = getClass().getSimpleName() + ".equalsForOutput";
	}
	// Location identifier must be the same.
	if ( !getLocationIdentifier().equals(right.getLocationIdentifier()) ) {
		return false;
	}
	if ( !getIrtem().equals(right.getIrtem()) ) {
		// Administration numbers don't match.
		return false;
	}
	if ( !getDecreeString().equals(right.getDecreeString()) ) {
		// Decree does not match.
		return false;
	}
	// Count whether collection type is set for either right.
	int collectionTypeCount = 0;
	if ( this.__collectionType != null ) {
		++collectionTypeCount;
	}
	if ( right.__collectionType != null ) {
		++collectionTypeCount;
	}
	if ( collectionTypeCount == 1 ) {
		// Collection type is different because only one is null.
		return false;
	}
	else if ( collectionTypeCount == 2 ) {
		// Also check the values of collection type since both are set.
		if ( this.__collectionType != right.__collectionType ) {
			return false;
		}
		else {
			// Collection type is the same for both so check the part ID type and value:
			// - part type will be null if not set
			int collectionPartTypeCount = 0;
			if ( this.__collectionPartType != null ) {
				++collectionPartTypeCount;
			}
			if ( right.__collectionPartType != null ) {
				++collectionPartTypeCount;
			}
			if ( collectionPartTypeCount == 1 ) {
				// Collection part type is different because only one is null.
				return false;
			}
			else if ( collectionPartTypeCount == 2 ) {
				// Collection part type is set for both.
				if ( this.__collectionPartIdType != right.__collectionPartIdType ) {
					// Collection part type is different.
					return false;
				}
				else {
					// Part ID type is the same so compare the ID.
					if ( !this.__collectionPartId.equals(right.__collectionPartId) ) {
						return false;
					}
				}
			}
		}
	}
	else if ( collectionTypeCount == 0 ) {
		// Not a collection so check the location ID, which will be an explicit diversion WDID:
		// - check for location identifier was done at the top so don't need to recheck here
		// - main collection type is not used but the part type for Well WDID or permit is used
		// - part type will be null if not set
		// - this code is similar to the collectionTypeCount==2 code
		int collectionPartTypeCount = 0;
		if ( this.__collectionPartType != null ) {
			++collectionPartTypeCount;
		}
		if ( right.__collectionPartType != null ) {
			++collectionPartTypeCount;
		}
		if ( collectionPartTypeCount == 1 ) {
			// Collection part type is different because only one is null.
			return false;
		}
		else if ( collectionPartTypeCount == 2 ) {
			// Collection part type is set for both.
			if ( this.__collectionPartIdType != right.__collectionPartIdType ) {
				// Collection part type is different.
				return false;
			}
			else {
				// Part ID type is the same so compare the ID.
				if ( !this.__collectionPartId.equals(right.__collectionPartId) ) {
					return false;
				}
			}
		}
	}
	return true;
}

/**
Return the administration number, as per the generic interface.
@return the administration number, as a String to protect from roundoff.
*/
public String getAdministrationNumber ()
{	return getIrtem();
}

/**
Return the collection part ID.
@return the collection part ID.
*/
public String getCollectionPartId ()
{	return __collectionPartId;
}

/**
Return the collection part ID type.
@return the collection part ID type.
*/
public StateMod_Well_CollectionPartIdType getCollectionPartIdType ()
{	return __collectionPartIdType;
}

/**
Return the collection part type.
@return the collection part type.
*/
public StateMod_Well_CollectionPartType getCollectionPartType ()
{	return __collectionPartType;
}

/**
Return the collection type.
@return the collection type.
*/
public StateMod_Well_CollectionType getCollectionType ()
{	return __collectionType;
}

/**
Returns the table header for StateMod_WellRight data tables.
@return String[] header - Array of header elements.
 */
public static String[] getDataHeader()
{
	return new String[] {
		"Num",
		"Well Right ID",
		"Well Station ID" };
		//"Well Name" };
}

/**
@return the decreed amount(cfs)
*/
public double getDcrdivw() {
	return _dcrdivw;
}

/**
Return the decree, as per the generic interface.
@return the decree, in the units of the data.
*/
public double getDecree()
{	return getDcrdivw();
}

/**
Return the decree string.
@return the decree string, formatted to 2 digits after the decimal.
*/
public String getDecreeString()
{	return this.decreeString;
}

// TODO SAM 2007-05-15 Need to evaluate whether should be hard-coded.
/**
Return the decree units.
@return the decree units.
*/
public String getDecreeUnits()
{	return "CFS";
}

/**
Return the right identifier, as per the generic interface.
@return the right identifier.
*/
public String getIdentifier()
{	return getID();
}

/**
@return the administration number
*/
public String getIrtem() {
	return _irtem;
}

/**
Return the right location identifier, as per the generic interface.
@return the right location identifier (location where right applies).
*/
public String getLocationIdentifier()
{	return getCgoto();
}

/**
@return the parcel identifier
*/
public String getParcelID() {
	return __parcelId;
}

/**
@return the parcel match class.
*/
public int getParcelMatchClass() {
	return __parcelMatchClass;
}

/**
@return the parcel year.
*/
public int getParcelYear() {
	return __parcelYear;
}

/**
Create summary comments suitable to add to a file header.
*/
private static List<String> getSummaryCommentList(List<StateMod_WellRight> rightList)
{
	List<String> summaryList = new ArrayList<String>();
	int size = rightList.size();
	List<Integer> parcelMatchClassList = new ArrayList<Integer>();
	StateMod_WellRight right = null;
	int parcelMatchClass;
	// Determine the unique list of water right classes - ok to end up with one class of -999
	// if class match information is not available.
	for ( int i = 0; i < size; i++ ) {
		right = rightList.get(i);
		parcelMatchClass = right.getParcelMatchClass();
		boolean found = false;
		for ( int j = 0; j < parcelMatchClassList.size(); j++ ) {
			if ( parcelMatchClassList.get(j) == parcelMatchClass ) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			parcelMatchClassList.add ( Integer.valueOf(parcelMatchClass));
		}
	}
	// Now summarize information by class
	//int parcelMatchClassListSize = parcelMatchClassList.size();
	//int [] countByClass = new int[parcelMatchClassListSize];
	return summaryList;
}

/**
 * Return the well permit receipt.	
 */
public String getXWDID () {
	return __xWDID;
}

/**
 * Return the well right use, for example from HydroBase well right use.
 */
public String getXUse () {
	return __xUse;
}


/**
 * Return the well permit receipt.	
 */
public String getXPermitReceipt () {
	return __xPermitReceipt;
}

/**
 * Return the well yield GPM.
 */
public double getXYieldGPM () {
	return __xYieldGPM;
}

/**
 * Return the well yield alternate point/exchange (APEX) GPM
 */
public double getXYieldApexGPM () {
	return __xYieldApexGPM;
}

/**
 * Return the well permit date.
 */
public Date getXPermitDate () {
	return __xPermitDate;
}

/**
 * Return the well permit date as an administration number.
 */
public String getXPermitDateAdminNumber () {
	return __xPermitDateAdminNumber;
}

/**
 * Well right appropriation date.
 */
public Date getXApproDate () {
	return __xApproDate;
}

/**
 * Well right appropriation date as an administration number.
 */
public String getXApproDateAdminNumber () {
	return __xApproDateAdminNumber;
}

/**
 * Return the fraction of yield attributed to the ditch supply
 */
public double getXDitchFraction () {
	return __xDitchFraction;
}

/**
 * Return the fraction of yield (percent_yield in HydroBase)
 */
public double getXFractionYield () {
	return __xFractionYield;
}

/**
 * Return the prorated yield based on parcel area
 */
public double getXProratedYield () {
	return __xProratedYield;
}

private void initialize() {
	_smdata_type = StateMod_DataSet.COMP_WELL_RIGHTS;
	_irtem = "99999";
	_dcrdivw = 0;
	// Parcel data...
	__parcelId = "";
	__parcelYear = StateMod_Util.MISSING_INT;
	__parcelMatchClass = StateMod_Util.MISSING_INT;
}

/**
Determine whether the right is for an estimated well.  Estimated wells are those that are copies of
real wells, as an estimate of water supply for parcels that are clearly groundwater irrigated but a supply
well is not physically evident in remote sensing work.
@return true if the well right is for an estimated well.
*/
public boolean isEstimatedWell ()
{
	int parcelMatchClass = getParcelMatchClass();
	if ( (parcelMatchClass == 4) || (parcelMatchClass == 9) ) {
		return true;
	}
	return false;
}

/**
Determine whether a file is a well right file.  Currently true is returned if the file extension is ".wer".
@param filename Name of the file being checked.
@return true if the file is a StateMod well right file.
*/
public static boolean isWellRightFile ( String filename )
{	if ( filename.toUpperCase().endsWith(".WER")) {
		return true;
	}
	return false;
}

/**
Cancels any changes made to this object within a GUI since createBackup()
was called and sets _original to null.
*/
public void restoreOriginal() {
	StateMod_WellRight right = (StateMod_WellRight)_original;
	super.restoreOriginal();

	_smdata_type = right._smdata_type;
	_irtem = right._irtem;
	_dcrdivw = right._dcrdivw;
	__parcelId = right.__parcelId;
	__parcelMatchClass = right.__parcelMatchClass;
	__parcelYear = right.__parcelYear;
	_isClone = false;
	_original = null;
}

/**
Read the well rights file.
@param filename name of file containing well rights
@return list of well rights
*/
public static List<StateMod_WellRight> readStateModFile(String filename)
throws Exception {
	String routine = "StateMod_WellRight.readStateModFile";
	List<StateMod_WellRight> theWellRights = new ArrayList<>();
	int [] format_0 = {
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_STRING,
				StringUtil.TYPE_DOUBLE,
				StringUtil.TYPE_INTEGER,
				// If comments used
				//StringUtil.TYPE_STRING,
				// Parcel data...
				StringUtil.TYPE_INTEGER, // Parcel year
				StringUtil.TYPE_INTEGER, // Class match (how well matched to parcel)
				StringUtil.TYPE_STRING // Parcel ID
				};
	// Extended, which includes parcel data and more
	// Same as above and additional columns
	int [] format_0Extended = {
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_STRING,
			StringUtil.TYPE_DOUBLE,
			StringUtil.TYPE_INTEGER,
			// If comments used
			//StringUtil.TYPE_STRING,
			// Parcel data...
			StringUtil.TYPE_INTEGER, // Parcel year
			StringUtil.TYPE_INTEGER, // Class match (how well matched to parcel)
			StringUtil.TYPE_STRING, // Parcel ID
			StringUtil.TYPE_STRING, // Collection type (Aggregate, System)
			StringUtil.TYPE_STRING, // Part type (Ditch, Well, Parcel)
			StringUtil.TYPE_STRING, // Part ID (a WDID or receipt)
			StringUtil.TYPE_STRING, // ID type ("WDID" or "Receipt")
			StringUtil.TYPE_STRING, // WDID (if available)
			StringUtil.TYPE_STRING, // Appropriation date YYYY-MM-DD
			StringUtil.TYPE_STRING, // Administration number for appropriation date
			StringUtil.TYPE_STRING, // Use
			StringUtil.TYPE_STRING, // Receipt (if available)
			StringUtil.TYPE_STRING, // Permit date YYYY-MM-DD
			StringUtil.TYPE_STRING, // Administration number for permit date
			StringUtil.TYPE_DOUBLE, // Well yield, GPM
			StringUtil.TYPE_DOUBLE, // Well yield, CFS
			StringUtil.TYPE_DOUBLE, // APEX, GPM
			StringUtil.TYPE_DOUBLE, // APEX, CFS
			StringUtil.TYPE_DOUBLE, // Well fraction
			StringUtil.TYPE_DOUBLE, // Ditch fraction
			StringUtil.TYPE_DOUBLE // Yield prorated as * well fraction * ditch fraction, GPM
			};
	int [] format_0w = {
				12,
				24,
				12,
				16,
				8,
				8,
				// If comments used
				//80,
				// Parcel data
				5,
				5,
				7};
	// Extended, which includes parcel data and more
	// Same as above and additional columns
	// x column adds 1 to each column
	int [] format_0wExtended = {
			12,
			24,
			12,
			16,
			8,
			8,
			// If comments used
			//80,
			// Parcel data
			5,
			5,
			13, // Note longer than older comments
			15, // Collection type (Aggregate, System)
			9, // Part type (Ditch, Well, Parcel)
			21, // Part ID (a WDID or receipt)
			9, // ID type ("WDID" or "Receipt")
			9, // WDID (if available)
			11, // Appropriation date YYYY-MM-DD
			12, // Administration number for appropriation date
			31, // Use
			9, // Receipt (if available)
			11, // Permit date YYYY-MM-DD
			12, // Administration number for permit date
			9, // Well yield, GPM
			9, // Well yield, CFS
			9, // APEX, GPM
			9, // APEX, CFS
			9, // Well fraction
			9, // Ditch fraction
			9 // Yield prorated as * well fraction * ditch fraction, GPM
			};
	String iline = null;
	List<Object> v = new ArrayList<Object>(10);
	BufferedReader in = null;
	StateMod_WellRight aRight = null;

	Message.printStatus(1, routine, "Reading well rights file: " + filename);

	int lineErrorCount = 0;
	try {
		in = new BufferedReader(new FileReader(IOUtil.getPathUsingWorkingDir(filename)));
		boolean formatSet = false;
		boolean fileHasExtendedComments = false; // Does file have the extended comments (parcel/well/permit/right)?
		int lineCount = 0;
		while ((iline = in.readLine()) != null) {
			// Check for extended comments
			++lineCount;
			if (iline.startsWith("#") || iline.trim().length() == 0) {
				// Check to see if extended comments which will be if the line starts with #>----- and is longer than 100
				if ( iline.startsWith("#>-----") && (iline.length() > 100) ) {
					fileHasExtendedComments = true;
				}
				continue;
			}
			if ( !formatSet && fileHasExtendedComments ) {
				format_0 = format_0Extended;
				format_0w = format_0wExtended;
				formatSet = true;
				Message.printStatus(1, routine, "Detected extended comments in well rights file.");
			}
			
			aRight = new StateMod_WellRight();

			int vSize = 0;
			if (Message.isDebugOn) {
				Message.printDebug(50, routine, "iline: " + iline);
			}
			try {
				StringUtil.fixedRead(iline, format_0, format_0w, v);
			}
			catch ( Exception e ) {
				Message.printWarning(3,routine,"Error reading line " + lineCount + " - data will be incomplete (" + e + "): " + iline );
				++lineErrorCount;
			}
			// Transfer as much data as possible from the read.
			vSize = v.size();
			if ( vSize > 0 ) {
				aRight.setID(((String)v.get(0)).trim());
			}
			if ( vSize > 1 ) {
				aRight.setName(((String)v.get(1)).trim());
			}
			if ( vSize > 2 ) {
				aRight.setCgoto(((String)v.get(2)).trim());
			}
			if ( vSize > 3 ) {
				aRight.setIrtem(((String)v.get(3)).trim());
			}
			if ( vSize > 4 ) {
				aRight.setDcrdivw((Double)v.get(4));
			}
			if ( vSize > 5 ) {
				aRight.setSwitch((Integer)v.get(5));
			}
			/* If comments used
			if ( v.size() > 6 ) {
				// Save the comment at the end of the line
				comment = (String)v.get(6);
				if ( comment != null ) {
					aRight.setComment ( comment.trim() );
				}
			}
			*/
			// Evaluate handling parcel data... 
			if ( vSize > 6 ) {
				aRight.setParcelYear((Integer)v.get(6));
			}
			if ( vSize > 7 ) {
				aRight.setParcelMatchClass((Integer)v.get(7));
			}
			if ( vSize > 8 ) {
				aRight.setParcelID(((String)v.get(8)).trim());
			}
			if ( fileHasExtendedComments ) {
				try {
					if ( vSize > 9 ) {
						aRight.setCollectionType(StateMod_Well_CollectionType.valueOfIgnoreCase((String)v.get(9)));
					}
					if ( vSize > 10 ) {
						aRight.setCollectionPartType(StateMod_Well_CollectionPartType.valueOfIgnoreCase((String)v.get(10)));
					}
					if ( vSize > 11 ) {
						aRight.setCollectionPartId((String)v.get(11));
					}
					if ( vSize > 12 ) {
						aRight.setCollectionPartIdType(StateMod_Well_CollectionPartIdType.valueOfIgnoreCase((String)v.get(12)));
					}
					if ( vSize > 13 ) {
						aRight.setXWDID((String)v.get(13));
					}
					try {
						if ( vSize > 14 ) {
							DateTime dt = DateTime.parse((String)v.get(14));
							aRight.setXApproDate(dt.getDate(TimeZoneDefaultType.LOCAL));
						}
					}
					catch ( Exception e2 ) {
					}
					if ( vSize > 15 ) {
						aRight.setXApproDateAdminNumber((String)v.get(15));
					}
					if ( vSize > 16 ) {
						aRight.setXUse((String)v.get(16));
					}
					if ( vSize > 17 ) {
						aRight.setXPermitReceipt((String)v.get(17));
					}
					try {
						if ( vSize > 18 ) {
							DateTime dt = DateTime.parse((String)v.get(18));
							aRight.setXPermitDate(dt.getDate(TimeZoneDefaultType.LOCAL));
						}
					}
					catch ( Exception e2 ) {
					} 
					if ( vSize > 19 ) {
						aRight.setXPermitDateAdminNumber((String)v.get(19));
					}
					if ( vSize > 20 ) {
						aRight.setXYieldGPM((Double)v.get(20));
					}
					if ( vSize > 21 ) {
						aRight.setXYieldApexGPM((Double)v.get(21));
					}
					if ( vSize > 22 ) {
						aRight.setXFractionYield((Double)v.get(22));
					}
					if ( vSize > 23 ) {
						aRight.setXDitchFraction((Double)v.get(23));
					}
					if ( vSize > 24 ) {
						aRight.setXProratedYield((Double)v.get(24));
					}
				}
				catch ( Exception e ) {
				    Message.printWarning(3,routine,"Error reading line " + lineCount + " - data will be incomplete (" + e + "): " + iline );
					++lineErrorCount;
				}
			}
			// If extended comments
			theWellRights.add(aRight);
		}
	} 
	catch(Exception e) {
		Message.printWarning(3, routine, e);
		throw e;
	}
	finally {
		if (in != null) {
			in.close();
		}
		in = null;
	}
	if ( lineErrorCount > 0 ) {
		throw new Exception ( "There were " + lineErrorCount + " errors reading the file.  Check the log file and fix.");
	}
	return theWellRights;
}

/**
Set the collection part ID.
@param collectionPartId collection part ID.
*/
public void setCollectionPartId(String collectionPartId) {
	if (collectionPartId == null) {
		return;
	}
	if (!collectionPartId.equals(__collectionPartId)) {
		__collectionPartId = collectionPartId.trim();
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_RIGHTS, true);
		}
	}
}

/**
Set the collection part ID type.
@param collectionPartId collection part ID type.
*/
public void setCollectionPartIdType(StateMod_Well_CollectionPartIdType collectionPartIdType) {
	if (collectionPartIdType == null) {
		return;
	}
	if ( collectionPartIdType != __collectionPartIdType ) {
		__collectionPartIdType = collectionPartIdType;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_RIGHTS, true);
		}
	}
}

/**
Set the collection part type.
@param collectionPartType collection part type.
*/
public void setCollectionPartType(StateMod_Well_CollectionPartType collectionPartType) {
	if (collectionPartType == null) {
		return;
	}
	if ( collectionPartType != __collectionPartType ) {
		__collectionPartType = collectionPartType;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_RIGHTS, true);
		}
	}
}

/**
Set the collection type.
@param collectionType collection part type.
*/
public void setCollectionType(StateMod_Well_CollectionType collectionType) {
	if (collectionType == null) {
		return;
	}
	if ( collectionType != __collectionType ) {
		__collectionType = collectionType;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_RIGHTS, true);
		}
	}
}

/**
Set the decreed amount(cfs)
@param dcrdivw decreed amount for this right
*/
public void setDcrdivw(double dcrdivw) {
	if (dcrdivw != _dcrdivw) {
		_dcrdivw = dcrdivw;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_RIGHTS, true);
		}
	}
}

/**
Set the decreed amount(cfs)
@param dcrdivw decreed amount for this right
*/
public void setDcrdivw(Double dcrdivw) {
	if (dcrdivw == null) {
		return;
	}
	setDcrdivw(dcrdivw.doubleValue());
}

/**
Set the decreed amount(cfs)
@param dcrdivw decreed amount for this right
*/
public void setDcrdivw(String dcrdivw) {
	if (dcrdivw == null) {
		return;
	}
	setDcrdivw(StringUtil.atod(dcrdivw.trim()));
}

/**
Set the decree, as per the generic interface.
@param decree decree, in the units of the data.
*/
public void setDecree(double decree)
{	setDcrdivw(decree);
	// Also set the string representation to streamline comparisons, when needed.
	this.decreeString = String.format("%.2f", decree);
}

/**
Set the administration number
@param irtem admin number of right
*/
public void setIrtem(String irtem) {
	if (irtem == null) {
		return;
	}
	if (!irtem.equals(_irtem)) {
		_irtem = irtem.trim();
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_RIGHTS, true);
		}
	}
}

/**
Set the parcel identifier.
@param parcel_id Parcel identifier.
*/
public void setParcelID(String parcel_id) {
	if (parcel_id == null) {
		return;
	}
	if (!parcel_id.equals(__parcelId)) {
		__parcelId = parcel_id.trim();
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_RIGHTS, true);
		}
	}
}

/**
Set the parcel match class.
@param parcel_match_class Parcel to well match class.
*/
public void setParcelMatchClass(Integer parcel_match_class) {
	if (parcel_match_class == null) {
		return;
	}
	setParcelMatchClass(parcel_match_class.intValue());
}

/**
Set the parcel match class, used to match wells to parcels.
@param parcel_match_class Parcel match class.
*/
public void setParcelMatchClass(int parcel_match_class) {
	if (parcel_match_class != __parcelMatchClass) {
		__parcelMatchClass = parcel_match_class;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_RIGHTS, true);
		}
	}
}

/**
Set the parcel year.
@param parcel_year Parcel year.
*/
public void setParcelYear(Integer parcel_year) {
	if (parcel_year == null) {
		return;
	}
	setParcelYear(parcel_year.intValue());
}

/**
Set the parcel year, used to match wells to parcels.
@param parcel_year Parcel year.
*/
public void setParcelYear(int parcel_year) {
	if (parcel_year != __parcelYear) {
		__parcelYear = parcel_year;
		setDirty ( true );
		if ( !_isClone && _dataset != null ) {
			_dataset.setDirty(StateMod_DataSet.COMP_WELL_RIGHTS, true);
		}
	}
}

/**
 * Set the well WDID.	
 */
public void setXWDID ( String xWDID ) {
	__xWDID = xWDID;
}

/**
 * Set the well right use.	
 */
public void setXUse ( String xUse ) {
	__xUse = xUse;
}

/**
 * Set the well permit receipt.	
 */
public void setXPermitReceipt ( String xPermitReceipt ) {
	__xPermitReceipt = xPermitReceipt;
}

/**
 * Set the well yield GPM.
 */
public void setXYieldGPM ( double xYieldGPM ) {
	__xYieldGPM = xYieldGPM;
}

/**
 * Set the well yield alternate point/exchange (APEX) GPM
 */
public void setXYieldApexGPM ( double xYieldApexGPM ) {
	__xYieldApexGPM = xYieldApexGPM;
}

/**
 * Set the well permit date.
 */
public void setXPermitDate ( Date xPermitDate ) {
	__xPermitDate = xPermitDate;
}

/**
 * Set the well permit date as an administration number.
 */
public void setXPermitDateAdminNumber ( String xPermitDateAdminNumber ) {
	__xPermitDateAdminNumber = xPermitDateAdminNumber;
}

/**
 * Set the well right appropriation date.
 */
public void setXApproDate ( Date xApproDate ) {
	__xApproDate = xApproDate;
}

/**
 * Set the well right appropriation date as an administration number.
 */
public void setXApproDateAdminNumber ( String xApproDateAdminNumber ) {
	__xApproDateAdminNumber = xApproDateAdminNumber;
}

/**
 * Set the prorated yield based on parcel area
 */
public void setXProratedYield ( double xProratedYield ) {
	__xProratedYield = xProratedYield;
}

/**
 * Set the fraction of yield attributed to parcel served by ditch
 */
public void setXDitchFraction ( double xDitchFraction) {
	__xDitchFraction = xDitchFraction;
}

/**
 * Set the fraction of yield (percent_yield in HydroBase)
 */
public void setXFractionYield ( double xFractionYield) {
	__xFractionYield = xFractionYield;
}

/**
Performs specific data checks for StateMod Well Rights.
@param dataset StateMod dataset.
@return validation results.
 */
public StateMod_ComponentValidation validateComponent( StateMod_DataSet dataset ) 
{
	StateMod_ComponentValidation validation = new StateMod_ComponentValidation();
	String id = getID();
	String name = getName();
	String cgoto = getCgoto();
	String irtem = getIrtem();
	double dcrdivw = getDcrdivw();
	// Make sure that basic information is not empty
	if ( StateMod_Util.isMissing(id) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well right identifier is blank.",
			"Specify a well right identifier.") );
	}
	if ( StateMod_Util.isMissing(name) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well right \"" + id + "\" name is blank.",
			"Specify a well right name to clarify data.") );
	}
	if ( StateMod_Util.isMissing(cgoto) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well right \"" + id +
			"\" well station ID is blank.",
			"Specify a well station to associate the well right.") );
	}
	else {
		// Verify that the well station is in the data set, if the network is available
		DataSetComponent comp2 = dataset.getComponentForComponentType(StateMod_DataSet.COMP_WELL_STATIONS);
		@SuppressWarnings("unchecked")
		List<StateMod_Well> wesList = (List<StateMod_Well>)comp2.getData();
		if ( (wesList != null) && (wesList.size() > 0) ) {
			if ( StateMod_Util.indexOf(wesList, cgoto) < 0 ) {
				validation.add(new StateMod_ComponentValidationProblem(this,"Well right \"" + id +
					"\" associated well (" + cgoto + ") is not found in the list of well stations.",
					"Specify a valid well station ID to associate with the well right.") );
			}
		}
	}
	if ( StateMod_Util.isMissing(irtem) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well right \"" + id +
			"\" administration number is blank.",
			"Specify an administration number NNNNN.NNNNN.") );
	}
	else if ( !StringUtil.isDouble(irtem) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well right \"" + id +
			"\" administration number (" + irtem + ") is invalid.",
			"Specify an administration number NNNNN.NNNNN.") );
	}
	else {
		double irtemd = Double.parseDouble(irtem);
		if ( irtemd < 0 ) {
			validation.add(new StateMod_ComponentValidationProblem(this,"Well right \"" + id +
				"\" administration number (" + irtem + ") is invalid.",
				"Specify an administration number NNNNN.NNNNN.") );
		}
	}
	if ( !(dcrdivw >= 0.0) ) {
		validation.add(new StateMod_ComponentValidationProblem(this,"Well right \"" + id + "\" decree (" +
			StringUtil.formatString(dcrdivw,"%.2f") + ") is invalid.",
			"Specify the decree as a number >= 0.") );
	}

	return validation;
}

// TODO smalers 2019-05-28 figure out how to enable
/**
FIXME SAM 2009-06-03 Evaluate how to call from main validate method
Check the well rights.
*/
@SuppressWarnings("unused")
private void validateComponent2 ( List<StateMod_WellRight> werList, List<StateMod_Well> wesList, String idpattern_Java,
	int warningCount, int warningLevel, String commandTag, CommandStatus status )
{	String routine = getClass().getName() + ".checkWellRights";
	String message;
	
	StateMod_Well wes_i = null;

	StateMod_Parcel parcel = null; // Parcel associated with a well station
	int wes_parcel_count = 0; // Parcel count for well station
	double wes_parcel_area = 0.0; // Area of parcels for well station
	int wes_well_parcel_count = 0; // Parcel (with wells) count for well station.
	double wes_well_parcel_area = 0.0; // Area of parcels with wells for well station.
	List<StateMod_Parcel> parcel_Vector; // List of parcels for well station.
	// TODO smalers 2019-05-29 figure out what to do with count
	//int count = 0; // Count of well stations with potential problems.
	String id_i = null;
	List<StateMod_WellRight> rightList = null;
	int welListSize = wesList.size();
	for ( int i = 0; i < welListSize; i++ ) {
		wes_i = wesList.get(i);
		if ( wes_i == null ) {
			continue;
		}
		id_i = wes_i.getID();
		rightList = StateMod_Util.getRightsForStation ( id_i, werList );
		// TODO SAM 2007-01-02 Evaluate how to put this code in a separate method and share between rights and stations.
		if ( (rightList == null) || (rightList.size() == 0) ) {
			// The following is essentially a copy of code for well
			// stations. Keep the code consistent.  Note that the
			// following assumes that when reading well rights from
			// HydroBase that lists of parcels are saved with well
			// stations.  This will clobber any parcel data that
			// may have been saved at the time that well stations
			// were processed (if processed in the same commands file).
			//++count;
			// Check for parcels...
			wes_parcel_count = 0;
			wes_parcel_area = 0.0;
			wes_well_parcel_count = 0;
			wes_well_parcel_area = 0.0;
			// Parcels associated with the well station
			parcel_Vector = wes_i.getParcels();
			if ( parcel_Vector != null ) {
				// Number of parcels associated with the well station
				wes_parcel_count = parcel_Vector.size();
				for ( int j = 0; j < wes_parcel_count; j++ ) {
					parcel = parcel_Vector.get(j);
					// Increment parcel area associated with the well station
					if ( parcel.getArea() > 0.0 ) {
						wes_parcel_area += parcel.getArea();
					}
					if ( parcel.getWellCount() > 0 ) {
						// Count and area of parcels that have wells
						wes_well_parcel_count += parcel.getWellCount();
						wes_well_parcel_area += parcel.getArea();
					}
				}
			}
			message = "The following well station has no water rights (no irrigated parcels served by " +
				"wells) well station ID=" + id_i +
				", well name=" + wes_i.getName() +
				", collection type=" + wes_i.getCollectionType() +
				", parcels for well station=" + wes_parcel_count +
				", parcel area for well station (acres)=" + StringUtil.formatString(wes_parcel_area,"%.3f") +
				", count of wells on parcels=" + wes_well_parcel_count +
				", area of parcels with wells (acres)=" + StringUtil.formatString(wes_well_parcel_area,"%.3f");
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			/*
			status.addToLog ( CommandPhaseType.RUN,
				new WellRightValidation(CommandStatusType.WARNING,
					message, "Data may be OK if the station has no wells.  " +
						"Parcel count and area in the following table are available " +
						"only if well rights are read from HydroBase." ) );
						*/
		}
	}

	// Since well rights are determined from parcel data, print a list of
	// well rights that do not have associated yield (decree)...

	int werListSize = werList.size();
	int pos = 0; // Position in well station vector
	String wes_name = null; // Well station name
	String wes_id = null; // Well station ID
	String wer_id = null; // Well right identifier
	double decree = 0.0;
	StateMod_WellRight wer_i = null;
	int matchCount = 0;
	for ( int i = 0; i < werListSize; i++ ) {
		wer_i = (StateMod_WellRight)werList.get(i);
		wer_id = wer_i.getID();
		if ( !wer_id.matches(idpattern_Java)) {
			continue;
		}
		++matchCount;
		// Format to two digits to match StateMod output...
		decree = StringUtil.atod(StringUtil.formatString(wer_i.getDcrdivw(),"%.2f") );
		if ( decree <= 0.0 ) {
			// Find associated well station for output to print ID and name...
			pos = StateMod_Util.indexOf(wesList,wer_i.getCgoto() );
			wes_i = null;
			if ( pos >= 0 ) {
				wes_i = (StateMod_Well)wesList.get(pos);
			}
			wes_name = "";
			if ( wes_i != null ) {
				wes_id = wes_i.getID();
				wes_name = wes_i.getName();
			}
			// Format suitable for output in a list that can be copied to a spreadsheet or table.
			message = "Well right \"" + wer_id + "\" (well station " + wes_id + " \"" + wes_name +
				"\") has decree (" + decree + ") <= 0.";
			Message.printWarning(warningLevel,
				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			/*
			status.addToLog ( CommandPhaseType.RUN,
				new WellRightValidation(CommandStatusType.FAILURE,
					message, "Verify that parcels are available for wells and check that well " +
						"right at 2-digit precision is > 0." ) );
						*/
		}
	}
	// Return values
	int [] retVals = new int[2];
	retVals[0] = matchCount;
	retVals[1] = warningCount;
	//return retVals;
}

/**
Write the well rights file.  The comments from the previous
rights file are transferred into the next one.  Also, a history is maintained
and printed in the header for the file.  Additional header comments can be added
through the new_comments parameter.
Comments for each data item will be written if provided - these are being used
for evaluation during development but are not a part of the standard file.
@param infile name of file to retrieve previous comments and history from
@param outfile name of output file to write.
@param theRights list of rights to write.
@param newComments additional comments to print to the comment section
@param writeProps Properties to control the rights.  Currently only WriteDataComments=True/False
and WriteExtendedDataComments are recognized
*/
public static void writeStateModFile(String infile, String outfile,
		List<StateMod_WellRight> theRights, List<String> newComments, PropList writeProps )
throws Exception {
	PrintWriter out = null;
	String routine = "StateMod_WellRight.writeStateModFile";
	
	if ( writeProps == null ) {
		// Create properties to check
		writeProps = new PropList ( "" );
	}
	String WriteDataComments = writeProps.getValue ( "WriteDataComments");
	boolean writeDataComments = false; // Default
	if ( (WriteDataComments != null) && WriteDataComments.equalsIgnoreCase("True") ) {
		writeDataComments = true;
	}
	String WriteExtendedDataComments = writeProps.getValue ( "WriteExtendedDataComments");
	boolean writeExtendedDataComments = false; // Default
	if ( (WriteExtendedDataComments != null) && WriteExtendedDataComments.equalsIgnoreCase("True") ) {
		writeExtendedDataComments = true;
	}

	if (outfile == null) {
		String msg = "Unable to write to null filename";
		Message.printWarning(3, routine, msg);
		throw new Exception(msg);
	}
			
	Message.printStatus(2, routine, "Writing well rights to: " + outfile);

	List<String> commentIndicators = new ArrayList<>(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new ArrayList<>(1);
	ignoredCommentIndicators.add ( "#>");
	try {	
		out = IOUtil.processFileHeaders(IOUtil.getPathUsingWorkingDir(infile),
			IOUtil.getPathUsingWorkingDir(outfile), 
			newComments, commentIndicators, ignoredCommentIndicators, 0);
	
		String iline = null;
		String cmnt = "#>";
		String format_0 = "%-12.12s%-24.24s%-12.12s%16.16s%8.2F%8d";
		StateMod_WellRight right = null;
		List<Object> v = new ArrayList<>(6);

		out.println(cmnt);
		out.println(cmnt + "***************************************************");
		out.println(cmnt + "  StateMod Well Right File (" + theRights.size() + " rights)");
		out.println(cmnt);
		List<String> fileSummary = getSummaryCommentList(theRights);
		for ( int i = 0; i < fileSummary.size(); i++ ) {
			out.println(cmnt + fileSummary.get(i));
		}
		out.println(cmnt);
		String format_add = "";
		if ( writeDataComments ) {
			format_add = ", 1x, i4," // Parcel year
				+ " 1x, i4," // Parcel match class
				+ " 1x, a6"; // Parcel ID (note 6 characters for legacy behavior)
		}
		else if ( writeExtendedDataComments ) {
			// Longer parcel ID to allow WD + ID, plus other information that shows the
			// original source of the right
			format_add = ", 1x, i4," // Parcel year
				+ " 1x, i4," // Parcel match class
				+ " 1x, a12," // Parcel ID (note 12 characters for new behavior)
				+ " 1x, a14," // Collection type
				+ " 1x, a8," // Part type
				+ " 1x, a20," // Part ID
				+ " 1x, a8," // Part ID type
				+ " 1x, a8," // WDID (if available)
				+ " 1x, a10," // Appropriation date YYYY-MM-DD
				+ " 1x, a11," // Appropriation date as administration number
				+ " 1x, a30," // Well right use (30 characters was maximum length in HydroBase as of 2016-10-03)
				+ " 1x, a8," // Receipt (if available)
				+ " 1x, a10," // Receipt date
				+ " 1x, a11," // Receipt date as administration number
				+ " 1x, f8.2," // Yield (GPM)
				+ " 1x, f8.2," // Yield (CFS)
				+ " 1x, f8.0," // APEX (GPM)
				+ " 1x, f8.2," // APEX (CFS)
				+ " 1x, f8.2," // Well fraction
				+ " 1x, f8.2," // Ditch fraction
				+ " 1x, f8.2"; // Yield prorated (GPM)
		}
		out.println(cmnt + "  Format:  (a12, a24, a12, f16.5, f8.2, i8" + format_add + ")");
		out.println(cmnt);
		out.println(cmnt + "     ID        cidvi:  Well right ID ");
		out.println(cmnt + "     Name     cnamew:  Well right name");
		out.println(cmnt + "     Struct    cgoto:  Well Structure ID associated with this right");
		out.println(cmnt + "     Admin #   irtem:  Administration number" );
		out.println(cmnt + "                       (priority, small is senior)");
		out.println(cmnt + "     Decree  dcrdivw:  Well right (cfs)");
		out.println(cmnt + "     On/Off  idvrsww:  Switch 0 = off, 1 = on");
		out.println(cmnt + "                       YYYY = on for years >= YYYY");
		out.println(cmnt + "                       -YYYY = off for years > YYYY" );
		String header1_add = "";
		String header2_add = "";
		String header3_add = "";
		if ( writeDataComments || writeExtendedDataComments) {
			if ( writeExtendedDataComments ) {
				// Wider ParcelID since more recent includes WD in ID
				header1_add = "|                      ";
				header2_add = "|PYr--Cls----ParcelID  ";
				header3_add = "xb--exb--exb----------e";
			}
			else {
				header1_add = "|               |";
				header2_add = "|PYr--Cls--PID  |";
				header3_add = "xb--exb--exb----e";
			}
			out.println(cmnt);
			out.println(cmnt + "The following data are NOT part of the standard StateMod file and StateMod will ignore.");
			out.println(cmnt + "                 Pyr:  Parcel year used for parcel/well matching (-999 if data applies to full period)");
			out.println(cmnt + "                 Cls:  Indicates how well was matched to parcel (see CDSS documentation).");
			out.println(cmnt + "            ParcelID:  Parcel ID for year.");
		}
		if ( writeExtendedDataComments ) {
			header1_add = header1_add + "|                                                     |                                                              |                                                                      Ditch    Well   Prorated";
			header2_add = header2_add + "|CollectionType PartType        PartID        IDType  |  WDID    ApproDate ApproDateAN               Use             | Receipt PermitDate PermtDateAN YieldGPM YieldCFS APEXGPM  APEXCFS |Fraction Fraction YieldGPM";
			header3_add = header3_add + "xb------------exb------exb------------------exb------exb------exb--------exb---------exb----------------------------exb------exb--------exb---------exb------exb------exb------exb------exb------exb------exb------e";
			out.println(cmnt);
			out.println(cmnt + "The following are output if extended data comments are requested and are useful for understanding parcel/well/ditch/right/permit.");
			out.println(cmnt + "      CollectionType:  Aggregate, System, etc.");
			out.println(cmnt + "            PartType:  Parcel ID for year.");
			out.println(cmnt + "              PartID:  Part ID for collection (original source of water right).");
			out.println(cmnt + "          PartIDType:  Part ID for PartID, if a well (WDID or Receipt).");
			out.println(cmnt + "                WDID:  Well structure WDID (if available) - part IDType controls initial data lookup.");
			out.println(cmnt + "           ApproDate:  Well right appropriation date (if available).");
			out.println(cmnt + "         ApproDateAN:  Well right appropriation date as administration number.");
			out.println(cmnt + "                 Use:  Well right decreed use.");
			out.println(cmnt + "             Receipt:  Well permit receipt (if available) - part IDType controls initial data lookup.");
			out.println(cmnt + "          PermitDate:  Permit date (if available).");
			out.println(cmnt + "         PermtDateAN:  Permit date as administration number.");
			out.println(cmnt + "            YieldGPM:  Well yield (GPM).");
			out.println(cmnt + "            YieldCFS:  Well yield (CFS).");
			out.println(cmnt + "             APEXGPM:  Alternate point/exchange yield, GPM, added to yield if requested during processing.");
			out.println(cmnt + "             APEXCFS:  Alternate point/exchange yield, CFS.");
			out.println(cmnt + "      Ditch Fraction:  Fraction of well yield to be used for this right (based on fraction of parcel served by ditch), 1.0 if no ditch.");
			out.println(cmnt + "       Well Fraction:  Fraction of well yield to be used for this right (based on number of wells serving parcel).");
			out.println(cmnt + "   Prorated YieldGPM:  Prorated yield (GPM, Yield*WellFraction*DitchFraction), may contain APEX depending on processing, equivalent to decree (CFS).");
		}
		out.println(cmnt);
		out.println(cmnt + "                                                                              " + header1_add );
		out.println(cmnt + "   ID               Name             Struct          Admin #   Decree  On/Off " + header2_add );
		out.println(cmnt + "---------eb----------------------eb----------eb--------------eb------eb------e" + header3_add );
		out.println(cmnt);
		out.println(cmnt + "EndHeader");
		out.println(cmnt);

		int num = 0;
		if (theRights != null) {
			num = theRights.size();
		}

		String comment = null; // Comment for data item
		for (int i = 0; i < num; i++) {
			right = theRights.get(i);
			if (right == null) {
				continue;
			}

			v.clear();
			v.add(right.getID());
			v.add(right.getName());
			v.add(right.getCgoto());
			v.add(right.getIrtem());
			v.add(Double.valueOf(right.getDcrdivw()));
			v.add(Integer.valueOf(right.getSwitch()));
			iline = StringUtil.formatString(v, format_0);
			if ( writeDataComments || writeExtendedDataComments) {
				comment = right.getComment(); // TODO SAM 2016-05-18 Figure out how this is used
				String parcelYear = "-999"; // Need to use this because well merging expects -999
				String parcelMatchClass = "    ";
				String parcelID = "      ";
				if ( writeExtendedDataComments ) {
					parcelID = "            ";
				}
				if ( right.getParcelYear() > 0 ) {
					parcelYear = StringUtil.formatString(right.getParcelYear(),"%4d");
				}
				if ( right.getParcelMatchClass() >= 0 ) {
					// TODO SAM 2016-05-17 Apparently the class does not get set to -999 or zero, etc. and can be -2147483648
					parcelMatchClass = StringUtil.formatString(right.getParcelMatchClass(),"%4d");
				}
				if ( (right.getParcelID() != null) && !right.getParcelID().equals("-999") ) {
					if ( writeExtendedDataComments ) {
						parcelID = StringUtil.formatString(right.getParcelID(),"%12.12s");
					}
					else {
						parcelID = StringUtil.formatString(right.getParcelID(),"%6.6s");
					}
				}
				comment = parcelYear + " " + parcelMatchClass + " " + parcelID;
				iline = iline + " " + comment;
			}
			if ( writeExtendedDataComments) {
				// Also add the additional properties
				StateMod_Well_CollectionType collectionType = right.getCollectionType();
				String collectionTypeString = "";
				if ( collectionType != null ) {
					collectionTypeString = collectionType.toString();
				}
				collectionTypeString = StringUtil.formatString(collectionTypeString,"%-14.14s");
				StateMod_Well_CollectionPartType partType = right.getCollectionPartType();
				String partTypeString = "";
				if ( partType != null ) {
					partTypeString = partType.toString();
				}
				partTypeString = StringUtil.formatString(partTypeString,"%-8.8s");
				String partId = right.getCollectionPartId();
				if ( partId == null ) {
					partId = "";
				}
				partId = StringUtil.formatString(partId,"%-20.20s");
				StateMod_Well_CollectionPartIdType partIdType = right.getCollectionPartIdType();
				String partIdTypeString = "";
				if ( partIdType != null ) {
					partIdTypeString = partIdType.toString();
				}
				partIdTypeString = StringUtil.formatString(partIdTypeString,"%-8.8s");
				String wdid = right.getXWDID();
				if ( wdid == null ) {
					wdid = "        ";
				}
				else {
					wdid = StringUtil.formatString(wdid,"%-8.8s");
				}
				Date approDate = right.getXApproDate();
				String approDateString = "";
				String approDateAdminNumberString = "";
				if ( approDate == null ) {
					approDateString = "          ";
					approDateAdminNumberString = "           ";
				}
				else {
					DateTime dt = new DateTime(approDate);
					approDateString = dt.toString();
					approDateAdminNumberString = StringUtil.formatString(right.getXApproDateAdminNumber(), "%-11.11s");
				}
				String use = right.getXUse();
				if ( use == null ) {
					use = "";
				}
				else {
					use = StringUtil.formatString(use,"%-30.30s");
				}
				String receipt = right.getXPermitReceipt();
				if ( receipt == null ) {
					receipt = "        ";
				}
				else {
					receipt = StringUtil.formatString(receipt,"%-8.8s");
				}
				Date permitDate = right.getXPermitDate();
				String permitDateString = "";
				String permitDateAdminNumberString = "";
				if ( permitDate == null ) {
					permitDateString = "          ";
					permitDateAdminNumberString = "           ";
				}
				else {
					DateTime dt = new DateTime(permitDate);
					permitDateString = dt.toString();
					permitDateAdminNumberString = StringUtil.formatString(right.getXPermitDateAdminNumber(), "%-11.11s");
				}
				double yieldGPM = right.getXYieldGPM();
				String yieldGPMString = "";
				String yieldCFSString = "";
				if ( Double.isNaN(yieldGPM) ) {
					yieldGPMString = "        ";
					yieldCFSString = "        ";
				}
				else {
					yieldGPMString = String.format("%8.2f", yieldGPM);
					yieldCFSString = String.format("%8.2f", yieldGPM*.002228);
				}
				double apexGPM = right.getXYieldApexGPM();
				String apexGPMString = "";
				String apexCFSString = "";
				if ( Double.isNaN(apexGPM) ) {
					apexGPMString = "        ";
					apexCFSString = "        ";
				}
				else {
					apexGPMString = String.format("%8.2f", apexGPM);
					apexCFSString = String.format("%8.2f", apexGPM*.002228);
				}
				double ditchFraction = right.getXDitchFraction();
				String ditchFractionString = "";
				if ( Double.isNaN(ditchFraction) ) {
					ditchFractionString = "        ";
				}
				else {
					ditchFractionString = String.format("%8.2f", ditchFraction);
				}
				double wellFraction = right.getXFractionYield();
				String wellFractionString = "";
				if ( Double.isNaN(wellFraction) ) {
					wellFractionString = "        ";
				}
				else {
					wellFractionString = String.format("%8.2f", wellFraction);
				}
				//double prorated = right.getXProratedYield();
				double proratedYield = right.getDcrdivw();
				String proratedYieldString = "";
				if ( Double.isNaN(proratedYield) ) {
					proratedYieldString = "        ";
				}
				else {
					proratedYieldString = String.format("%8.2f", proratedYield/.002228); // Convert decree as CFS to GPM
				}
				iline = iline + " " + collectionTypeString + " " + partTypeString + " " + partId + " " + partIdTypeString + " "
					+ wdid + " " + approDateString + " " + approDateAdminNumberString + " " + use + " " + receipt + " " + permitDateString
					+ " " + permitDateAdminNumberString + " " + yieldGPMString + " " + yieldCFSString + " "
					+ apexGPMString + " " + apexCFSString + " " + ditchFractionString + " "
					+ wellFractionString + " " + proratedYieldString;
			}
			// Print the line to the file
			out.println(iline);
		}
	} 
	catch(Exception e) {
		Message.printWarning(3, routine, e);
		throw e;
	}
	finally {
		if (out != null) {
			out.close();
		}
		out = null;
	}
}

/**
Writes a list of StateMod_WellRight objects to a list file.  A header is 
printed to the top of the file, containing the commands used to generate the 
file.  Any strings in the body of the file that contain the field delimiter will be wrapped in "...".  
@param filename the name of the file to which the data will be written.
@param delimiter the delimiter to use for separating field values.
@param update whether to update an existing file, retaining the current 
header (true) or to create a new file with a new header.
@param data the list of objects to write.
@param newComments the list of new comments to write to the header.
@throws Exception if an error occurs.
*/
public static void writeListFile(String filename, String delimiter, boolean update, List<StateMod_WellRight> data,
	List<String> newComments ) 
throws Exception {
	int size = 0;
	if (data != null) {
		size = data.size();
	}
	
	List<String> fields = new Vector<String>();
	fields.add("ID");
	fields.add("Name");
	fields.add("StationID");
	fields.add("AdministrationNumber");
	fields.add("Decree");
	fields.add("OnOff");
	int fieldCount = fields.size();

	String[] names = new String[fieldCount];
	String[] formats = new String[fieldCount]; 
	int comp = StateMod_DataSet.COMP_WELL_RIGHTS;
	String s = null;
	for (int i = 0; i < fieldCount; i++) {
		s = (String)fields.get(i);
		names[i] = StateMod_Util.lookupPropValue(comp, "FieldName", s);
		formats[i] = StateMod_Util.lookupPropValue(comp, "Format", s);
	}

	String oldFile = null;	
	if (update) {
		oldFile = IOUtil.getPathUsingWorkingDir(filename);
	}
	
	int j = 0;
	PrintWriter out = null;
	StateMod_WellRight right = null;
	List<String> commentIndicators = new Vector<String>(1);
	commentIndicators.add ( "#" );
	List<String> ignoredCommentIndicators = new Vector<String>(1);
	ignoredCommentIndicators.add ( "#>");
	String[] line = new String[fieldCount];
	StringBuffer buffer = new StringBuffer();
	
	try {
		// Add some basic comments at the top of the file.  Do this to a copy of the
		// incoming comments so that they are not modified in the calling code.
		List<String> newComments2 = null;
		if ( newComments == null ) {
			newComments2 = new Vector<String>();
		}
		else {
			newComments2 = new Vector<String>(newComments);
		}
		newComments2.add(0,"");
		newComments2.add(1,"StateMod well rights as a delimited list file.");
		newComments2.add(2,"");
		out = IOUtil.processFileHeaders(
			oldFile,
			IOUtil.getPathUsingWorkingDir(filename), 
			newComments2, commentIndicators, ignoredCommentIndicators, 0);

		for (int i = 0; i < fieldCount; i++) {
			if (i > 0) {
				buffer.append(delimiter);
			}
			buffer.append("\"" + names[i] + "\"");
		}

		out.println(buffer.toString());
		
		for (int i = 0; i < size; i++) {
			right = data.get(i);
			
			line[0] = StringUtil.formatString(right.getID(), formats[0]).trim();
			line[1] = StringUtil.formatString(right.getName(), formats[1]).trim();
			line[2] = StringUtil.formatString(right.getCgoto(), formats[2]).trim();
			line[3] = StringUtil.formatString(right.getIrtem(), formats[3]).trim();
			line[4] = StringUtil.formatString(right.getDcrdivw(), formats[4]).trim();
			line[5] = StringUtil.formatString(right.getSwitch(), formats[5]).trim();

			buffer = new StringBuffer();	
			for (j = 0; j < fieldCount; j++) {
				if (j > 0) {
					buffer.append(delimiter);
				}
				if (line[j].indexOf(delimiter) > -1) {
					line[j] = "\"" + line[j] + "\"";
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
	}
}

}
