package project.rayedchan.utilities;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Operations.tcLookupOperationsIntf;
import Thor.API.tcResultSet;
import com.thortech.xl.dataaccess.tcDataSet;
import com.thortech.xl.dataaccess.tcDataSetException;
import com.thortech.xl.dataobj.PreparedStatementUtil;
import com.thortech.xl.orb.dataaccess.tcDataAccessException;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import oracle.iam.configservice.vo.AttributeDefinition;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import oracle.iam.configservice.api.Constants;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import project.rayedchan.services.tcOIMDatabaseConnection;

/**
 * @author rayedchan
 * This class deals with the backend creation of User Defined Fields.
 * A CSV file is processed and a UDF XML file is generated in order to be used for OIM Deployment Manager.
 * This utility does not support creation of encrypted UDFs.
 * Use standard convention by having USR_UDF_ as the prefix for each backend attribute name.
 * 
 * Side Notes: 
 * OIM does not allow you to export encrypted UDFs.
 * Length of OIM column name must be less than or equal to 30.
 * You can only have 22 characters or less for API Name when adding 
 * through OIM console. OIM will prefix with USR_UDF_ on the backend column name.
 */
public class BackendUDFUtility 
{
    /**
     * This creates a single user defined field (UDF) in XML format.
     * @param   document    Object representation of XML document file
     * @param   attrObj     Representation of a single User Defined Field
     */
    public void createUserDefinedField(Document document, AttributeDefinition attrObj) throws XPathExpressionException
    {
        // Get properties from Attribute Definition object
        String api_name = attrObj.getName();
        boolean isVisible = attrObj.isVisible();
        boolean isBackendRequired = attrObj.isBackendRequired();
        String type = attrObj.getType();
        boolean isSystemControlled = attrObj.isSystemControlled();
        boolean isCustomAttribute = attrObj.isCustomAttribute();
        String  encryption = attrObj.getEncryption();
        String  description = attrObj.getDescription();
        String  backendType = attrObj.getBackendType();
        boolean isMultiRepresented = attrObj.isMultiRepresented();
        boolean isRequired = attrObj.isRequired();
        Integer maxSize = attrObj.getMaxSize();
        boolean isMultiValued = attrObj.isMultiValued();
        boolean isUserSearchable = attrObj.isUserSearchable();
        boolean isReadOnly = attrObj.isReadOnly();
        String  displayType = attrObj.getDisplayType();
        boolean isBulkUpdatable = attrObj.isBulkUpdatable();
        boolean isMLS = attrObj.isMLS();
        String  backendName = attrObj.getBackendName();
        boolean isSearchable = attrObj.isSearchable();
        String category = attrObj.getCategory();
        String lookupCode = attrObj.getLookupCode(); // Lookup Definition Name
        Map<String, String> possibleValues = attrObj.getPossibleValues();
       
        // Locate proper level to add the new Attribute Definition into the xml
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        NodeList nodes =  (NodeList) xpath.evaluate("xl-ddm-data", document, XPathConstants.NODESET);
        
        // Attribute Definition parent element
        Element attrDef  = document.createElement("AttributeDefinition");
        
        // Set parent tag properties
        attrDef.setAttribute("repo-type", "API"); 
        attrDef.setAttribute("name", api_name);
        attrDef.setAttribute("subtype", "User Metadata");
        
        // Special case: Additonal XML elements to handle UDF of type LOV
        boolean isListOfValuesType = Constants.DisplayType.LOV.name().equals(displayType);
        if(isListOfValuesType)
        {
            Element possibleValuesTag = document.createElement("possibleValues"); // Wrapper for all entries
            
            // Iterate each entry defined in lookup definition
            for(Map.Entry<String,String> entry: possibleValues.entrySet())
            {
                String codeKey = entry.getKey();
                String decode = entry.getValue();
                Element possibleValueTag = document.createElement("possibleValue");
                Element codeTag = document.createElement("code");
                Element meaningTag = document.createElement("meaning");
                
                // Set values for nested elements (A lookup entry)
                codeTag.setTextContent(codeKey);
                meaningTag.setTextContent(decode);
                
                // Nest entry tags into possibleValueTag
                possibleValueTag.appendChild(codeTag);
                possibleValueTag.appendChild(meaningTag);
                
                // Nest possibleValueTag into possibleValuesTag
                possibleValuesTag.appendChild(possibleValueTag);
            }
            
            // Nest to AttributeDefinition Tag
            attrDef.appendChild(possibleValuesTag);
        }
        
        // Child Elements
        Element visibleTag = document.createElement("visible");
        Element backendRequiredTag = document.createElement("backendRequired");
        Element typeTag = document.createElement("type");
        Element systemControlledTag = document.createElement("systemControlled");
        Element customAttributeTag = document.createElement("customAttribute");
        Element encryptionTag = document.createElement("encryption");
        Element descriptionTag = document.createElement("description");
        Element backendTypeTag = document.createElement("backendType");
        Element multiRepresentedTag = document.createElement("multiRepresented");
        Element requiredTag = document.createElement("required");
        Element maxSizeTag = document.createElement("maxSize");
        Element multiValuedTag = document.createElement("multiValued");
        Element userSearchableTag = document.createElement("userSearchable");
        Element readOnlyTag = document.createElement("readOnly");
        Element displayTypeTag = document.createElement("displayType");
        Element bulkUpdatableTag = document.createElement("bulkUpdatable");
        Element mLSTag = document.createElement("MLS");
        Element backendNameTag = document.createElement("backendName");
        Element searchableTag = document.createElement("searchable");
        Element lookupCodeTag = document.createElement("lookupCode");
        Element sourcescopeTag = document.createElement("source-scope");
        Element categoryTag = document.createElement("category");
        
        // Set values of child elements
        visibleTag.setTextContent(String.valueOf(isVisible));
        backendRequiredTag.setTextContent(String.valueOf(isBackendRequired));
        typeTag.setTextContent(type);
        systemControlledTag .setTextContent(String.valueOf(isSystemControlled));
        customAttributeTag.setTextContent(String.valueOf(isCustomAttribute));
        encryptionTag.setTextContent(encryption);
        descriptionTag.setTextContent(description);
        backendTypeTag.setTextContent(backendType);
        multiRepresentedTag.setTextContent(String.valueOf(isMultiRepresented));
        requiredTag.setTextContent(String.valueOf(isRequired));
        maxSizeTag.setTextContent(String.valueOf(maxSize));
        multiValuedTag.setTextContent(String.valueOf(isMultiValued));
        userSearchableTag.setTextContent(String.valueOf(isUserSearchable));
        readOnlyTag.setTextContent(String.valueOf(isReadOnly));
        displayTypeTag.setTextContent(displayType);
        bulkUpdatableTag.setTextContent(String.valueOf(isBulkUpdatable));
        mLSTag.setTextContent(String.valueOf(isMLS));
        backendNameTag.setTextContent(backendName);
        lookupCodeTag.setTextContent(lookupCode);
        searchableTag.setTextContent(String.valueOf(isSearchable));
        
        // Set properties of child elements if any       
        sourcescopeTag.setAttribute("type", "CategoryDefinition"); 
        sourcescopeTag.setAttribute("name", category);
        sourcescopeTag.setAttribute("subtype", "User Metadata");
        categoryTag.setAttribute("CategoryDefinition", category);
        
        // Add child elements to parent element     
        attrDef.appendChild(visibleTag);
        attrDef.appendChild(backendRequiredTag);
        attrDef.appendChild(typeTag);
        attrDef.appendChild(systemControlledTag);
        attrDef.appendChild(customAttributeTag);
        attrDef.appendChild(encryptionTag);
        attrDef.appendChild(descriptionTag);
        attrDef.appendChild(backendTypeTag);
        attrDef.appendChild(multiRepresentedTag);
        attrDef.appendChild(requiredTag);
        attrDef.appendChild(maxSizeTag);
        attrDef.appendChild(multiValuedTag);
        attrDef.appendChild(userSearchableTag);
        attrDef.appendChild(readOnlyTag);
        attrDef.appendChild(displayTypeTag);
        attrDef.appendChild(bulkUpdatableTag);
        attrDef.appendChild(mLSTag);
        attrDef.appendChild(backendNameTag);
        if(isListOfValuesType){attrDef.appendChild(lookupCodeTag);} // Preserve order of elements in XML
        attrDef.appendChild(searchableTag);
        attrDef.appendChild(sourcescopeTag);
        attrDef.appendChild(categoryTag);
              
        // Get root node and add the new Attribute Defintion to it
        Node rootNode = nodes.item(0); 
        rootNode.appendChild(attrDef); 
    }
    
