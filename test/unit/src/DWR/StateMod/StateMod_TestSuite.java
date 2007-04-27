package DWR.StateMod;

import java.io.File;
import java.util.ArrayList;
import RTi.Util.Test.TestCollector;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

public class StateMod_TestSuite extends TestCase {

    private static ArrayList testList;
    
    public StateMod_TestSuite(String testname)
    {
        super(testname);
    }

    public StateMod_TestSuite()
    {   
    }   
    
    public static Test suite() throws ClassNotFoundException
    {
        testList = new ArrayList();
        TestSuite suite = new TestSuite();
        TestCollector tests = new TestCollector();
        File path = new File("test\\unit\\src");
        System.out.println(path.toString());
        tests.visitAllFiles(path);
        testList = tests.getTestList();
        
        for(int i = 0; i < testList.size(); i++)
        {
            String testName = (testList.get(i).toString());
            System.out.println(testName);
            String test = tests.formatFileName(testName);
            suite.addTestSuite(Class.forName(test));
        }
        
        return suite;
    } 
}
