package project.rayedchan.utilities;

import java.io.StringWriter;
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
        String  category = attrObj.getCategory();
        
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
    
}
