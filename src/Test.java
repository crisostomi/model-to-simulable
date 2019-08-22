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
        String cmd = "diff "+file1+" "+file2;
        Runtime run = Runtime.getRuntime();
        Process pr = null;
        pr = run.exec(cmd);
        pr.waitFor();
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line = "";
        StringBuilder difference = new StringBuilder();
        while ((line=buf.readLine())!=null) {
            difference.append(line);
        }
        return difference.toString();
    }

    public static Map<String, String> getDifferences(String out, String correctOut) throws IOException, InterruptedException {

        File dir = new File(out);
        File correctDir = new File(correctOut);
        File[] outDirListing = dir.listFiles();
        Map<String, String> differences = new HashMap<>();
        if (outDirListing != null) {
            for (File child : outDirListing) {
                String difference = getFileDifferences(child.toString(), correctDir+"/"+child.toString());
                if (difference != ""){
                    differences.put(child.toString(), difference);
                }
            }
        }
        return differences;
    }
}
