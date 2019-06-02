// StateCU_ComponentDataCheck - class for checking data for components

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
* File: StateCU_ComponentDataCheck.java
* Author: KAT
* Date: 2007-04-11
* Class for checking data on components.  Only StateCU components methods 
* should be added to this class.  This class extends from ComponentDataCheck in
* RTi_Common. 
*******************************************************************************
* Revisions 
*******************************************************************************
* 2007-04-11	Kurt Tometich	Initial version.
******************************************************************************/

package DWR.StateCU;

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
import RTi.Util.Message.Message;

public class StateCU_ComponentDataCheck extends DataSet_ComponentDataCheck
{
	CheckFile __check_file;		// Keeps track of all data checks
	int __type;					// StateCU Component type
	StateCU_DataSet __dataset;	// StateCU dataset object 
	private int __gen_problems = 0;

/**
Constructor that initializes the component type and CheckFile.
The check file can contain data checks from several components.
This class only checks data for one component.  Each time this
class is called to check a component the same check file should
be sent if the data is to be appended to that check file.  The
CheckFile object is passed around the data checks and data
that fails checks are added to it.
@param comp StateCU component type.
@param file CheckFile to append data checks to.
 */
public StateCU_ComponentDataCheck( int type, CheckFile file, 
StateCU_DataSet set )
{
	super( type, file );
	__check_file = file;
	__type = type;
	__dataset = set;
}

/**
Finds out which check method to call based on the input type.
@param props Property list for properties on data checks.
@return CheckFile A data check file object.
 */
public CheckFile checkComponentType( PropList props )
{
	// reset general data problem count
	__gen_problems = 0;
	// check for component data.  If none exists then do no checks.
	/*
	List<Object> data_vector = getComponentData( __type );
	if ( data_vector == null || data_vector.size() == 0 ) {
		return __check_file;
	}
	*/
	switch( __type) {
		case StateCU_DataSet.COMP_BLANEY_CRIDDLE:
			@SuppressWarnings("unchecked")
			List<StateCU_BlaneyCriddle> kbcList = (List<StateCU_BlaneyCriddle>)getComponentData(__type);
			if ( kbcList == null || kbcList.size() == 0 ) {
				return __check_file;
			}
			checkBlaneyCriddleData( props, kbcList );
			break;
		case StateCU_DataSet.COMP_CLIMATE_STATIONS:
			@SuppressWarnings("unchecked")
			List<StateCU_ClimateStation> climstaList = (List<StateCU_ClimateStation>)getComponentData(__type);
			if ( climstaList == null || climstaList.size() == 0 ) {
				return __check_file;
			}
			checkClimateStationData( props, climstaList );
			break;
		case StateCU_DataSet.COMP_CROP_CHARACTERISTICS:
			@SuppressWarnings("unchecked")
			List<StateCU_CropCharacteristics> ccList = (List<StateCU_CropCharacteristics>)getComponentData(__type);
			if ( ccList == null || ccList.size() == 0 ) {
				return __check_file;
			}
			checkCropCharacteristicsData( props, ccList );
			break;
		case StateCU_DataSet.COMP_CROP_PATTERN_TS_YEARLY:  
			@SuppressWarnings("unchecked")
			List<StateCU_CropPatternTS> cdsList = (List<StateCU_CropPatternTS>)getComponentData(__type);
			if ( cdsList == null || cdsList.size() == 0 ) {
				return __check_file;
			}
			checkCropPatternTSData( props, cdsList );
			break;
		case StateCU_DataSet.COMP_CU_LOCATIONS:
			@SuppressWarnings("unchecked")
			List<StateCU_Location> culocList = (List<StateCU_Location>)getComponentData(__type);
			if ( culocList == null || culocList.size() == 0 ) {
				return __check_file;
			}
			checkLocationData( props, culocList );
			break;
		case StateCU_DataSet.COMP_CU_LOCATION_CLIMATE_STATIONS:
			//@SuppressWarnings("unchecked")
			//List<StateCU_Location_ClimateStation> locclimsta = (List<StateCU_Location_ClimateStation>)getComponentData(__type);
			//if ( locclimsta == null || locclimsta.size() == 0 ) {
			//	return __check_file;
			//}
			//checkLocationClimateStationsData( props, locclimsta );
			break;
		case StateCU_DataSet.COMP_CU_LOCATION_COLLECTIONS:
			//@SuppressWarnings("unchecked")
			//List<StateCU_Location_Collection> cucolList = (List<StateCU_Location_Collection>)getComponentData(__type);
			//if ( cucolList == null || cucolList.size() == 0 ) {
			//	return __check_file;
			//}
			//checkLocationCollectionData( props, cucolList );
			break;
		case StateCU_DataSet.COMP_DELAY_TABLE_ASSIGNMENT_MONTHLY:
			@SuppressWarnings("unchecked")
			List<StateCU_DelayTableAssignment> dtaList = (List<StateCU_DelayTableAssignment>)getComponentData(__type);
			if ( dtaList == null || dtaList.size() == 0 ) {
				return __check_file;
			}
			checkDelayTableAssignmentData( props, dtaList );
			break;
		case StateCU_DataSet.COMP_IRRIGATION_PRACTICE_TS_YEARLY:
			@SuppressWarnings("unchecked")
			List<StateCU_IrrigationPracticeTS> ipyList = (List<StateCU_IrrigationPracticeTS>)getComponentData(__type);
			if ( ipyList == null || ipyList.size() == 0 ) {
				return __check_file;
			}
			checkIrrigationPracticeTSData( props, ipyList );
			break;
		default: ;	// do nothing
	}
	return __check_file;
}

/**
Performs general and specific data checks on Blaney-Criddle data. 
@param props A property list for specific properties.
@param kbcList list of data to check.
*/
private void checkBlaneyCriddleData(PropList props, List<StateCU_BlaneyCriddle> kbcList) 
{
	// Create elements for the checks and check file
	String[] header = StateCU_BlaneyCriddle.getDataHeader();
	List<StateCU_ComponentValidationProblem> data = new Vector<StateCU_ComponentValidationProblem>();
	String title = "Blaney Criddle";
	
	// Perform the general validation using the Data Table Model
	StateCU_Data_TableModel tm = new StateCU_BlaneyCriddle_TableModel( kbcList, false );
	List<String[]> checked = performDataValidation( tm, title );
	//String [] columnHeader = getDataTableModelColumnHeader( tm );
	String [] columnHeader = getColumnHeader( tm );
	
	// Do specific checks
	int size = 0;
	if ( kbcList != null ) {
		size = kbcList.size();
	}
	data = doSpecificDataChecks( kbcList, props );
	// Add the data and checks to the check file.
	// Provides basic header information for this data check table 
	String info = "The following " + title +  " (" + data.size() + 
	" out of " + size + ") have no .....";

	// Create data models for Check file
	CheckFile_DataModel dm = new CheckFile_DataModel(
			toCheckDataModelArray(data), header, title, info, data.size(), tm.getRowCount() );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
			checked, columnHeader, title + " Missing or Invalid Data",
			"", __gen_problems, tm.getRowCount() );
	__check_file.addData( dm, gen_dm );	
}

