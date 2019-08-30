import DataTypes.PreconditionsException;
import Model.Model;
import ModelicaSimulableModel.ModelicaSimulableModel;
import Parser.ConfigBuilder;
import Parser.FormatNotSupportedException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ToolTests {
    @Test
    public void toolChain_dummyPathway_generateCorrectModelica() throws SAXException, PreconditionsException, IOException, XMLStreamException, ParserConfigurationException, FormatNotSupportedException, SimulableModel.PreconditionsException, TransformerException, InterruptedException {
        String projectFolder = "/home/scacio/Dropbox/Tesisti/software/development";
        String testFolder = projectFolder + "/test-cases/dummy";

        String inputFolder = testFolder + "/in";
        String kbPath = inputFolder + "/dummyPathway.sbml";

        String outputFolder = testFolder + "/out";
        String dumpPath = outputFolder + "/model_dump.xml";
        String xmlPath = inputFolder + "/quantitative.xml";

        String generatedPath = outputFolder+"/generated";
        String correctPath = outputFolder+"/correct";

        Set<String> kbPaths = new HashSet<>();
        kbPaths.add(kbPath);

        Model m = HandleModel.createModel(kbPaths);
        ConfigBuilder c = new ConfigBuilder(m, xmlPath);
        c.buildConfig();
        m.dump(dumpPath);

        Model m_loaded = Model.load(dumpPath);
        ModelicaSimulableModel msm = HandleModelica.buildSimulableModel(m_loaded);
        HandleModelica.buildModelica(msm, generatedPath);
        Set<String> changedFiles = getChangedFiles(generatedPath, correctPath);
        assertTrue(changedFiles.isEmpty(),"The following files have changed: "+changedFiles.toString() );

    }

    public static boolean getFileDifferences(File file1, File file2) throws IOException, InterruptedException {
        String file1content = fileToString(file1);
        String file2content = fileToString(file2);
        String sortedFile1content = sortString(file1content);
        String sortedFile2content = sortString(file2content);
        return !sortedFile1content.equals(sortedFile2content);

    }

    public static String fileToString(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file.toString()));
        StringBuilder builder = new StringBuilder();
        String currentLine = reader.readLine();
        while (currentLine != null) {
            builder.append(currentLine);
            builder.append("n");
            currentLine = reader.readLine();
        }
        return builder.toString();
    }

    public static String sortString(String inputString) {
        char tempArray[] = inputString.toCharArray();
        Arrays.sort(tempArray);
        return new String(tempArray);
    }

    public static Set<String> getChangedFiles(String out, String correctOut) throws IOException, InterruptedException {
        File dir = new File(out);
        File correctDir = new File(correctOut);
        File[] outDirListing = dir.listFiles();
        Set<String> differences = new HashSet<>();
        if (outDirListing != null) {
            for (File child : outDirListing) {
                boolean difference = getFileDifferences(child, new File(correctDir+"/"+child.getName()));
                if (difference){
                    differences.add(child.toString());
                }
            }
        }
        return differences;
    }
}

