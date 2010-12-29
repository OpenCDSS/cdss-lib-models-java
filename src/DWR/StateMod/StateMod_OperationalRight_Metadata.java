package DWR.StateMod;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/* TODO SAM 2010-12-09 Evaluate whether want to read from Excel
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
*/

import RTi.Util.Message.Message;

/**
Class to hold operational right metadata, which helps the software with error checks and visualization.
The term "Operational Right" and "Operating Rule" are used interchangeably.  For example, by knowing the
source types, software can present the proper lists of structures for editing and the proper node types
can be search for when annotating the network diagram.
*/
public class StateMod_OperationalRight_Metadata
{

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
List of static global metadata, meant to be initialized once and shared within the application.
*/
private static List<StateMod_OperationalRight_Metadata> __opRightsMetadataList = null;

/**
Constructor for metadata.
*/
public StateMod_OperationalRight_Metadata ( int rightTypeNumber, String rightName,
	StateMod_OperationalRight_Metadata_RuleType ruleTypeCategory,
	StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Types,
	StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Types,
	StateMod_OperationalRight_Metadata_Source2AllowedType source2AllowedType,
	StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationTypes,
	StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationType,
	StateMod_OperationalRight_Metadata_DeliveryMethodType deliveryMethodType,
	StateMod_OperationalRight_Metadata_CarrierAllowedType carrierAllowedType,
	StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedTypes,
	StateMod_OperationalRight_Metadata_DiversionType [] diversionTypes,
	StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType transitAndConveyanceLossAllowedType,
	String comment )
{
	setRightTypeNumber ( rightTypeNumber );
	setRightTypeName ( rightName );
	setRuleTypeCategory ( ruleTypeCategory );
	setDestinationTypes ( destinationTypes );
	setSource1Types ( source1Types );
	setSource2Types ( source2Types );
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
Return the allowed destination types for the right.
*/
public StateMod_OperationalRight_Metadata_SourceOrDestinationType [] getDestinationTypes ()
{
	return __destinationTypes;
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_1 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Reservoir Release to an Instream Flow",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_1,
					source2Array_1,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_1,
					destinationLocationArray_1,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_1,
					diversionTypeArray_1,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_2 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Reservoir Release to a Diversion, Reservoir, or Carrier",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_2,
					source2Array_2,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_2,
					destinationLocationArray_2,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					associatedPlanAllowedArray_2,
					diversionTypeArray_2,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
					"" );
				break;
			case 3:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_3 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_3 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_3 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.CARRIER };
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_3 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_3 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_3 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Reservoir Release to a Direct Diversion or Reservoir by a Carrier",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_3,
					source2Array_3,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_3,
					destinationLocationArray_3,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					associatedPlanAllowedArray_3,
					diversionTypeArray_3,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
					"" );
				break;
			case 4:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_4 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_4 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_4 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION };
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_4 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.UPSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_4 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_4 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Reservoir Release to a Direct Diversion by Exchange with the River",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_4,
					source2Array_4,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_4,
					destinationLocationArray_4,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_4,
					diversionTypeArray_4,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_5 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Reservoir Storage by Exchange",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_5,
					source2Array_5,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_5,
					destinationLocationArray_5,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_5,
					diversionTypeArray_5,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_6 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Paper Exchange Between Reservoirs",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_6,
					source2Array_6,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_6,
					destinationLocationArray_6,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.BOOKOVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_6,
					diversionTypeArray_6,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_7 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Reservoir to a Carrier by Exchange",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_7,
					source2Array_7,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_7,
					destinationLocationArray_7,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					associatedPlanAllowedArray_7,
					diversionTypeArray_7,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_8 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Out of Priority Reservoir Bookover",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_8,
					source2Array_8,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_8,
					destinationLocationArray_8,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_8,
					diversionTypeArray_8,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_9 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Reservoir Release to Meet Target",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_9,
					source2Array_9,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_9,
					destinationLocationArray_9,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_9,
					diversionTypeArray_9,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_10 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION,
					StateMod_OperationalRight_Metadata_DiversionType.DEPLETION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"General Replacement Reservoir to a Diversion by a Direct Release or Exchange",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_10,
					source2Array_10,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_10,
					destinationLocationArray_10,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_10,
					diversionTypeArray_10,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
					"StateMod determines if the supply is the river or by exchange" );
				break;
			case 11:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_11 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION_RIGHT};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_11 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_11 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_11 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_11 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_11 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Carrier to a Ditch or Reservoir",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_11,
					source2Array_11,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_11,
					destinationLocationArray_11,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.CARRIER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					associatedPlanAllowedArray_11,
					diversionTypeArray_11,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_12 =
					{StateMod_OperationalRight_Metadata_DiversionType.NA};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Reoperate Water Rights",
					StateMod_OperationalRight_Metadata_RuleType.OTHER,
					source1Array_12,
					source2Array_12,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_12,
					destinationLocationArray_12,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_12,
					diversionTypeArray_12,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_13 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"La Plata Compact (Index Flow Contraint on Stream Gage)",
					StateMod_OperationalRight_Metadata_RuleType.COMPACT,
					source1Array_13,
					source2Array_13,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_13,
					destinationLocationArray_13,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_13,
					diversionTypeArray_13,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
					"" );
				break;
			case 14:
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source1Array_14 =
			    	{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] source2Array_14 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.NA};
				StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationArray_14 =
					{StateMod_OperationalRight_Metadata_SourceOrDestinationType.DIVERSION,
					StateMod_OperationalRight_Metadata_SourceOrDestinationType.RESERVOIR};
				StateMod_OperationalRight_Metadata_DestinationLocationType [] destinationLocationArray_14 =
					{StateMod_OperationalRight_Metadata_DestinationLocationType.DOWNSTREAM};
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_14 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_14 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Carrier Right with Constrained Demand",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_14,
					source2Array_14,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_14,
					destinationLocationArray_14,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					associatedPlanAllowedArray_14,
					diversionTypeArray_14,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_15 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Interruptible Supply",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_15,
					source2Array_15,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_15,
					destinationLocationArray_15,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_15,
					diversionTypeArray_15,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_16 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Direct Flow Storage",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_16,
					source2Array_16,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_16,
					destinationLocationArray_16,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_16,
					diversionTypeArray_16,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_17 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Rio Grande Compact (Rio Grande)",
					StateMod_OperationalRight_Metadata_RuleType.COMPACT,
					source1Array_17,
					source2Array_17,
					StateMod_OperationalRight_Metadata_Source2AllowedType.REQUIRED,
					destinationArray_17,
					destinationLocationArray_17,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_17,
					diversionTypeArray_17,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_18 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Rio Grande Compact (Conejos)",
					StateMod_OperationalRight_Metadata_RuleType.COMPACT,
					source1Array_18,
					source2Array_18,
					StateMod_OperationalRight_Metadata_Source2AllowedType.REQUIRED,
					destinationArray_18,
					destinationLocationArray_18,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_18,
					diversionTypeArray_18,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_19 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Split Channel Operation",
					StateMod_OperationalRight_Metadata_RuleType.OTHER,
					source1Array_19,
					source2Array_19,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_19,
					destinationLocationArray_19,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_19,
					diversionTypeArray_19,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_20 =
					{StateMod_OperationalRight_Metadata_DiversionType.NA};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"San Juan Reservoir RIP Operation",
					StateMod_OperationalRight_Metadata_RuleType.COMPACT,
					source1Array_20,
					source2Array_20,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_20,
					destinationLocationArray_20,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_20,
					diversionTypeArray_20,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_21 =
					{StateMod_OperationalRight_Metadata_DiversionType.NA};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Wells with Sprinkler Use",
					StateMod_OperationalRight_Metadata_RuleType.OTHER,
					source1Array_21,
					source2Array_21,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_21,
					destinationLocationArray_21,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_21,
					diversionTypeArray_21,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_22 =
					{StateMod_OperationalRight_Metadata_DiversionType.NA};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Soil Moisture Use",
					StateMod_OperationalRight_Metadata_RuleType.SOIL_MOISTURE,
					source1Array_22,
					source2Array_22,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_22,
					destinationLocationArray_22,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_22,
					diversionTypeArray_22,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_23 =
					{StateMod_OperationalRight_Metadata_DiversionType.NA};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Downstream Call",
					StateMod_OperationalRight_Metadata_RuleType.OTHER,
					source1Array_23,
					source2Array_23,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_23,
					destinationLocationArray_23,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_23,
					diversionTypeArray_23,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Direct Flow Exchange of a Pro-rata Water Right",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_24,
					source2Array_24,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_24,
					destinationLocationArray_24,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
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
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Direct Flow Bypass of a Pro-rata Water Right",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_25,
					source2Array_25,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_25,
					destinationLocationArray_25,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
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
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Not currently used - see type 48, 49",
					StateMod_OperationalRight_Metadata_RuleType.NA,
					source1Array_26,
					source2Array_26,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_26,
					destinationLocationArray_26,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.NA,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NA,
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
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Reservoir, Reuse, or Accounting Plan to a Diversion, Reservoir, or Carrier, with Reuse",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_PLAN_STRUCTURE,
					source1Array_27,
					source2Array_27,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_27,
					destinationLocationArray_27,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
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
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Reuse Plan to a Diversion or Reservoir by Exchange with or without Destination Reuse",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_PLAN_STRUCTURE,
					source1Array_28,
					source2Array_28,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_28,
					destinationLocationArray_28,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_29 =
					{StateMod_OperationalRight_Metadata_DiversionType.NA};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Reuse or Accounting Plan Spill",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_PLAN_STRUCTURE,
					source1Array_29,
					source2Array_29,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_29,
					destinationLocationArray_29,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_29,
					diversionTypeArray_29,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_30 =
					{StateMod_OperationalRight_Metadata_DiversionType.NA};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Rservoir Re-diversion",
					StateMod_OperationalRight_Metadata_RuleType.OTHER,
					source1Array_30,
					source2Array_30,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_30,
					destinationLocationArray_30,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_30,
					diversionTypeArray_30,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Carrier Right to a Ditch or Reservoir with Reuseable Return Flows",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_31,
					source2Array_31,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_31,
					destinationLocationArray_31,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.CARRIER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
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
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Reservoir and Plan to a Direct Flow or Reservoir or Carrier Direct with or without Destination Reuse",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_PLAN_STRUCTURE,
					source1Array_32,
					source2Array_32,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_32,
					destinationLocationArray_32,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
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
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Reservoir and Plan to a Direct Flow or Reservoir or Carrier by Exchange with or without Destination Reuse",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_33,
					source2Array_33,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_33,
					destinationLocationArray_33,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
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
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Reservoir to Reservoir Transfer (Bookover) with Reuse",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_RESERVOIR,
					source1Array_34,
					source2Array_34,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_34,
					destinationLocationArray_34,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.BOOKOVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					associatedPlanAllowedArray_34,
					diversionTypeArray_34,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Import to a Diversion, Reservoir, or Carrier with or without Reuse",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_PLAN_STRUCTURE,
					source1Array_35,
					source2Array_35,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_35,
					destinationLocationArray_35,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					associatedPlanAllowedArray_35,
					diversionTypeArray_35,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO};
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_36 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Seasonal (daily) On/Off Capability (e.g., Meadow Rights)",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_36,
					source2Array_36,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_36,
					destinationLocationArray_36,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					associatedPlanAllowedArray_36,
					diversionTypeArray_36,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Augmentation Well",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_GROUNDWATER,
					source1Array_37,
					source2Array_37,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_37,
					destinationLocationArray_37,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					associatedPlanAllowedArray_37,
					diversionTypeArray_37,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Out of Priority Diversion (addresses the upstream storage statute)",
					StateMod_OperationalRight_Metadata_RuleType.STORAGE_OPERATIONS,
					source1Array_38,
					source2Array_38,
					StateMod_OperationalRight_Metadata_Source2AllowedType.REQUIRED,
					destinationArray_38,
					destinationLocationArray_38,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					associatedPlanAllowedArray_38,
					diversionTypeArray_38,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_39 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Alternate Point Diversion",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_39,
					source2Array_39,
					StateMod_OperationalRight_Metadata_Source2AllowedType.REQUIRED,
					destinationArray_39,
					destinationLocationArray_39,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					associatedPlanAllowedArray_39,
					diversionTypeArray_39,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_40 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"South Platte Compact",
					StateMod_OperationalRight_Metadata_RuleType.COMPACT,
					source1Array_40,
					source2Array_40,
					StateMod_OperationalRight_Metadata_Source2AllowedType.REQUIRED,
					destinationArray_40,
					destinationLocationArray_40,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_40,
					diversionTypeArray_40,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_41 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Reservoir Storage with Special Limits",
					StateMod_OperationalRight_Metadata_RuleType.STORAGE_OPERATIONS,
					source1Array_41,
					source2Array_41,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_41,
					destinationLocationArray_41,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_41,
					diversionTypeArray_41,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_42 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Plan Demand Reset",
					StateMod_OperationalRight_Metadata_RuleType.STORAGE_OPERATIONS,
					source1Array_42,
					source2Array_42,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_42,
					destinationLocationArray_42,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_42,
					diversionTypeArray_42,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
				metaData = new StateMod_OperationalRight_Metadata( i,
					"In-Priority Well Depletion",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_PLAN_STRUCTURE,
					source1Array_43,
					source2Array_43,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_43,
					destinationLocationArray_43,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_43,
					diversionTypeArray_43,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Recharge Well",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_GROUNDWATER,
					source1Array_44,
					source2Array_44,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_44,
					destinationLocationArray_44,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					associatedPlanAllowedArray_44,
					diversionTypeArray_44,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Carrier with Transit Loss (allows multiple carriers and associated losses)",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_DIRECT_FLOW_RIGHT,
					source1Array_45,
					source2Array_45,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_45,
					destinationLocationArray_45,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_46 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Multiple Ownership Plans (distributes plan contents to multiple plans)",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_PLAN_STRUCTURE,
					source1Array_46,
					source2Array_46,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_46,
					destinationLocationArray_46,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					associatedPlanAllowedArray_46,
					diversionTypeArray_46,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.NO };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_47 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Administration Plan Limits",
					StateMod_OperationalRight_Metadata_RuleType.STORAGE_OPERATIONS,
					source1Array_47,
					source2Array_47,
					StateMod_OperationalRight_Metadata_Source2AllowedType.NA,
					destinationArray_47,
					destinationLocationArray_47,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.YES,
					associatedPlanAllowedArray_47,
					diversionTypeArray_47,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
				StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType [] associatedPlanAllowedArray_48 =
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.YES };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_48 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Plan or Reservoir Reuse to a Plan Direct",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_PLAN_STRUCTURE,
					source1Array_48,
					source2Array_48,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_48,
					destinationLocationArray_48,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_48,
					diversionTypeArray_48,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
					{StateMod_OperationalRight_Metadata_AssociatedPlanAllowedType.YES };
				StateMod_OperationalRight_Metadata_DiversionType [] diversionTypeArray_49 =
					{StateMod_OperationalRight_Metadata_DiversionType.DIVERSION};
				metaData = new StateMod_OperationalRight_Metadata( i,
					"Plan or Reservoir Reuse to a Plan by Exchange",
					StateMod_OperationalRight_Metadata_RuleType.SOURCE_PLAN_STRUCTURE,
					source1Array_49,
					source2Array_49,
					StateMod_OperationalRight_Metadata_Source2AllowedType.ALLOWED,
					destinationArray_49,
					destinationLocationArray_49,
					StateMod_OperationalRight_Metadata_DeliveryMethodType.RIVER,
					StateMod_OperationalRight_Metadata_CarrierAllowedType.NO,
					associatedPlanAllowedArray_49,
					diversionTypeArray_49,
					StateMod_OperationalRight_Metadata_TransitAndConveyanceLossAllowedType.NO,
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
*/
public static void readGlobalData ( String filename )
throws FileNotFoundException, IOException
{
	//List<StateMod_OperationalRight_Metadata> metadataList = readSpreadsheet ( filename );

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
Set the destination types (in agreement with StateMod documentation).
Make private because objects should be immutable.
@param destinationTypes types of destinations allowed for the right
*/
private void setDestinationTypes ( StateMod_OperationalRight_Metadata_SourceOrDestinationType [] destinationTypes )
{
	__destinationTypes = destinationTypes;
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

}