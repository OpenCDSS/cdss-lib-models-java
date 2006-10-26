//------------------------------------------------------------------------------
// writeStateMod_Command - handle the writeStateMod() command
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2005-08-30	Steven A. Malers, RTi	Initial version.  Copy and modify
//					writeRiverWare().
// 2005-11-22	SAM, RTi		Fix so that a negative precision is
//					allowed, since it is used for special
//					formatting.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.io.File;
import java.util.Vector;

import javax.swing.JFrame;

import RTi.TS.TS;
import RTi.TS.TSCommandProcessor;
import RTi.TS.TSUtil;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.IO.SkeletonCommand;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
<p>
This class initializes, checks, and runs the writeStateMod() command.
</p>
<p>The CommandProcessor must return the following properties:  CreateOutput,
OutputYearType, TSResultsList, WorkingDir.
</p>
*/
public class writeStateMod_Command extends SkeletonCommand implements Command
{

/**
Protected data members shared with the dialog and other related classes.
*/
protected final String _AllTS = "AllTS";
protected final String _SelectedTS = "SelectedTS";
protected final String _AllMatchingTSID = "AllMatchingTSID";

/**
Constructor.
*/
public writeStateMod_Command ()
{	super();
	setCommandName ( "writeStateMod" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor
dialogs).
*/
public void checkCommandParameters (	PropList parameters, String command_tag,
					int warning_level )
throws InvalidCommandParameterException
{	String TSList = parameters.getValue ( "TSList" );
	String TSID = parameters.getValue ( "TSID" );
	String OutputFile = parameters.getValue ( "OutputFile" );
	String OutputStart = parameters.getValue ( "OutputStart" );
	String OutputEnd = parameters.getValue ( "OutputEnd" );
	String MissingValue = parameters.getValue ( "MissingValue" );
	String Precision = parameters.getValue ( "Precision" );
	String warning = "";

	if ( (TSList != null) && (TSList.length() > 0) ) {
		if (	TSList.equalsIgnoreCase(_AllMatchingTSID) &&
			((TSID == null) || (TSID.length() == 0)) ) {
			warning +=
				"\nTSList=AllMatchingID requires a TSID value.";
		}
	}

	if ( (OutputFile == null) || (OutputFile.length() == 0) ) {
		warning += "\nThe output file must be specified.";
	}
	else {	String working_dir = (String)
			_processor.getPropContents ( "WorkingDir" );
		try {	String adjusted_path = IOUtil.adjustPath (
				working_dir, OutputFile);
			File f = new File ( adjusted_path );
			File f2 = new File ( f.getParent() );
			if ( !f2.exists() ) {
				warning +=
				"\nThe output file parent directory does " +
				"not exist: \"" + adjusted_path + "\".";
			}
			f = null;
			f2 = null;
		}
		catch ( Exception e ) {
			warning +=
				"\nThe working directory:\n" +
				"    \"" + working_dir +
				"\"\ncannot be adjusted using:\n" +
				"    \"" + OutputFile + "\".";
		}
	}

	if (	(OutputStart != null) && !OutputStart.equals("") &&
		!OutputStart.equalsIgnoreCase("OutputStart") &&
		!OutputStart.equalsIgnoreCase("OutputEnd") ) {
		try {	DateTime OutputStart_DateTime =
			DateTime.parse(OutputStart);
		}
		catch ( Exception e ) {
			warning += 
				"\nThe output start date/time \"" +OutputStart +
				"\" is not a valid date/time.\n"+
				"Specify a date/time or OutputStart.";
		}
	}
	if (	(OutputEnd != null) && !OutputEnd.equals("") &&
		!OutputEnd.equalsIgnoreCase("OutputStart") &&
		!OutputEnd.equalsIgnoreCase("OutputEnd") ) {
		try {	DateTime OutputEnd_DateTime =
				DateTime.parse( OutputEnd );
		}
		catch ( Exception e ) {
			warning +=
				"\nThe output end date/time \"" + OutputEnd +
				"\" is not a valid date/time.\n"+
				"Specify a date/time or OutputEnd.";
		}
	}

	if ( (MissingValue != null) && !MissingValue.equals("") ) {
		if ( !StringUtil.isDouble(MissingValue) ) {
			warning += "\nThe missing value \"" + MissingValue +
				"\" is not a number.";
		}
	}

	if ( (Precision != null) && !Precision.equals("") ) {
		if ( !StringUtil.isInteger(Precision) ) {
			warning += "\nThe precision \"" + Precision +
				"\" is not an integer.";
		}
	}

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level),
		warning );
		throw new InvalidCommandParameterException ( warning );
	}
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new writeStateMod_JDialog ( parent, this )).ok();
}

