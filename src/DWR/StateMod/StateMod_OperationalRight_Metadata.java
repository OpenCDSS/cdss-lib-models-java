package DWR.StateMod;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

/* TODO SAM 2010-12-09 Evaluate whether want to read from Excel
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
*/

import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Table.DataTable;

/**
Class to hold operational right metadata, which helps the software with error checks and visualization.
The term "Operational Right" and "Operating Rule" are used interchangeably.  For example, by knowing the
source types, software can present the proper lists of structures for editing and the proper node types
can be search for when annotating the network diagram.
*/
public class StateMod_OperationalRight_Metadata
{
	
/**
Indicate whether the editing class supports the right (true) - if false, a text editor will
be used for the operational right.
*/
private boolean __fullEditingSupported = false;

/**
Operational right (operating rule) type - to group rules for editing.
*/
private StateMod_OperationalRight_Metadata_RuleType __ruleTypeCategory;
	
/**
Operational right type (number).
*/
private int __rightTypeNumber;

/**
Operational right name.
*/
private String __rightTypeName;

/**
Types of data that can be used as the destination for an operational right.
*/
private StateMod_OperationalRight_Metadata_SourceOrDestinationType [] __destinationTypes = null;

/**
Types of data that can be used as the source1 for an operational right.
*/
private StateMod_OperationalRight_Metadata_SourceOrDestinationType [] __source1Types = null;

/**
Types of data that can be used as the source2 for an operational right.
*/
private StateMod_OperationalRight_Metadata_SourceOrDestinationType [] __source2Types = null;

/**
Types of plans that that can be used as the associated plan for an operational right.
*/
private StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] __associatedPlanAllowedTypes = null;

/**
Types of diversion that that can be used for an operational right.
*/
private StateMod_OperationalRight_Metadata_DiversionType [] __diversionTypes = null;

/**
Whether transit and conveyance loss are allowed for an operational right.
*/
private StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType __transitAndConveyanceLossAllowed = null;
	
/**
List of static global metadata, meant to be initialized once and shared within the application.
*/
private static List<StateMod_OperationalRight_Metadata> __opRightsMetadataList = null;

/**
Whether the operational right uses intervening structures.  These are usually indicated in the dumx value
and are provided in separate records.  This might be the same as whether the carrier is allowed
but Ray Bennett was going to check on it.
*/
private boolean __rightTypeUsesInterveningStructures = false;

/**
Constructor for metadata.
*/
public StateMod_OperationalRight_Metadata ( int rightTypeNumber, boolean fullEditingSupported,
	String rightName,
	StateMod_OperationalRight_Metadata_RuleType ruleTypeCategory,
	StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Types,
	StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Types,
	StateMod_OperationalRight_Metadata_Source2AllowedType source2AllowedType,
	StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationTypes,
	StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationType,
	StateMod_OperationalRight_Metadata_DeliveryMethodType deliveryMethodType,
	StateMod_OperationalRight_Metadata_CarrierAllowedType carrierAllowedType,
	boolean usesInterveningStructures,
	StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedTypes,
	StateMod_OperationalRight_Metadata_DiversionType [] diversionTypes,
	StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType transitAndConveyanceLossAllowedType,
	String comment )
{
	setRightTypeNumber ( rightTypeNumber );
	setFullEditingSupported ( fullEditingSupported );
	setRightTypeName ( rightName );
	setRuleTypeCategory ( ruleTypeCategory );
	setDestinationTypes ( destinationTypes );
	setSource1Types ( source1Types );
	setSource2Types ( source2Types );
	setUsesInterveningStructures ( usesInterveningStructures );
	setAssociatedPlanAllowedTypes ( associatedPlanAllowedTypes );
	setDiversionTypes ( diversionTypes );
	setTransitAndConveyanceLossAllowed ( transitAndConveyanceLossAllowedType );
}

/**
Return the list of all metadata.
*/
public static List<StateMod_OperationalRight_Metadata> getAllMetadata ()
{
	initialize();
	return __opRightsMetadataList;
}

/**
Return the allowed associated plan types for the right.
*/
public StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] getAssociatedPlanAllowedTypes ()
{
	return __associatedPlanAllowedTypes;
}

/**
Return the allowed destination types for the right.
*/
public StateMod_OperationalRight_Metadata_SourceOrDestinationType [] getDestinationTypes ()
{
	return __destinationTypes;
}

/**
Return the diversion types for the right.
*/
public StateMod_OperationalRight_Metadata_DiversionType [] getDiversionTypes ()
{
	return __diversionTypes;
}

/**
Return whether full editing is supported for the right.
*/
public boolean getFullEditingSupported ()
{
	return __fullEditingSupported;
}

/**
Return the metadata given an operational right number.
@param rightTypeNumber the operational right type number
*/
public static StateMod_OperationalRight_Metadata getMetadata ( int rightTypeNumber )
{
	initialize();
	for ( StateMod_OperationalRight_Metadata metadata : getAllMetadata() ) {
		if ( metadata.getRightTypeNumber() == rightTypeNumber ) {
			return metadata;
		}
	}
	return null;
}

