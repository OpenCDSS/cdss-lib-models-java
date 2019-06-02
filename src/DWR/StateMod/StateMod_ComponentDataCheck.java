// StateMod_ComponentDataCheck - base class for checking data on components

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

/******************************************************************************
* File: ComponentDataCheck.java
* Author: KAT
* Date: 2007-03-19
* Base class for checking data on components.  Only StateMod components methods 
* should be added to this class.  This class extends from ComponentDataCheck in
* RTi_Common. 
*******************************************************************************
* Revisions 
*******************************************************************************
* 2007-03-15	Kurt Tometich	Initial version.
******************************************************************************/

package DWR.StateMod;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import RTi.Util.IO.CheckFile;
import RTi.Util.IO.CheckFile_DataModel;
import RTi.Util.IO.DataSetComponent;
import RTi.Util.IO.DataSet_ComponentDataCheck;
import RTi.Util.IO.PropList;
import RTi.Util.IO.Status;
import RTi.Util.IO.Validator;

public class StateMod_ComponentDataCheck extends DataSet_ComponentDataCheck
{
	CheckFile __check_file;		// Keeps track of all data checks
	int __type;					// StateMod Component type
	StateMod_DataSet __dataset;	// StateMod dataset object 
	private int __gen_problems = 0;	// Keeps track of the # of
									// general data problems

/**
Constructor that initializes the component type and CheckFile.
The check file can contain data checks from several components.
Each time this class is called to check a component the same
check file should be sent if the data is to be appended to 
that check file.  The CheckFile object is passed around the
data checks and data that fails the checks are added to it.
@param comp StateMod component type.
@param file CheckFile to append data checks to.
 */
public StateMod_ComponentDataCheck( int type, CheckFile file, 
StateMod_DataSet set )
{
	super( type, file );
	__check_file = file;
	__type = type;
	__dataset = set;
}

/**
Finds out which check method to call based on the input type.  Acts
like a factory for StateMod data checks.
@param props Property list for properties on data checks.
@return CheckFile A data check file object.
 */
public CheckFile checkComponentType( PropList props )
{
	// reset general data problem count
	__gen_problems = 0;
	// find out which data type is being checked
	// and call the associated check method.
	switch( __type) {
		// check for component data.  If none exists then do no checks.
		case StateMod_DataSet.COMP_DELAY_TABLES_MONTHLY:
			@SuppressWarnings("unchecked")
			List<StateMod_DelayTable> dlymList = (List<StateMod_DelayTable>)getComponentData( __type );
			if ( dlymList == null || dlymList.size() == 0 ) {
				return __check_file;
			}
			checkDelayTableMonthlyData( props, dlymList );
			break;
		case StateMod_DataSet.COMP_DELAY_TABLES_DAILY:
			@SuppressWarnings("unchecked")
			List<StateMod_DelayTable> dlydList = (List<StateMod_DelayTable>)getComponentData( __type );
			if ( dlydList == null || dlydList.size() == 0 ) {
				return __check_file;
			}
			checkDelayTableDailyData( props, dlydList );
			break;
		case StateMod_DataSet.COMP_DIVERSION_STATIONS:  
			@SuppressWarnings("unchecked")
			List<StateMod_Diversion> ddsList = (List<StateMod_Diversion>)getComponentData( __type );
			if ( ddsList == null || ddsList.size() == 0 ) {
				return __check_file;
			}
			checkDiversionStationData( props, ddsList );
			break;
		case StateMod_DataSet.COMP_DIVERSION_RIGHTS:
			@SuppressWarnings("unchecked")
			List<StateMod_DiversionRight> ddrList = (List<StateMod_DiversionRight>)getComponentData( __type );
			if ( ddrList == null || ddrList.size() == 0 ) {
				return __check_file;
			}
			checkDiversionRightsData( props, ddrList );
			break;
		case StateMod_DataSet.COMP_INSTREAM_STATIONS:
			@SuppressWarnings("unchecked")
			List<StateMod_InstreamFlow> ifsList = (List<StateMod_InstreamFlow>)getComponentData( __type );
			if ( ifsList == null || ifsList.size() == 0 ) {
				return __check_file;
			}
			checkInstreamFlowStationData( props, ifsList );
			break;
		case StateMod_DataSet.COMP_INSTREAM_RIGHTS:
			@SuppressWarnings("unchecked")
			List<StateMod_InstreamFlowRight> ifrList = (List<StateMod_InstreamFlowRight>)getComponentData( __type );
			if ( ifrList == null || ifrList.size() == 0 ) {
				return __check_file;
			}
			checkInstreamFlowRightData( props, ifrList );
			break;
		case StateMod_DataSet.COMP_RESERVOIR_STATIONS:
			@SuppressWarnings("unchecked")
			List<StateMod_Reservoir> resList = (List<StateMod_Reservoir>)getComponentData( __type );
			if ( resList == null || resList.size() == 0 ) {
				return __check_file;
			}
			checkReservoirStationData( props, resList );
			break;
		case StateMod_DataSet.COMP_RESERVOIR_RIGHTS:
			@SuppressWarnings("unchecked")
			List<StateMod_ReservoirRight> rerList = (List<StateMod_ReservoirRight>)getComponentData( __type );
			if ( rerList == null || rerList.size() == 0 ) {
				return __check_file;
			}
			checkReservoirRightData( props, rerList );
			break;
		case StateMod_DataSet.COMP_RIVER_NETWORK:
			@SuppressWarnings("unchecked")
			List<StateMod_RiverNetworkNode> rinList = (List<StateMod_RiverNetworkNode>)getComponentData( __type );
			if ( rinList == null || rinList.size() == 0 ) {
				return __check_file;
			}
			checkRiverNetworkData( props, rinList );
			break;
		case StateMod_DataSet.COMP_STREAMESTIMATE_STATIONS:
			@SuppressWarnings("unchecked")
			List<StateMod_StreamEstimate> sesList = (List<StateMod_StreamEstimate>)getComponentData( __type );
			if ( sesList == null || sesList.size() == 0 ) {
				return __check_file;
			}
			checkStreamEstimateStationData( props, sesList );
			break;
		case StateMod_DataSet.COMP_STREAMESTIMATE_COEFFICIENTS:
			@SuppressWarnings("unchecked")
			List<StateMod_StreamEstimate_Coefficients> ribList = (List<StateMod_StreamEstimate_Coefficients>)getComponentData( __type );
			if ( ribList == null || ribList.size() == 0 ) {
				return __check_file;
			}
			checkStreamEstimateCoefficientData( props, ribList );
			break;
		case StateMod_DataSet.COMP_STREAMGAGE_STATIONS:
			@SuppressWarnings("unchecked")
			List<StateMod_StreamGage> risList = (List<StateMod_StreamGage>)getComponentData( __type );
			if ( risList == null || risList.size() == 0 ) {
				return __check_file;
			}
			checkStreamGageStationData( props, risList );
			break;
		case StateMod_DataSet.COMP_WELL_STATIONS:  
			@SuppressWarnings("unchecked")
			List<StateMod_Well> wesList = (List<StateMod_Well>)getComponentData( __type );
			if ( wesList == null || wesList.size() == 0 ) {
				return __check_file;
			}
			checkWellStationData( props, wesList );
			break;
		case StateMod_DataSet.COMP_WELL_RIGHTS:
			@SuppressWarnings("unchecked")
			List<StateMod_WellRight> werList = (List<StateMod_WellRight>)getComponentData( __type );
			if ( werList == null || werList.size() == 0 ) {
				return __check_file;
			}
			checkWellStationRights( props, werList );
			break;
		default: ;
	}
	return __check_file;
}

/**
Performs data checks on delay table daily data. 
@param props A property list for specific properties
on checking this data.
@param data_vector Vector of data to check.
 */
private void checkDelayTableDailyData ( PropList props, List<StateMod_DelayTable> data_vector )
{
	
}

/**
Performs data checks on delay table monthly data. 
@param props A property list for specific properties
on checking this data.
@param data_vector Vector of data to check.
 */
private void checkDelayTableMonthlyData ( PropList props, List<StateMod_DelayTable> data_vector )
{
	
}

/**
Performs data checks on diversion rights data. 
@param props A property list for specific properties
on checking this data.
@param ddrList list of data to check.
 */
private void checkDiversionRightsData( PropList props, List<StateMod_DiversionRight> ddrList )
{
	// create elements for the checks and check file
	String[] header = StateMod_DiversionRight.getDataCheckTableHeader();
	List<StateMod_ComponentValidationProblem> data = new Vector<StateMod_ComponentValidationProblem>();
	String title = "Diversion Rights";
	
	// Perform the general validation using the Data Table Model
	StateMod_Data_TableModel tm = new StateMod_DiversionRight_Data_TableModel( ddrList, false );
	List<String []> checked = performDataValidation( tm, title );
	//String [] columnHeader = getDataTableModelColumnHeader( tm );
	String [] columnHeader = getColumnHeader( tm );
	
	//	 do specific checks
	int size = 0;
	if ( ddrList != null ) {
		size = ddrList.size();
	}
	data = doSpecificDataChecks( ddrList, props );
	// add the data and checks to the check file	
	// provides basic header information for this data check table 
	String info = "The following diversion rights (" + data.size() + 
		" out of " + size +
		") have no .....";
	
	// create data models for Check file
	CheckFile_DataModel dm = new CheckFile_DataModel(
		toCheckDataModelArray(data), header, title, info, data.size(), size );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
		checked, columnHeader, title + " Missing or Invalid Data",
		"", __gen_problems, size );
	__check_file.addData( dm, gen_dm );	
}

