package DWR.StateMod;

/**
This interface describes general behavior for a StateMod water right,
to allow generalized handling of water rights.
*/
public interface StateMod_Right {

/**
Return the administration number.
@return the administration number as a String, to preserve exact
precision.
*/
public String getAdministrationNumber();

/**
Return the decree amount.
@return the water right decree amount, in units for the data.
*/
public double getDecree();

/**
Return the units for the decree;
*/
public String getDecreeUnits();

/**
Return the right identifier.
@return the right identifier.
*/
public String getIdentifier();

/**
Return the right location identifier.
@return the right location identifier.
*/
public String getLocationIdentifier();

/**
Return the right name.
@return the right name.
*/
public String getName();

/**
Return the on/off switch.
@return the on/off switch.
*/
public int getSwitch();

/**
Set the decree amount.
*/
public void setDecree(double decree);

}
