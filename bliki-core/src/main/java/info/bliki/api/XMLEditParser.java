package info.bliki.api;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Reads <code>Edit</code> data from an XML file generated by the <a href="https://meta.wikimedia.org/w/api.php">Wikimedia API</a>
 */
public class XMLEditParser extends AbstractXMLParser {

    private static final String ERROR_TAG = "error";
    private static final String INFO_ATTR = "info";
    private static final String CODE_ATTR = "code";

    private ErrorData errorData;

    public XMLEditParser(String xmlText) throws SAXException, ParserConfigurationException {
        super(xmlText);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        fAttributes = attributes;
        if (ERROR_TAG.equalsIgnoreCase(qName)) {
            errorData = new ErrorData();
            errorData.setCode(attributes.getValue(CODE_ATTR));
            errorData.setInfo(attributes.getValue(INFO_ATTR));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        fAttributes = null;
    }

    public ErrorData getErrorData() {
        return errorData;
    }
}
