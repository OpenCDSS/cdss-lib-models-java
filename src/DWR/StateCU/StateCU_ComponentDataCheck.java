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
	List data_vector = getComponentData( __type );
	if ( data_vector == null || data_vector.size() == 0 ) {
		return __check_file;
	}
	switch( __type) {
		case StateCU_DataSet.COMP_BLANEY_CRIDDLE:
			checkBlaneyCriddleData( props, data_vector );
		break;
		case StateCU_DataSet.COMP_CLIMATE_STATIONS:
			checkClimateStationData( props, data_vector );
		break;
		case StateCU_DataSet.COMP_CROP_CHARACTERISTICS:
			checkCropCharacteristicsData( props, data_vector );
		break;
		case StateCU_DataSet.COMP_CROP_PATTERN_TS_YEARLY:  
			checkCropPatternTSData( props, data_vector );
		break;
		case StateCU_DataSet.COMP_CU_LOCATIONS:
			checkLocationData( props, data_vector );
		break;
		case StateCU_DataSet.COMP_CU_LOCATION_CLIMATE_STATIONS:
			checkLocationClimateStationsData( props, data_vector );
		break;
		case StateCU_DataSet.COMP_CU_LOCATION_COLLECTIONS:
			checkLocationCollectionData( props, data_vector );
		break;
		case StateCU_DataSet.COMP_DELAY_TABLE_ASSIGNMENT_MONTHLY:
			checkDelayTableAssignmentData( props, data_vector );
		break;
		case StateCU_DataSet.COMP_IRRIGATION_PRACTICE_TS_YEARLY:
			checkIrrigationPracticeTSData( props, data_vector );
		break;
		default: ;	// do nothing
	}
	return __check_file;
}

/**
Performs general and specific data checks on blaney criddle data. 
@param props A property list for specific properties.
@param data_vector Vector of data to check.
*/
private void checkBlaneyCriddleData(PropList props, List data_vector) 
{
	// Create elements for the checks and check file
	String[] header = StateCU_BlaneyCriddle.getDataHeader();
	List data = new Vector();
	String title = "Blaney Criddle";
	
	// Perform the general validation using the Data Table Model
	StateCU_Data_TableModel tm = new StateCU_BlaneyCriddle_TableModel(
			data_vector, false );
	List checked = performDataValidation( tm, title );
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
			data, header, title, info, data.size(), tm.getRowCount() );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
			checked, columnHeader, title + " Missing or Invalid Data",
			"", __gen_problems, tm.getRowCount() );
	__check_file.addData( dm, gen_dm );	
}

/**
Performs general and specific data checks on climate station data. 
@param props A property list for specific properties.
@param data_vector Vector of data to check.
*/
private void checkClimateStationData( PropList props, List data_vector ) 
{
	// Create elements for the checks and check file
	String[] header = StateCU_ClimateStation.getDataHeader();
	List data = new Vector();
	String title = "Climate Station";
	
	// Perform the general validation using the Data Table Model
	StateCU_Data_TableModel tm;
	try {
		tm = new StateCU_ClimateStation_TableModel( data_vector, false );
	} 
	catch (Exception e) {
		Message.printWarning( 3, 
			"StateCU_ComponentDataCheck.checkClimateStationData", e );
		return;
	}
	List checked = performDataValidation( tm, title );
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
			data, header, title, info, data.size(), tm.getRowCount() );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
			checked, columnHeader, title + " Missing or Invalid Data",
			"", __gen_problems, tm.getRowCount() );
	__check_file.addData( dm, gen_dm );	
}

/**
Performs general and specific data checks on crop characteristics data. 
@param props A property list for specific properties.
@param data_vector Vector of data to check.
*/
private void checkCropCharacteristicsData(PropList props, List data_vector) 
{
	// Create elements for the checks and check file
	String[] header = StateCU_CropCharacteristics.getDataHeader();
	List data = new Vector();
	String title = "Crop Characteristics";
	
	// Perform the general validation using the Data Table Model
	StateCU_Data_TableModel tm;
	try {
		tm = new StateCU_CropCharacteristics_TableModel( data_vector, 
			false, false );
	} catch (Exception e) {
		Message.printWarning( 3, 
			"StateCU_ComponentDataCheck.checkCropCharacteristicsData", e );
		return;
	}
	List checked = performDataValidation( tm, title );
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
			data, header, title, info, data.size(), size );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
			checked, columnHeader, title + " Missing or Invalid Data",
			"", __gen_problems, size );
	__check_file.addData( dm, gen_dm );	
}