/**
Return the operational right type name.
@return the operational right type name.
*/
public String getRightTypeName()
{
	return __rightTypeName;
}

/**
Return the operational right type name list (all names).
@return the operational right type name (all names).
*/
public static List<String> getRightTypeNameList()
{
	initialize();
	List<StateMod_OperationalRight_Metadata> metadataList = getAllMetadata();
	List<String>names = new Vector();
	for ( StateMod_OperationalRight_Metadata metadata: metadataList ) {
		names.add ( metadata.getRightTypeName() );
	}
	return names;
}

/**
Return the operational right type number.
@return the operational right type number.
*/
public int getRightTypeNumber()
{
	return __rightTypeNumber;
}

/**
Return the operating rule type.
@return the operating rule type.
*/
public StateMod_OperationalRight_Metadata_RuleType getRuleTypeCategory()
{
	return __ruleTypeCategory;
}

/**
Indicate whether the operational right uses an associated plan.
*/
public boolean getRightTypeUsesAssociatedPlan ()
{	StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedTypes =
		getAssociatedPlanAllowedTypes();
	if ( (associatedPlanAllowedTypes.length == 0) ||
		(associatedPlanAllowedTypes[0] == StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA) ) {
		return false;
	}
	else {
		return true;
	}
}

/**
Indicate whether the operational right uses conveyance loss.
*/
public boolean getRightTypeUsesConveyanceLoss ()
{
	StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType transitAndConveyanceLossAllowed =
		getTransitAndConveyanceLossAllowedType();
	if ( (transitAndConveyanceLossAllowed ==
		StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA) ) {
		return false;
	}
	else {
		return true;
	}
}

/**
Indicate whether the operational right uses the destination.
*/
public boolean getRightTypeUsesDestination ()
{	StateMod_OperationalRight_Metadata_SourceOrDestinationType [] allowedDestinationTypes =
		getDestinationTypes();
	if ( (allowedDestinationTypes.length == 0) ||
		(allowedDestinationTypes[0] == StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA) ) {
		return false;
	}
	else {
		return true;
	}
}

/**
Indicate whether the operational right uses a diversion type.
*/
public boolean getRightTypeUsesDiversionType ()
{	StateMod_OperationalRight_Metadata_DiversionType [] diversionTypes = getDiversionTypes();
	if ( (diversionTypes.length == 0) ||
		(diversionTypes[0] == StateMod_OperationalRight_Metadata_DiversionType.NA) ) {
		return false;
	}
	else {
		return true;
	}
}

/**
Indicate whether the operational right uses intervening structures.  Intervening structures are not required
but may be used.
*/
public boolean getRightTypeUsesInterveningStructures ()
{
	return __rightTypeUsesInterveningStructures;
}

/**
Indicate whether the operational right uses limits.
*/
public boolean getRightTypeUsesLimits ()
{
	return false; // FIXME SAM 2011-01-31 Need to check when other rights are enabled
	/*
	StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType transitAndConveyanceLossAllowed =
		getTransitAndConveyanceLossAllowedType();
	if ( (transitAndConveyanceLossAllowed ==
		StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA) ) {
		return false;
	}
	else {
		return true;
	}
	*/
}

/**
Indicate whether the operational right uses monthly switches.  Switches are not required, but may be used.
*/
public boolean getRightTypeUsesMonthlySwitch ()
{
	// TODO SAM 2011-01-29 For now always return true.  If any right types are enabled that do not
	// use the monthly switch, add to the metadata and return the corresponding data member
	return true;
}

/**
Indicate whether the operational right uses negative reservoir destination accounts.
For now hard code here but once list is known, may add to metadata.
*/
public boolean getRightTypeUsesNegativeDestinationAccounts ()
{
	int rightTypeNumber = getRightTypeNumber();
	if ( rightTypeNumber == 2 ) {
		return true;
	}
	else {
		return false;
	}
}

/**
Indicate whether the operational right uses the source 1.
*/
public boolean getRightTypeUsesSource1 ()
{	StateMod_OperationalRight_Metadata_SourceOrDestinationType [] allowedSourceTypes =
		getSource1Types();
	if ( (allowedSourceTypes.length == 0) ||
		(allowedSourceTypes[0] == StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA) ) {
		return false;
	}
	else {
		return true;
	}
}

/**
Indicate whether the operational right uses the source 2.
*/
public boolean getRightTypeUsesSource2 ()
{	StateMod_OperationalRight_Metadata_SourceOrDestinationType [] allowedSourceTypes =
		getSource2Types();
	if ( (allowedSourceTypes.length == 0) ||
		(allowedSourceTypes[0] == StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA) ) {
		return false;
	}
	else {
		return true;
	}
}

/**
Indicate whether the operational right uses the special source account 2 choices.
*/
public boolean getRightTypeUsesSpecialSourceAccount2 ()
{	int rightTypeNumber = getRightTypeNumber();
	if ( rightTypeNumber == 2 ) {
		return true;
	}
	else {
		return false;
	}
}