/**
Performs data checks on diversion station data. 
@param props A property list for specific properties
on checking this data.
@param des_vector Vector of data to check.
 */
private void checkDiversionStationData( PropList props, List<StateMod_Diversion> des_vector )
{
	// create elements for the checks and check file
	String[] header = StateMod_Diversion.getDataHeader();
	List<StateMod_ComponentValidationProblem> data = new Vector<StateMod_ComponentValidationProblem>();
	String title = "Diversion Station";
	
	// perform the general validation using the Data Table Model
	StateMod_Data_TableModel tm = new StateMod_Diversion_Data_TableModel(
			des_vector, false );
	List<String []> checked = performDataValidation( tm, title );
	//String [] columnHeader = getDataTableModelColumnHeader( tm );
	String [] columnHeader = getColumnHeader( tm );
	
	//	 do specific checks
	int size = 0;
	if ( des_vector != null ) {
		size = des_vector.size();
	}
	data = doSpecificDataChecks( des_vector, props );
	// add the data and checks to the check file
	// provides basic header information for this data check table 
	String info = "The following diversion stations (" + data.size() + 
	" out of " + size +
	") have no .....";

	// create data models for Check file
	CheckFile_DataModel dm = new CheckFile_DataModel(
			toCheckDataModelArray(data), header, title, info, data.size(), size );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
			checked, columnHeader, title + " Missing or Invalid Data",
			"", __gen_problems, size );
	__check_file.addData( dm, gen_dm );	
}

