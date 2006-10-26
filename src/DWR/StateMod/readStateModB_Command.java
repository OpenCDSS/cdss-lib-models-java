//------------------------------------------------------------------------------
// readStateModB_Command - handle the readStateModB() and
//				TS Alias = readStateModB() commands
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2005-09-29	Steven A. Malers, RTi	Initial version.  Copy and modify
//					readStateMod().  Update the original
//					syntax to add the InputStart, InputEnd,
//					and Version parameters.
// 2005-12-21	SAM, RTi		Enable passing the version to the
//					StateMod_BTS constructor.
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
This class initializes, checks, and runs the readStateModB() command.
</p>
<p>The CommandProcessor must return the following properties:
TSResultsList, WorkingDir.
</p>
*/
public class readStateModB_Command extends SkeletonCommand implements Command
{

// Indicates whether the TS Alias version of the command is being used...

protected boolean _use_alias = false;

/**
Constructor.
*/
public readStateModB_Command ()
{	super();
	setCommandName ( "readStateModB" );
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
	String TSID = parameters.getValue ( "TSID" );
	String InputStart = parameters.getValue ( "InputStart" );
	String InputEnd = parameters.getValue ( "InputEnd" );
	String Version = parameters.getValue ( "Version" );
	String warning = "";

	if ( (InputFile == null) || (InputFile.length() == 0) ) {
		warning += "\nThe input file must be specified.";
	}
	else {	String working_dir = (String)
			_processor.getPropContents ( "WorkingDir" );
		try {	String adjusted_path = IOUtil.adjustPath (
				working_dir, InputFile);
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
				"    \"" + working_dir +
				"\"\ncannot be adjusted using:\n" +
				"    \"" + InputFile + "\".";
		}
	}
	if (	(InputStart != null) && !InputStart.equals("") &&
		!InputStart.equalsIgnoreCase("InputStart") &&
		!InputStart.equalsIgnoreCase("InputEnd") ) {
		try {	DateTime InputStart_DateTime =
			DateTime.parse(InputStart);
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
		try {	DateTime InputEnd_DateTime =
				DateTime.parse( InputEnd );
		}
		catch ( Exception e ) {
			warning +=
				"\nThe input end date/time \"" + InputEnd +
				"\" is not a valid date/time.\n"+
				"Specify a date/time or InputEnd.";
		}
	}
	if ( (Version != null) && !StringUtil.isDouble(Version) ) {
		warning += "\nThe StateMod version must be a number.";
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
	return (new readStateModB_JDialog ( parent, this )).ok();
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
	String routine = "readStateModB_Command.parseCommand", message;

	if ( StringUtil.startsWithIgnoreCase(command_string,"TS") ) {
		// Syntax is TS Alias = readStateModB()
		_use_alias = true;
		message = "TS Alias = readStateModB() is not yet supported.";
		throw new InvalidCommandSyntaxException ( message );
	}
	else {	// Syntax is readStateModB()
		_use_alias = false;
		super.parseCommand (command_string, command_tag,
			warning_level);
	}
}

/**
Run the commands:
<pre>
readStateModB(InputFile="X",TSID="X",InputStart="X",InputEnd="X",Version="X")
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
{	String routine = "readStateModB_Command.runCommand", message;
	int warning_count = 0;

	String InputFile = _parameters.getValue ( "InputFile" );
	String TSID = _parameters.getValue ( "TSID" );
	String Version = _parameters.getValue ( "Version" );
	String InputStart = _parameters.getValue ( "InputStart" );
	DateTime InputStart_DateTime = null;
	if ( InputStart != null ) {
		try {	InputStart_DateTime = ((TSCommandProcessor)_processor).
				getDateTime(InputStart);
		}
		catch ( Exception e ) {
			message = "InputStart \"" + InputStart +
				"\" is invalid.";
			Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count), routine, message );
		}
	}
	else {	// Get from the processor...
		Object o = _processor.getPropContents ( "InputStart" );
		if ( o != null ) {
			InputStart_DateTime = (DateTime)o;
		}
	}
	String InputEnd = _parameters.getValue ( "InputEnd" );
	DateTime InputEnd_DateTime = null;
	if ( InputEnd != null ) {
		try {	InputEnd_DateTime = ((TSCommandProcessor)_processor).
				getDateTime (InputEnd);
		}
		catch ( Exception e ) {
			message = "InputEnd \"" + InputEnd + "\" is invalid.";
			Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count), routine, message );
		}
	}
	else {	// Get from the processor...
		Object o = _processor.getPropContents ( "InputEnd" );
		if ( o != null ) {
			InputEnd_DateTime = (DateTime)o;
		}
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
		"Reading StateMod binary file \"" + InputFile + "\"" );

		TS ts = null;
		StateMod_BTS bts = null;
		if ( (Version != null) && StringUtil.isDouble(Version) ) {
			bts = new StateMod_BTS ( InputFile,
				StringUtil.atod(Version) );
		}
		else {	bts = new StateMod_BTS ( InputFile );
		}
		Vector tslist = bts.readTimeSeriesList (
			TSID, InputStart_DateTime, InputEnd_DateTime,
			null, true );
		bts.close();
		bts = null;

		// Now add the time series to the end of the normal list...

		if ( tslist != null ) {
			TSCommandProcessor tsprocessor =
				(TSCommandProcessor)_processor;
			Vector v = (Vector)tsprocessor.
				getPropContents("TSResultsList");
			int vsize = 0;	// Existing list size.
			if ( v != null ) {
				vsize = v.size();
			}

			// Further process the time series...
			// This makes sure the period is at least as long as the
			// output period...
			Message.printStatus ( 2, routine,
			"Read " + vsize + " StateMod time series." );
			try {	tsprocessor.readTimeSeries2 ( tslist );
			}
			catch ( Exception e ) {
				message =
				"Error processing time series after read.";
				Message.printWarning ( warning_level, 
				MessageUtil.formatMessageTag(command_tag,
				++warning_count), routine, message );
				throw new CommandException ( message );
			}
			int size = tslist.size();
			for ( int i = 0; i < size; i++ ) {
				tsprocessor.setTimeSeries (
					(TS)tslist.elementAt(i), (vsize + i) );
			}
		}

		// Free resources from StateMod list...
		tslist = null;
		// Force a garbage collect because this is an intensive task...
		System.gc();
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message =
			"Error reading time series from StateMod binary file.";
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
	String TSID = props.getValue("TSID");
	String InputStart = props.getValue("InputStart");
	String InputEnd = props.getValue("InputEnd");
	String Version = props.getValue("Version");
	StringBuffer b = new StringBuffer ();
	if ( (InputFile != null) && (InputFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "InputFile=\"" + InputFile + "\"" );
	}
	if ( (TSID != null) && (TSID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "TSID=\"" + TSID + "\"" );
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
	if ( (Version != null) && (Version.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Version=\"" + Version + "\"" );
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
