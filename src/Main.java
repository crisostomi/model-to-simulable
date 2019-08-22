import Model.Model;
import ModelicaSimulableModel.ModelicaSimulableModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        String projectFolder = "/home/don/Dropbox/Tesisti/software/development";
        String testFolder = projectFolder + "/test-cases/test-case-4";

        String kbPath = testFolder + "/in/galactose.sbml";
        String xmlPath = testFolder + "/in/quantitative.xml";
        // String logPath = testFolder + "/out/log.txt";
        String dumpPath = testFolder + "/out/model_dump.json";

        String outputFolder = testFolder+"/simulable output";

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
            HandleModelica.buildModelica(msm, outputFolder);
            String correctOut = testFolder+"/correct output";
            Map<String, String> differences = Test.getDifferences(outputFolder,correctOut);
            for (Map.Entry<String, String> entry : differences.entrySet()) {
                System.out.println(entry.getKey() + "/" + entry.getValue());
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
