package DWR.StateMod;

/**
This enumeration stores values for whether an operational right "source 2" is allowed, required, or not
applicable, which can be used to perform checks and visualization.
*/
public enum StateMod_OperationalRight_Metadata_Source2AllowedType
{
    /**
     * Allowed.
     */
    ALLOWED("Allowed"),
    /**
     * Not applicable.
     */
    NA("NA"),
    /**
     * Required.
     */
    REQUIRED("Required");
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;
    
    /**
     * Construct a time series statistic enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private StateMod_OperationalRight_Metadata_Source2AllowedType(String displayName) {
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
public static StateMod_OperationalRight_Metadata_Source2AllowedType valueOfIgnoreCase(String name)
{
	StateMod_OperationalRight_Metadata_Source2AllowedType [] values = values();
    // Currently supported values
    for ( StateMod_OperationalRight_Metadata_Source2AllowedType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}