/**
Performs data checks on instream flow right data. 
@param props A property list for specific properties
on checking this data.
@param data_vector Vector of data to check.
 */
private void checkInstreamFlowRightData ( 
PropList props, List<StateMod_InstreamFlowRight> data_vector )
{
	//	 Create elements for the checks and check file
	String[] header = StateMod_InstreamFlowRight.getDataHeader();
	List<StateMod_ComponentValidationProblem> data = new Vector<StateMod_ComponentValidationProblem>();
	String title = "Instream Flow Right";
	
	// Perform the general validation using the Data Table Model
	StateMod_Data_TableModel tm = 
		new StateMod_InstreamFlowRight_Data_TableModel( data_vector, false );
	List<String []> checked = performDataValidation( tm, title );
	//String [] columnHeader = getDataTableModelColumnHeader( tm );
	String [] columnHeader = getColumnHeader( tm );
	
	// Do specific checks
	int size = 0;
	if ( data_vector != null ) {
		size = data_vector.size();
	}
	data = doSpecificDataChecks( data_vector, props );
	// Add the data and checks to the check file.
	// Provides basic header information for this data check table 
	String info = "The following " + title +  " (" + data.size() + 
	" out of " + size + ") have no .....";

	// Create data models for Check file
	CheckFile_DataModel dm = new CheckFile_DataModel(
			toCheckDataModelArray(data), header, title, info, data.size(), size );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
			checked, columnHeader, title + " Missing or Invalid Data",
			"", __gen_problems, size );
	__check_file.addData( dm, gen_dm );	
}

/**
Performs data checks on instream flow station data. 
@param props A property list for specific properties
on checking this data.
@param data_vector Vector of data to check.
 */
