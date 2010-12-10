package DWR.StateMod;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/* TODO SAM 2010-12-09 Evaluate whether want to read from Excel
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
*/

import RTi.Util.Message.Message;

/**
Class to hold operational right metadata, which is normally read from the OperatingRules.xls file.
*/
public class StateMod_OperationalRight_Metadata
{
	
/**
List of static global metadata, meant to be read once and shared within the application.
*/
private static List<StateMod_OperationalRight_Metadata> __opRightsMetadataList = new Vector();

/**
Read the global public metadata.
*/
public static void readGlobalData ( String filename )
throws FileNotFoundException, IOException
{
	//List<StateMod_OperationalRight_Metadata> metadataList = readSpreadsheet ( filename );
	//setGlobalMetadata ( metadataList );
}
	
/**
Read the contents of the operating rule definitions file.
@param filename name of file to read (absolute path).
@return a list of StateMod_OperationalRights_Metadata from the definitions file.
*/
/*
public static List readSpreadsheet ( String filename )
throws FileNotFoundException, IOException
{	String routine = "StateMod_OperationalRight_Metadata.readSpreadsheet";
	FileInputStream fis = null;
	List metadataList = new Vector();
	try {
		POIFSFileSystem fs = null;
		fs = new POIFSFileSystem(fis = new FileInputStream(filename) );
		// Create a workbook (why not just get it from the fs?)
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		// Get the first sheet from the workbook
		HSSFSheet mySheet = wb.getSheetAt(0);
		// Iterate through the rows and cells
		Iterator rowIter = mySheet.rowIterator(); 
		while(rowIter.hasNext()){
			  HSSFRow myRow = (HSSFRow)rowIter.next();
			  Iterator cellIter = myRow.cellIterator();
			  Vector cellStoreVector=new Vector();
			  while(cellIter.hasNext()) {
				  HSSFCell myCell = (HSSFCell) cellIter.next();
				  cellStoreVector.addElement(myCell);
				  Message.printStatus(2,routine, "Row " + myRow.getRowNum() + " Cell " + myCell.getColumnIndex() +
					  " value=" + myCell );
			  }
		 }
	}
	finally {
		if ( fis != null ) {
			fis.close();
		}
	}
	return metadataList;
}
*/

/**
Set the global metadata list.
*/
private static void setGlobalMetadata ( List<StateMod_OperationalRight_Metadata> metadataList )
{
	__opRightsMetadataList = metadataList;
}

}