    /**
     * Builds the UDF XML file.
     * @param   dbConnection    Database connection to the OIM Schema
     * @param   lookupOps       OIM Lookup operation services
     * @param   parser          Parser object contains the CSV file to be processed
     * @return  Document of the final UDF XML MetaData to be used for deployment manager  
     */
    public Document buildUDFDocument(tcOIMDatabaseConnection dbConnection, tcLookupOperationsIntf lookupOps,  CSVParser parser) throws tcDataSetException, tcDataAccessException, ParserConfigurationException, XPathExpressionException
    {              
        // Get all the columns in the USR table     
        Set<String> columnNames =  getAllUSRColumns(dbConnection);

        // Used to determine if there are duplicates UDF in csv file
        Set<String> udfDuplicateValidator = new HashSet<String>();

        // Create base template document for a UDF
        Document doc = createUDFDocument();
        
        // Iterate each entry in the csv file
        for(CSVRecord record : parser)
        {
            // Get values of a record from csv file; Change header value if different
            String attrNameCSV = record.get("ColumnName");
            String displayTypeCSV = record.get("Field Type").toUpperCase();
            String lengthCSV = record.get("Max Length");
            String searchableCSV = record.get("Searchable").toUpperCase();
            String lookupNameCSV = record.get("Lookup Table");
            Map entries = null;
            int attrNameLength = attrNameCSV.length();

            // Check if the attribute name (also the column name contains any spaces
            if(HelperUtility.containsWhitespace(attrNameCSV))
            {
                System.out.println("WARNING: Backend contains a whitespace: " + record);
                continue; // Skip to next iteration
            }
            
            // Check if the column name is valid
            if(!HelperUtility.isColumnNameValid(attrNameCSV))
            {                
                System.out.println("WARNING: Column name is invalid: " + record);
                continue; // Skip to next iteration
            }

            // Check if the attribute name, which is also the column name for this utility, is valid
            if(attrNameLength > 30 || attrNameLength < 1)
            {                
                System.out.println("WARNING: Backend column must have length 1 to 30: " + record);
                continue; // Skip to next iteration
            }

            // Check if the column name in CSV file exists in the USR table
            if(columnNames.contains(attrNameCSV.toUpperCase()))
            {
                System.out.println("WARNING: UDF already exists in Identity: " + record);
                continue; // Skip to next iteration
            }

            // Check if current attribute name has already processed
            if(udfDuplicateValidator.contains(attrNameCSV))
            {
                System.out.println("WARNING: The attribute name has already been staged: " + record);
                continue; // Skip to next iteration
            }

            // Check if the display type is valid
            if(!isUDFDisplayTypeValidate(displayTypeCSV))
            {                 
                System.out.println("WARNING: Invalid display type: " + record);
                continue; // Skip to next iteration
            }

            // Check if length is an integer
            if(!HelperUtility.isInteger(lengthCSV))
            {
                System.out.println("WARNING: Length is not a valid number: " + record);
                continue; // Skip to next iteration                   
            }

            // Check if length is between 1 to 4000
            int lengthSize = Integer.parseInt(lengthCSV);
            if(lengthSize > 4000 || lengthSize < 1)
            {
                System.out.println("WARNING: Length must be between 1 and 4000: " + record);
                continue; // Skip to next iteration 
            }

            // Check if searchable has a valid value
            if(!isSearchableValidValue(searchableCSV))
            {                 
                System.out.println("WARNING: Searchable is not a valid: " + record);
                continue; // Skip to next iteration
            }

            // For LOV display type, get lookup entries
            if(Constants.DisplayType.LOV.name().equals(displayTypeCSV))
            {
                try
                {
                    // Calling method checks if the lookup definition exists
                    entries = getLookupEntries(lookupOps, lookupNameCSV); 

                    // LOV UDFs must have at least one element to be processed
                    if(entries.isEmpty())
                    {
                        System.out.println("WARNING: There must be at least one entry in lookup: " + record);
                        continue;
                    }
                }

                // Fix exception to print full stack trace when logging is setup
                catch(tcAPIException e){System.out.println("ERROR: Exception " + e + " for :"+ record); continue;}
                catch(tcInvalidLookupException e){System.out.println("ERROR: Exception " + e + " for :"+ record); continue;}
                catch(tcColumnNotFoundException e){System.out.println("ERROR: Exception " + e + " for :"+ record); continue;}
            }

            // Derive backend type based on display type
            String derivedType = deriveBackendTypeFromDisplayType(displayTypeCSV);

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
            createUserDefinedField(doc, newUDF); 

            // UDF in staging process
            udfDuplicateValidator.add(attrNameCSV);
        }
        
        return doc;
    }

