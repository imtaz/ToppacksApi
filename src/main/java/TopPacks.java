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
public class TopPacks {
    private static int c=0;

    /**
     *
     */
    private static StringBuffer arr = new StringBuffer();
    private OkHttpClient client = new OkHttpClient();

    private String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
    public static void main(String[] args) throws Exception {
        Scanner ip = new Scanner(System.in);
        String searchoutput;
        String keyword = ip.nextLine();
        String url = "https://api.github.com/search/repositories?q="+keyword+"&sort=stars&order=desc";
        searchoutput = search(url);
        try(PrintStream out = new PrintStream(new FileOutputStream("Search_op1.txt"))){
            out.append(searchoutput);
        }
        Map<String, Integer> occurrences = new HashMap<>();
        String[] splitWords = (arr.toString()).split(",\n");
        for ( String word : splitWords ) {
            Integer oldCount = occurrences.get(word);
            if ( oldCount == null ) {
                oldCount = 0;
            }
            occurrences.put(word, oldCount + 1);
        }
        String[] toppack = toppacks(occurrences);
        if (toppack!=null) {
            int max = toppack.length;
            if (max > 10)
                max = 10;
            try (PrintStream out = new PrintStream(new FileOutputStream("TopPacks1.txt"))) {
                System.out.println(toppack[0]);
                for (int i = 0; i < max; i++) {
                    if (i == 0)
                        out.append("Top Packs\n\n");
                    out.append(toppack[i]);
                    out.append("\n");
                    System.out.println(toppack[i]);
                }
            }
        }
        else{
            System.out.println("No package.json files found in the directories related to the keyword.");
        }
    }
    private static String search(String keyword) throws Exception{
        TopPacks example = new TopPacks();
        String response = example.run(keyword);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(response);
        JSONArray json1 = (JSONArray) json.get("items");
        StringBuilder result = new StringBuilder();
        for (int i =0; i<1 ; i++) {
            result.append(getRepositories(json1));
        }
        return result.toString();
    }
    private static String getRepositories(JSONArray json1) throws Exception {
        try {
            JSONObject json1_1 = (JSONObject) json1.get(c);
            String result = getRep(json1_1);
            c++;
            return result;
        }
        catch (Exception e){
            return null;
        }

    }
    private static String getRep(JSONObject json) throws Exception {
        String name = (String)json.get("name");
        long strcnt = (Long) json.get("stargazers_count");
        long frkcnt = (Long) json.get("forks");
        JSONObject owner_obj = (JSONObject) json.get("owner");
        String owner = (String)owner_obj.get("login");
        String imported = importContent(owner,name);
        if(imported==null) {
            arr.append("");
        }
        else
            arr.append(imported);
        return ("\nName : " + name + "\nOwner Name: " + owner + "\nStar Count : " + strcnt + "\nFork Count: " + frkcnt + "\n\n");
    }
    private static String importContent(String owner,String name) throws Exception {
        String url = "https://api.github.com/repos/"+owner+"/"+name+"/contents/package.json";
        TopPacks example = new TopPacks();
        String response = example.run(url);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(response);
        JSONObject packages;
        String s = (String) json.get("download_url");
        String packagecontents;
        StringBuffer packagesarr = new StringBuffer();
        try{
           packagecontents =  example.run(s);
           JSONObject packagejson = (JSONObject) parser.parse(packagecontents);
           packages= (JSONObject) packagejson.get("dependencies");
            Set<String > list = packages.keySet();
            for(String ls:list){
                packagesarr.append(ls);
                packagesarr.append(",\n");
            }
            return packagesarr.toString();
        }
        catch(NullPointerException e){
            return null;
        }
    }
    private static String[] toppacks(Map occurrences){

        ValueComparator bvc = new ValueComparator(occurrences);
        TreeMap<String, Integer> sorted_map = new TreeMap<>(bvc);
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
    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    public int compare(String a, String b) {
        if (base.get(a) > base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}