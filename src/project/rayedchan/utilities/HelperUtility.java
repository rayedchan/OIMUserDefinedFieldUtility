package project.rayedchan.utilities;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.tcResultSet;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.w3c.dom.Document;

/**
 * @author rayedchan
 * A class that contains helpful miscellaneous methods.
 */
public class HelperUtility 
{
        
    /**
     * Creates the XML metadata file from a document object
     * @param   doc                 Document object
     * @param   destinationPath     The absolute path of file to be created 
     */
    public static void createXMLFile(Document doc, String destinationPath) throws TransformerConfigurationException, TransformerException
    {       
        // Write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(destinationPath));
        transformer.transform(source, result);
    }
         
    /**
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
        
    /**
     * Print content of csv file to standard out.
     * @param   fileName    Absolute path of CSV file
     * @param   headers     The header names defined in the CSV file
     * @param   delimiter   A separator used to separate values in CSV file
     */
    public static void printCSVFile(String fileName, String[] headers, char delimiter) throws IOException
    {
        CSVParser parser = null;
        
        try
        {
            CSVFormat format = CSVFormat.DEFAULT.withHeader().withDelimiter(delimiter);  
            parser = new CSVParser(new FileReader(fileName), format);

            for(CSVRecord record : parser)
            {
                ArrayList<String> entry = new ArrayList();
                
                for(String headerName: headers)
                {
                    entry.add(record.get(headerName));
                }
                
                System.out.println(entry);
            }
        }

        finally
        {
            if(parser != null)
                parser.close(); 
        }
    }
            
    /**
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
     
    /**
     * Determine if a string can be parse into an integer.
     * @param   strValue    validate if string value can be parsed 
     * @return  boolean value to indicate if string is an integer
     */
    public static boolean isInteger(String strValue)
    {
        try 
        {
            Integer.parseInt(strValue);
            return true;
        }
        
        catch (NumberFormatException nfe)
        {
            return false;
        }
    }
    
    /**
     * Determines if a String contains a whitespace
     * @param   value   Determine if strong contains a whitespace
     * @return  true if string contains whitespace; false otherwise
     */
    public static boolean containsWhitespace(String value)
    {
        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(value);
        return matcher.find();
    }
        
    /**
     * Determines if column name is valid
     * @param   columnName  Column name to be checked
     * @return  true is column name is valid; false otherwise
     */
    public static boolean isColumnNameValid(String columnName)
    {
        Pattern pattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
        Matcher matcher = pattern.matcher(columnName);
        return matcher.find();
        
    }
}
