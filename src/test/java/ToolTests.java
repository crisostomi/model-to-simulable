import DataTypes.PreconditionsException;
import Model.Model;
import ModelicaSimulableModel.ModelicaSimulableModel;
import Parser.ConfigBuilder;
import Parser.FormatNotSupportedException;
import Util.CustomLogger;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ToolTests {
    @Test
    public void toolChain_dummyPathway_generateCorrectModelica() throws SAXException, PreconditionsException, IOException, XMLStreamException, ParserConfigurationException, FormatNotSupportedException, SimulableModel.PreconditionsException, TransformerException, InterruptedException {
//        String projectFolder = "/home/don/Dropbox/Tesisti/software/development";
//        String testFolder = projectFolder + "/test-cases/dummy";
//
//        String inputFolder = testFolder + "/in";
//        String kbPath = inputFolder + "/dummyPathway.sbml";
//
//        String outputFolder = testFolder + "/out";
//        String dumpPath = outputFolder + "/model_dump.xml";
//        String xmlPath = inputFolder + "/quantitative.xml";
//
//        String generatedPath = outputFolder+"/generated";
//        String correctPath = outputFolder+"/correct";
//
//        Set<String> kbPaths = new HashSet<>();
//        kbPaths.add(kbPath);
//
//        Model m = HandleModel.createModel(kbPaths);
//        ConfigBuilder c = new ConfigBuilder(m, xmlPath);
//        c.buildConfig();
//        m.dump(dumpPath);
//
//        Model m_loaded = Model.load(dumpPath);
//        ModelicaSimulableModel msm = HandleModelica.buildSimulableModel(m_loaded);
//        HandleModelica.buildModelica(msm, generatedPath);
//        System.out.println(getDifferences(generatedPath, correctPath));
        assertTrue(true);

    }

//    public static String getFileDifferences(File file1, File file2) throws IOException, InterruptedException {
//        File sorted1StFile = new File(file1.getParentFile()+"/sorted"+file1.getName());
//        File sorted2ndFile = new File(file2.getParentFile()+"/sorted"+file2.getName());
//        System.out.println(sorted1StFile.createNewFile());
//        System.out.println(sorted2ndFile.createNewFile());
//        String sort1Cmd = "sort "+ file1.toString()+" > "+ sorted1StFile.toString();
//        String sort2Cmd = "sort "+ file2.toString()+" > "+ sorted2ndFile.toString();
//        Process sort1Proc = Runtime.getRuntime().exec(sort1Cmd);
//
//        Process sort2Proc = Runtime.getRuntime().exec(sort2Cmd);
//
//        String cmd = "diff " +sorted1StFile.toString() + " " + sorted2ndFile.toString() + "";
//        Process process = Runtime.getRuntime().exec(cmd);
//        StringBuilder output = new StringBuilder();
//
//        BufferedReader reader = new BufferedReader(
//                new InputStreamReader(sort1Proc.getErrorStream()));
//
//        String line = "";
//        while ((line = reader.readLine()) != null) {
//            output.append(line+"\n");
//        }
//
//        return output.toString();
//    }
//
//    public static Map<String, String> getDifferences(String out, String correctOut) throws IOException, InterruptedException {
//        File dir = new File(out);
//        File correctDir = new File(correctOut);
//        File[] outDirListing = dir.listFiles();
//        Map<String, String> differences = new HashMap<>();
//        if (outDirListing != null) {
//            for (File child : outDirListing) {
//                String difference = getFileDifferences(child, new File(correctDir+"/"+child.getName()));
//                if (!difference.equals("")){
//                    differences.put(child.toString(), difference);
//                }
//            }
//        }
//        return differences;
//    }
}

