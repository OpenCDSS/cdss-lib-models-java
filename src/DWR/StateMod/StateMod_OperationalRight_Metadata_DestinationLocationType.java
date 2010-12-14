package DWR.StateMod;

/**
This enumeration stores values for allowed operational right destination locations, which can be used to perform
checks and visualization.
*/
public enum StateMod_OperationalRight_Metadata_DestinationLocationType
{
    /**
     * Downstream.
     */
    DOWNSTREAM("Downstream"),
    /**
     * Source.
     */
    SOURCE("Source"),
    /**
     * Upstream.
     */
    UPSTREAM("Upstream"),
    /**
     * Not applicable.
     */
    NA("NA");

    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;
    
    /**
     * Construct a time series statistic enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private StateMod_OperationalRight_Metadata_DestinationLocationType(String displayName) {
        this.displayName = displayName;
    }

/**
 * Return the display name for the statistic.  This is usually the same as the
 * value but using appropriate mixed case.
 * @return the display name.
 */
@Override
public String toString() {
    return displayName;
}

/**
 * Return the enumeration value given a string name (case-independent).
 * @return the enumeration value given a string name (case-independent), or null if not matched.
 */
public static StateMod_OperationalRight_Metadata_DestinationLocationType valueOfIgnoreCase(String name)
{
	StateMod_OperationalRight_Metadata_DestinationLocationType [] values = values();
    // Currently supported values
    for ( StateMod_OperationalRight_Metadata_DestinationLocationType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}