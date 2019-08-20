import DataTypes.ModelicaCode;
import Model.Model;
import ModelicaSimulableModel.ModelicaSimulableModel;
import SimulableModel.PreconditionsException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class HandleModel {
    public static ModelicaSimulableModel buildSimulableModel(Model m) throws PreconditionsException {
        return new ModelicaSimulableModel(m);
    }

    public static void buildModelica(ModelicaSimulableModel m, String outputFolder)
                throws IOException {
        Map<String, ModelicaCode> modelicaCodeMap = m.getModules();

        for (Map.Entry<String, ModelicaCode> entry : modelicaCodeMap.entrySet()) {
            String fileName = entry.getKey();
            ModelicaCode code = entry.getValue();

            File f = new File(outputFolder + "/"+ fileName);
            FileWriter fw = new FileWriter(f);
            fw.write(code.getCode());
            fw.close();
        }

        File f = new File(outputFolder + "/"+ "parameters.xml");
        FileWriter fw = new FileWriter(f);
        fw.write(m.getParameters().getCode());
        fw.close();
    }

    public static ModelicaSimulableModel loadModel(String modelPath) throws IOException, PreconditionsException {
        Model m = Model.load(modelPath);
        return buildSimulableModel(m);
    }
}
