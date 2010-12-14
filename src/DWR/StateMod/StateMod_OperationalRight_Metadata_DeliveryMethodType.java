package DWR.StateMod;

/**
This enumeration stores values for allowed operational right delivery methods, which can be used to perform
checks and visualization.
*/
public enum StateMod_OperationalRight_Metadata_DeliveryMethodType
{
    /**
     * Bookover.
     */
    BOOKOVER("Bookover"),
    /**
     * TODO SAM 2010-12-12 How to know when a diversion station is a carrier?
     * Carrier.
     */
    CARRIER("Carrier"),
    /**
     * Not applicable.
     */
    NA("NA"),
    /**
     * River.
     */
    RIVER("River");
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;
    
    /**
     * Construct a time series statistic enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private StateMod_OperationalRight_Metadata_DeliveryMethodType(String displayName) {
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
public static StateMod_OperationalRight_Metadata_DeliveryMethodType valueOfIgnoreCase(String name)
{
	StateMod_OperationalRight_Metadata_DeliveryMethodType [] values = values();
    // Currently supported values
    for ( StateMod_OperationalRight_Metadata_DeliveryMethodType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}