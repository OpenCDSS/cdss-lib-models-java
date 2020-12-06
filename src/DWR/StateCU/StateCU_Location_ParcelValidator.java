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
	 * Precision (number of digits) for area comparison checks.
	 * The modeler may need to change depending on how roundoff impacts the numbers.
	 */
	private int areaPrecision = 3;

	/**
	 * Constructor for the validator.
	 * @param culoc CU Location for which to check parcels.
	 * @param culocList a list of all CU Locations for deep check.
	 * @param deepCheck whether to perform deep checks, currently disabled
	 * @param areaPrecision number of digits for area checks
	 */
	public StateCU_Location_ParcelValidator ( StateCU_Location culoc, List<StateCU_Location> culocList,
		boolean deepCheck, int areaPrecision ) {
		this.culoc = culoc;
		this.culocList = culocList;
		this.deepCheck = deepCheck;
		this.areaPrecision = areaPrecision;
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
		int swSupplyCountAllYears = 0;
		int gwSupplyCountAllYears = 0;
		// For only one year.
		int swSupplyCount = 0;
		int gwSupplyCount = 0;
		String areaFormat = "%." + this.areaPrecision + "f";
		StateCU_SupplyFromSW supplyFromSW;
		for ( StateCU_Parcel parcel : parcelList ) {
			// A parcel corresponds to a specific year
			parcelYear = parcel.getYear();
			swSupplyCount = parcel.getSupplyFromSWCount();
			gwSupplyCount = parcel.getSupplyFromGWCount();
			swSupplyCountAllYears += swSupplyCount;
			gwSupplyCountAllYears += gwSupplyCount;

			if ( this.culoc.hasGroundwaterOnlySupply() && parcel.hasSurfaceWaterSupply() ) {
				// By definition groundwater only does not include parcels with surface water supply so don't check.
				continue;
			}
			if ( (swSupplyCount == 0) && (gwSupplyCount == 0) ) {
				validation.add(new StateCU_ComponentValidationProblem(this,
					"CU location \"" + id + "\" year " + parcelYear + " has 0 supplies.  At least 1 supply is required.",
					"Check irrigated parcels data.  Should not be included in irrigated acres?") );
			}

			if ( swSupplyCount > 0 ) {
				// Do a check to make sure that sum of calculated ditch irrigated acres using percent_irrig is the
				// same as the parcel area.
				// - TODO smalers 2020-10-12 if fails, need to check whether CULocation is a MultiStruct
				//   with more than one one ditch supplying a parcel?
				double swSupplyArea = 0.0;
				for ( StateCU_Supply supply : parcel.getSupplyList() ) {
					if ( supply instanceof StateCU_SupplyFromSW ) {
						supplyFromSW = (StateCU_SupplyFromSW)supply;
						swSupplyArea += supplyFromSW.getAreaIrrig();

						double areaIrrigFraction = supplyFromSW.getAreaIrrigFraction();
						double areaIrrigFractionHydroBase = supplyFromSW.getAreaIrrigFractionHydroBase();
						if ( !String.format(areaFormat, areaIrrigFraction).equals(String.format(areaFormat, areaIrrigFractionHydroBase))) {
							validation.add(new StateCU_ComponentValidationProblem(this,
								"CU location \"" + id + "\" year " + parcelYear + " parcel " + parcel.getID() +
								" supply ID \"" + supply.getID() + "\" parcel area fraction from ditch supply number of ditches(" +
								String.format(areaFormat, areaIrrigFraction) + ") does not equal parcel area fraction from HydroBase (" +
								String.format(areaFormat, areaIrrigFractionHydroBase) + ").",
								"Check irrigated parcels data and supply assignment.") );
							supplyFromSW.setAreaIrrigFractionHydroBaseError("ERROR");
						}
						// TODO smalers 2020-11-08 the following ws redundant - remove when the above checks out
						if ( !String.format(areaFormat, supplyFromSW.getAreaIrrigFraction()).equals(
							String.format(areaFormat, supplyFromSW.getAreaIrrigFractionHydroBase())) ) {
							//Message.printWarning(3, "", "Calculated supply fraction is " + supplyFromSW.getAreaIrrigFraction() +
							//	" HydroBase fraction is " + supplyFromSW.getAreaIrrigFractionHydroBase() );
							//supplyFromSW.setAreaIrrigFractionHydroBaseError("ERROR");
						}
					}
				}
				// Use formatted strings to compare since roundoff can be an issue
				// TODO smalers 2020-11-07 tried to use 3 digits but see a difference in .001 so use precision from command parameter.
				if ( !String.format(areaFormat, parcel.getArea()).equals(String.format(areaFormat, swSupplyArea)) ) {
					validation.add(new StateCU_ComponentValidationProblem(this,
						"CU location \"" + id + "\" year " + parcelYear + " parcel " + parcel.getID() +
						" has " + swSupplyCount + " surface water supplies across locations (" + parcel.getModelIdListString() +
						") and sum of fractional areas from ditch supplies (" +
						String.format(areaFormat, swSupplyArea) + ") does not equal parcel area (" +
						String.format(areaFormat, parcel.getArea()) + ").",
						"Check irrigated parcels data and supply assignment.") );
					if ( gwSupplyCount > 0 ) {
						// Possible issue since SW acres control and also have groundwater
						validation.add(new StateCU_ComponentValidationProblem(this,
							"Also have groundwater at locations for parcel but SW acres (with warning) are used - total parcel area may not be properly handled.",
							"Check irrigated parcels data to ensure that parcel surface water supply acres match the total parcel area.") );
					}
							
					// TODO smalers 2020-11-05 old code that does not apply with new data management
					// - remove when the code checks out
					// It is possible that the parcel is associated with another model node so search for them
					// - only need to do this when the initial issue is detected because full search is slow
					// Still have an issue so add the validation warning.
					/*
					if ( (swSupplyArea < parcelAreaMin) || (swSupplyArea > parcelAreaMax) ) {
						String culocIDs = "";
						for ( int iculoc = 0; iculoc < parcel.getModelLocListSize(); iculoc++ ) {
							StateCU_Location culoc = parcel.getStateCULocation(iculoc);
							if ( culoc.getID().equals(culoc.getID())) {
								// Don't process the same CU location as itself
								continue;
							}
							// See if the CU location has a parcel that matches the one being validated.
							List<StateCU_Parcel> parcelList2 = culoc.getParcelList();
							boolean culocidAdded = false;
							for ( StateCU_Parcel parcel2 : parcelList2 ) {
								if ( (parcel.getYear() == parcel2.getYear()) && (parcel.getID().equals(parcel2.getID())) ) {
									// Same parcel in the other culoc so see if any surface water supply
									if ( !culocidAdded ) {
										// Only add the culoc ID once
										culocIDs += ", " + parcel2.getModelIdListString();
										culocidAdded = true;
									}
									for ( StateCU_Supply supply : parcel2.getSupplyList() ) {
										if ( supply instanceof StateCU_SupplyFromSW ) {
											++swSupplyCount;
											swSupplyArea += ((StateCU_SupplyFromSW)supply).getAreaIrrig();
										}
									}
									// Increment the groundwater supply count
									gwSupplyCount += parcel2.getSupplyFromGWCount();
								}
							}
						}
						if ( (swSupplyArea < parcelAreaMin) || (swSupplyArea > parcelAreaMax) ) {
							validation.add(new StateCU_ComponentValidationProblem(this,
								"CU location \"" + id + "\" year " + parcelYear + " parcel " + parcel.getID() +
								" has " + swSupplyCount + " surface water supplies across locations (" + culocIDs +
								") and sum of fractional areas from ditch supplies (" +
								swSupplyArea + ") does not equal parcel area (" + parcel.getArea() + ").",
								"Check irrigated parcels data.") );
							if ( gwSupplyCount > 0 ) {
								// Possible issue since SW acres control and also have groundwater
								validation.add(new StateCU_ComponentValidationProblem(this,
									"Also have groundwater at locations for parcel but SW acres (with warning) are used - total parcel area may not be properly handled.",
									"Check irrigated parcels data to ensure that parcel surface water supply acres match the total parcel area.") );
							}
						}
					}
					*/
				}
			}
		}

		if ( (swSupplyCountAllYears == 0) && (gwSupplyCountAllYears == 0) ) {
			// This should probably never happen.
			validation.add(new StateCU_ComponentValidationProblem(this,
				"CU location \"" + id + "\" has parcels with 0 supplies for all years.  At least 1 supply is required.",
				"Check irrigated parcels data.  Should not be included as irrigated location?") );
		}
		
		// TODO smalers 2020-11-05 Disable the following for now
		// - based on conversations with Kara Sobieski, ArkDSS data, and current design these checks are not appropriate.
		// - a well might be included with a D&W and also separately in a Well-only list.
		boolean enableDeepCheck = false;
		if ( this.deepCheck && enableDeepCheck ) {
			// TODO smalers 2020-11-05 this check is actually not valid.  Only the above is valid.
			// Loop through all parcels in the dataset and make sure that the same parcel does
			// not show up under another CU location.
			// - require the year to also be the same, although not sure this is required for uniqueness
			boolean parcelHasGroundWaterSupply;
			boolean parcelHasSurfaceWaterSupply;
			boolean parcel2HasGroundWaterSupply;
			boolean parcel2HasSurfaceWaterSupply;
			for ( StateCU_Parcel parcel : parcelList ) {
				parcelHasGroundWaterSupply = parcel.hasGroundWaterSupply();
				parcelHasSurfaceWaterSupply = parcel.hasSurfaceWaterSupply();
				for ( StateCU_Location culoc2 : this.culocList ) {
					if ( culoc.getID().equals(culoc2.getID()) ) {
						// Don't compare the same parcel 
						continue;
					}
					for ( StateCU_Parcel parcel2 : culoc2.getParcelList() ) {
						parcel2HasGroundWaterSupply = parcel2.hasGroundWaterSupply();
						parcel2HasSurfaceWaterSupply = parcel2.hasSurfaceWaterSupply();
						if ( parcel.getYear() == parcel2.getYear() ) {
							// Year is the same.
							if ( parcel.getID().equals(parcel2.getID()) ) {
								// Parcel ID is the same.
								// Only an issue if parcel has only groundwater supply in both cases,
								// or only surface supply in both cases.
								if ( parcelHasGroundWaterSupply && parcel2HasGroundWaterSupply && !parcelHasSurfaceWaterSupply && !parcel2HasSurfaceWaterSupply ) {
									validation.add(new StateCU_ComponentValidationProblem(this,
										"CU locations \"" + id + "\" and \"" + culoc2.getID() +
										"\" have parcel with same year (" + parcel.getYear() + "), parcel ID (" + parcel.getID() +
										") and both only have groundwater supply.",
										"Check irrigated parcels data.  Check collection assignments.  "
										+ "A parcel can only be in two model nodes if one of the nodes has "
										+ "commingled groundwater supply and the other is groundwater-only supply.") );
								}
								/* TODO smalers 2020-10-17 Leave this out for now
								 * Surface water supply only is OK if the parts add up to parcel total.
								else if ( !parcelHasGroundWaterSupply && !parcel2HasGroundWaterSupply && parcelHasSurfaceWaterSupply && parcel2HasSurfaceWaterSupply ) {
									// OK i
									validation.add(new StateCU_ComponentValidationProblem(this,
										"CU locations \"" + id + "\" and \"" + culoc2.getID() +
										"\" have parcel with same year (" + parcel.getYear() + "), parcel ID (" + parcel.getID() +
										") and both only have surface water supply.",
										"Check irrigated parcels data.  Check collection assignments.  "
										+ "A parcel can only be in two model nodes if one of the nodes has "
										+ "commingled groundwater supply and the other is groundwater-only supply.") );
								}
								*/
							}
						}
					}
				}
			}
		}

		return validation;
	}

}