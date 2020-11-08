// StateCU_Parcel_Comparator - compare StateCU_Parcel for sorting

/* NoticeStart

CDSS Models Java Library
CDSS Models Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2020 Colorado Department of Natural Resources

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

package DWR.StateCU;

import java.util.Comparator;

public class StateCU_Parcel_Comparator implements Comparator<StateCU_Parcel> {
	
	public static int YEAR_DIVISION_DISTRICT_PARCELID = 1;
	public static int YEAR_PARCELID = 1;
	
	private int sortOrder = 0;
	
	/**
	 * Constructor.
	 * @param sortOrder order to sort the parcels
	 */
	public StateCU_Parcel_Comparator( int sortOrder ) {
		this.sortOrder = sortOrder;
	}

	/**
	 * Compare two StateCU_Parcel, sorting by year, division, district, parcel ID
	 * @param a first StateCU_Parcel to compare
	 * @param b second StateCU_Parcel to compare
	 * @return -1 if a < b, 1 if a > b, and 0 if a == b
	 */
	public int compare ( StateCU_Parcel a, StateCU_Parcel b ) {
		if ( this.sortOrder == YEAR_DIVISION_DISTRICT_PARCELID ) {
			if ( a.getYear() < b.getYear() ) {
				return -1;
			}
			else if ( a.getYear() > b.getYear() ) {
				return 1;
			}
			else {
				// Year is the same so need to compare division
				if ( a.getDiv() < b.getDiv() ) {
					return -1;
				}
				else if ( a.getDiv() > b.getDiv() ) {
					return 1;
				}
				else {
					// Division is the same so need to compare district
					if ( a.getWD() < b.getWD() ) {
						return -1;
					}
					else if ( a.getWD() > b.getWD() ) {
						return 1;
					}
					else {
						// Water district is the same so need to compare parcel ID
						if ( a.getIdInt() < b.getIdInt() ) {
							return -1;
						}
						else if ( a.getIdInt() > b.getIdInt() ) {
							return 1;
						}
						else {
							// Everything is the same
							return 0;
						}
					}
				}
			}
		}
		else if ( this.sortOrder == YEAR_PARCELID ) {
			if ( a.getYear() < b.getYear() ) {
				return -1;
			}
			else if ( a.getYear() > b.getYear() ) {
				return 1;
			}
			else {
				// Year is the same so need to parcel ID 
				if ( a.getIdInt() < b.getIdInt() ) {
					return -1;
				}
				else if ( a.getIdInt() > b.getIdInt() ) {
					return 1;
				}
				else {
					// Everything is the same
					return 0;
				}
			}
		}
		else {
			throw new RuntimeException ( "Unrecognized parcel sort order: " + this.sortOrder );
		}
	}

}