/**
Performs general and specific data checks on climate station data. 
@param props A property list for specific properties.
@param climstaList Vector of data to check.
*/
private void checkClimateStationData( PropList props, List<StateCU_ClimateStation> climstaList ) 
{
	// Create elements for the checks and check file
	String[] header = StateCU_ClimateStation.getDataHeader();
	List<StateCU_ComponentValidationProblem> data = new Vector<StateCU_ComponentValidationProblem>();
	String title = "Climate Station";
	
	// Perform the general validation using the Data Table Model
	StateCU_Data_TableModel tm;
	try {
		tm = new StateCU_ClimateStation_TableModel( climstaList, false );
	} 
	catch (Exception e) {
		Message.printWarning( 3, 
			"StateCU_ComponentDataCheck.checkClimateStationData", e );
		return;
	}
	List<String[]> checked = performDataValidation( tm, title );
	//String [] columnHeader = getDataTableModelColumnHeader( tm );
	String [] columnHeader = getColumnHeader( tm );
	
	// Do specific checks
	int size = 0;
	if ( climstaList != null ) {
		size = climstaList.size();
	}
	data = doSpecificDataChecks( climstaList, props );
	// Add the data and checks to the check file.
	// Provides basic header information for this data check table 
	String info = "The following " + title +  " (" + data.size() + 
	" out of " + size + ") have no .....";

	// Create data models for Check file
	CheckFile_DataModel dm = new CheckFile_DataModel(
			toCheckDataModelArray(data), header, title, info, data.size(), tm.getRowCount() );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
			checked, columnHeader, title + " Missing or Invalid Data",
			"", __gen_problems, tm.getRowCount() );
	__check_file.addData( dm, gen_dm );	
}

