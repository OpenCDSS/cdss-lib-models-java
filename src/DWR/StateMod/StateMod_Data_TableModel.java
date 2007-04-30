/******************************************************************************
* File: ComponentDataCheck.java
* Author: KAT
* Date: 2007-03-19
*******************************************************************************
* Revisions 
*******************************************************************************
* 2007-03-22	Kurt Tometich	Initial version.
******************************************************************************/
package DWR.StateMod;

import RTi.Util.IO.Validator;
import RTi.Util.IO.Validators;

/**
Interface for all data table models to implement for abstraction.
 */
public interface StateMod_Data_TableModel {
  
	// Some shared validators that are common across many components.
	Validator [] ids = StateMod_Util.getIDValidators();
	Validator [] nums = StateMod_Util.getNumberValidators();
	Validator [] blank = { Validators.notBlankValidator() };
	Validator [] on_off_switch = StateMod_Util.getOnOffSwitchValidator();
	
	// Returns all validators for a given column of data
    Validator[] getValidators( int col );
    
    // Returns number of rows
    int getRowCount();
    // Returns the number of columns
    int getColumnCount();
    
    // Returns the data value at a particular index of the
    // component data vector
    Object getValueAt(int i, int j);
    
    // Returns the column name.  Used when printing headers for
    // for data.
    String getColumnName( int col );
}