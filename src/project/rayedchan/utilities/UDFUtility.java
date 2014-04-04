package project.rayedchan.utilities;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Operations.tcLookupOperationsIntf;
import Thor.API.tcResultSet;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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

/**
 * @author rayedchan
 */
public class UDFUtility 
{
    /*
     * This creates a single user defined field (UDF) in XML format.
     * @param   document    Object representation of XML document file
     * @param   attrObj     Representation of a single User Defined Field
     */
    public static void createUserDefinedField(Document document, AttributeDefinition attrObj) throws XPathExpressionException
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
    
    /*
     * Converts a Document into String representation
     * UTF-8 conversion
     * @param   document    Document object to be parsed
     * @return  String representation of xml content
     */
    public static String parseDocumentIntoStringXML(Document document) throws TransformerConfigurationException, TransformerException
    {
        StringWriter output = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(document), new StreamResult(output));
        String newObjectResourceXML = output.toString();
        return newObjectResourceXML;
    }
        
    /*
     * Prints the records of a tcResultSet.
     * @param   tcResultSetObj  tcResultSetObject
     */
    public static void printTcResultSetRecords(tcResultSet tcResultSetObj) throws tcAPIException, tcColumnNotFoundException
    {
        String[] columnNames = tcResultSetObj.getColumnNames();
        int numRows = tcResultSetObj.getTotalRowCount();
        
        for(int i = 0; i < numRows; i++)
        {
            tcResultSetObj.goToRow(i);
            for(String columnName: columnNames)
            {
                System.out.println(columnName + " = " + tcResultSetObj.getStringValue(columnName));
            }
            System.out.println();
        }
    }
    
    /*
     * Gets the entries from a lookup definition
     * @param   lookupOps         Service class for lookup operations
     * @param   lookupDefName   Name of lookup definition
     * @return  HashMap object where the key = code key and value = decode of a lookup entry
     */
    public static Map<String,String> getLookupEntries(tcLookupOperationsIntf lookupOps, String lookupDefName) throws tcAPIException, tcInvalidLookupException, tcColumnNotFoundException
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
}
