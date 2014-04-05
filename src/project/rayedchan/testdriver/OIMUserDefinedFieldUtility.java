package project.rayedchan.testdriver;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Operations.tcLookupOperationsIntf;
import Thor.API.tcResultSet;
import com.thortech.xl.dataaccess.tcDataSetException;
import com.thortech.xl.orb.dataaccess.tcDataAccessException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import javax.security.auth.login.LoginException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;
import oracle.iam.configservice.api.Constants;
import oracle.iam.configservice.vo.AttributeDefinition;
import oracle.iam.identity.exception.UserSearchException;
import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.authz.exception.AccessDeniedException;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import project.rayedchan.services.tcOIMDatabaseConnection;
import project.rayedchan.utilities.UDFUtility;

/**
 * @author rayedchan
 * This class reads from a CSV file and generates a UDF XML file to
 * be used from OIM Deployment Manager.
 * This utility does not support creation of encrypted UDFs.
 * Use standard convention by having USR_UDF_ as the prefix for each backend attribute name.
 * Side Notes: 
 * OIM does not allow you to export encrypted UDFs.
 * Length of OIM column name must be less than or equal to 30.
 * You can only have 22 characters or less for API Name when adding 
 * through OIM console. OIM will prefix with USR_UDF_ on the backend column name.
 */
public class OIMUserDefinedFieldUtility 
{
    public static final String OIM_HOSTNAME = "localhost";
    public static final String OIM_PORT = "14000";
    public static final String OIM_PROVIDER_URL = "t3://"+ OIM_HOSTNAME + ":" + OIM_PORT;
    public static final String OIM_USERNAME = "xelsysadm";
    public static final String OIM_PASSWORD = "Password1";
    public static final String OIM_CLIENT_HOME = "/home/oracle/Desktop/oimclient";
    public static final String AUTHWL_PATH = OIM_CLIENT_HOME + "/conf/authwl.conf";
    public static final String DESTINATION_PATH_OF_UDF_METADATA = "/home/oracle/Desktop/udf_util.xml";
    public static final String SOURCE_PATH_OF_CSV_FILE = "/home/oracle/Desktop/sample_UDFs.csv";
    
    public static void main(String[] args) throws LoginException, AccessDeniedException, UserSearchException, ParserConfigurationException, TransformerConfigurationException, TransformerException, XPathExpressionException, tcDataSetException, tcDataAccessException, IOException 
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
            
            // Get all the columns in the USR table     
            Set<String> columnNames =  UDFUtility.getAllUSRColumns(dbConnection);
           
            // Used to determine if there are duplicates UDF in csv file
            Set<String> udfDuplicateValidator = new HashSet<String>();
           
            // Create base template document for a UDF
            Document doc = UDFUtility.createUDFDocument();
            
            // Objects for parsing CSV file 
            CSVFormat format = CSVFormat.DEFAULT.withHeader().withDelimiter(',');
            parser = new CSVParser(new FileReader(SOURCE_PATH_OF_CSV_FILE), format);
        