/**
Performs general and specific data checks on crop pattern ts data. 
@param props A property list for specific properties.
@param data_vector Vector of data to check.
*/
private void checkCropPatternTSData(PropList props, List data_vector) 
{
	
}

/**
Performs general and specific data checks on delay table assignment data. 
@param props A property list for specific properties.
@param data_vector Vector of data to check.
*/
private void checkDelayTableAssignmentData(PropList props, List data_vector) 
{
	// Create elements for the checks and check file
	String[] header = StateCU_CropCharacteristics.getDataHeader();
	List data = new Vector();
	String title = "Delay Table Assignment";
	
	// Perform the general validation using the Data Table Model
	StateCU_Data_TableModel tm = 
		new StateCU_DelayTableAssignment_Data_TableModel( data_vector );
	List checked = performDataValidation( tm, title );
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
			data, header, title, info, data.size(), size );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
			checked, columnHeader, title + " Missing or Invalid Data",
			"", __gen_problems, size );
	__check_file.addData( dm, gen_dm );	
}

/**
Performs general and specific data checks on irrigation practice ts data.
@param props A property list for specific properties.
@param data_vector Vector of data to check.
 */
private void checkIrrigationPracticeTSData(PropList props, List data_vector) 
{
	
}

/**
Performs general and specific data checks on location climate station data. 
@param props A property list for specific properties.
@param data_vector Vector of data to check.
*/
private void checkLocationClimateStationsData(PropList props, List data_vector) 
{
	// Create elements for the checks and check file
	String[] header = StateCU_CropCharacteristics.getDataHeader();
	List data = new Vector();
	String title = "Location Climate Station";
	
	// Perform the general validation using the Data Table Model
	StateCU_Data_TableModel tm = 
		new StateCU_Location_ClimateStation_TableModel( data_vector );
	List checked = performDataValidation( tm, title );
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
			data, header, title, info, data.size(), size );
	CheckFile_DataModel gen_dm = new CheckFile_DataModel(
			checked, columnHeader, title + " Missing or Invalid Data",
			"", __gen_problems, size );
	__check_file.addData( dm, gen_dm );	
}

/**
Performs general and specific data checks on location collection data. 
@param props A property list for specific properties.
@param data_vector Vector of data to check.
*/
private void checkLocationCollectionData(PropList props, List data_vector) 
{

}

/**
Performs general and specific data checks on location data. 
@param props A property list for specific properties.
@param data_vector Vector of data to check.
*/
private void checkLocationData(PropList props, List data_vector) 
{
	//	 Create elements for the checks and check file
	String[] header = StateCU_Location.getDataHeader();
	List data = new Vector();
	String title = "CU Location";
	
	// Perform the general validation using the Data Table Model
	StateCU_Data_TableModel tm;
	try {
		tm = new StateCU_Location_TableModel( data_vector, false, false );
	} catch (Exception e) {
		Message.printWarning( 3, 
			"StateCU_ComponentDataCheck.checkLocationData", e );
		return;
	}
	List checked = performDataValidation( tm, title );
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
			data, header, title, info, data.size(), size );
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
private List doSpecificDataChecks( List data, PropList props )
{
	List checks = new Vector();
	if ( data == null ) {
		return checks;
	}
	// Check each component object by calling the
	// checkComponentData() method.  Each component
	// needs to implement this method and extend from
	// the StateMod_Component interface
	StateCU_Component comp = null;
	for ( int i = 0; i < data.size(); i++ ) {
		comp = (StateCU_Component)data.get( i );
		String [] invalid_data = comp.checkComponentData( 
			i, __dataset, props );
		if ( invalid_data != null && invalid_data.length > 0 ) {
			checks.add( invalid_data );
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
Helper method to return the data vector for the component
type.  This is maintained by the StateMod dataset.
@param type Component type to get data for. 
@return Vector of data for a specific component.
 */
private List getComponentData( int type )
{
	DataSetComponent comp = 
		__dataset.getComponentForComponentType( type );
	List data_vector = ( List )comp.getData();
	
	return data_vector; 
}

/**
Uses the table model object to obtain the column
headers.
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
private List performDataValidation( StateCU_Data_TableModel tm, 
String title )
{
	List data = new Vector();
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

}
