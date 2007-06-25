//------------------------------------------------------------------------------
// readStateMod_Command - handle the readStateMod() and
//				TS Alias = readStateMod() commands
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2005-09-02	Steven A. Malers, RTi	Initial version.  Copy and modify
//					writeStateMod().
// 2007-05-16	SAM, RTi                Add ability to read well rights file.
//------------------------------------------------------------------------------
// EndHeader

package DWR.StateMod;

import java.io.File;
import java.util.Vector;

import javax.swing.JFrame;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
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
This class initializes, checks, and runs the readStateMod() command.
</p>
<p>The CommandProcessor must return the following properties:
TSResultsList, WorkingDir.
</p>
*/
public class readStateMod_Command extends SkeletonCommand implements Command
{
// Flags used when setting the interval
protected String _Day = "Day";
protected String _Month = "Month";
protected String _Year = "Year";
//protected String _Irregular = "Irregular";

// Flags used when setting the spatial aggregation
protected String _Location = "Location";
protected String _Parcel = "Parcel";
protected String _None = "None";

// Indicates whether the TS Alias version of the command is being used...

protected boolean _use_alias = false;

private String __working_dir = null;	// Application working directory

/**
Constructor.
*/
public readStateMod_Command ()
{	super();
	setCommandName ( "readStateMod" );
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
{	String InputFile = parameters.getValue ( "InputFile" );
	String InputStart = parameters.getValue ( "InputStart" );
	String InputEnd = parameters.getValue ( "InputEnd" );
	String Interval = parameters.getValue ( "Interval" );
	String SpatialAggregation = parameters.getValue ( "SpatialAggregation" );
	String ParcelYear = parameters.getValue ( "ParcelYear" );
	String warning = "";

	if ( (InputFile == null) || (InputFile.length() == 0) ) {
		warning += "\nThe input file must be specified.";
	}
	else {	try { Object o = _processor.getPropContents ( "WorkingDir" );
				// Working directory is available so use it...
				if ( o != null ) {
					__working_dir = (String)o;
				}
			}
			catch ( Exception e ) {
				// Not fatal, but of use to developers.
				String message = "Error requesting WorkingDir from processor - not using.";
				String routine = getCommandName() + ".checkCommandParameters";
				Message.printDebug(10, routine, message );
			}
	
		try {	String adjusted_path = IOUtil.adjustPath (
				__working_dir, InputFile);
			File f = new File ( adjusted_path );
			File f2 = new File ( f.getParent() );
			if ( !f2.exists() ) {
				warning +=
				"\nThe input file parent directory does " +
				"not exist: \"" + adjusted_path + "\".";
			}
			f = null;
			f2 = null;
		}
		catch ( Exception e ) {
			warning +=
				"\nThe working directory:\n" +
				"    \"" + __working_dir +
				"\"\ncannot be used to adjust the file:\n" +
				"    \"" + InputFile + "\".";
		}
	}

	if (	(InputStart != null) && !InputStart.equals("") &&
		!InputStart.equalsIgnoreCase("InputStart") &&
		!InputStart.equalsIgnoreCase("InputEnd") ) {
		try {	DateTime.parse(InputStart);
		}
		catch ( Exception e ) {
			warning += 
				"\nThe input start date/time \"" +InputStart +
				"\" is not a valid date/time.\n"+
				"Specify a date/time or InputStart.";
		}
	}
	if (	(InputEnd != null) && !InputEnd.equals("") &&
		!InputEnd.equalsIgnoreCase("InputStart") &&
		!InputEnd.equalsIgnoreCase("InputEnd") ) {
		try {	DateTime.parse( InputEnd );
		}
		catch ( Exception e ) {
			warning +=
				"\nThe input end date/time \"" + InputEnd +
				"\" is not a valid date/time.\n"+
				"Specify a date/time or InputEnd.";
		}
	}
	if ( (Interval != null) && !Interval.equals("") &&
		!Interval.equalsIgnoreCase(_Day) &&
		!Interval.equalsIgnoreCase(_Month) &&
		!Interval.equalsIgnoreCase(_Year) ) {
			warning +=
				"\nThe interval must be Day, Month, or Year.";
		}
	
	if ( (SpatialAggregation != null)&&
			!SpatialAggregation.equals("") &&
			!SpatialAggregation.equalsIgnoreCase(_Location) &&
			!SpatialAggregation.equalsIgnoreCase(_Parcel) &&
			!SpatialAggregation.equalsIgnoreCase(_None) ) {
				warning +=
					"\nThe spatial aggregation must be Location, Parcel, or None.";
			}
	
	if ( (ParcelYear != null) && (ParcelYear.length() > 0) ) {
		if ( !StringUtil.isInteger(ParcelYear)) {
			warning +=
				"\nThe parcel year must be an integer.";
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
	return (new readStateMod_JDialog ( parent, this )).ok();
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
{	String routine = "readStateMod_Command.parseCommand", message;

	if ( StringUtil.startsWithIgnoreCase(command_string,"TS") ) {
		// Syntax is TS Alias = readStateMod()
		_use_alias = true;
		message = "TS Alias = readStateMod() is not yet supported.";
		throw new InvalidCommandSyntaxException ( message );
	}
	else {	// Syntax is readStateMod()
		_use_alias = false;
		if ( command_string.indexOf("=") > 0 ) {
			// New syntax...
			super.parseCommand (command_string, command_tag,
				warning_level);
		}
		else {	// Parse the old command...
			Vector tokens = StringUtil.breakStringList (
				command_string,
				"(,)", StringUtil.DELIM_ALLOW_STRINGS );
			if ( tokens.size() != 2 ) {
				message = "Invalid syntax for command.  " +
					"Expecting 1 parameter.";
				Message.printWarning ( warning_level, routine,
					message);
				throw new InvalidCommandSyntaxException (
					message );
			}
			String InputFile = ((String)tokens.elementAt(1)).trim();
			_parameters = new PropList ( getCommandName() );
			_parameters.setHowSet ( Prop.SET_FROM_PERSISTENT );
			if ( InputFile.length() > 0 ) {
				_parameters.set ( "InputFile", InputFile );
			}
			_parameters.setHowSet ( Prop.SET_UNKNOWN );
		}
	}
}
	
/**
Run the commands:
<pre>
readStateMod(InputFile="X",InputStart="X",InputEnd="X",Interval=X)
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
{	String routine = "readStateMod_Command.runCommand", message;
	int warning_count = 0;
	int log_level = 3;	// Log level for non-user warnings

	String InputFile = _parameters.getValue ( "InputFile" );

	String InputStart = _parameters.getValue ( "InputStart" );
	DateTime InputStart_DateTime = null;
	String InputEnd = _parameters.getValue ( "InputEnd" );
	String Interval = _parameters.getValue ( "Interval" );
	String SpatialAggregation = _parameters.getValue ( "SpatialAggregation" );
	String ParcelYear = _parameters.getValue ( "ParcelYear" );
	DateTime InputEnd_DateTime = null;
	
	if ( InputStart != null ) {
		try {
		PropList request_params = new PropList ( "" );
		request_params.set ( "DateTime", InputStart );
		CommandProcessorRequestResultsBean bean = null;
		try { bean =
			_processor.processRequest( "DateTime", request_params);
		}
		catch ( Exception e ) {
			message = "Error requesting InputStart DateTime(DateTime=" +
			InputStart + "\" from processor.";
			Message.printWarning(log_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
			throw new InvalidCommandParameterException ( message );
		}

		PropList bean_PropList = bean.getResultsPropList();
		Object prop_contents = bean_PropList.getContents ( "DateTime" );
		if ( prop_contents == null ) {
			message = "Null value for InputStart DateTime(DateTime=" +
			InputStart +	"\") returned from processor.";
			Message.printWarning(log_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
			throw new InvalidCommandParameterException ( message );
		}
		else {	InputStart_DateTime = (DateTime)prop_contents;
		}
	}
	catch ( Exception e ) {
		message = "InputStart \"" + InputStart + "\" is invalid.";
		Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
		throw new InvalidCommandParameterException ( message );
	}
	}
	else {	// Get from the processor...
		try {	Object o = _processor.getPropContents ( "InputStart" );
				if ( o != null ) {
					InputStart_DateTime = (DateTime)o;
				}
		}
		catch ( Exception e ) {
			// Not fatal, but of use to developers.
			message = "Error requesting InputStart from processor - not using.";
			Message.printDebug(10, routine, message );
		}
	}
	
		if ( InputEnd != null ) {
			try {
			PropList request_params = new PropList ( "" );
			request_params.set ( "DateTime", InputEnd );
			CommandProcessorRequestResultsBean bean = null;
			try { bean =
				_processor.processRequest( "DateTime", request_params);
			}
			catch ( Exception e ) {
				message = "Error requesting InputEnd DateTime(DateTime=" +
				InputEnd + "\" from processor.";
				Message.printWarning(log_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
				throw new InvalidCommandParameterException ( message );
			}

			PropList bean_PropList = bean.getResultsPropList();
			Object prop_contents = bean_PropList.getContents ( "DateTime" );
			if ( prop_contents == null ) {
				message = "Null value for InputEnd DateTime(DateTime=" +
				InputEnd +	"\") returned from processor.";
				Message.printWarning(log_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				throw new InvalidCommandParameterException ( message );
			}
			else {	InputEnd_DateTime = (DateTime)prop_contents;
			}
		}
		catch ( Exception e ) {
			message = "InputEnd \"" + InputEnd + "\" is invalid.";
			Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
			throw new InvalidCommandParameterException ( message );
		}
		}
		else {	// Get from the processor...
			try {	Object o = _processor.getPropContents ( "InputEnd" );
					if ( o != null ) {
						InputEnd_DateTime = (DateTime)o;
					}
			}
			catch ( Exception e ) {
				// Not fatal, but of use to developers.
				message = "Error requesting InputEnd from processor - not using.";
				Message.printDebug(10, routine, message );
			}
	}
	
		
	int SpatialAggregation_int = 0;  // Default
	if ( SpatialAggregation == null ) {
		SpatialAggregation = _Location;
		SpatialAggregation_int = 0;
	}
	else if ( SpatialAggregation.equalsIgnoreCase(_Parcel) ) {
		SpatialAggregation_int = 1;
	}
	else if ( SpatialAggregation.equalsIgnoreCase(_None) ) {
		SpatialAggregation_int = 2;
	}
	int ParcelYear_int = -1;	// Default - consider all
	if ( ParcelYear != null ) {
		ParcelYear_int = StringUtil.atoi ( ParcelYear );
	}

	if ( warning_count > 0 ) {
		message = "There were " + warning_count +
			" warnings about command parameters.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
		throw new InvalidCommandParameterException ( message );
	}

	// Now try to read...

	try {	Message.printStatus ( 2, routine,
		"Reading StateMod file \"" + InputFile + "\"" );
	
		Vector tslist = null;
		if ( StateMod_DiversionRight.isDiversionRightFile(InputFile)) {
			if ( (Interval == null) || Interval.equals("") ) {
				Interval = "Year";
			}
			TimeInterval Interval_TimeInterval = TimeInterval.parseInterval( Interval );
			// Read the diversion rights file and convert to time series
			// (default is to sum time series at a location).
			Vector ddr_Vector = StateMod_WellRight.readStateModFile ( InputFile );
			// Convert the rights to time series (one per location)...
			tslist = StateMod_Util.createWaterRightTimeSeriesList (
					ddr_Vector,        // raw water rights
					Interval_TimeInterval.getBase(),  // time series interval
					SpatialAggregation_int,          // Where to summarize time series
					ParcelYear_int,			// Parcel year for filter
					true,				// Create a data set total
					null,              // time series start
					null,              // time series end
					999999.00000,	// No special free water rights
					null,			// ...
					null,			// ...
					true );            // do read data
		}
		else if ( StateMod_InstreamFlowRight.isInstreamFlowRightFile(InputFile)) {
			if ( (Interval == null) || Interval.equals("") ) {
				Interval = "Year";
			}
			TimeInterval Interval_TimeInterval = TimeInterval.parseInterval( Interval );
			// Read the instream flow rights file and convert to time series
			// (default is to sum time series at a location).
			Vector ifr_Vector = StateMod_WellRight.readStateModFile ( InputFile );
			// Convert the rights to time series (one per location)...
			tslist = StateMod_Util.createWaterRightTimeSeriesList (
					ifr_Vector,        // raw water rights
					Interval_TimeInterval.getBase(),  // time series interval
					SpatialAggregation_int,          // Where to summarize time series
					ParcelYear_int,			// Parcel year for filter
					true,				// Create a data set total
					null,              // time series start
					null,              // time series end
					999999.00000,	// No special free water rights
					null,			// ...
					null,			// ...
					true );            // do read data
		}
		else if ( StateMod_ReservoirRight.isReservoirRightFile(InputFile)) {
			if ( (Interval == null) || Interval.equals("") ) {
				Interval = "Year";
			}
			TimeInterval Interval_TimeInterval = TimeInterval.parseInterval( Interval );
			// Read the reservoir rights file and convert to time series
			// (default is to sum time series at a location).
			Vector rer_Vector = StateMod_WellRight.readStateModFile ( InputFile );
			// Convert the rights to time series (one per location)...
			tslist = StateMod_Util.createWaterRightTimeSeriesList (
					rer_Vector,        // raw water rights
					Interval_TimeInterval.getBase(),  // time series interval
					SpatialAggregation_int,          // Where to summarize time series
					ParcelYear_int,			// Parcel year for filter
					true,				// Create a data set total
					null,              // time series start
					null,              // time series end
					999999.00000,	// No special free water rights
					null,			// ...
					null,			// ...
					true );            // do read data
		}
		else if ( StateMod_WellRight.isWellRightFile(InputFile)) {
			if ( (Interval == null) || Interval.equals("") ) {
				Interval = "Year";
			}
			TimeInterval Interval_TimeInterval = TimeInterval.parseInterval( Interval );
			// Read the well rights file and convert to time series
			// (default is to sum time series at a location).
			Vector wer_Vector = StateMod_WellRight.readStateModFile ( InputFile );
			// Convert the rights to time series (one per location)...
			tslist = StateMod_Util.createWaterRightTimeSeriesList (
					wer_Vector,        // raw water rights
					Interval_TimeInterval.getBase(),  // time series interval
					SpatialAggregation_int,          // Where to summarize time series
					ParcelYear_int,			// Parcel year for filter
					true,				// Create a data set total
					null,              // time series start
					null,              // time series end
					999999.00000,	// No special free water rights
					null,			// ...
					null,			// ...
					true );            // do read data
		}
		else {	// Read a traditional time series file
			int interval = StateMod_TS.getFileDataInterval(InputFile);

			if (	(interval == TimeInterval.MONTH) ||
					(interval == TimeInterval.DAY) ) {
				tslist = StateMod_TS.readTimeSeriesList (
				InputFile, InputStart_DateTime,
				InputEnd_DateTime,
				null,	// Requested units
				true );	// Read all data
			}
			else {	message = "StateMod file \"" + InputFile +
				"\" is not a recognized interval (bad file format?).";
				Message.printWarning ( warning_level, 
						MessageUtil.formatMessageTag(command_tag,
						++warning_count), routine, message );
				throw new CommandException ( message );
			}
		}

		// Now add the time series to the end of the normal list...

		if ( tslist != null ) {
			Vector TSResultsList_Vector = null;
			try { Object o = _processor.getPropContents( "TSResultsList" );
					TSResultsList_Vector = (Vector)o;
			}
			catch ( Exception e ){
				message = "Cannot get time series list to add read time series.  Starting new list.";
				Message.printWarning ( warning_level,
						MessageUtil.formatMessageTag(
						command_tag, ++warning_count),
						routine,message);
				TSResultsList_Vector = new Vector();
			}

			// Further process the time series...
			// This makes sure the period is at least as long as the
			// output period...
			int size = tslist.size();
			Message.printStatus ( 2, routine,
			"Read " + size + " StateMod time series." );
			PropList request_params = new PropList ( "" );
			request_params.setUsingObject ( "TSList", tslist );
			try {
				_processor.processRequest( "ReadTimeSeries2", request_params);
			}
			catch ( Exception e ) {
				message =
					"Error post-processing StateMod time series after read.";
					Message.printWarning ( warning_level, 
					MessageUtil.formatMessageTag(command_tag,
					++warning_count), routine, message );
					Message.printWarning(log_level, routine, e);
					throw new CommandException ( message );
			}

			for ( int i = 0; i < size; i++ ) {
				TSResultsList_Vector.addElement ( tslist.elementAt(i) );
			}
			
			// Now reset the list in the processor...
			if ( TSResultsList_Vector != null ) {
				try {	_processor.setPropContents ( "TSResultsList", TSResultsList_Vector );
				}
				catch ( Exception e ){
					message = "Cannot set updated time series list.  Results may not be visible.";
					Message.printWarning ( warning_level,
						MessageUtil.formatMessageTag(
						command_tag, ++warning_count),
						routine,message);
				}
			}
		}

		// Free resources from StateMod list...
		tslist = null;
	}
	catch ( Exception e ) {
		Message.printWarning ( log_level, routine, e );
		message = "Error reading time series from StateMod file.";
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
	String InputFile = props.getValue("InputFile");
	String InputStart = props.getValue("InputStart");
	String InputEnd = props.getValue("InputEnd");
	String Interval = props.getValue("Interval");
	String SpatialAggregation = props.getValue("SpatialAggregation");
	String ParcelYear = props.getValue("ParcelYear");
	StringBuffer b = new StringBuffer ();
	if ( (InputFile != null) && (InputFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "InputFile=\"" + InputFile + "\"" );
	}
	if ( (InputStart != null) && (InputStart.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "InputStart=\"" + InputStart + "\"" );
	}
	if ( (InputEnd != null) && (InputEnd.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "InputEnd=\"" + InputEnd + "\"" );
	}
	if ( (Interval != null) && (Interval.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Interval=\"" + Interval + "\"" );
	}
	if ( (SpatialAggregation != null) && (SpatialAggregation.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SpatialAggregation=" + SpatialAggregation );
	}
	if ( (ParcelYear != null) && (ParcelYear.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ParcelYear=" + ParcelYear );
	}
	return getCommandName() + "(" + b.toString() + ")";
}

/**
Indicate whether the alias version of the command is being used.  This method
should be called only after the parseCommandParameters() method is called.
*/
protected boolean useAlias ()
{	return _use_alias;
}

}