    /**
     * Gets the entries from a lookup definition
     * @param   lookupOps         Service class for lookup operations
     * @param   lookupDefName   Name of lookup definition
     * @return  HashMap object where the key = code key and value = decode of a lookup entry
     */
    public Map<String,String> getLookupEntries(tcLookupOperationsIntf lookupOps, String lookupDefName) throws tcAPIException, tcInvalidLookupException, tcColumnNotFoundException
    {
        Map<String, String> entries = new HashMap<String,String>(); // Data structure to store code and decode of the entries in a lookup
        tcResultSet tcResultSetObj = lookupOps.getLookupValues(lookupDefName); // Get lookup entries as a result set
        int numRows = tcResultSetObj.getTotalRowCount();

        for(int i = 0; i < numRows; i++)
        {
            tcResultSetObj.goToRow(i); // Move pointer to next record
            String codeKey = tcResultSetObj.getStringValue("Lookup Definition.Lookup Code Information.Code Key");
            String decode = tcResultSetObj.getStringValue("Lookup Definition.Lookup Code Information.Decode");
            entries.put(codeKey, decode);
        }
       
        return entries;
    }
    
    /**
     * Creates a User Defined Field (UDF) template document to 
     * represent the base structure of a UDF metadata
     * @return document object
     */
    public Document createUDFDocument() throws ParserConfigurationException
    {          
        // Create Document object
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
        
        return doc;
    }
     