private void checkInstreamFlowStationData ( 
PropList props, List<StateMod_InstreamFlow> data_vector )
{
	//	 Create elements for the checks and check file
	String[] header = StateMod_InstreamFlow.getDataHeader();
	List<StateMod_ComponentValidationProblem> data = new Vector<StateMod_ComponentValidationProblem>();
	String title = "Instream Flow Station";
	
	// Perform the general validation using the Data Table Model
	StateMod_Data_TableModel tm = 
		new StateMod_InstreamFlow_Data_TableModel( data_vector, false );
	List<String []> checked = performDataValidation( tm, title );
	//String [] columnHeader = getDataTableModelColumnHeader( tm );
	String [] columnHeader = getColumnHeader( tm );
	
	// Do specific checks
	int size = 0;
	if ( data_vector != null ) {
		size = data_vector.size();
	}
	data = doSpecificDataChecks( data_vector, props );
	// Add the data and checks to the check file.
	// Provides basic header information for this data check table 
	String info = "The following " + title +  " (" + data.size() + 
	" out of " + size + ") have no .....";

	// Create data models for Check file
	CheckFile_DataModel dm = new CheckFile_DataModel(
			toCheckDataModelArray(data), header, title, info, data.size(), size );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
			checked, columnHeader, title + " Missing or Invalid Data",
			"", __gen_problems, size );
	__check_file.addData( dm, gen_dm );	
}

/**
Performs data checks on reservoir right data. 
@param props A property list for specific properties
on checking this data.
@param data_vector Vector of data to check.
 */
private void checkReservoirRightData ( PropList props, List<StateMod_ReservoirRight> data_vector )
{
	//	 Create elements for the checks and check file
	String[] header = StateMod_ReservoirRight.getDataHeader();
	List<StateMod_ComponentValidationProblem> data = new Vector<StateMod_ComponentValidationProblem>();
	String title = "Reservoir Right";
	
	// Perform the general validation using the Data Table Model
	StateMod_Data_TableModel tm = 
		new StateMod_ReservoirRight_Data_TableModel( data_vector, false );
	List<String []> checked = performDataValidation( tm, title );
	//String [] columnHeader = getDataTableModelColumnHeader( tm );
	String [] columnHeader = getColumnHeader( tm );
	
	// Do specific checks
	int size = 0;
	if ( data_vector != null ) {
		size = data_vector.size();
	}
	data = doSpecificDataChecks( data_vector, props );
	// Add the data and checks to the check file.
	// Provides basic header information for this data check table 
	String info = "The following " + title +  " (" + data.size() + 
	" out of " + size + ") have no .....";

	// Create data models for Check file
	CheckFile_DataModel dm = new CheckFile_DataModel(
			toCheckDataModelArray(data), header, title, info, data.size(), size );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
			checked, columnHeader, title + " Missing or Invalid Data",
			"", __gen_problems, size );
	__check_file.addData( dm, gen_dm );	
}

/**
Performs data checks on reservoir station data. 
@param props A property list for specific properties
on checking this data.
@param data_vector Vector of data to check.
 */
private void checkReservoirStationData ( PropList props, List<StateMod_Reservoir> data_vector )
{
	//	 Create elements for the checks and check file
	String[] header = StateMod_Reservoir.getDataHeader();
	List<StateMod_ComponentValidationProblem> data = new Vector<StateMod_ComponentValidationProblem>();
	String title = "Reservoir Station";
	
	// Perform the general validation using the Data Table Model
	StateMod_Data_TableModel tm = 
		new StateMod_Reservoir_Data_TableModel( data_vector, false );
	List<String []> checked = performDataValidation( tm, title );
	//String [] columnHeader = getDataTableModelColumnHeader( tm );
	String [] columnHeader = getColumnHeader( tm );
	
	// Do specific checks
	int size = 0;
	if ( data_vector != null ) {
		size = data_vector.size();
	}
	data = doSpecificDataChecks( data_vector, props );
	// Add the data and checks to the check file.
	// Provides basic header information for this data check table 
	String info = "The following " + title +  " (" + data.size() + 
	" out of " + size + ") have no .....";

	// Create data models for Check file
	CheckFile_DataModel dm = new CheckFile_DataModel(
			toCheckDataModelArray(data), header, title, info, data.size(), size );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
			checked, columnHeader, title + " Missing or Invalid Data",
			"", __gen_problems, size );
	__check_file.addData( dm, gen_dm );	
}

/**
Performs general and specific data checks on river network data. 
@param props A property list for specific properties
@param data_vector Vector of data to check.
on checking this data.
 */
