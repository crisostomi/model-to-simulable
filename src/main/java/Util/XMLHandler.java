package Util;

import DataTypes.UndefinedModelicaParameter;
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
import java.util.Map;
import java.util.Set;

public class XMLHandler {

    public static void buildParametersXML(Set<UndefinedModelicaParameter> params, String path)
            throws ParserConfigurationException,
                    TransformerException
    {
        writeDocument(buildParametersDocument(params), path);
    }

    public static void buildProteinConstraintsXML(Map<String, Double> abundances, String path)
            throws ParserConfigurationException, TransformerException
    {
        writeDocument(buildProteinConstraintsDocument(abundances), path);
    }

    private static Document buildProteinConstraintsDocument(Map<String, Double> abundances) throws ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document result = dBuilder.newDocument();

        Node constraints = result.createElement("constraints");
        result.appendChild(constraints);

        for (Map.Entry<String, Double> ab: abundances.entrySet()) {
            String variableName = ab.getKey();
            double abundance = ab.getValue();
            Element element = result.createElement("protein");
            element.setAttribute("name", variableName);
            element.setAttribute("abundance", String.valueOf(abundance));

            constraints.appendChild(element);
        }

        return result;
    }

    private static Document buildParametersDocument(Set<UndefinedModelicaParameter> params)
            throws ParserConfigurationException {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document result = dBuilder.newDocument();

        Node parameters = result.createElement("parameters");
        result.appendChild(parameters);

        for (UndefinedModelicaParameter p: params) {
            Element element = result.createElement("parameter");
            element.setAttribute("name", p.getParameterName());
            element.setAttribute("lowerBound", String.valueOf(p.getLowerBound()));
            element.setAttribute("upperBound", String.valueOf(p.getUpperBound()));

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
