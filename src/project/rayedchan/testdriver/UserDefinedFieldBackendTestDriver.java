package project.rayedchan.testdriver;

import Thor.API.Operations.tcLookupOperationsIntf;
import com.thortech.xl.dataaccess.tcDataSetException;
import com.thortech.xl.orb.dataaccess.tcDataAccessException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import javax.security.auth.login.LoginException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import oracle.iam.identity.exception.UserSearchException;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.authz.exception.AccessDeniedException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import project.rayedchan.services.tcOIMDatabaseConnection;
import project.rayedchan.utilities.HelperUtility;
import project.rayedchan.utilities.BackendUDFUtility;

/**
 * @author rayedchan
 * This is a test driver class to generate the UDF XML MetaData file. 
 * The UDF information is supplied by a CSV file from user.  
 * 
 * The generated XML file can be imported into OIM via Deployment Manager. 
 * OIM will handle the column creation in the backend UDF (E.g. columns in OIM.USR Table)
 */
public class UserDefinedFieldBackendTestDriver 
{
    // OIM Environment specific variables
    public static final String OIM_HOSTNAME = "localhost";
    public static final String OIM_PORT = "14000";
    public static final String OIM_PROVIDER_URL = "t3://"+ OIM_HOSTNAME + ":" + OIM_PORT;
    public static final String OIM_USERNAME = "xelsysadm";
    public static final String OIM_PASSWORD = "Password1";
    public static final String OIM_CLIENT_HOME = "/home/oracle/Desktop/oimclient";
    public static final String AUTHWL_PATH = OIM_CLIENT_HOME + "/conf/authwl.conf";
    
    // Path of generated XML to be output to
    public static final String DESTINATION_PATH_OF_UDF_METADATA = "/home/oracle/Desktop/udf_util.xml";
    
    // Path of the CSV 
    public static final String SOURCE_PATH_OF_CSV_FILE = "/home/oracle/NetBeansProjects/OIMUserDefinedFieldUtility/sample_data/UDF_Metadata/csv/sample_UDFs_2.csv";
    
    public static void main(String[] args) throws LoginException, AccessDeniedException, UserSearchException, ParserConfigurationException, TransformerConfigurationException, TransformerException, XPathExpressionException, tcDataSetException, tcDataAccessException, IOException, SAXException 
    {
        OIMClient oimClient = null;
        tcLookupOperationsIntf lookupOps = null;
        CSVParser parser = null;
        
        try
        {
            // Set system properties required for OIMClient
            System.setProperty("java.security.auth.login.config", AUTHWL_PATH);
            System.setProperty("APPSERVER_TYPE", "wls");  
 
            // Create an instance of OIMClient with OIM environment information 
            Hashtable env = new Hashtable();
            env.put(OIMClient.JAVA_NAMING_FACTORY_INITIAL, "weblogic.jndi.WLInitialContextFactory");
            env.put(OIMClient.JAVA_NAMING_PROVIDER_URL, OIM_PROVIDER_URL);
            oimClient = new OIMClient(env);
 
            // Log in to OIM with the approriate credentials
            oimClient.login(OIM_USERNAME, OIM_PASSWORD.toCharArray());
            
            // OIM API services
            lookupOps = oimClient.getService(tcLookupOperationsIntf.class);
                 
            // Connect OIM Schema through OIM Client
            tcOIMDatabaseConnection dbConnection = new tcOIMDatabaseConnection(oimClient);
            
            // Objects for parsing CSV file 
            CSVFormat format = CSVFormat.DEFAULT.withHeader().withDelimiter(',');
            parser = new CSVParser(new FileReader(SOURCE_PATH_OF_CSV_FILE), format);
            
            // Create UDFUtility Object
            BackendUDFUtility udfUtil = new BackendUDFUtility();
            
            // Get the final UDF document
            Document doc = udfUtil.buildUDFDocument(dbConnection, lookupOps, parser);
            
            // Create XML file
            HelperUtility.createXMLFile(doc, DESTINATION_PATH_OF_UDF_METADATA);

            // Print Document object to String
            //System.out.println(HelperUtility.parseDocumentIntoStringXML(doc));
        } 
            
        finally
        {
            if(lookupOps != null)
                lookupOps.close();
            
            // Logout user from OIMClient
            if(oimClient != null)
                oimClient.logout();
            
            if(parser != null)
                parser.close();
        }
 
    }
}