private void checkRiverNetworkData ( PropList props, List<StateMod_RiverNetworkNode> data_vector )
{
	//	 Create elements for the checks and check file
	String[] header = StateMod_RiverNetworkNode.getDataHeader();
	List<StateMod_ComponentValidationProblem> data = new Vector<StateMod_ComponentValidationProblem>();
	String title = "River Network Node";
	
	// Perform the general validation using the Data Table Model
	StateMod_Data_TableModel tm = 
		new StateMod_RiverNetworkNode_Data_TableModel( data_vector );
	List<String []> checked = performDataValidation( tm, title );
	//String [] columnHeader = getDataTableModelColumnHeader( tm );
	String [] columnHeader = getColumnHeader( tm );
	
	// Do specific checks
	int size = 0;
	if ( data_vector != null ) {
		size = data_vector.size();
	}
	data = doSpecificDataChecks( data_vector, props );
	// Add the data and checks to the check file.
	// Provides basic header information for this data check table 
	String info = "The following " + title +  " (" + data.size() + 
	" out of " + size + ") have no .....";

	// Create data models for Check file
	CheckFile_DataModel dm = new CheckFile_DataModel(
			toCheckDataModelArray(data), header, title, info, data.size(), size );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
			checked, columnHeader, title + " Missing or Invalid Data",
			"", __gen_problems, size );
	__check_file.addData( dm, gen_dm );	
}

/**
Checks whether the status object is in the ERROR state and if
it is then the object is formatted and returned.  The object needs
to be formatted if there is an error to provide the HTML check file
to format the text and font correctly.
@param status Status of the current validation.
@param value Value currently being validated.
@return Status string.
 */
private String checkStatus( Status status, Object value )
{
	if ( status.getLevel() == Status.ERROR ) {
		return createHTMLErrorTooltip( status, value );
	}
	else {
		return value.toString();
	}
}

/**
Performs general and specific data checks on
stream estimate coefficient data. 
@param props A property list for specific properties
@param data_vector Vector of data to check.
on checking this data.
 */
private void checkStreamEstimateCoefficientData ( 
PropList props, List<StateMod_StreamEstimate_Coefficients> data_vector )
{
	//	 Create elements for the checks and check file
	String[] header = StateMod_StreamEstimate_Coefficients.getDataHeader();
	List<StateMod_ComponentValidationProblem> data = new Vector<StateMod_ComponentValidationProblem>();
	String title = "Stream Estimate Coefficients";
	
	// Perform the general validation using the Data Table Model
	StateMod_Data_TableModel tm = 
		new StateMod_StreamEstimateCoefficients_Data_TableModel( 
		data_vector, false );
	List<String []> checked = performDataValidation( tm, title );
	//String [] columnHeader = getDataTableModelColumnHeader( tm );
	String [] columnHeader = getColumnHeader( tm );
	
	// Do specific checks
	int size = 0;
	if ( data_vector != null ) {
		size = data_vector.size();
	}
	data = doSpecificDataChecks( data_vector, props );
	// Add the data and checks to the check file.
	// Provides basic header information for this data check table 
	String info = "The following " + title +  " (" + data.size() + 
	" out of " + size + ") have no .....";

	// Create data models for Check file
	CheckFile_DataModel dm = new CheckFile_DataModel(
			toCheckDataModelArray(data), header, title, info, data.size(), size );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
			checked, columnHeader, title + " Missing or Invalid Data",
			"", __gen_problems, size );
	__check_file.addData( dm, gen_dm );	
}

/**
Performs general and specific data checks on stream estimate station data. 
@param props A property list for specific properties
@param data_vector Vector of data to check.
on checking this data.
 */
private void checkStreamEstimateStationData ( 
PropList props, List<StateMod_StreamEstimate> data_vector )
{
	// Create elements for the checks and check file
	String[] header = StateMod_StreamEstimate.getDataHeader();
	List<StateMod_ComponentValidationProblem> data = new Vector<StateMod_ComponentValidationProblem>();
	String title = "Stream Estimate Station";
	
	// Perform the general validation using the Data Table Model
	StateMod_Data_TableModel tm = 
		new StateMod_StreamEstimate_Data_TableModel( data_vector, false );
	List<String []> checked = performDataValidation( tm, title );
	//String [] columnHeader = getDataTableModelColumnHeader( tm );
	String [] columnHeader = getColumnHeader( tm );
	
	// Do specific checks
	int size = 0;
	if ( data_vector != null ) {
		size = data_vector.size();
	}
	data = doSpecificDataChecks( data_vector, props );
	// Add the data and checks to the check file.
	// Provides basic header information for this data check table 
	String info = "The following " + title +  " (" + data.size() + 
	" out of " + size + ") have no .....";

	// Create data models for Check file
	CheckFile_DataModel dm = new CheckFile_DataModel(
			toCheckDataModelArray(data), header, title, info, data.size(), size );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
			checked, columnHeader, title + " Missing or Invalid Data",
			"", __gen_problems, size );
	__check_file.addData( dm, gen_dm );	
}

/**
Performs general and specific data checks on stream gage station data. 
@param props A property list for specific properties
@param data_vector Vector of data to check.
on checking this data.
 */
