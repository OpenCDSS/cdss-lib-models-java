// StateCU_Data_TableModel - interface for all data table models to implement for abstraction.

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
