import DataTypes.PreconditionsException;
import Model.*;
import ModelicaSimulableModel.*;
import Parser.FormatNotSupportedException;
import Util.CustomLogger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Main {
    static final String ABUNDANCES_FILENAME = "abundances.tsv";
    static final String REACTOME_FILENAME = "pathway.sbml";
    static final String LOG_FILENAME = "log.txt";
    static final String TEST = "small-sumoylation";

    static final double HeLaProteins = 2.3e9;
    static final double HeLaSize = 3e-12;
    
    static final double minInitialAmount = 0;
    static final double maxInitialAmount = 1.79e-12;
    static final double minK = 1e2;
    static final double maxK = 1e9;
    static final double minKcat = 1e-2;
    static final double maxKcat = 1e5;
    static final double minKm = 1e-8;
    static final double maxKm = 1;

    public static void main(String[] args){
        String username = System.getProperty("user.name");
        String testFolder = "/home/" + username + "/Dropbox/Tesisti/software/test-cases";
        test(TEST);
        System.out.println("All done!");
    }

    public static void reset(String folder){
        File folderFile = new File(folder);
        for(String file: folderFile.list()){
            test(file);
        }
    }

    public static void test(String test) {
        String username = System.getProperty("user.name");
        String projectFolder = "/home/" + username + "/Dropbox/Tesisti/software";
        String knowledgeFolder = projectFolder + "/knowledge";
        String testFolder = projectFolder + "/test-cases/" + test;
        String kbPath = testFolder + "/in/" + REACTOME_FILENAME;
        String globalAbundancesPath = knowledgeFolder + "/" + ABUNDANCES_FILENAME;
        String localAbundancesPath = testFolder + "/in/" + ABUNDANCES_FILENAME;
        String dumpPath = testFolder + "/out/model_dump.xml";
        String xmlPath = testFolder + "/in/quantitative.xml";
        String logPath = testFolder + "/out/" + LOG_FILENAME;
        CustomLogger.setup(logPath);

        System.out.println("Model2Simulable: testing test-case " + test);

        try {
            Bootstrap.joinAbundances(kbPath, globalAbundancesPath, localAbundancesPath);
            Bootstrap.buildQuantitativeFile(kbPath, xmlPath,
                    minInitialAmount, maxInitialAmount, minK, maxK, minKcat, maxKcat, minKm, maxKm);
        } catch (IOException | XMLStreamException | ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
        try {
            Set<String> kbPaths = new HashSet<>();
            kbPaths.add(kbPath);
            kbPaths.add(xmlPath);
            kbPaths.add(localAbundancesPath);

            Model m = HandleModel.createModel(kbPaths);
            CellType helaCell = new CellType("HeLa", HeLaSize, HeLaProteins);
            m.setCellType(helaCell);

            m.dump(dumpPath);

            Model m_loaded = Model.load(dumpPath);
            ModelicaSimulableModel msm = HandleModelica.buildSimulableModel(m_loaded);
            HandleModelica.buildModelica(msm, testFolder + "/out");
        } catch (XMLStreamException | IOException | FormatNotSupportedException | PreconditionsException | ParserConfigurationException | SAXException | TransformerException ex) {
            ex.printStackTrace();
        }
    }
}