private void checkStreamGageStationData( PropList props, List<StateMod_StreamGage> data_vector )
{
	// Create elements for the checks and check file
	String[] header = StateMod_StreamGage.getDataHeader();
	List<StateMod_ComponentValidationProblem> data = new Vector<StateMod_ComponentValidationProblem>();
	String title = "Stream Gage Station";
	
	// Perform the general validation using the Data Table Model
	StateMod_Data_TableModel tm = 
		new StateMod_StreamGage_Data_TableModel( data_vector, false );
	List<String []> checked = performDataValidation( tm, title );
	//String [] columnHeader = getDataTableModelColumnHeader( tm );
	String [] columnHeader = getColumnHeader( tm );
	
	// Do specific checks
	int size = 0;
	if ( data_vector != null ) {
		size = data_vector.size();
	}
	data = doSpecificDataChecks( data_vector, props );
	// Add the data and checks to the check file.
	// Provides basic header information for this data check table 
	String info = "The following " + title +  " (" + data.size() + 
	" out of " + size + ") have no .....";

	// Create data models for Check file
	CheckFile_DataModel dm = new CheckFile_DataModel(
			toCheckDataModelArray(data), header, title, info, data.size(), size );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
			checked, columnHeader, title + " Missing or Invalid Data",
			"", __gen_problems, size );
	__check_file.addData( dm, gen_dm );	
}

/**
Performs general and specific data checks on well station data. 
@param props A property list for specific properties
@param wes_Vector Vector of data to check.
on checking this data.
 */
private void checkWellStationData( PropList props, List<StateMod_Well> wes_Vector )
{
	// create elements for the checks and check file
	String[] header = StateMod_Well.getDataHeader();
	String title = "Well Station";
	
	// first do the general data validation
	// using this components data table model
	StateMod_Data_TableModel tm = new StateMod_Well_Data_TableModel(
		wes_Vector, false );
	List<String []> checked = performDataValidation( tm, title );
	//String [] columnHeader = getDataTableModelColumnHeader( tm );
	String [] columnHeader = getColumnHeader( tm );
	
	// do specific checks
	int size = 0;
	if ( wes_Vector != null ) {
		size = wes_Vector.size();
	}
	List<StateMod_ComponentValidationProblem> data = new Vector<StateMod_ComponentValidationProblem>();
	data = doSpecificDataChecks( wes_Vector, props );
	// add the data and checks to the check file
	// provides basic header information for this data check table 
	String info = "The following well stations (" + data.size() + 
	" out of " + size +
	") have no irrigated parcels served by wells.\n" +
	"Data may be OK if the station is an M&I or has no wells.\n" +
	"Parcel count and area in the following table are available " +
	"only if well stations are read from HydroBase.\n";
	
	// create data models for Check file
	CheckFile_DataModel dm = new CheckFile_DataModel(
			toCheckDataModelArray(data), header, title, info, data.size(), size );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
			checked, columnHeader, title + " Missing or Invalid Data",
			"", __gen_problems, size );
		__check_file.addData( dm, gen_dm );	
	
	// Check to make sure the sum of well rights equals the well station
	// capacity..
	checkWellRights_CapacityData ();
}

/**
Helper method to check well rights component data.  The following are checked:
<ol>
<li>	Well stations without at least one right are listed.  This requires that
	the dataset include well stations.</li>
<li>	Well rights with yield <= 0.0</li>
<li>	Well rights summary for a station is not equal to the well capacity.
	This requires that the dataset include well stations.<li>
</ol>
*/
private void checkWellStationRights ( PropList props, List<StateMod_WellRight> wer_Vector)
{		
	int size = 0;
	// create elements for the checks and check file
	String[] header = StateMod_WellRight.getDataHeader();
	List<StateMod_ComponentValidationProblem> data = new Vector<StateMod_ComponentValidationProblem>();
	String title = "Well Rights";
	
	// Do the general data validation
	// using this components data table model
	StateMod_Data_TableModel tm = new StateMod_WellRight_Data_TableModel(
		wer_Vector, false );
	List<String []> checked = performDataValidation( tm, title );
	//String [] columnHeader = getDataTableModelColumnHeader( tm );
	String [] columnHeader = getColumnHeader( tm );
	
	// check Well Station data
	PropList props_rights = new PropList( "Well Rights" );
	props_rights.add("checkRights=true");
	@SuppressWarnings("unchecked")
	List<StateMod_Well> wes_Vector = (List<StateMod_Well>)getComponentData(StateMod_DataSet.COMP_WELL_STATIONS );
	if ( wes_Vector != null && wes_Vector.size() > 0 ) {
		checkWellStationData( props_rights, wes_Vector );
	}
	props_rights = null;	// cleanup

	// Check to make sure the sum of well rights equals the well station
	// capacity...
	checkWellRights_CapacityData( );

	// Since well rights are determined from parcel data, print a list of
	// well rights that do not have associated yield (decree)...
	size = 0;
	if ( wer_Vector != null ) {
		size = wer_Vector.size();
	}
	// Do data checks listed in the StateMod_WellRight class
	// Remove all previous checks from StateMod_Well
	data.clear();
	data = doSpecificDataChecks( wer_Vector, props );	
	// provides basic header information for this data check table 
	String info = "The following well rights (" + data.size() +
	" out of " + size +
	") have no decree (checked to StateMod file .XX precision).\n" +
	"Well yield data may not be available.";

	// create data models for Check file
	CheckFile_DataModel dm = new CheckFile_DataModel(
			toCheckDataModelArray(data), header, title, info, data.size(), size );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
			checked, columnHeader, title + " Missing or Invalid Data",
			"", __gen_problems, size );
	__check_file.addData(dm, gen_dm );	
}

