import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class PostmanGenerator {

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(new File("endpoints.txt"));
            List<Folder> folders = new ArrayList<>();
            Folder currentFolder = null;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                
                if (line.isEmpty() || line.startsWith("=") || line.startsWith("-") || line.startsWith("Microfinance")) {
                    continue;
                }

                // If line contains a parenthesis, it's a category header
                if (line.contains("(") && line.contains(")")) {
                    String folderName = line.substring(0, line.indexOf("(")).trim();
                    String basePath = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
                    currentFolder = new Folder(folderName, basePath);
                    folders.add(currentFolder);
                } 
                // Otherwise it's an endpoint
                else if (currentFolder != null && (line.startsWith("GET") || line.startsWith("POST") || line.startsWith("PUT") || line.startsWith("DELETE") || line.startsWith("PATCH"))) {
                    String[] parts = line.split("\\s+");
                    String method = parts[0];
                    String path = parts[1];
                    currentFolder.endpoints.add(new Endpoint(method, path));
                }
            }
            scanner.close();

            // Generate JSON
            PrintWriter out = new PrintWriter("postman_collection.json");
            out.println("{");
            out.println("  \"info\": {");
            out.println("    \"name\": \"Microfinance Loan System API\",");
            out.println("    \"schema\": \"https://schema.getpostman.com/json/collection/v2.1.0/collection.json\"");
            out.println("  },");
            out.println("  \"item\": [");

            for (int i = 0; i < folders.size(); i++) {
                Folder f = folders.get(i);
                out.println("    {");
                out.println("      \"name\": \"" + f.name + "\",");
                out.println("      \"item\": [");
                
                for (int j = 0; j < f.endpoints.size(); j++) {
                    Endpoint ep = f.endpoints.get(j);
                    String fullPath = f.basePath.equals("/") ? ep.path : (f.basePath.endsWith("/") ? f.basePath.substring(0, f.basePath.length() - 1) : f.basePath) + (ep.path.startsWith("/") ? "" : "/") + ep.path;
                    String[] pathSegments = fullPath.startsWith("/") ? fullPath.substring(1).split("/") : fullPath.split("/");
                    
                    out.println("        {");
                    out.println("          \"name\": \"" + ep.method + " " + ep.path + "\",");
                    out.println("          \"request\": {");
                    out.println("            \"method\": \"" + ep.method + "\",");
                    out.println("            \"header\": [ { \"key\": \"Authorization\", \"value\": \"Bearer {{token}}\", \"type\": \"text\" } ],");
                    out.println("            \"url\": {");
                    out.println("              \"raw\": \"http://localhost:8080" + fullPath + "\",");
                    out.println("              \"protocol\": \"http\",");
                    out.println("              \"host\": [\"localhost\"],");
                    out.println("              \"port\": \"8080\",");
                    out.println("              \"path\": [");
                    for(int k=0; k<pathSegments.length; k++) {
                        out.print("                \"" + pathSegments[k] + "\"");
                        if(k < pathSegments.length - 1) out.println(","); else out.println();
                    }
                    out.println("              ]");
                    out.println("            }");
                    out.println("          }");
                    out.print("        }");
                    if (j < f.endpoints.size() - 1) out.println(","); else out.println();
                }
                out.println("      ]");
                out.print("    }");
                if (i < folders.size() - 1) out.println(","); else out.println();
            }

            out.println("  ]");
            out.println("}");
            out.close();

            System.out.println("postman_collection.json successfully generated.");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    static class Folder {
        String name;
        String basePath;
        List<Endpoint> endpoints = new ArrayList<>();
        Folder(String name, String basePath) {
            this.name = name;
            this.basePath = basePath;
        }
    }

    static class Endpoint {
        String method;
        String path;
        Endpoint(String method, String path) {
            this.method = method;
            this.path = path;
        }
    }
}