    /**
     * Validates the display type of a user defined field
     * Possible display types:
     *  - CHECKBOX
     *  - LOV
     *  - TEXT
     *  - DATE_ONLY
     *  - NUMBER
     * @param   displayType Attribute display type on the GUI
     * @return  true if the display type is valid; false otherwise
     */
    public boolean isUDFDisplayTypeValidate(String displayType)
    {
        return displayType.equals(Constants.DisplayType.TEXT.name()) ||
               displayType.equals(Constants.DisplayType.CHECKBOX.name()) ||
               displayType.equals(Constants.DisplayType.LOV.name()) ||
               displayType.equals(Constants.DisplayType.DATE_ONLY.name()) ||
               displayType.equals(Constants.DisplayType.NUMBER.name());   
    }
    
    /**
     * Get all the column names in the USR table.
     * @param dbConnection  Connection to OIM Schema via OIMClient
     * @return names of all the columns in the USR table
     */
    public Set<String> getAllUSRColumns(tcOIMDatabaseConnection dbConnection) throws tcDataSetException, tcDataAccessException
    { 
        Set<String> columnNames = new HashSet<String>();
        String query = "SELECT column_name FROM USER_TAB_COLUMNS WHERE table_name = ?";          
        PreparedStatementUtil ps = new PreparedStatementUtil();
        ps.setStatement(dbConnection.getDbProvider(), query);
        ps.setString(1, "USR");
        ps.execute();
        tcDataSet usrColumnDataSet = ps.getDataSet();
        int recordCount = usrColumnDataSet.getTotalRowCount();
        
        // Iterate all the columns
        for(int i = 0; i < recordCount; i++)
        {
            usrColumnDataSet.goToRow(i); // Move pointer to next record
            String columnName = usrColumnDataSet.getString("column_name");
            columnNames.add(columnName); // Add column name to set
        }
        
        return columnNames;
    }
    
    /**
     * Determine if searchable attribute on a UDF is a valid value.
     * @param   strValue    validate if string value valid
     * @return  boolean value to indicate if string valid
     */
    public boolean isSearchableValidValue(String strValue)
    {
        if("Y".equals(strValue) || "N".equals(strValue))
        {
            return true;
        }
        
        return false;      
    }
    
    /**
     * Derive the backend type of a UDF from the display type
     * @param displayType   UDF type on the front-end
     * @return corresponding backend type
     */
    public String deriveBackendTypeFromDisplayType(String displayType)
    {
        String backendType = "";
        
        // Checkbox display type
        if(displayType.equals(Constants.DisplayType.CHECKBOX.name()))
        {
            backendType = "string";
        }
        
        // LOV display type
        else if(displayType.equals(Constants.DisplayType.LOV.name()))
        {
            backendType = "string";
        }
        
        // TEXT display type
        else if(displayType.equals(Constants.DisplayType.TEXT.name()))
        {
            backendType = "string";
        }
        
        // DATE_ONLY display type
        else if(displayType.equals(Constants.DisplayType.DATE_ONLY.name()))
        {
            backendType = "date";
        }
               
        // NUMBER display type
        else if(displayType.equals(Constants.DisplayType.NUMBER.name()))
        {
            backendType = "number";
        }

        return backendType;
    }
}