/**
TODO SAM 2011-01-31 Move to metadata if a pattern emerges
Return the special source2 types for the right.
*/
public List<String> getSourceAccount2SpecialChoices ()
{
	List<String> specialChoices = new Vector();
	int rightTypeNumber = getRightTypeNumber();
	if ( rightTypeNumber == 2 ) {
		specialChoices.add("0 - Reservoir demand is not adjusted");
		specialChoices.add("-1 - Provide depletion replacement");
		for ( int i = 1; i <= 100; i++ ) {
			specialChoices.add("" + i + " - Limit reservoir demand to CIR/n");
		}
	}
	return specialChoices;
}

/**
Return the allowed source1 types for the right.
*/
public StateMod_OperationalRight_Metadata_SourceOrDestinationType [] getSource1Types ()
{
	return __source1Types;
}

/**
Return the allowed source2 types for the right.
*/
public StateMod_OperationalRight_Metadata_SourceOrDestinationType [] getSource2Types ()
{
	return __source2Types;
}

/**
Return whether transit and conveyance loss is allowed for the right type.
*/
public StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType getTransitAndConveyanceLossAllowedType ()
{
	return __transitAndConveyanceLossAllowed;
}

/**
Initialize the singleton list of operational right metadata.  Do this rather than having a large amount
of static data in memory.
*/
private static void initialize ()
{
	if ( __opRightsMetadataList != null ) {
		// No need to initialize.
		return;
	}
	__opRightsMetadataList = new Vector();
	// Initialize the list of metadata
	StateMod_OperationalRight_Metadata metaData = null;
	// Loop through the number of known operational right types.  This should ideally be the
	// same number as StateMod_OperationalRight.MAX_HANDLED_TYPE but may be different during
	// development
	for ( int i = 1; i <= 49; i++ ) {
		switch ( i ) {
			case 1:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_1 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_1 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_1 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.INSTREAM_FLOW};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_1 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_1 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_1 =
					{StateMod_OperationalRight_Metadata_DiversionType.NA};
					// Summary has...
					//{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, true,
					"Reservoir Release to an Instream Flow",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_1,
					source2Array_1,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_1,
					destinationLocationArray_1,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_1,
					diversionTypeArray_1,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 2:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_2 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_2 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_2 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR };
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_2 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_2 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_2 =
					{StateMod_OperationalRight_Metadata_DiversionType.NA};
					// Summary has the following...
					//{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, true,
					"Reservoir Release to a Diversion, Reservoir, or Carrier",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_2,
					source2Array_2,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_2,
					destinationLocationArray_2,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_2,
					diversionTypeArray_2,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 3:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_3 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_3 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_3 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					//StateMod_OperationalRight_Metadata_SourceOrDestinationType.CARRIER,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR };
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_3 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_3 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_3 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Reservoir Release to a Direct Diversion or Reservoir by a Carrier",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_3,
					source2Array_3,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_3,
					destinationLocationArray_3,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					false,
					associatedPlanAllowedArray_3,
					diversionTypeArray_3,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 4:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_4 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_4 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_4 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR };
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_4 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.UPSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_4 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_4 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Reservoir Release to a Direct Diversion by Exchange with the River",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_4,
					source2Array_4,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_4,
					destinationLocationArray_4,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_4,
					diversionTypeArray_4,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 5:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_5 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_5 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_5 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR };
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_5 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_5 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_5 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Reservoir Storage by Exchange",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_5,
					source2Array_5,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_5,
					destinationLocationArray_5,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_5,
					diversionTypeArray_5,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 6:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_6 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_6 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_6 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR };
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_6 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.SOURCE};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_6 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_6 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Paper Exchange Between Reservoirs",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_6,
					source2Array_6,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_6,
					destinationLocationArray_6,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.BOOKOVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_6,
					diversionTypeArray_6,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 7:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_7 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_7 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_7 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.CARRIER };
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_7 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.UPSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_7 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_7 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Reservoir to a Carrier by Exchange",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_7,
					source2Array_7,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_7,
					destinationLocationArray_7,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_7,
					diversionTypeArray_7,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 8:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_8 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_8 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_8 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR };
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_8 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_8 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_8 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Out of Priority Reservoir Bookover",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_8,
					source2Array_8,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_8,
					destinationLocationArray_8,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_8,
					diversionTypeArray_8,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 9:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_9 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_9 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_9 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA };
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_9 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_9 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_9 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Reservoir Release to Meet Target",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_9,
					source2Array_9,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_9,
					destinationLocationArray_9,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_9,
					diversionTypeArray_9,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 10:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_10 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_10 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_10 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION };
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_10 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_10 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_10 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION,
					StateMod_OperationalRight_Metadata_DiversionType.DEPLETION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"General Replacement Reservoir to a Diversion by a Direct Release or Exchange",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_10,
					source2Array_10,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_10,
					destinationLocationArray_10,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_10,
					diversionTypeArray_10,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"StateMod determines if the supply is the river or by exchange" );
				break;
			case 11:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_11 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION_RIGHT,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR_RIGHT};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_11 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_11 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_11 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_11 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_11 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Carrier to a Ditch or Reservoir",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_11,
					source2Array_11,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_11,
					destinationLocationArray_11,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.CARRIER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_11,
					diversionTypeArray_11,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"Same as type 32 but does not allow reuse and loss" );
				break;
			case 12:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_12 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_12 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_12 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_12 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_12 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_12 =
					{StateMod_OperationalRight_Metadata_DiversionType.NA};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Reoperate Water Rights",
					StateMod_OperationalRight_Metadata_RuleType.OTHER,
					source1Array_12,
					source2Array_12,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_12,
					destinationLocationArray_12,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_12,
					diversionTypeArray_12,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 13:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_13 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.STREAM_GAGE};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_13 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_13 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.INSTREAM_FLOW};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_13 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_13 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_13 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"La Plata Compact (Index Flow Contraint on Stream Gage)",
					StateMod_OperationalRight_Metadata_RuleType.COMPACT,
					source1Array_13,
					source2Array_13,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_13,
					destinationLocationArray_13,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_13,
					diversionTypeArray_13,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 14:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_14 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION_RIGHT};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_14 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_14 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_14 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_14 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_14 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Carrier Right with Constrained Demand",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_14,
					source2Array_14,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_14,
					destinationLocationArray_14,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_14,
					diversionTypeArray_14,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 15:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_15 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION_RIGHT};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_15 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_15 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.INSTREAM_FLOW};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_15 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_15 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_15 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Interruptible Supply",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_15,
					source2Array_15,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_15,
					destinationLocationArray_15,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					true,
					associatedPlanAllowedArray_15,
					diversionTypeArray_15,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 16:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_16 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION_RIGHT};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_16 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_16 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_16 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.UPSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_16 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_16 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Direct Flow Storage",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_16,
					source2Array_16,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_16,
					destinationLocationArray_16,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					true,
					associatedPlanAllowedArray_16,
					diversionTypeArray_16,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 17:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_17 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.STREAM_GAGE};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_17 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.STREAM_GAGE};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_17 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.INSTREAM_FLOW};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_17 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_17 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_17 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Rio Grande Compact (Rio Grande)",
					StateMod_OperationalRight_Metadata_RuleType.COMPACT,
					source1Array_17,
					source2Array_17,
					StateMod_OperationalRight_Metadata_Source2AllowedType.REQUIRED,
					destinationArray_17,
					destinationLocationArray_17,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_17,
					diversionTypeArray_17,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"Compact Data" );
				break;
			case 18:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_18 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.STREAM_GAGE};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_18 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.STREAM_GAGE};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_18 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.INSTREAM_FLOW};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_18 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_18 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_18 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Rio Grande Compact (Conejos)",
					StateMod_OperationalRight_Metadata_RuleType.COMPACT,
					source1Array_18,
					source2Array_18,
					StateMod_OperationalRight_Metadata_Source2AllowedType.REQUIRED,
					destinationArray_18,
					destinationLocationArray_18,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_18,
					diversionTypeArray_18,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"Compact Data" );
				break;
			case 19:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_19 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.STREAM_GAGE};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_19 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_19 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_19 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_19 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_19 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Split Channel Operation",
					StateMod_OperationalRight_Metadata_RuleType.OTHER,
					source1Array_19,
					source2Array_19,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_19,
					destinationLocationArray_19,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_19,
					diversionTypeArray_19,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 20:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_20 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_20 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_20 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_20 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.SOURCE};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_20 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_20 =
					{StateMod_OperationalRight_Metadata_DiversionType.NA};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"San Juan Reservoir RIP Operation",
					StateMod_OperationalRight_Metadata_RuleType.COMPACT,
					source1Array_20,
					source2Array_20,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_20,
					destinationLocationArray_20,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_20,
					diversionTypeArray_20,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 21:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_21 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_21 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_21 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.WELL};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_21 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.SOURCE};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_21 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_21 =
					{StateMod_OperationalRight_Metadata_DiversionType.NA};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Wells with Sprinkler Use",
					StateMod_OperationalRight_Metadata_RuleType.OTHER,
					source1Array_21,
					source2Array_21,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_21,
					destinationLocationArray_21,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_21,
					diversionTypeArray_21,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 22:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_22 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_22 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_22 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.WELL};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_22 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.SOURCE};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_22 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_22 =
					{StateMod_OperationalRight_Metadata_DiversionType.NA};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Soil Moisture Use",
					StateMod_OperationalRight_Metadata_RuleType.SOIL_MOISTURE,
					source1Array_22,
					source2Array_22,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_22,
					destinationLocationArray_22,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_22,
					diversionTypeArray_22,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 23:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_23 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.STREAM_GAGE};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_23 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_23 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DOWNSTREAM_CALL};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_23 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_23 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_23 =
					{StateMod_OperationalRight_Metadata_DiversionType.NA};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Downstream Call",
					StateMod_OperationalRight_Metadata_RuleType.OTHER,
					source1Array_23,
					source2Array_23,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_23,
					destinationLocationArray_23,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_23,
					diversionTypeArray_23,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 24:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_24 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION_RIGHT};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_24 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_TC};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_24 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.CARRIER,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_24 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.UPSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_24 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_TC};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_24 =
					{StateMod_OperationalRight_Metadata_DiversionType.DEPLETION,
					StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Direct Flow Exchange of a Pro-rata Water Right",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_24,
					source2Array_24,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_24,
					destinationLocationArray_24,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_24,
					diversionTypeArray_24,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.YES,
					"Exchange Limit" );
				break;
			case 25:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_25 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION_RIGHT};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_25 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_TC};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_25 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.CARRIER,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_25 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_25 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_TC};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_25 =
					{StateMod_OperationalRight_Metadata_DiversionType.DEPLETION,
					StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Direct Flow Bypass of a Pro-rata Water Right",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_25,
					source2Array_25,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_25,
					destinationLocationArray_25,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_25,
					diversionTypeArray_25,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.YES,
					"Exchange Limit" );
				break;
			case 26:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_26 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_26 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_26 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_26 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.NA};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_26 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_26 =
					{StateMod_OperationalRight_Metadata_DiversionType.NA};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Not currently used - see type 48, 49",
					StateMod_OperationalRight_Metadata_RuleType.NA,
					source1Array_26,
					source2Array_26,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_26,
					destinationLocationArray_26,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.NA,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NA,
					false,
					associatedPlanAllowedArray_26,
					diversionTypeArray_26,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 27:
				// TODO SAM 2010-12-13 Need to check the following
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_27 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_ACCOUNTING,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_27 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_TC};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_27 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.CARRIER,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_27 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_27 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_DIVERSION,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_RESERVOIR,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_27 =
					{StateMod_OperationalRight_Metadata_DiversionType.DEPLETION,
					StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Reservoir, Reuse, or Accounting Plan to a Diversion, Reservoir, or Carrier, with Reuse",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_PLAN_STRUCTURE,
					source1Array_27,
					source2Array_27,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_27,
					destinationLocationArray_27,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_27,
					diversionTypeArray_27,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.YES,
					"" );
				break;
			case 28:
				// TODO SAM 2010-12-13 Need to check the following
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_28 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_ACCOUNTING,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_28 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_TC};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_28 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.CARRIER,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_28 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.UPSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_28 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_DIVERSION,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_RESERVOIR,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_28 =
					{StateMod_OperationalRight_Metadata_DiversionType.DEPLETION,
					StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Reuse Plan to a Diversion or Reservoir by Exchange with or without Destination Reuse",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_PLAN_STRUCTURE,
					source1Array_28,
					source2Array_28,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_28,
					destinationLocationArray_28,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_28,
					diversionTypeArray_28,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.YES,
					"" );
				break;
			case 29:
				// TODO SAM 2010-12-13 Need to check the following
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_29 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_ACCOUNTING,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_29 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_TC};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_29 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.CARRIER,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_29 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.UPSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_29 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_29 =
					{StateMod_OperationalRight_Metadata_DiversionType.NA};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Reuse or Accounting Plan Spill",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_PLAN_STRUCTURE,
					source1Array_29,
					source2Array_29,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_29,
					destinationLocationArray_29,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_29,
					diversionTypeArray_29,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 30:
				// TODO SAM 2010-12-13 Need to check the following
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_30 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.OPERATIONAL_RIGHT};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_30 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_30 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_30 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.SOURCE};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_30 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_30 =
					{StateMod_OperationalRight_Metadata_DiversionType.NA};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Rservoir Re-diversion",
					StateMod_OperationalRight_Metadata_RuleType.OTHER,
					source1Array_30,
					source2Array_30,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_30,
					destinationLocationArray_30,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					true,
					associatedPlanAllowedArray_30,
					diversionTypeArray_30,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 31:
				// TODO SAM 2010-12-13 Need to check the following
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_31 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION_RIGHT };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_31 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_31 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.CARRIER,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_31 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_31 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_DIVERSION,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_RESERVOIR,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_31 =
					{StateMod_OperationalRight_Metadata_DiversionType.DEPLETION,
					StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Carrier Right to a Ditch or Reservoir with Reuseable Return Flows",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_31,
					source2Array_31,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_31,
					destinationLocationArray_31,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.CARRIER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_31,
					diversionTypeArray_31,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.YES,
					"" );
				break;
			case 32:
				// TODO SAM 2010-12-13 Need to check the following
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_32 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_32 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_32 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.CARRIER,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_32 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				// TODO SAM 2010-12-13 Should reservoir account be added?
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_32 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_DIVERSION,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_RESERVOIR,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_32 =
					{StateMod_OperationalRight_Metadata_DiversionType.DEPLETION,
					StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Reservoir and Plan to a Direct Flow or Reservoir or Carrier Direct with or without Destination Reuse",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_PLAN_STRUCTURE,
					source1Array_32,
					source2Array_32,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_32,
					destinationLocationArray_32,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_32,
					diversionTypeArray_32,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.YES,
					"Same as type 11 but allows reuse and loss." );
				break;
			case 33:
				// TODO SAM 2010-12-13 Need to check the following
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_33 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_33 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_33 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.CARRIER,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_33 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.UPSTREAM};
				// TODO SAM 2010-12-13 Should reservoir account be added?
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_33 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_DIVERSION,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_RESERVOIR,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_33 =
					{StateMod_OperationalRight_Metadata_DiversionType.DEPLETION,
					StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Reservoir and Plan to a Direct Flow or Reservoir or Carrier by Exchange with or without Destination Reuse",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_33,
					source2Array_33,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_33,
					destinationLocationArray_33,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_33,
					diversionTypeArray_33,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.YES,
					"" );
				break;
			case 34:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_34 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_34 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_OUT_OF_PRIORITY,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_34 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.CARRIER,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_34 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.SOURCE};
				// TODO SAM 2010-12-13 Should reservoir account be added?
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_34 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_OUT_OF_PRIORITY,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_DIVERSION,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_RESERVOIR,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_34 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Reservoir to Reservoir Transfer (Bookover) with Reuse",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_34,
					source2Array_34,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_34,
					destinationLocationArray_34,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.BOOKOVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_34,
					diversionTypeArray_34,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 35:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_35 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_TRANSMOUNTAIN_IMPORT };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_35 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_35 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.CARRIER,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_35 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_35 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_DIVERSION,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_RESERVOIR,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_35 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Import to a Diversion, Reservoir, or Carrier with or without Reuse",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_PLAN_STRUCTURE,
					source1Array_35,
					source2Array_35,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_35,
					destinationLocationArray_35,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_35,
					diversionTypeArray_35,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 36:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_36 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION_RIGHT };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_36 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_36 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_36 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.SOURCE};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_36 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_36 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Seasonal (daily) On/Off Capability (e.g., Meadow Rights)",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_36,
					source2Array_36,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_36,
					destinationLocationArray_36,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_36,
					diversionTypeArray_36,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"Limits a diversion to part of the year (season)" );
				break;
			case 37:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_37 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.WELL_RIGHT };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_37 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_WELL_AUGMENTATION };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_37 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_TC,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_WELL_AUGMENTATION};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_37 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_37 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_DIVERSION,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_RESERVOIR,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_37 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Augmentation Well",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_GROUNDWATER,
					source1Array_37,
					source2Array_37,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_37,
					destinationLocationArray_37,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_37,
					diversionTypeArray_37,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 38:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_38 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR_RIGHT };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_38 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR_RIGHT };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_38 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_38 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.SOURCE};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_38 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_DIVERSION,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_RESERVOIR,
					StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_38 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Out of Priority Diversion (addresses the upstream storage statute)",
					StateMod_OperationalRight_Metadata_RuleType.STORAGE_OPERATIONS,
					source1Array_38,
					source2Array_38,
					StateMod_OperationalRight_Metadata_Source2AllowedType.REQUIRED,
					destinationArray_38,
					destinationLocationArray_38,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_38,
					diversionTypeArray_38,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 39:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_39 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION_RIGHT };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_39 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_39 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_39 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM,
					StateMod_OperationalRight_Metadata_DestinationLocationType.UPSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_39 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_39 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Alternate Point Diversion",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_39,
					source2Array_39,
					StateMod_OperationalRight_Metadata_Source2AllowedType.REQUIRED,
					destinationArray_39,
					destinationLocationArray_39,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_39,
					diversionTypeArray_39,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 40:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_40 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.STREAM_GAGE };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_40 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.STREAM_GAGE };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_40 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.INSTREAM_FLOW};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_40 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_40 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_40 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"South Platte Compact",
					StateMod_OperationalRight_Metadata_RuleType.COMPACT,
					source1Array_40,
					source2Array_40,
					StateMod_OperationalRight_Metadata_Source2AllowedType.REQUIRED,
					destinationArray_40,
					destinationLocationArray_40,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					true,
					associatedPlanAllowedArray_40,
					diversionTypeArray_40,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 41:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_41 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR_RIGHT };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_41 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_41 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_41 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.SOURCE};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_41 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_41 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Reservoir Storage with Special Limits",
					StateMod_OperationalRight_Metadata_RuleType.STORAGE_OPERATIONS,
					source1Array_41,
					source2Array_41,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_41,
					destinationLocationArray_41,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					true,
					associatedPlanAllowedArray_41,
					diversionTypeArray_41,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"OOP Plan Limits" );
				break;
			case 42:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_42 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_OUT_OF_PRIORITY,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_TC,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_WELL_AUGMENTATION};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_42 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_42 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_42 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.SOURCE};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_42 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_42 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Plan Demand Reset",
					StateMod_OperationalRight_Metadata_RuleType.STORAGE_OPERATIONS,
					source1Array_42,
					source2Array_42,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_42,
					destinationLocationArray_42,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_42,
					diversionTypeArray_42,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 43:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_43 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_43 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_43 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_43 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.SOURCE};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_43 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_WELL_AUGMENTATION };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_43 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"In-Priority Well Depletion",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_PLAN_STRUCTURE,
					source1Array_43,
					source2Array_43,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_43,
					destinationLocationArray_43,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_43,
					diversionTypeArray_43,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 44:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_44 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.WELL_RIGHT};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_44 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_44 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_44 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.SOURCE};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_44 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_WELL_AUGMENTATION };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_44 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Recharge Well",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_GROUNDWATER,
					source1Array_44,
					source2Array_44,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_44,
					destinationLocationArray_44,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_44,
					diversionTypeArray_44,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 45:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_45 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION_RIGHT};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_45 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_45 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_45 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.SOURCE};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_45 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.PLAN_RECHARGE };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_45 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Carrier with Transit Loss (allows multiple carriers and associated losses)",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_45,
					source2Array_45,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_45,
					destinationLocationArray_45,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_45,
					diversionTypeArray_45,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.YES,
					"" );
				break;
			case 46:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_46 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_ACCOUNTING};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_46 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_46 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_ACCOUNTING};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_46 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.SOURCE};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_46 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_46 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Multiple Ownership Plans (distributes plan contents to multiple plans)",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_PLAN_STRUCTURE,
					source1Array_46,
					source2Array_46,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_46,
					destinationLocationArray_46,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_46,
					diversionTypeArray_46,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"Multiple Ownership" );
				break;
			case 47:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_47 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_ACCOUNTING};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_47 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_47 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.CARRIER,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_47 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.SOURCE};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_47 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_47 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Administration Plan Limits",
					StateMod_OperationalRight_Metadata_RuleType.STORAGE_OPERATIONS,
					source1Array_47,
					source2Array_47,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_47,
					destinationLocationArray_47,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					true,
					associatedPlanAllowedArray_47,
					diversionTypeArray_47,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 48:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_48 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_RECHARGE,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_TC,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_SPECIAL_WELL_AUGMENTATION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_WELL_AUGMENTATION};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_48 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_TC };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_48 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_TC,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_SPECIAL_WELL_AUGMENTATION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_WELL_AUGMENTATION};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_48 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				// FIXME SAM 2011-01-31 the following was YES but need list of specific plans
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_48 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_48 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Plan or Reservoir Reuse to a Plan Direct",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_PLAN_STRUCTURE,
					source1Array_48,
					source2Array_48,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_48,
					destinationLocationArray_48,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_48,
					diversionTypeArray_48,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
			case 49:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_49 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_RECHARGE,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_DIVERSION_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_REUSE_TO_RESERVOIR_FROM_TRANSMOUNTAIN,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_TC,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_SPECIAL_WELL_AUGMENTATION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_WELL_AUGMENTATION};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_49 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_TC };
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_49 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_TC,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_SPECIAL_WELL_AUGMENTATION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.PLAN_WELL_AUGMENTATION};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_49 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.UPSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_49 =
					// FIXME SAM 2011-01-31 the following was YES but need list of specific plans	
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NA };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_49 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i, false,
					"Plan or Reservoir Reuse to a Plan by Exchange",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_PLAN_STRUCTURE,
					source1Array_49,
					source2Array_49,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_49,
					destinationLocationArray_49,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					false,
					associatedPlanAllowedArray_49,
					diversionTypeArray_49,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NA,
					"" );
				break;
		}
		__opRightsMetadataList.add ( metaData );
	}
}

