package project.rayedchan.testdriver;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Operations.tcLookupOperationsIntf;
import Thor.API.tcResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import javax.security.auth.login.LoginException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import oracle.iam.configservice.api.Constants;
import oracle.iam.configservice.vo.AttributeDefinition;
import oracle.iam.identity.exception.UserSearchException;
import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.authz.exception.AccessDeniedException;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import project.rayedchan.utilities.UDFUtility;

/**
 *
 * @author rayedchan
 * This utility does not support creation of encrypted UDFs.
 * OIM does not allow you to export encrypted UDFs.
 * TODO: Handle special case with lookup UDF with no entries in lookup
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
    
    public static void main(String[] args) throws LoginException, AccessDeniedException, UserSearchException, ParserConfigurationException, TransformerConfigurationException, TransformerException, XPathExpressionException, tcAPIException, tcInvalidLookupException, tcColumnNotFoundException 
    {
        OIMClient oimClient = null;
        tcLookupOperationsIntf lookupOps = null;
        
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
            //UserManager usermgr = oimClient.getService(UserManager.class);
            lookupOps = oimClient.getService(tcLookupOperationsIntf.class);
            String lookupName = "Lookup.LDAP.Students.OU.ProvAttrMap";
            //tcResultSet entries = lookupOps.getLookupValues("Lookup.LDAP.Students.OU.ProvAttrMap");
            //UDFUtility.printTcResultSetRecords(entries);
            System.out.println(UDFUtility.getLookupEntries(lookupOps, lookupName));
            
            // Call a method from a service
            //List<User> users = usermgr.search(new SearchCriteria("User Login", "*", SearchCriteria.Operator.EQUAL), new HashSet(), new HashMap());
            //System.out.println(users);
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.newDocument();
            
            // Root Element to hold all AttributeDefinition Elements
            Element rootTag = doc.createElement("xl-ddm-data");
            
            // Set properties of root element
            rootTag.setAttribute("version", "2.0.2.1");
            rootTag.setAttribute("user", "XELSYSADM");
            rootTag.setAttribute("database", "jdbc:oracle:thin:@localhost:1521/orcl");
            rootTag.setAttribute("exported-date", "1396307720581");
            rootTag.setAttribute("description", "");
            
            // Add root element to document object
            doc.appendChild(rootTag);
            
            // Built new Attribute Definition object which represents a UDF
            AttributeDefinition newUDF = new AttributeDefinition();
               
            String api_name = "util_attr"; //
            boolean isVisible = true;
            boolean isBackendRequired = false;
            String type = "string"; //
            boolean isSystemControlled = false;
            boolean isCustomAttribute = true;
            String encryption = "CLEAR";
            String description = "";
            String backendType = type; 
            boolean isMultiRepresented = false;
            boolean isRequired = false;
            Integer maxSize = 100; //
            boolean isMultiValued = false;
            boolean isUserSearchable = false;
            boolean isReadOnly = false;
            String  displayType = "TEXT"; //
            boolean isBulkUpdatable = false;
            boolean isMLS = false;
            String  backendName = "usr_udf_util_attr"; //
            boolean isSearchable = isUserSearchable; //
            String  category = "Basic User Information";
            
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
            newUDF.setSearchable(isSearchable);
            newUDF.setCategory(category);

            // Add new UDF to document
            UDFUtility.createUserDefinedField(doc, newUDF);
            
            // Print Document object to String
            System.out.println(UDFUtility.parseDocumentIntoStringXML(doc));
            
        } 
            
        finally
        {
            if(lookupOps != null)
                lookupOps.close();
            
            // Logout user from OIMClient
            if(oimClient != null)
                oimClient.logout();
        }
 
    }
}