/**
Helper method to check that well rights sum to the well station capacity.  This
is called by the well right and well station checks.  The check is performed
by formatting the capacity and decree sum to .NN precision.
*/
private void checkWellRights_CapacityData()
{	
	// get component data
	@SuppressWarnings("unchecked")
	List<StateMod_Well> wes_Vector = (List<StateMod_Well>)getComponentData(StateMod_DataSet.COMP_WELL_STATIONS);
	@SuppressWarnings("unchecked")
	List<StateMod_WellRight> wer_Vector = (List<StateMod_WellRight>)getComponentData(StateMod_DataSet.COMP_WELL_RIGHTS);
	if ( wes_Vector == null || wer_Vector == null ) {
		return;
	}
	List<String []> data = new Vector<String []>();
	int size = 0;
	
	// initialize some info for the check file
	String[] header = StateMod_Well.getCapacityHeader();
	String title = "Well Station Capacity";
	
	// check that there data available
	StateMod_Well wes_i = null;
	// loop through the vector of data and perform specific
	// data checks
	size = wes_Vector.size();
	for ( int i = 0; i < size; i++ ) {
		wes_i = wes_Vector.get(i);
		if ( wes_i == null ) {
			continue;
		}
		String [] checks = 
		wes_i.checkComponentData_Capacity( wer_Vector, i );
		// add the data to the data vector
		if ( checks != null && checks.length > 0 ) {
			data.add(checks);
		}
	}
	// add the data and checks to the check file
	if ( data.size() > 0 ) {
		// provides basic header information for this data table 
		String info = "The following well stations (" + data.size() +
			" out of " + size +
			") have capacity different\nfrom the sum of well rights for " +
			"the station.\n" +
			"Parcel count and area in the following table are available " +
			"only if well rights are read from HydroBase.\n";
		
		// create data models for Check file
		CheckFile_DataModel dm = new CheckFile_DataModel(
			data, header, title, info, data.size(), size );
		__check_file.addData( dm, null );
	}
}

/**
Performs specific data checks for a component.  The
intelligence and checks are stored in the component itself.
@param data List of data objects to check.
@return List of data that failed the data checks.
 */
private List<StateMod_ComponentValidationProblem> doSpecificDataChecks( List<? extends StateMod_ComponentValidator> data, PropList props )
{
	List<StateMod_ComponentValidationProblem> checks = new Vector<StateMod_ComponentValidationProblem>();
	if ( data == null ) {
		return checks;
	}
	// Check each component object by calling the
	// checkComponentData() method.  Each component
	// needs to implement this method and extend from
	// the StateMod_Component interface.
	StateMod_ComponentValidator comp = null;
	for ( int i = 0; i < data.size(); i++ ) {
		comp = data.get( i );
		StateMod_ComponentValidation validation = comp.validateComponent( __dataset );
		if ( validation.size() > 0 ) {
			checks.addAll( validation.getAll() );
		}
	}
	return checks;
}

/**
Returns a formatted header from the component's table model
with tooltips needed for the check file.  There is no HTML
here, instead keyword are injected as Strings that the check
file parser will convert into HTML tooltips.
@param tm Table model for the component under validation.
@return List of column headers with tooltips.
 */