/**
Read the global public metadata.  This is intended to supply metadata only for rights that are not
understood by the code.  For example, it is possible that new rights will be added to the FORTRAN model
but the Java will not be updated.  Consequently, the external file helps the software implement advanced
features such as drawing the operational right on the network.
Currently the only data read indicate whether full editing for the right is supported.
@param filename name of operational rights metadata
*/
public static void readGlobalData ( String filename )
throws FileNotFoundException, IOException
{	String routine = "StateMod_OperationalRight_Metadata.readGlobalData";
	try {
		PropList props = new PropList("opr");
		props.set("CommentLineIndicator=#");
		props.set("Delimiter=,");
		props.set("ColumnDataTypes=Auto");
		props.set("TrimStrigs=true");
		DataTable table = DataTable.parseFile(filename, props);
		// Loop through the table rows and reset if any rights are not supported for editing
		int nrows = table.getNumberOfRecords();
		String fullEditingSupported;
		Integer ruleType;
		for ( int iRow = 0; iRow < nrows; iRow++ ) {
			ruleType = (Integer)table.getFieldValue(iRow,"Type");
			fullEditingSupported = (String)table.getFieldValue(iRow, "EditingSupported" );
			if ( (fullEditingSupported != null) && fullEditingSupported.equalsIgnoreCase("no") ) {
				Message.printStatus(2, routine, "Turning off full editing for operational right type " + ruleType );
				StateMod_OperationalRight_Metadata.getMetadata(ruleType).setFullEditingSupported(false);
			}
		}
	}
	catch ( Exception e ) {
		throw new IOException ( "Error reading operational right configuruation file \"" + filename + "\"", e );
	}
}
	
