/******************************************************************************
* File: StateCU_Data_TableModel.java
* Author: KAT
* Date: 2007-04-12
*******************************************************************************
* Revisions 
*******************************************************************************
* 2007-04-12	Kurt Tometich	Initial version.
******************************************************************************/
package DWR.StateCU;

import RTi.Util.IO.Validator;
import RTi.Util.IO.Validators;

/**
Interface for all data table models to implement for abstraction.
 */
public interface StateCU_Data_TableModel {
  
	Validator [] ids = StateCU_Util.getIDValidators();
	Validator [] nums = StateCU_Util.getNumberValidators();
	Validator [] blank = { Validators.notBlankValidator() };
	Validator [] on_off_switch = StateCU_Util.getOnOffSwitchValidator();
	
	// Returns all validators for a given column of data
    Validator[] getValidators( int col );
    int getRowCount();
    int getColumnCount();
    // Returns the data value at a particular index of the
    // component data vector
    Object getValueAt(int i, int j);
    // Returns the column name.  Used when printing headers for
    // for data.
    String getColumnName( int col );
}