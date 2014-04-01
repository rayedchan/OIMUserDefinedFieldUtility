package project.rayedchan.testdriver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import javax.security.auth.login.LoginException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import oracle.iam.identity.exception.UserSearchException;
import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.authz.exception.AccessDeniedException;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import org.w3c.dom.Document;

/**
 *
 * @author rayedchan
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
    
    public static void main(String[] args) throws LoginException, AccessDeniedException, UserSearchException, ParserConfigurationException 
    {
        OIMClient oimClient = null;
        
        try
        {
            /*
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
 
            // Lookup a service
            UserManager usermgr = oimClient.getService(UserManager.class);
   
            // Call a method from a service
            List<User> users = usermgr.search(new SearchCriteria("User Login", "*", SearchCriteria.Operator.EQUAL), new HashSet(), new HashMap());
            System.out.println(users);
            */
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.newDocument();
            
            
            
        } 
            
        finally
        {
            // Logout user from OIMClient
            if(oimClient != null)
                oimClient.logout();
        }
 
    }
}
