import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Test {

    public static String getFileDifferences(String file1, String file2) throws IOException, InterruptedException {

        String cmd = "diff " + file1 + " " + file2 + "";
        Process process = Runtime.getRuntime().exec(cmd);
        StringBuilder output = new StringBuilder();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        String line = "";
        while ((line = reader.readLine()) != null) {
            output.append(line+"\n");
        }

        return output.toString();
    }

    public static Map<String, String> getDifferences(String out, String correctOut) throws IOException, InterruptedException {
        File dir = new File(out);
        File correctDir = new File(correctOut);
        File[] outDirListing = dir.listFiles();
        Map<String, String> differences = new HashMap<>();
        if (outDirListing != null) {
            for (File child : outDirListing) {
                String difference = getFileDifferences(child.toString(), correctDir+"/"+child.getName());
                if (!difference.equals("")){
                    differences.put(child.toString(), difference);
                }
            }
        }
        return differences;
    }
}