/**
Read the contents of the operating rule definitions file.
@param filename name of file to read (absolute path).
@return a list of StateMod_OperationalRights_Metadata from the definitions file.
*/
/*
public static List readSpreadsheet ( String filename )
throws FileNotFoundException, IOException
{	String routine = "StateMod_OperationalRight_Metadata.readSpreadsheet";
	FileInputStream fis = null;
	List metadataList = new Vector();
	try {
		POIFSFileSystem fs = null;
		fs = new POIFSFileSystem(fis = new FileInputStream(filename) );
		// Create a workbook (why not just get it from the fs?)
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		// Get the first sheet from the workbook
		HSSFSheet mySheet = wb.getSheetAt(0);
		// Iterate through the rows and cells
		Iterator rowIter = mySheet.rowIterator(); 
		while(rowIter.hasNext()){
			  HSSFRow myRow = (HSSFRow)rowIter.next();
			  Iterator cellIter = myRow.cellIterator();
			  Vector cellStoreVector=new Vector();
			  while(cellIter.hasNext()) {
				  HSSFCell myCell = (HSSFCell) cellIter.next();
				  cellStoreVector.addElement(myCell);
				  Message.printStatus(2,routine, "Row " + myRow.getRowNum() + " Cell " + myCell.getColumnIndex() +
					  " value=" + myCell );
			  }
		 }
	}
	finally {
		if ( fis != null ) {
			fis.close();
		}
	}
	return metadataList;
}
*/