            // Iterate each entry in the csv file
            for(CSVRecord record : parser)
            {
                // Get values of a record from csv file; Change header value if different
                String attrNameCSV = record.get("UDF");
                String displayTypeCSV = record.get("Field Type").toUpperCase();
                String lengthCSV = record.get("Max Length");
                String searchableCSV = record.get("Searchable").toUpperCase();
                String lookupNameCSV = record.get("Lookup Table");
                Map entries = null;
                int attrNameLength = attrNameCSV.length();
                
                // Check if the attribute name, which is also the column name for this utility, is valid
                if(attrNameLength > 30 || attrNameLength < 1)
                {                
                    System.out.println("Backend column must have length 1 to 30 " + record);
                    continue; // Skip to next iteration
                }
                
                // Check if the column name in CSV file exists in the USR table
                if(columnNames.contains(attrNameCSV.toUpperCase()))
                {
                    System.out.println("UDF already exists in Identity: " + record);
                    continue; // Skip to next iteration
                }
                
                // Check if current attribute name has already processed
                if(udfDuplicateValidator.contains(attrNameCSV))
                {
                    System.out.println("The attribute name has already been staged: " + record);
                    continue; // Skip to next iteration
                }
            
                // Check if the display type is valid
                if(!UDFUtility.isUDFDisplayTypeValidate(displayTypeCSV))
                {                 
                    System.out.println("Invalid display type: " + record);
                    continue; // Skip to next iteration
                }
                
                // Check if length is an integer
                if(!UDFUtility.isInteger(lengthCSV))
                {
                    System.out.println("Length is not a valid number: " + record);
                    continue; // Skip to next iteration                   
                }
                
                // Check if length is between 1 to 4000
                int lengthSize = Integer.parseInt(lengthCSV);
                if(lengthSize > 4000 || lengthSize < 1)
                {
                    System.out.println("Length must be between 1 and 4000: " + record);
                    continue; // Skip to next iteration 
                }

                // Check if searchable has a valid value
                if(!UDFUtility.isSearchableValidValue(searchableCSV))
                {                 
                    System.out.println("Searchable is not a valid: " + record);
                    continue; // Skip to next iteration
                }
                
                // For LOV display type, get lookup entries
                if(Constants.DisplayType.LOV.name().equals(displayTypeCSV))
                {
                    try
                    {
                        // Calling method checks if the lookup definition exists
                        entries = UDFUtility.getLookupEntries(lookupOps, lookupNameCSV); 
                                       
                        // LOV UDFs must have at least one element to be processed
                        if(entries.isEmpty())
                        {
                            System.out.println("There must be at least one entry in lookup: " + record);
                            continue;
                        }
                    }

                    // Fix exception to print full stack trace when logging is setup
                    catch(tcAPIException e){System.out.println("Exception " + e + " for :"+ record); continue;}
                    catch(tcInvalidLookupException e){System.out.println("Exception " + e + " for :"+ record); continue;}
                    catch(tcColumnNotFoundException e){System.out.println("Exception " + e + " for :"+ record); continue;}
                }
 
                // Derive backend type based on display type
                String derivedType = UDFUtility.deriveBackendTypeFromDisplayType(displayTypeCSV);
                 
                // Built new Attribute Definition object which represents a UDF
                AttributeDefinition newUDF = new AttributeDefinition();

                String api_name = attrNameCSV; // Value from file
                boolean isVisible = true;
                boolean isBackendRequired = false;
                String type = derivedType; // Transformation of value from file
                boolean isSystemControlled = false;
                boolean isCustomAttribute = true;
                String encryption = "CLEAR";
                String description = "";
                String backendType = type; //
                boolean isMultiRepresented = false;
                boolean isRequired = false;
                Integer maxSize = Integer.parseInt(lengthCSV); // Value from file
                boolean isMultiValued = false;
                boolean isUserSearchable = (searchableCSV.equals("Y")) ? true : false; // Transformation of value from file
                boolean isReadOnly = false;
                String displayType = displayTypeCSV; // Value from file
                boolean isBulkUpdatable = false;
                boolean isMLS = false;
                String backendName = api_name.toLowerCase(); //
                String lookupCode = lookupNameCSV; //
                boolean isSearchable = isUserSearchable; //
                String  category = "Basic User Information";

                newUDF.setPossibleValues(lookupCode, entries);
                newUDF.setName(api_name);
                newUDF.setVisible(isVisible);
                newUDF.setBackendRequired(isBackendRequired);
                newUDF.setType(type);
                newUDF.setSystemControlled(isSystemControlled);
                newUDF.setCustomAttribute(isCustomAttribute);
                newUDF.setEncryption(encryption);
                newUDF.setDescription(description);
                newUDF.setBackendType(backendType);
                newUDF.setMultiRepresented(isMultiRepresented);
                newUDF.setRequired(isRequired);
                newUDF.setMaxSize(maxSize);
                newUDF.setMultiValued(isMultiValued);
                newUDF.setUserSearchable(isUserSearchable);
                newUDF.setReadOnly(isReadOnly);
                newUDF.setDisplayType(displayType);
                newUDF.setBulkUpdatable(isBulkUpdatable);
                newUDF.setMLS(isMLS);
                newUDF.setBackendName(backendName);
                newUDF.setLookupCode(lookupCode);
                newUDF.setSearchable(isSearchable);
                newUDF.setCategory(category);

                // Add new UDF to document
                UDFUtility.createUserDefinedField(doc, newUDF); 
                
                // UDF in staging process
                udfDuplicateValidator.add(attrNameCSV);
            }
             
            // Create XML file
            UDFUtility.createXMLFile(doc, DESTINATION_PATH_OF_UDF_METADATA);
            
            // Print Document object to String
            //System.out.println(UDFUtility.parseDocumentIntoStringXML(doc));
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
