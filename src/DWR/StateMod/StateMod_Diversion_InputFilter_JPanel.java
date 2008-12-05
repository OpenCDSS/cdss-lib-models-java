//------------------------------------------------------------------------------
// StateMod_Diversion_InputFilter_JPanel - input filter panel diversion data
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2004-10-25	Steven A. Malers, RTi	Implement for StateMod GUI query tool.
//------------------------------------------------------------------------------

package DWR.StateMod;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.InputFilter;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;

public class StateMod_Diversion_InputFilter_JPanel extends InputFilter_JPanel
{

/**
Create an InputFilter_JPanel for creating where clauses
for StateMod diversion station queries.  This is used by the StateMod GUI.
@param dataset StateMod_DataSet instance.
@return a JPanel containing InputFilter instances for StateMod_Diversion
queries.
@exception Exception if there is an error.
*/
public StateMod_Diversion_InputFilter_JPanel ( StateMod_DataSet dataset )
throws Exception
{	List input_filters = new Vector(15);
	InputFilter filter = null;
	input_filters.add ( new InputFilter (
		"", "",
		StringUtil.TYPE_STRING,
		null, null, true ) );	// Blank to disable filter
	input_filters.add ( new InputFilter (
		"ID", "ID",
		StringUtil.TYPE_STRING,
		null, null, true ) );
	input_filters.add ( new InputFilter (
		"Name", "Name",
		StringUtil.TYPE_STRING,
		null, null, true ) );
	input_filters.add ( new InputFilter (
		"River Node ID", "RiverNodeID",
		StringUtil.TYPE_STRING,
		null, null, true ) );
	filter = new InputFilter (
		"On/Off Switch", "OnOff",
		StringUtil.TYPE_INTEGER,
		StateMod_Diversion.getIdivswChoices(true),
		StateMod_Diversion.getIdivswChoices(false), true );
	filter.setTokenInfo ( " ", 0 );
	input_filters.add ( filter );
	input_filters.add ( new InputFilter (
		"Capacity", "Capacity",
		StringUtil.TYPE_DOUBLE,
		null, null, true ) );
	filter = new InputFilter (
		"Replacement Reservoir Option", "ReplaceResOption",
		StringUtil.TYPE_INTEGER,
		StateMod_Diversion.getIreptypeChoices(true),
		StateMod_Diversion.getIreptypeChoices(false), true );
	filter.setTokenInfo ( " ", 0 );
	input_filters.add ( filter );
	input_filters.add ( new InputFilter (
		"Daily ID", "DailyID",
		StringUtil.TYPE_STRING,
		null, null, true ) );
	input_filters.add ( new InputFilter (
		"User Name", "UserName",
		StringUtil.TYPE_STRING,
		null, null, true ) );
	filter = new InputFilter (
		"Demand Type", "DemandType",
		StringUtil.TYPE_INTEGER,
		StateMod_Diversion.getIdvcomChoices(true),
		StateMod_Diversion.getIdvcomChoices(false), true );
	filter.setTokenInfo ( " ", 0 );
	input_filters.add ( filter );
	/*
	input_filters.addElement ( new InputFilter (
		"Number of Returns", "NumReturns",
		StringUtil.TYPE_INTEGER,
		null, null, true ) );
	*/
	input_filters.add ( new InputFilter (
		"Efficiency (Annual)", "EffAnnual",
		StringUtil.TYPE_DOUBLE,
		null, null, true ) );
	input_filters.add ( new InputFilter (
		"Area (ACRE)", "Area",
		StringUtil.TYPE_DOUBLE,
		null, null, true ) );
	filter = new InputFilter (
		"Use Type", "UseType",
		StringUtil.TYPE_INTEGER,
		StateMod_Diversion.getIrturnChoices(true),
		StateMod_Diversion.getIrturnChoices(false), true );
	filter.setTokenInfo ( " ", 0 );
	input_filters.add ( filter );
	filter = new InputFilter (
		"Demand Source", "DemandSource",
		StringUtil.TYPE_INTEGER,
		StateMod_Diversion.getDemsrcChoices(true),
		StateMod_Diversion.getDemsrcChoices(false), true );
	filter.setTokenInfo ( " ", 0 );
	input_filters.add ( filter );
	/* REVISIT SAM 2004-10-25 monthly efficiencies?
		Returns?
	*/

	PropList filter_props = new PropList ( "InputFilter" );
	filter_props.set ( "NumFilterGroups=3" );
	setToolTipText (
		"<HTML>HydroBase queries can be filtered" +
		"<BR>based on station data." +
		"</HTML>" );
	setInputFilters ( input_filters, filter_props );
}

}