private String[] getColumnHeader( StateMod_Data_TableModel tm )
{
	String [] header = getDataTableModelColumnHeader( tm );
	String [] return_header = new String[header.length];
	// format the header to include HTML tooltips based on
	// the validation being performed on a column of data
	for ( int i = 0; i < header.length; i++ ) {
		Validator[] vals = ( tm.getValidators( i ) );
		String val_desc = "Data validators for this column:\n"; 
		for ( int j = 0; j < vals.length; j++ ) {
			int num = j + 1;
			val_desc =  val_desc + num +  ". " + vals[j].toString();
		}
		// write the final header string with tooltip
		// the %tooltip_start and %tooltip_end are special/keyword
		// Strings that are recognized when the HTML file is rendered.
		// These keywords will be converted into title tags needed to
		// produce an HTML tooltip.  See the checkText() method in 
		// HTMLWriter.java class in RTi_Common for conversions.
		return_header[i] = "%tooltip_start" + val_desc +
			"%tooltip_end" + header[i];
	}
	return return_header;
}

/**
Helper method to return the data vector for the component
type.  This is maintained by the StateMod dataset.
@param type Component type to get data for. 
@return Vector of data for a specific component.
 */
private List<? extends StateMod_Data> getComponentData( int type )
{
	DataSetComponent comp = __dataset.getComponentForComponentType( type );
	@SuppressWarnings("unchecked")
	List<? extends StateMod_Data> data_vector = (List<? extends StateMod_Data>)comp.getData();
	
	return data_vector; 
}

/**
Uses the table model object to obtain the column
headers.
@param tm StateMod_Data_TableModel Object.
@return List of column headers from the table model.
 */
private String[] getDataTableModelColumnHeader( StateMod_Data_TableModel tm )
{
	if ( tm == null ) {
		return new String[] {};
	}
	String [] header = new String[tm.getColumnCount()];
	for ( int i = 0; i < tm.getColumnCount(); i++ ) {
		header[i] = tm.getColumnName( i );
	}
	return header;
}

/**
Performs basic data validation based on the validators found in
the table model for the current component.
@param tm Interface implemented by each data table model.
@return List of data that has gone through data validation.
If any element fails any one of its validators the content
of that element is formatted to tag it as an error.
 */
private List<String[]> performDataValidation( StateMod_Data_TableModel tm, String title )
{
	List<String[]> data = new Vector<String[]>();
	if ( tm == null ) {
		return data;
	}
	Status status = Status.OKAY;
	boolean row_had_problem = false;
	__gen_problems = 0;
	// Go through rows of data objects
	for ( int i = 0; i < tm.getRowCount(); i++ ) {
		String [] row = new String[tm.getColumnCount()];
		row_had_problem = false;
		// Get the data column (this contains the data that needs to
		// be checked).
		for ( int j = 0; j < tm.getColumnCount(); j++ ) {
			Validator[] vals = ( tm.getValidators( j ) );
			// if there are no validators for this column then
			// just add the data value.  If the value is blank
			// then add a space so the check file is able to show
			// the column.
			if ( vals.length == 0 ) {
				String value = tm.getValueAt(i, j ).toString(); 
				if ( value.length() == 0 ) {
					value = " ";
				}
				row[j] = value;
			}	
			// Run all validators found in the table model
			for ( int k = 0; k < vals.length; k++ ) {
				// Run all the validators to get a status on the data
				status = vals[k].validate( tm.getValueAt( i, j ) );
				// Format the status as HTML
				row[j] = checkStatus( status, tm.getValueAt( i, j ) );
				// if the current validator fails then don't
				// check finer grained validators since there is no need.
				// Log the error as a runtime message in the check file
				if ( status.getLevel() != Status.OK ) {
					row_had_problem = true;
					break;
				}
			}
		}
		// Add the data regardless if it fails or not.
		data.add( row );
		if ( row_had_problem ) {
			__gen_problems++;
		}
	}
	return data;
}

//TODO smalers 2019-05-29 need to fix this to actually translate the data
/**
 * Convert a ComponentValidationProblem to String[] needed for general data check report formatting.
 * Each ComponentValidationProblem has data for a specific column.
 */
private List<String []> toCheckDataModelArray( List<StateMod_ComponentValidationProblem> problems ) {
	List<String []> checkDataModelDataList = new ArrayList<String []>();
	for ( StateMod_ComponentValidationProblem problem : problems ) {
		if ( problem == null ) {
			// code to prevent compiler warning not being used - fix when fill out this logic
		}
		String [] checkData = new String[3];
		checkData[0] = "";
		checkData[1] = "";
		checkData[2] = "";
	}
	return checkDataModelDataList;
}

}