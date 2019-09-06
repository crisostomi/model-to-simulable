import Model.*;
import ModelicaSimulableModel.*;
import Util.CustomLogger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static final String ABUNDANCES_FILENAME = "abundances.tsv";
    public static final String REACTOME_FILENAME = "pathway.sbml";
    public static final String LOG_FILENAME = "log.txt";
    public static final String TEST = "meiotic-recombination";

    public static final double HeLaProteins = 2.3e9;

    public static void main(String[] args) {

        String username = System.getProperty("user.name");
        String projectFolder = "/home/"+username+"/Dropbox/Tesisti/software";
        String testFolder = projectFolder + "/test-cases/"+TEST;
        String kbPath = testFolder + "/in/"+REACTOME_FILENAME;
        String globalAbundancesPath = projectFolder +"/test-cases/"+ ABUNDANCES_FILENAME;
        String logPath = testFolder +"/out/"+ LOG_FILENAME;
        String localAbundancesPath = testFolder+"/in/"+ABUNDANCES_FILENAME;
        String dumpPath = testFolder + "/out/model_dump.xml";
        String xmlPath = testFolder + "/in/quantitative.xml";

        System.out.println("Model2Simulable: testing test-case "+TEST);
        CustomLogger.setup(logPath);

        try {
            Bootstrap.joinAbundances(kbPath, globalAbundancesPath, localAbundancesPath);
//            Bootstrap.buildQuantitativeFile(kbPath, xmlPath);
        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }

        try {
            Set<String> kbPaths = new HashSet<>();
            kbPaths.add(kbPath);
            kbPaths.add(xmlPath);
            kbPaths.add(localAbundancesPath);

            Model m = HandleModel.createModel(kbPaths);
            CellType helaCell = new CellType("HeLa", HeLaProteins);
            m.setCellType(helaCell);
            m.consolidateAbundance();

            m.dump(dumpPath);

            Model m_loaded = Model.load(dumpPath);
            ModelicaSimulableModel msm = HandleModelica.buildSimulableModel(m_loaded);
            HandleModelica.buildModelica(msm, testFolder + "/out");

            System.out.println("All done!");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
