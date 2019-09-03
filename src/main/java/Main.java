import Model.Model;
import ModelicaSimulableModel.ModelicaSimulableModel;
import Util.CustomLogger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        // TODO: fix linking of enzymes between reactions and system.
        String projectFolder = "/home/don/Dropbox/Tesisti/software";
        String testFolder = projectFolder + "/test-cases/dummy";

        String kbPath = testFolder + "/in/dummyPathway.sbml";
//        String xmlPath = testFolder + "/in/quantitative.xml";
//        String tsvPath = testFolder + "/in/abundances.tsv";
        String logPath = testFolder + "/out/log.txt";
        String dumpPath = testFolder + "/out/model_dump.xml";

        CustomLogger.setup(logPath);

        try {
            Set<String> kbPaths = new HashSet<>();
            kbPaths.add(kbPath);
            // kbPaths.add(xmlPath);
//            kbPaths.add(tsvPath);

            Model m = HandleModel.createModel(kbPaths);
            // ConfigBuilder c = new ConfigBuilder(m, xmlPath);
            // c.buildConfig();
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