/**
Set the allowed associated plan types (in agreement with StateMod documentation).
Make private because objects should be immutable.
@param associatedPlanAllowedTypes types of destinations allowed for the right
*/
private void setAssociatedPlanAllowedTypes (
	StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedTypes )
{
	__associatedPlanAllowedTypes = associatedPlanAllowedTypes;
}

/**
Set the destination types (in agreement with StateMod documentation).
Make private because objects should be immutable.
@param destinationTypes types of destinations allowed for the right
*/
private void setDestinationTypes ( StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationTypes )
{
	__destinationTypes = destinationTypes;
}

/**
Set the allowed diversion types (in agreement with StateMod documentation).
Make private because objects should be immutable.
@param diversionTypes types of diversions allowed for the right
*/
private void setDiversionTypes (
	StateMod_OperationalRight_Metadata_DiversionType [] diversionTypes )
{
	__diversionTypes = diversionTypes;
}

/**
Set whether full editing is supported.
@param fullEditingSupported whether full editing is supported
*/
private void setFullEditingSupported ( boolean fullEditingSupported )
{
	__fullEditingSupported = fullEditingSupported;
}

/**
Set the operational right name (in agreement with StateMod documentation).
Make private because objects should be immutable.
@param rightTypeName name of operational right
*/
private void setRightTypeName ( String rightTypeName )
{
	__rightTypeName = rightTypeName;
}

