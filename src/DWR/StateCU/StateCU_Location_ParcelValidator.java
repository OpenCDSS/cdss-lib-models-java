// StateCU_Parcel_Validator - class to hold StateCU Parcel validator

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

/**
 * Validator for StateCU parcel data.
 * @author sam
 *
 */
public class StateCU_Location_ParcelValidator implements StateCU_ComponentValidator {
	
	/**
	 * StateCU_Location to validate parcel data.
	 */
	private StateCU_Location culoc = null;

	/**
	 * StateCU_Location list used to validate parcel data.
	 */
	private List<StateCU_Location> culocList = null;
	
	/**
	 * Whether checks are deep, and slower.
	 */
	private boolean deepCheck = false;

	/**
	 * Constructor for the validator.
	 * @param culoc CU Location for which to check parcels.
	 * @param culocList a list of all CU Locations for deep check.
	 * @param deepCheck whether to perform deep checks, including confirming that parcel only shows up in
	 * one 
	 */
	public StateCU_Location_ParcelValidator ( StateCU_Location culoc, List<StateCU_Location> culocList, boolean deepCheck ) {
		this.culoc = culoc;
		this.culocList = culocList;
		this.deepCheck = deepCheck;
	}

	/**
	Performs specific data checks and returns a list of data that failed the data checks.
	@param dataset StateCU dataset currently in memory.
	@return Validation results.
	*/
	public StateCU_ComponentValidation validateComponent( StateCU_DataSet dataset ) {
		StateCU_ComponentValidation validation = new StateCU_ComponentValidation();
		// Make sure that there is only one surface supply
		// - otherwise MultiStruct could be used?
		String id = this.culoc.getID();
	
		List<StateCU_Parcel> parcelList = culoc.getParcelList();
		int parcelYear;
		// All years.
		int swSupplyCountAll = 0;
		int gwSupplyCountAll = 0;
		// For only one year.
		int swSupplyCount = 0;
		int gwSupplyCount = 0;
		for ( StateCU_Parcel parcel : parcelList ) {
			// This is for a specific year
			parcelYear = parcel.getYear();
			swSupplyCount = parcel.getSupplyFromSWCount();
			gwSupplyCount = parcel.getSupplyFromGWCount();
			swSupplyCountAll += swSupplyCount;
			gwSupplyCountAll += gwSupplyCount;

			if ( (swSupplyCount == 0) && (gwSupplyCount == 0) ) {
				validation.add(new StateCU_ComponentValidationProblem(this,
					"CU location \"" + id + "\" year " + parcelYear + " has 0 supplies.  At least 1 supply is required.",
					"Check irrigated parcels data.  Should not be included in irrigated acres?") );
			}

			if ( swSupplyCount > 1 ) {
				// Do a check to make sure that sum of calculated ditch irrigated acres using percent_irrig is <= the parcel area
				// TODO smalers 2020-10-12 if fails, need to check whether CULocation is a MultiStruct?
				double swSupplyArea = 0.0;
				for ( StateCU_Supply supply : parcel.getSupplyList() ) {
					if ( supply instanceof StateCU_SupplyFromSW ) {
						swSupplyArea += ((StateCU_SupplyFromSW)supply).getAreaIrrig();
					}
				}
				double parcelAreaMin = parcel.getArea()*.99999;
				double parcelAreaMax = parcel.getArea()*1.00001;
				if ( (swSupplyArea < parcelAreaMin) || (swSupplyArea > parcelAreaMax) ) {
					validation.add(new StateCU_ComponentValidationProblem(this,
						"CU location \"" + id + "\" year " + parcelYear + " parcel " + parcel.getID() +
						" has " + swSupplyCount + " surface water supplies and sum of fractional areas from ditch supplies (" +
						swSupplyArea + ") does not equal parcel area (" + parcel.getArea() + ").",
						"Check irrigated parcels data.") );
					if ( gwSupplyCount > 0 ) {
						// Could be more of an issue since SW acres control and also have groundwater
						validation.add(new StateCU_ComponentValidationProblem(this,
							"Also have groundwater at this location but SW acres are used - could be an issue.",
							"Check irrigated parcels data.") );
					}
				}
			}
		}

		if ( (swSupplyCountAll == 0) && (gwSupplyCountAll == 0) ) {
			validation.add(new StateCU_ComponentValidationProblem(this,
				"CU location \"" + id + "\" has parcels with 0 supplies for all years.  At least 1 supply is required.",
				"Check irrigated parcels data.  Should not be included as irrigated location?") );
		}
		
		if ( this.deepCheck ) {
			// Loop through all parcels in the dataset and make sure that the same parcel does
			// not show up another CU location.
			// - require the year to also be the same, although not sure this is required for uniqueness
			for ( StateCU_Parcel parcel : parcelList ) {
				for ( StateCU_Location culoc2 : this.culocList ) {
					if ( culoc.getID().equals(culoc2.getID()) ) {
						// Don't compare the same parcel 
						continue;
					}
					for ( StateCU_Parcel parcel2 : culoc2.getParcelList() ) {
						if ( parcel.getYear() == parcel2.getYear() ) {
							// Year is the same.
							if ( parcel.getID().equals(parcel2.getID()) ) {
								// Parcel ID is also the same.
								validation.add(new StateCU_ComponentValidationProblem(this,
									"CU locations \"" + id + "\" and \"" + culoc2.getID() +
									"\" have parcels with same year (" + parcel.getYear() + ") and parcel ID (" + parcel.getID() + ").",
									"Check irrigated parcels data.  Check collection assignments.") );
							}
						}
					}
				}
			}
		}

		return validation;
	}

}