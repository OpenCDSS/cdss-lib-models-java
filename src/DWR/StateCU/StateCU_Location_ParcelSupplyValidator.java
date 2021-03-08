// StateCU_ParcelSupply_Validator - class to hold StateCU Parcel supply validator

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

NoticeEnd
*/

package DWR.StateCU;

import java.util.List;

import RTi.Util.Message.Message;

/**
 * Validator for StateCU parcel supply data.
 * @author sam
 *
 */
public class StateCU_Location_ParcelSupplyValidator implements StateCU_ComponentValidator {
	
	/**
	 * StateCU_Location to validate parcel supply data.
	 */
	//private StateCU_Location culoc = null;

	/**
	 * StateCU_Location list used to validate parcel supply data.
	 */
	private List<StateCU_Location> culocList = null;
	
	/**
	 * Constructor for the validator.
	 * @param culocList a list of all CU Locations for deep check.
	 */
	public StateCU_Location_ParcelSupplyValidator ( List<StateCU_Location> culocList) {
		this.culocList = culocList;
	}

	/**
	Performs specific data checks across all locations and returns a list of data that failed the data checks.
	This method should be called once, such as with the first CropPatternTS checked.
	@param dataset StateCU dataset currently in memory.
	@return Validation results.
	*/
	public StateCU_ComponentValidation validateAllComponentData( StateCU_DataSet dataset ) {
		String routine = getClass().getSimpleName() + ".validateAllComponentData";
		StateCU_ComponentValidation validation = new StateCU_ComponentValidation();
	
		for ( StateCU_Location culoc : this.culocList ) {
			int unknownCount = 0; // CDS:UNK and no set or fill
			int supplyCount = 0;
			//int yearPrev = -1;
			boolean locHasSet, locHasFill;
			for ( StateCU_Parcel parcel : culoc.getParcelList() ) {
				// Location does not have StateDMI set command for crop pattern time series.
				// - check for CDS:UNK to indicate that crop pattern data have not been read or set
				// Get all supplies for the parcel
				locHasSet = culoc.hasSetCropPatternTSCommands(parcel.getYear());
				locHasFill = culoc.hasFillCropPatternTSCommands(parcel.getYear());
				for ( StateCU_Supply supply : parcel.getSupplyList() ) {
					++supplyCount;
					if ( supply.getIncludeParcelInCdsType() == IncludeParcelInCdsType.UNKNOWN) {
						if ( !locHasSet && !locHasFill ) {
							++unknownCount;
							Message.printStatus(2, routine, "Location " + culoc.getID() + " year " + parcel.getYear() + " parcelId=" +
								parcel.getID() + " has CDS:UNK and no Set or Fill command.");
						}
					}
				}
				/* TODO smalers 2021-02-27 remove when checks out
				if ( culoc.hasSetCropPatternTSCommands(parcel.getYear()) ) {
					// Location has StateDMI set command for crop pattern time series.
					// - therefore CDS:UNK does not matter
					if ( parcel.getYear() != yearPrev ) {
						Message.printStatus(2, routine, "Location " + culoc.getID() + " year " + parcel.getYear() + " has set command.");
						yearPrev = parcel.getYear();
					}
				}
				else {
					// Location does not have StateDMI set command for crop pattern time series.
					// - check for CDS:UNK to indicate that crop pattern data have not been read or set
					// Get all supplies for the parcel
					for ( StateCU_Supply supply : parcel.getSupplyList() ) {
						if ( supply.getIncludeParcelInCdsType() == IncludeParcelInCdsType.UNKNOWN) {
							++unknownCount;
							Message.printStatus(2, routine, "Location " + culoc.getID() + " year " + parcel.getYear() + " parcelId=" +
								parcel.getID() + " has CDS:UNK and no set command.");
						}
					}
				}
				*/
			}
			/*
			if ( unknownCount > 0 ) {
				validation.add(new StateCU_ComponentValidationProblem(this,
					"Location " + culoc.getID() + " has parcel supplies that are not fully evaluated for crop pattern time series (CDS:UNK in parcel report file).",
					"Confirm that location has been processed with ReadParcelsFromHydroBase() or has a SetCropPatternTS*() command.") );
			}
			*/
			if ( unknownCount == supplyCount ) {
				validation.add(new StateCU_ComponentValidationProblem(this,
					"Location " + culoc.getID() + " ALL parcel supplies have CDS:UNK in parcel report file.",
					"Confirm that location has been processed with ReadParcelsFromHydroBase() or has a SetCropPatternTS*() or FillCropPatternTS*() command.") );
			}
		}

		return validation;
	}

	/**
	Performs specific data checks and returns a list of data that failed the data checks.
	This currently does nothing.
	@param dataset StateCU dataset currently in memory.
	@return Validation results.
	*/
	public StateCU_ComponentValidation validateComponent( StateCU_DataSet dataset ) {
		return new StateCU_ComponentValidation();
	}
}