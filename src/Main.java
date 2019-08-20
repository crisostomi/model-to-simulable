import ModelicaSimulableModel.ModelicaSimulableModel;

public class Main {
    public static void main(String[] args) {
        String projectFolder = "/home/scacio/Dropbox/Tesisti/software/development/KnowledgeAcquisition";
        String testFolder = projectFolder + "/test-cases/test-case-4";

        String modelPath = testFolder + "/out/model_dump.json";
        try {
            ModelicaSimulableModel m = HandleModel.loadModel(modelPath);
            HandleModel.buildModelica(m, testFolder);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
