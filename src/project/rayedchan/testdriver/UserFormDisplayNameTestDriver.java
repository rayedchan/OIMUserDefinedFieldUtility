package project.rayedchan.testdriver;

import java.io.FileReader;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import project.rayedchan.utilities.HelperUtility;
import project.rayedchan.utilities.UserFormDisplayNameUtility;

/**
 * @author rayedchan
 * Test driver to modify the MetaData file responsible for the Display Name
 * of the User Defined Field in the User form (In Form Designer search).
 * 
 * 
 * MDS responsible for storing the front-end display names of the UDFs:
 * /xliffBundles/oracle/iam/ui/runtime/BizEditorBundle.xlf
 * 
 */
public class UserFormDisplayNameTestDriver 
{
    public static final String SOURCE_PATH_OF_COLUMNNAME_TO_DISPLAYNAME_MAPPING_CSV_FILE = "/home/oracle/NetBeansProjects/OIMUserDefinedFieldUtility/sample_data/userForm/csv/mapping.csv";
    public static final String SOURCE_PATH_TO_BIZ_EDITOR_BUNDLE_XLF_FILE = "/home/oracle/Desktop/sysadmin/xliffBundles/oracle/iam/ui/runtime/BizEditorBundle.xlf";
    public static final String DESTINATION_PATH_OF_MODIFED_BIZ_EDITOR_BUNDLE_XLF_FILE="/home/oracle/Desktop/BizEditorBundle.xlf";
    
    public static void main(String[] args) throws TransformerConfigurationException, TransformerException, XPathExpressionException, SAXException, IOException, ParserConfigurationException
    {         
        CSVParser parser = null;
         
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document userFormDoc = builder.parse(SOURCE_PATH_TO_BIZ_EDITOR_BUNDLE_XLF_FILE);
            UserFormDisplayNameUtility ufdnUtil = new UserFormDisplayNameUtility();

            // Objects for parsing CSV file     
            CSVFormat format = CSVFormat.DEFAULT.withHeader().withDelimiter(',');
            parser = new CSVParser(new FileReader(SOURCE_PATH_OF_COLUMNNAME_TO_DISPLAYNAME_MAPPING_CSV_FILE), format);

               
            for(CSVRecord record : parser)
            {
                // Get values of a record from csv file; Change header value if different
                String columnNameCSV = record.get("ColumnName");
                String displaNameCSV = record.get("DisplayName");
                boolean valid = ufdnUtil.changeUDFDisplayName(userFormDoc, columnNameCSV, displaNameCSV);
                
                if(!valid)
                {
                    System.out.printf("Record from file is invalid: %s\n ", record);
                }
            }
            
            HelperUtility.createXMLFile(userFormDoc, DESTINATION_PATH_OF_MODIFED_BIZ_EDITOR_BUNDLE_XLF_FILE);
            //System.out.println(HelperUtility.parseDocumentIntoStringXML(userFormDoc));
        }
        
        finally
        {
            
        }
        
    }
    
}
