package DWR.StateMod;

import java.util.Random;
import java.util.Vector;

import junit.framework.TestCase;

public class StateMod_ComponentDataCheckTest extends TestCase 
{
	private RTi.Util.IO.CheckFile __check_file;
	private StateMod_DataSet __data_set;
	
public void setup() 
{
	__check_file = new RTi.Util.IO.CheckFile("myCheckFile", "commands");
	__data_set = new StateMod_DataSet();
}
	
public void testCheckForMissingData() 
{
	// setup test object
	StateMod_ComponentDataCheck testObj = new StateMod_ComponentDataCheck(
		StateMod_DataSet.COMP_WELL_STATIONS, __check_file, __data_set );
	
	Vector orig = new Vector();
	int numMissing = 3;
	orig.add( getStringList(  true ) );
	orig.add( getStringList(  true ) );
	orig.add( getStringList(  true ) );
	orig.add( getStringList(  false ) );
	orig.add( getStringList(  false ) );
	//Vector missing = testObj.checkForMissingData( orig );
	//assertEquals( numMissing, missing.size() );
	
	orig.clear();
	numMissing = 0;
	orig.add( getStringList(  false ) );
	//missing = testObj.checkForMissingData( orig );
	//assertEquals( 0, missing.size() );
	
	orig.clear();
	numMissing = 0;
	//missing = testObj.checkForMissingData( null );
	//assertEquals( null, missing );
	
}


private String[] getStringList( boolean missing )
{
	Random generator = new Random( 19580427 );
	if ( missing ) {
		return new String[] { 
			new Double(generator.nextDouble() * -1).toString(),
			"",
			new Double(generator.nextDouble() * -1).toString(),
			new Integer(generator.nextInt()).toString(),
			""};
	}
	else {
		return new String[] {
			new Integer(generator.nextInt()).toString(),
			"Valid String",
			new Double(generator.nextDouble()).toString(),
			"Another Valid String",
			new Double(generator.nextDouble()).toString()};
	}
}


}
