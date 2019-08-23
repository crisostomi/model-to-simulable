import DataTypes.ModelicaCode;
import Model.Model;
import ModelicaSimulableModel.ModelicaSimulableModel;
import SimulableModel.PreconditionsException;
import Util.Parameter2XML;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class HandleModelica {
    public static ModelicaSimulableModel buildSimulableModel(Model m) throws PreconditionsException {
        return new ModelicaSimulableModel(m);
    }

    public static void buildModelica(ModelicaSimulableModel m, String outputFolder)
                throws IOException, ParserConfigurationException, TransformerException
    {
        Map<String, ModelicaCode> modelicaCodeMap = m.getModules();

        for (Map.Entry<String, ModelicaCode> entry : modelicaCodeMap.entrySet()) {
            String fileName = entry.getKey();
            ModelicaCode code = entry.getValue();

            File f = new File(outputFolder + "/"+ fileName + ".mo");
            FileWriter fw = new FileWriter(f);
            fw.write(code.getCode());
            fw.close();
        }

        Parameter2XML.buildParametersXML(m.getUndefinedParameters(), outputFolder + "/parameters.xml");
    }

    public static ModelicaSimulableModel loadModel(String modelPath) throws IOException, PreconditionsException {
        Model m = Model.load(modelPath);
        return buildSimulableModel(m);
    }
}
