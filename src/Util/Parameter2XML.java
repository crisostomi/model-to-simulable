package Util;

import DataTypes.Parameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.List;
import java.util.Map;

public class Parameter2XML {

    public static void buildParametersXML(List<Parameter> params, String path)
            throws ParserConfigurationException,
                    TransformerException
    {
        writeDocument(buildParametersDocument(params), path);
    }

    private static Document buildParametersDocument(List<Parameter> params)
            throws ParserConfigurationException {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document result = dBuilder.newDocument();

        Node parameters = result.createElement("parameters");
        result.appendChild(parameters);

        for (Parameter p: params) {
            Element element = result.createElement(p.getEntity());
            element.setAttribute("id", p.getId());
            for (Map.Entry<String, String> entry: p.getProperties().entrySet()) {
                String property = entry.getKey();
                String value = entry.getValue();

                element.setAttribute(property, value);
            }

            parameters.appendChild(element);
        }

        return result;
    }

    private static void trimWhitespace(Node node) {
        NodeList children = node.getChildNodes();
        for(int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if(child.getNodeType() == Node.TEXT_NODE) {
                child.setTextContent(child.getTextContent().trim());
            }
            trimWhitespace(child);
        }
    }


    private static void writeDocument(Document doc, String path) throws TransformerException {

        doc.normalizeDocument();
        trimWhitespace(doc);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(doc);
        StreamResult streamResult = new StreamResult(new File(path));
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(domSource, streamResult);
    }

}
