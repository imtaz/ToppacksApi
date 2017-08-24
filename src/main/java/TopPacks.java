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
    private String getUrlData(String url){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
        catch (IOException e){
            return null;
        }
    }
    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        int j;
        do {
            System.out.println("1:Search\n2:Import\n3:TopPacks\n4:Exit");
            j = scan.nextInt();
            if (j == 1) {
                scan.nextLine();
                String searchOutput = searchRepositories();
                System.out.println(searchOutput);
            }
            else if (j == 2) {
                try {
                    String importedPackages = importContent();
                    arrayToppacks.append(importedPackages);
                    System.out.println(importedPackages+"\n");
                }
                catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }/*
            else if (j == 3) {
                String topPackages = getTopPacks();
                System.out.println(topPackages);
            } */
        }while(j<4);
    }

    private static String searchRepositories() {
        Scanner scan = new Scanner(System.in);
        System.out.println("\nEnter the keyword");
        String keyword = scan.nextLine();
        String url = "https://api.github.com/search/repositories?q=" + keyword + "&sort=stars&order=desc";
        TopPacks example = new TopPacks();
        String response = example.getUrlData(url);
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonResponse = (JSONObject) parser.parse(response);
            JSONArray repositoryList = (JSONArray) jsonResponse.get("items");
            StringBuilder searchResult = new StringBuilder();
            for (Object aRepositoryList : repositoryList) {
                JSONObject item = (JSONObject) aRepositoryList;
                searchResult.append(getData(item));
            }
            return searchResult.toString();
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
    private static String importContent(){
        Scanner scan = new Scanner(System.in);
        System.out.println("\nEnter the id of the repository");
        Long id = scan.nextLong();
        String u = getDetails(id);
        String url = "https://api.github.com/repos/"+u+"/contents/package.json";
        String packageJsonContent;
        JSONObject packageJsonItems;
        JSONObject jsonResponse;
        JSONObject packages;
        StringBuffer packagesArray = new StringBuffer();
        TopPacks example = new TopPacks();
        JSONParser parser = new JSONParser();
        String response = example.getUrlData(url);
        try {
            jsonResponse = (JSONObject) parser.parse(response);
        }
        catch (ParseException e){
            jsonResponse = null;
            System.out.println(e.getMessage());
        }
        String downloadUrl = (String) jsonResponse.get("download_url");
        packageJsonContent =  example.getUrlData(downloadUrl);
        try {
            packageJsonItems = (JSONObject) parser.parse(packageJsonContent);
        }
        catch (ParseException e){
            System.out.println(e.getMessage());
            packageJsonItems = null;
        }
        packages= (JSONObject) packageJsonItems.get("dependencies");
        Set<String > packageNames = packages.keySet();
        for(String ls:packageNames){
            packagesArray.append(ls);
            packagesArray.append(",\n");
        }
        return packagesArray.toString();
    }
    private static String getDetails(Long id){
        String url = "https://api.github.com/repositories/"+id;
        TopPacks example = new TopPacks();
        try {
            String response = example.getUrlData(url);
            JSONParser parser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) parser.parse(response);
            String name = (String)jsonResponse.get("name");
            JSONObject ownerObj = (JSONObject) jsonResponse.get("owner");
            String owner = (String)ownerObj.get("login");
            return owner+"/"+name;
        } catch (ParseException e){
            System.out.println(e.getMessage());
            return "";
        }
    }
    private static String getTopPacks(){
        Map<String, Integer> occurrences = new HashMap<>();
        String[] splitWords = (arrayToppacks.toString()).split(",\n");
        for (String word : splitWords) {
            Integer oldCount = occurrences.get(word);
            if (oldCount == null) {
                oldCount = 0;
            }
            occurrences.put(word, oldCount + 1);
        }
        String[] topPackages = sortPackages(occurrences);
        StringBuffer topTen = new StringBuffer();
        if (topPackages != null) {
            int packageLength = topPackages.length;
            if (packageLength > 10)
                packageLength = 10;
            try (PrintStream out = new PrintStream(new FileOutputStream("TopPacks.txt"))) {
                for (int i = 0; i < packageLength; i++) {
                    if (i == 0)
                        out.append("Top Packs\n\n");
                    out.append(topPackages[i]);
                    out.append("\n");
                    topTen.append(topPackages[i]).append("\n");
                }
                System.out.println();
                return topTen.toString();
            } catch (IOException e) {
                e.getMessage();
                return null;
            }
        } else {
            return "No package.json files found. ";
        }
    }
    private static String[] sortPackages(Map occurrences){

        ValueComparator valueComparator = new ValueComparator(occurrences);
        TreeMap<String, Integer> sortedMap = new TreeMap<>(valueComparator);
        sortedMap.putAll(occurrences);
        String sorted4 = sortedMap.entrySet().toString();
        String sorted2 = sorted4.replace(" ","");
        String sorted1 = sorted2.replace("[","");
        String sorted = sorted1.replace("]","");
        String sortedPackages[] =  sorted.split(",");
        if(sortedPackages[0].equals("=1")){
            return null;
        }
        else {
            return sortedPackages;
        }
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
        }
    }
}