/**
Set the operational right type (1+ in agreement with StateMod documentation).
Make private because objects should be immutable.
@param rightTypeNumber type (number) of operational right
*/
private void setRightTypeNumber ( int rightTypeNumber )
{
	__rightTypeNumber = rightTypeNumber;
}

/**
Set the operating rule type, in agreement with StateMod documentation.
Make private because objects should be immutable.
@param ruleTypeCategory category of operating rule
*/
private void setRuleTypeCategory ( StateMod_OperationalRight_Metadata_RuleType ruleTypeCategory )
{
	__ruleTypeCategory = ruleTypeCategory;
}

/**
Set the source1 types (in agreement with StateMod documentation).
Make private because objects should be immutable.
@param sourceTypes types of destinations allowed for the right
*/
private void setSource1Types ( StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Types )
{
	__source1Types = source1Types;
}

/**
Set the source2 types (in agreement with StateMod documentation).
Make private because objects should be immutable.
@param sourceTypes types of destinations allowed for the right
*/
private void setSource2Types ( StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Types )
{
	__source2Types = source2Types;
}

/**
Set whether transit and conveyance loss are allowed.
Make private because objects should be immutable.
@param diversionTypes types of diversions allowed for the right
*/
private void setTransitAndConveyanceLossAllowed (
	StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType transitAndConveyanceLossAllowed )
{
	__transitAndConveyanceLossAllowed = transitAndConveyanceLossAllowed;
}

/**
Set whether the operational right uses intervening structures.
Make private because objects should be immutable.
@param usesInterveningStructures whether intervening structures are used
*/
private void setUsesInterveningStructures ( boolean usesInterveningStructures )
{
	__rightTypeUsesInterveningStructures = usesInterveningStructures;
}

}