/**
Performs general and specific data checks on crop characteristics data. 
@param props A property list for specific properties.
@param cchList list of data to check.
*/
private void checkCropCharacteristicsData(PropList props, List<StateCU_CropCharacteristics> cchList) 
{
	// Create elements for the checks and check file
	String[] header = StateCU_CropCharacteristics.getDataHeader();
	List<StateCU_ComponentValidationProblem> data = new Vector<StateCU_ComponentValidationProblem>();
	String title = "Crop Characteristics";
	
	// Perform the general validation using the Data Table Model
	StateCU_Data_TableModel tm;
	try {
		tm = new StateCU_CropCharacteristics_TableModel( cchList, false, false );
	} catch (Exception e) {
		Message.printWarning( 3, 
			"StateCU_ComponentDataCheck.checkCropCharacteristicsData", e );
		return;
	}
	List<String []> checked = performDataValidation( tm, title );
	//String [] columnHeader = getDataTableModelColumnHeader( tm );
	String [] columnHeader = getColumnHeader( tm );
	
	// Do specific checks
	int size = 0;
	if ( cchList != null ) {
		size = cchList.size();
	}
	data = doSpecificDataChecks( cchList, props );
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
Performs general and specific data checks on crop pattern ts data. 
@param props A property list for specific properties.
@param dataList list of data to check.
*/
private void checkCropPatternTSData(PropList props, List<StateCU_CropPatternTS> dataList) 
{
	
}

/**
Performs general and specific data checks on delay table assignment data. 
@param props A property list for specific properties.
@param dtaList list of data to check.
*/
private void checkDelayTableAssignmentData(PropList props, List<StateCU_DelayTableAssignment> dtaList) 
{
	// Create elements for the checks and check file
	String[] header = StateCU_DelayTableAssignment.getDataHeader();
	List<StateCU_ComponentValidationProblem> data = new Vector<StateCU_ComponentValidationProblem>();
	String title = "Delay Table Assignment";
	
	// Perform the general validation using the Data Table Model
	StateCU_Data_TableModel tm = new StateCU_DelayTableAssignment_Data_TableModel( dtaList );
	List<String []> checked = performDataValidation( tm, title );
	//String [] columnHeader = getDataTableModelColumnHeader( tm );
	String [] columnHeader = getColumnHeader( tm );
	
	// Do specific checks
	int size = 0;
	if ( dtaList != null ) {
		size = dtaList.size();
	}
	data = doSpecificDataChecks( dtaList, props );
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
Performs general and specific data checks on irrigation practice ts data.
@param props A property list for specific properties.
@param data_vector list of data to check.
 */
private void checkIrrigationPracticeTSData(PropList props, List<StateCU_IrrigationPracticeTS> data_vector) 
{
	
}

/**
Performs general and specific data checks on location climate station data. 
@param props A property list for specific properties.
@param locclimstaList list of data to check.
*/
@SuppressWarnings("unused")
private void checkLocationClimateStationsData(PropList props, List<StateCU_Location> locclimstaList) 
{
	// Create elements for the checks and check file
	// TODO need to update this class if actually implemented
	String[] header = StateCU_CropCharacteristics.getDataHeader();
	List<StateCU_ComponentValidationProblem> data = new Vector<StateCU_ComponentValidationProblem>();
	String title = "Location Climate Station";
	
	// Perform the general validation using the Data Table Model
	StateCU_Data_TableModel tm = new StateCU_Location_ClimateStation_TableModel( locclimstaList );
	List<String []> checked = performDataValidation( tm, title );
	//String [] columnHeader = getDataTableModelColumnHeader( tm );
	String [] columnHeader = getColumnHeader( tm );
	
	// Do specific checks
	int size = 0;
	if ( locclimstaList != null ) {
		size = locclimstaList.size();
	}
	data = doSpecificDataChecks( locclimstaList, props );
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
Performs general and specific data checks on location collection data. 
@param props A property list for specific properties.
@param loccolList list of data to check.
*/
@SuppressWarnings("unused")
private void checkLocationCollectionData(PropList props, List<StateCU_Location> loccolList) 
{

}

/**
Performs general and specific data checks on location data. 
@param props A property list for specific properties.
@param culocList list of data to check.
*/
private void checkLocationData(PropList props, List<StateCU_Location> culocList) 
{
	//	 Create elements for the checks and check file
	String[] header = StateCU_Location.getDataHeader();
	List<StateCU_ComponentValidationProblem> data = new Vector<StateCU_ComponentValidationProblem>();
	String title = "CU Location";
	
	// Perform the general validation using the Data Table Model
	StateCU_Data_TableModel tm;
	try {
		tm = new StateCU_Location_TableModel( culocList, false, false );
	} catch (Exception e) {
		Message.printWarning( 3, 
			"StateCU_ComponentDataCheck.checkLocationData", e );
		return;
	}
	List<String []> checked = performDataValidation( tm, title );
	//String [] columnHeader = getDataTableModelColumnHeader( tm );
	String [] columnHeader = getColumnHeader( tm );
	
	// Do specific checks
	int size = 0;
	if ( culocList != null ) {
		size = culocList.size();
	}
	data = doSpecificDataChecks( culocList, props );
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
it is then the object is formatted and returned.
 * @param status
 * @param value
 * @return
 */
private String checkStatus( Status status, Object value )
{
	if ( status.getLevel() == Status.ERROR ) {
		return createHTMLErrorTooltip( status, value );
	}
	else { return value.toString(); }
}

/**
Performs specific data checks for a component.  The
intelligence and checks are stored in the component itself.
@param data List of data objects to check.
@return List of data that failed the data checks.
 */
private List<StateCU_ComponentValidationProblem> doSpecificDataChecks( List<? extends StateCU_ComponentValidator> data, PropList props )
{
	List<StateCU_ComponentValidationProblem> checks = new Vector<StateCU_ComponentValidationProblem>();
	if ( data == null ) {
		return checks;
	}
	// Check each component object by calling the
	// checkComponentData() method.  Each component
	// needs to implement this method and extend from
	// the StateMod_Component interface
	StateCU_ComponentValidator comp = null;
	for ( int i = 0; i < data.size(); i++ ) {
		comp = data.get( i );
		StateCU_ComponentValidation validation = comp.validateComponent(__dataset );
		if ( validation.size() > 0 ) {
			checks.addAll( validation.getAll() );
		}
	}
	return checks;
}

/**
Returns a formatted header from the table model
with HTML tooltips.
@param tm Table model for the component under validation.
@return List of column headers with tooltips.
 */
private String[] getColumnHeader( StateCU_Data_TableModel tm )
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
		return_header[i] = "%tooltip_start" + val_desc +
			"%tooltip_end" + header[i];
	}
	return return_header;
}

/**
Helper method to return the data list for the component
type.  This is maintained by the StateCU dataset.
@param type Component type to get data for. 
@return list of data for a specific component.
 */
private List<? extends StateCU_Data> getComponentData( int type )
{
	DataSetComponent comp = __dataset.getComponentForComponentType( type );
	@SuppressWarnings("unchecked")
	List<? extends StateCU_Data> data_vector = (List<? extends StateCU_Data>)comp.getData();
	
	return data_vector; 
}

/**
Uses the table model object to obtain the column headers.
@param tm StateMod_Data_TableModel Object.
@return List of column headers from the table model.
 */
private String[] getDataTableModelColumnHeader( StateCU_Data_TableModel tm )
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
Performs data validation based on the validators found in
the table model for the current component.
@param tm Interface implemented by each data table model.
@return List of data that has gone through data validation.
If any element fails any one of its validators the content
of that element is formatted to tag it as an error.
 */
private List<String []> performDataValidation( StateCU_Data_TableModel tm, 
String title )
{
	List<String []> data = new Vector<String []>();
	if ( tm == null ) {
		return data;
	}
	Status status = Status.OKAY;
	boolean row_had_problem = false;
	__gen_problems = 0;
	// Validate every row and column of data found in the table model
	for ( int i = 0; i < tm.getRowCount(); i++ ) {
		String [] row = new String[tm.getColumnCount()];
		row_had_problem = false;
		for ( int j = 0; j < tm.getColumnCount(); j++ ) {
			Validator[] vals = ( tm.getValidators( j ) );
			// If there are no validators for this column then
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
			for ( int k = 0; k < vals.length; k++ ) {
				status = vals[k].validate( tm.getValueAt( i, j ) );
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
private List<String []> toCheckDataModelArray( List<StateCU_ComponentValidationProblem> problems ) {
	List<String []> checkDataModelDataList = new ArrayList<String []>();
	for ( StateCU_ComponentValidationProblem problem : problems ) {
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