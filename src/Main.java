import Model.Model;
import ModelicaSimulableModel.ModelicaSimulableModel;

import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        String projectFolder = "/home/don/Dropbox/Tesisti/software/development/KnowledgeAcquisition";
        String testFolder = projectFolder + "/test-cases/test-case-4";

        String kbPath = testFolder + "/in/galactose.sbml";
        String xmlPath = testFolder + "/out/quantitative.xml";
        // String logPath = testFolder + "/out/log.txt";
        String dumpPath = testFolder + "/out/model_dump.json";

        // CustomLogger.setup(logPath);


        try {
            Set<String> kbPaths = new HashSet<>();
            kbPaths.add(kbPath);
            kbPaths.add(xmlPath);

            Model m = HandleModel.createModel(kbPaths);
            // ConfigBuilder c = new ConfigBuilder(m, xmlPath);
            // c.buildConfig();
            System.out.println("All done!");
            // m.dump(dumpPath);

            // String modelPath = testFolder + "/out/model_dump.json";
            ModelicaSimulableModel msm = HandleModelica.buildSimulableModel(m);
            HandleModelica.buildModelica(msm, testFolder);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
