package DWR.StateMod;

import java.util.Comparator;

/**
This class compares StateMod_Right instances to allow for sorting.
*/
public class StateMod_Right_Comparator implements Comparator
{
	
/**
Sort field and order - sort right ID in ascending order.
*/
public static final int IDAscending = 0;

/**
Sort field and order - sort location ID in ascending order.
*/
public static final int LocationIDAscending = 1;

private int __order = IDAscending;
private int __order2 = -1;
	
/**
Compare two StateMod_Right according to the orders that have been set.
See setOrder() and setOrder2().
*/
public int compare ( Object o1, Object o2 )
{	StateMod_Right right1 = (StateMod_Right)o1;
	StateMod_Right right2 = (StateMod_Right)o2;
	String id1 = right1.getIdentifier();
	String id2 = right2.getIdentifier();
	String locid1 = right1.getLocationIdentifier();
	String locid2 = right2.getLocationIdentifier();
	int compare_result = 0;	// Result of compare
	// Do the first level check...
	if ( __order == IDAscending ) {
		compare_result = id1.compareTo(id2);
	}
	else if ( __order == LocationIDAscending ) {
		compare_result = locid1.compareTo(locid2);
	}
	if ( compare_result != 0 ) {
		return compare_result;
	}
	// Do the second level check...
	if ( __order2 >= 0 ) {
		if ( __order2 == IDAscending ) {
			compare_result = id1.compareTo(id2);
		}
		else if ( __order2 == LocationIDAscending ) {
			compare_result = locid1.compareTo(locid2);
		}
		if ( compare_result != 0 ) {
			return compare_result;
		}
	}
	// Do the third level check (always sort by administration number)...
	String adminnum1 = right1.getAdministrationNumber();
	String adminnum2 = right2.getAdministrationNumber();
	compare_result = adminnum1.compareTo(adminnum2);
	if ( compare_result != 0 ) {
		return compare_result;
	}
	// Do the fourth level check (always sort by decree)...
	double decree1 = right1.getDecree();
	double decree2 = right2.getDecree();
	if ( decree1 < decree2 ) {
		return -1;
	}
	else if ( decree1 > decree2 ) {
		return 1;
	}
	else {
		return 0;
	}
}

/**
Set the first sort order criteria for comparison.
*/
public void setOrder ( int order )
{	__order = order;
}

/**
Set the second sort order criteria for comparison.
*/
public void setOrder2 ( int order )
{	__order2 = order;
}

}
