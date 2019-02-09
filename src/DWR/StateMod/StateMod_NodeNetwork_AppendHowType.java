// StateMod_NodeNetwork_AppendHowType - enumeration to store values for how a network can be appended to another.

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Models Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Models Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Models Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package DWR.StateMod;

/**
This enumeration stores values for how a network can be appended to another.
*/
public enum StateMod_NodeNetwork_AppendHowType
{
    /**
     * Add the appended network upstream of the existing downstream node (leaving existing nodes upstream
     * of the existing downstream node).
     */
    ADD_UPSTREAM_OF_DOWNSTREAM("AddUpstreamOfDownstream"),
    /**
     * Replace the downstream node with the upstream appended network.
     */
    // TODO SAM 2011-01-05 Enable this later
    //REPLACE_DOWNSTREAM_WITH_UPSTREAM("ReplaceDownstreamWithUpstream"),
    /**
     * Add the appended network upstream of the existing downstream node (replacing all nodes upstream
     * of the existing downstream node).
     */
    REPLACE_UPSTREAM_OF_DOWNSTREAM("ReplaceUpstreamOfDownstream"),;
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;
    
    /**
     * Construct an enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private StateMod_NodeNetwork_AppendHowType(String displayName) {
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
public static StateMod_NodeNetwork_AppendHowType valueOfIgnoreCase(String name)
{	if ( name == null ) {
		return null;
	}
	StateMod_NodeNetwork_AppendHowType [] values = values();
    // Currently supported values
    for ( StateMod_NodeNetwork_AppendHowType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}
