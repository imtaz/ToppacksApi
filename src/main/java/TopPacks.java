import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TopPacks {
    private static StringBuffer arrayToppacks = new StringBuffer();
    private OkHttpClient client = new OkHttpClient();

    private String run(String url){
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
        catch (IOException e){
            return null;
        }
    }
    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        String searchOutput;
        int j;
        do {
            System.out.println("1:Search\n2:Import\n3:TopPacks\n4:Exit");
            j = scan.nextInt();
            if (j == 1) {
                scan.nextLine();
                System.out.println("\nEnter the keyword");
                String keyword = scan.nextLine();
                try {
                    searchOutput = search(keyword);
                } catch (Exception e) {
                    searchOutput = "";
                    System.out.println(e.getMessage());
                }
                try (PrintStream out = new PrintStream(new FileOutputStream("Search_op.txt"))) {
                    out.append(searchOutput);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
            else if (j == 2) {
                scan.nextLine();
                System.out.println("\nEnter the id of the repository");
                Long id = scan.nextLong();
                String importedPackages;
                try {
                    importedPackages = importContent(id);
                    arrayToppacks.append(importedPackages);
                    System.out.println(importedPackages+"\n");
                }
                catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
            else if (j == 3) {
                Map<String, Integer> occurrences = new HashMap<>();
                String[] splitWords = (arrayToppacks.toString()).split(",\n");
                for (String word : splitWords) {
                    Integer oldCount = occurrences.get(word);
                    if (oldCount == null) {
                        oldCount = 0;
                    }
                    occurrences.put(word, oldCount + 1);
                }
                String[] topPackages = topPacks(occurrences);
                if (topPackages != null) {
                    int packageLength = topPackages.length;
                    if (packageLength > 10)
                        packageLength = 10;
                    try (PrintStream out = new PrintStream(new FileOutputStream("TopPacks.txt"))) {
                        System.out.println(topPackages[0]);
                        for (int i = 0; i < packageLength; i++) {
                            if (i == 0)
                                out.append("Top Packs\n\n");
                            out.append(topPackages[i]);
                            out.append("\n");
                            System.out.println(topPackages[i]);
                        }
                        System.out.println();
                    } catch (IOException e) {
                        e.getMessage();
                    }
                } else {
                    System.out.println("No package.json files found.");
                }
            }
        }while(j<4);
    }
    private static String search(String keyword) {
        String url = "https://api.github.com/search/repositories?q=" + keyword + "&sort=stars&order=desc";
        TopPacks example = new TopPacks();
        String response = example.run(url);
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(response);
            JSONArray jsonArray = (JSONArray) jsonObject.get("items");
            StringBuilder result = new StringBuilder();
            for (int i = 0 ; i < jsonArray.size() ;i++) {
                JSONObject item = (JSONObject) jsonArray.get(i);
                result.append(getData(item));
            }
            return result.toString();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }
    private static String getData(JSONObject json) {
        long starCount = (Long) json.get("stargazers_count");
        long forkCount = (Long) json.get("forks");
        String name = (String)json.get("name");
        JSONObject ownerObject = (JSONObject) json.get("owner");
        String ownerName = (String)ownerObject.get("login");
        return ("\nName : " + name + "\nOwner Name: " + ownerName + "\nStar Count : " + starCount + "\nFork Count: " + forkCount + "\n\n");
    }
    private static String getDetails(Long id){
        String url = "https://api.github.com/repositories/"+id;
        TopPacks example = new TopPacks();
        try {
            String response = example.run(url);
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(response);
            String name = (String)json.get("name");
            JSONObject owner_obj = (JSONObject) json.get("owner");
            String owner = (String)owner_obj.get("login");
            return owner+"/"+name;
        }
        catch (ParseException e){
            System.out.println(e.getMessage());
            return "";
        }
    }
    private static String importContent(Long id){
        String u = getDetails(id);
        String url = "https://api.github.com/repos/"+u+"/contents/package.json";
        String packageJsonContent;
        JSONObject packageJsonObjects;
        JSONObject jsonObject;
        JSONObject packages;
        StringBuffer packagesArray = new StringBuffer();
        TopPacks example = new TopPacks();
        JSONParser parser = new JSONParser();
        String response = example.run(url);
        try {
            jsonObject = (JSONObject) parser.parse(response);
        }
        catch (ParseException e){
            jsonObject = null;
            System.out.println(e.getMessage());
        }
        String downloadUrl = (String) jsonObject.get("download_url");
        packageJsonContent =  example.run(downloadUrl);
        try {
            packageJsonObjects = (JSONObject) parser.parse(packageJsonContent);
        }
        catch (ParseException e){
            System.out.println(e.getMessage());
            packageJsonObjects = null;
        }
        packages= (JSONObject) packageJsonObjects.get("dependencies");
        Set<String > packageNames = packages.keySet();
        for(String ls:packageNames){
            packagesArray.append(ls);
            packagesArray.append(",\n");
        }
        return packagesArray.toString();
    }
    private static String[] topPacks(Map occurrences){

        ValueComparator valueComparator = new ValueComparator(occurrences);
        TreeMap<String, Integer> sorted_map = new TreeMap<>(valueComparator);
        sorted_map.putAll(occurrences);
        String sorted4 = sorted_map.entrySet().toString();
        String sorted2 = sorted4.replace(" ","");
        String sorted1 = sorted2.replace("[","");
        String sorted = sorted1.replace("]","");
        String sortedpacks[] =  sorted.split(",");
        if(sortedpacks[0].equals("=1"))
            return null;
        else
            return sortedpacks;
    }
}
class ValueComparator implements Comparator<String> {
    Map<String, Integer> base;
    public ValueComparator(Map<String, Integer> base) {
        this.base = base;
    }
    public int compare(String a, String b) {
        if (base.get(a) > base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}