/**
Parse the command string into a PropList of parameters.
@param command_string A string command to parse.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2).
@exception InvalidCommandSyntaxException if during parsing the command is
determined to have invalid syntax.
syntax of the command are bad.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void parseCommand (	String command_string, String command_tag,
				int warning_level )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{	int warning_count = 0;
	String routine = "writeStateMod_Command.parseCommand", message;

	if ( command_string.indexOf("=") > 0 ) {
		// New syntax...
		super.parseCommand (command_string, command_tag, warning_level);
	}
	else {	// Parse the old command...
		Vector tokens = StringUtil.breakStringList ( command_string,
			"(,)", StringUtil.DELIM_ALLOW_STRINGS );
		if ( tokens.size() != 3 ) {
			message =
			"Invalid syntax for command.  Expecting 2 parameters.";
			Message.printWarning ( warning_level, routine, message);
			throw new InvalidCommandSyntaxException ( message );
		}
		String OutputFile = ((String)tokens.elementAt(1)).trim();
		String Precision = ((String)tokens.elementAt(2)).trim();
		if ( Precision.equals("*") ) {
			Precision = "";
		}
		// Defaults because not in the old command...
		String TSList = "AllTS";
		_parameters = new PropList ( getCommandName() );
		_parameters.setHowSet ( Prop.SET_FROM_PERSISTENT );
		_parameters.set ( "TSList", TSList );
		if ( OutputFile.length() > 0 ) {
			_parameters.set ( "OutputFile", OutputFile );
		}
		if ( Precision.length() > 0 ) {
			_parameters.set ( "Precision", Precision );
		}
		_parameters.setHowSet ( Prop.SET_UNKNOWN );
	}
}

/**
Run the commands:
<pre>
writeStateMod(TSList=X,TSID="X",OutputStart="X",OutputEnd="X",
MissingValue=X,Precision=X)
</pre>
@param processor The CommandProcessor that is executing the command, which will
provide necessary data inputs and receive output(s).
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2).
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could
not produce output).
*/
public void runCommand ( String command_tag, int warning_level )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String routine = "writeStateMod_Command.runCommand", message;
	int warning_count = 0;

	// Check whether the application wants output files to be created...
	
	Object o = _processor.getPropContents ( "CreateOutput" );
	if ( (o != null) && ((String)o).equalsIgnoreCase("False") ) {
		Message.printStatus ( 2, routine,
		"Skipping \"" + toString() +
		"\" because output is to be ignored." );
		return;
	}

	String TSList = _parameters.getValue ( "TSList" );
	String TSID = _parameters.getValue ( "TSID" );
	String OutputFile = _parameters.getValue ( "OutputFile" );

	// Get the time series to process...
	Vector v = ((TSCommandProcessor)_processor).
		getTimeSeriesToProcess( TSList, TSID);
	Vector tslist = (Vector)v.elementAt(0);
	if ( (tslist == null) && (tslist.size() == 0) ) {
		message = "Unable to find time series to write using TSID \"" +
		TSID + "\".";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(
		command_tag,++warning_count), routine, message );
	}

	String OutputStart = _parameters.getValue ( "OutputStart" );
	DateTime OutputStart_DateTime = null;
	if ( OutputStart != null ) {
		try {	OutputStart_DateTime = ((TSCommandProcessor)_processor).
				getDateTime(OutputStart);
		}
		catch ( Exception e ) {
			message = "OutputStart \"" + OutputStart +
				"\" is invalid.";
			Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count), routine, message );
		}
	}
	else {	// Get from the processor...
		o = _processor.getPropContents ( "OutputStart" );
		if ( o != null ) {
			OutputStart_DateTime = (DateTime)o;
		}
	}
	String OutputEnd = _parameters.getValue ( "OutputEnd" );
	DateTime OutputEnd_DateTime = null;
	if ( OutputEnd != null ) {
		try {	OutputEnd_DateTime = ((TSCommandProcessor)_processor).
				getDateTime (OutputEnd);
		}
		catch ( Exception e ) {
			message = "OutputEnd \"" + OutputEnd + "\" is invalid.";
			Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count), routine, message );
		}
	}
	else {	// Get from the processor...
		o = _processor.getPropContents ( "OutputEnd" );
		if ( o != null ) {
			OutputEnd_DateTime = (DateTime)o;
		}
	}

	String Precision = _parameters.getValue ( "Precision" );
	String MissingValue = _parameters.getValue ( "MissingValue" );

	String OutputYearType = "";
	o = _processor.getPropContents ( "OutputYearType" );
	if ( o != null ) {
		OutputYearType = (String)o;
	}
	else {	OutputYearType = "Calendar";
	}
	if (	!OutputYearType.equalsIgnoreCase("Calendar") &&
		!OutputYearType.equalsIgnoreCase("Water") ) {
		message = "\nThe output year type (" + OutputYearType +
			") must be \"Calendar\" or \"Water\".";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(
		command_tag,++warning_count), routine, message );
	}
		
	if ( warning_count > 0 ) {
		message = "There were " + warning_count +
			" warnings about command parameters.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
		throw new InvalidCommandParameterException ( message );
	}

	// Now try to write...

	try {	Message.printStatus ( 2, routine,
		"Writing StateMod file \"" + OutputFile + "\"" );

		// Format the StateMod year type...
		String sm_calendar = "CalendarYear";
		if ( OutputYearType.equalsIgnoreCase("Water") ) {
			sm_calendar = "WaterYear";
		}

		// Set the precision default precision for output (-2 generally
		// works OK)...
		int Precision_int = -2;
		if ( (Precision != null) && (Precision.length() > 0) ) {
			Precision_int = StringUtil.atoi(Precision);
		}
		else {	// Precision is determined from units and possibly data
			// type...
			Precision_int = StateMod_Util.
				getTimeSeriesOutputPrecision ( tslist );
		}

		// Get the comments to add to the top of the file.

		Vector OutputComments_Vector = null;
		o = _processor.getPropContents ( "OutputComments" );
		if ( o != null ) {
			OutputComments_Vector = (Vector)o;
		}

		TS ts = null;	// Time series being checked.
		if ( TSUtil.intervalsMatch ( tslist ) ) {
			// The time series to write have the same interval so
			// write using the first interval....
			int interval = TimeInterval.UNKNOWN;
			int size = 0;
			if ( tslist != null ) {
				size = tslist.size();
			}
			for ( int i = 0; i < size; i++ ) {
				ts = (TS)tslist.elementAt(i);
				if ( ts == null ) {
					continue;
				}
				interval = ts.getDataIntervalBase();
				break;
			}
			if ( interval == TimeInterval.UNKNOWN ) {
				message = "Unable to determine time interval " +
				"from time series.  Can't write output.";
				Message.printWarning ( warning_level, 
				MessageUtil.formatMessageTag(command_tag,
				++warning_count), routine, message );
				throw new CommandException ( message );
			}
			else if ((interval == TimeInterval.DAY) ||
				(interval == TimeInterval.MONTH) ) {
				PropList smprops = new PropList ( "StateMod" );
				// Don't set input file since it is null...
				smprops.set ( "OutputFile", OutputFile );
				if ( OutputComments_Vector != null ) {
					smprops.setUsingObject ( "NewComments",
					StringUtil.toArray(
					OutputComments_Vector) );
				}
				if ( OutputStart_DateTime != null ) {
					smprops.set("OutputStart=" +
					OutputStart_DateTime.toString());
				}
				if ( OutputEnd_DateTime != null ) {
					smprops.set ( "OutputEnd=" +
					OutputEnd_DateTime.toString());
				}
				if ( OutputYearType != null ) {
					smprops.set ( "CalendarType",
					sm_calendar );
				}
				if ( MissingValue != null ) {
					smprops.set ( "MissingDataValue",
						MissingValue );
				}
				if ( Precision != null ) {
					smprops.set ( "OutputPrecision",
					Precision );
				}
				/* REVISIT SAM 2005-09-01
					Need to handle from processor...
				if ( _detailedheader ) {
					smprops.set ( "PrintGenesis", "true" );
				}
				else {	smprops.set ( "PrintGenesis", "false" );
				}
				*/
				try {	StateMod_TS.writeTimeSeriesList (
						tslist, smprops );
				}
				catch ( Exception e ) {
					Message.printWarning ( 3, routine, e );
					Message.printWarning ( warning_level, 
					MessageUtil.formatMessageTag(
					command_tag, ++warning_count), routine,
					"Unable to write StateMod file \"" +
					OutputFile + "\"" );
				}
			}
			else {	message =
				"Don't know how to write StateMod output for " +
				"interval \"" +
				ts.getIdentifier().getInterval() + "\".";
				Message.printWarning ( warning_level, 
				MessageUtil.formatMessageTag(command_tag,
					++warning_count), routine, message );
				throw new CommandException ( message );
			}
		}
		else {	message = "Unable to write StateMod time series of " +
				"different intervals.";
			Message.printWarning ( warning_level, 
			MessageUtil.formatMessageTag(command_tag,
				++warning_count), routine, message );
			throw new CommandException ( message );
		}
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Error writing time series to StateMod file.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
		throw new CommandException ( message );
	}
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
	String TSList = props.getValue("TSList");
	String TSID = props.getValue("TSID");
	String OutputFile = props.getValue("OutputFile");
	String OutputStart = props.getValue("OutputStart");
	String OutputEnd = props.getValue("OutputEnd");
	String MissingValue = props.getValue("MissingValue");
	String Precision = props.getValue("Precision");
	StringBuffer b = new StringBuffer ();
	if ( (TSList != null) && (TSList.length() > 0) ) {
		b.append ( "TSList=" + TSList );
	}
	if ( (TSID != null) && (TSID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "TSID=\"" + TSID + "\"" );
	}
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
	}
	if ( (OutputStart != null) && (OutputStart.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputStart=\"" + OutputStart + "\"" );
	}
	if ( (OutputEnd != null) && (OutputEnd.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputEnd=\"" + OutputEnd + "\"" );
	}
	if ( (MissingValue != null) && (MissingValue.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "MissingValue=" + MissingValue );
	}
	if ( (Precision != null) && (Precision.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Precision=" + Precision );
	}
	return getCommandName() + "(" + b.toString() + ")";
}

}
