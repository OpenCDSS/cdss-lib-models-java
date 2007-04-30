/******************************************************************************
* File: ComponentDataCheck.java
* Author: KAT
* Date: 2007-03-19
*******************************************************************************
* Revisions 
*******************************************************************************
* 2007-03-15	Kurt Tometich	Initial version.
******************************************************************************/
package DWR.StateMod;

import RTi.Util.IO.PropList;

/**
Interface for StateMod components to implement.  The checkComponentData method
is a method that should be implemented by every StateMod Component.  The method
is used to check specific data and data dependencies.  Errors found in these
checks are added to a check file.
 */
public interface StateMod_Component {
	// This method provides for a more abstract coding when running
	// data checks on a specific component.
	// Each component should implement this method by adding
	// specific and dependency data checks.
    String[] checkComponentData( int count, StateMod_DataSet dataset,
    		PropList props );
}