//------------------------------------------------------------------------------
// StateMod_GUIUpdatable - interface to allow the StateMod_DataSet_WindowManager
//				to call StateModGUI_JFrame.updateWindowStatus() 
//				without StateModGUI_JFrame being known to the
//				StateMod package.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 2003-10-14	Steven A. Malers, RTi	Created interface.
//------------------------------------------------------------------------------

package DWR.StateMod;

public interface StateMod_GUIUpdatable
{

/**
When called, this method will trigger the window to refresh its state, including
the title bar and menu states, based on current conditions of the StateMod
data set.
*/
public void updateWindowStatus ();

}
