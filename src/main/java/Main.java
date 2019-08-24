import Model.Model;
import ModelicaSimulableModel.ModelicaSimulableModel;
import Util.CustomLogger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        String projectFolder = "/home/scacio/Dropbox/Tesisti/software/development";
        String testFolder = projectFolder + "/test-cases/test-case-4";

        String inputFolder = testFolder + "/in";
        String kbPath = inputFolder + "/galactose.sbml";
        String xmlPath = inputFolder + "/quantitative.xml";

        String outputFolder = testFolder+"/out";
        String logPath = outputFolder + "/log.txt";
        String dumpPath = outputFolder + "/model_dump.xml";

        CustomLogger.setup(logPath);


        try {
            Set<String> kbPaths = new HashSet<>();
            kbPaths.add(kbPath);
            kbPaths.add(xmlPath);

            Model m = HandleModel.createModel(kbPaths);
            // ConfigBuilder c = new ConfigBuilder(m, xmlPath);
            // c.buildConfig();
            m.dump(dumpPath);

            Model m_loaded = Model.load(dumpPath);
            ModelicaSimulableModel msm = HandleModelica.buildSimulableModel(m_loaded);
            HandleModelica.buildModelica(msm, outputFolder);

            System.out.println("All done!");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
