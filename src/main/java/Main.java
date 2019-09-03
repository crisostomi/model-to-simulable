import Model.Model;
import ModelicaSimulableModel.ModelicaSimulableModel;
import Util.CustomLogger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Main {
    public static final String ABUNDANCES_FILENAME = "abundances.tsv";
    public static final String REACTOME_FILENAME = "pathway.sbml";
    public static final String LOG_FILENAME = "log.txt";

    public static void main(String[] args) {

        String username = System.getProperty("user.name");
        String projectFolder = "/home/"+username+"/Dropbox/Tesisti/software";

        String testFolder = projectFolder + "/test-cases/urea/";
        String kbPath = testFolder + "/in/"+REACTOME_FILENAME;
        String tsvPath = testFolder +"/in/"+ ABUNDANCES_FILENAME;
        String logPath = testFolder + LOG_FILENAME;
        String dumpPath = testFolder + "/out/model_dump.xml";
//        String xmlPath = testFolder + "/in/quantitative.xml";

        CustomLogger.setup(logPath);

        try {
            Set<String> kbPaths = new HashSet<>();
            kbPaths.add(kbPath);
            // kbPaths.add(xmlPath);
            kbPaths.add(tsvPath);

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
