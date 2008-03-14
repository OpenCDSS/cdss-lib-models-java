package DWR.StateMod;

import RTi.Util.IO.ProcessManagerOutputFilter;

/**
 * Filter to trim the StateMod output to a more reasonable level, so that
 * the ProcessManagerJDialog does not print every line of output (and bog
 * down in the process).
 * This filter only passes progress messages indicating a new month and year,
 * and all other messages (STOP, etc.).  Additional iteration messages are not
 * passed.
 */
public class StateMod_OutputFilter implements ProcessManagerOutputFilter
{
	/**
	 * Last "Execut;  Year  YYYY  Month SSS" that was read and printed as output.
	 * If a similar "Excecut;..." line is read but has a different month, then
	 * print the output.
	 */
	private String __lastPrintedExecutLinePart = null;
	
	/**
	 * Construct a new instance of a StateMod_OutputFilter.
	 */
	public StateMod_OutputFilter ()
	{
	}
	
	/**
	 * Filter the string.
	 * @param line Line of output to check.
	 * @return The string if it is a major progress indicator (new month and year)
	 * or null if not a major progress indicator.
	 */
	public String filterOutput ( String line )
	{
		if ( line.startsWith ( " Execut;  Year" ) ) {
			// Output the line only if a new year and month.
			String part = line.substring(0,30);
			if ( (__lastPrintedExecutLinePart == null) || !part.equals(__lastPrintedExecutLinePart) ) {
				// NOT still in the same month and year so print the line and save the new part
				__lastPrintedExecutLinePart = part;
			}
			else {
				// Still iterating through day/iteration for the same year and month so no
				// need to print.
				line = null;
			}
		}
		// Fall through is to print what came in.
		return line;
	}
}
