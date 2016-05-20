package adapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chinta.revathi on 25/04/16.
 */
public class Adapter {

    Database db = null;

    public Adapter(String url, String username, String password){
        if(db == null) {
            db = new Database(url, username, password);
        }
    }

    protected Database getDbConnection(){
        return db;
    }

    public void create_database() throws JSONException, SQLException {
        getDbConnection().create_table_in_database();
    }

    public void insert(String name, JSONObject json) throws SQLException {
        getDbConnection().insert(name, json.toString());
    }

    public void update(String name, JSONObject json) throws SQLException{
        getDbConnection().update(name, json.toString());
    }

    public void delete(String name) throws SQLException{
        getDbConnection().delete(name);
    }


    protected JSONObject fetchJSONTemplate(String name)
            throws JSONException, SQLException, JsonValidationException {
       return getDbConnection().fetch_template_json(name);
    }

    /**
     * @param name
     * @param filepath
     * @throws SQLException
     * @throws JsonValidationException
     * @throws JSONException
     * @throws ParseException
     * @throws IOException
     *
     * This method with fetch sample json from db and convert it to csv in the specified file path.
     */
    public void fetch_sample_csv(String name, String filepath)
            throws SQLException, JsonValidationException, JSONException, ParseException, IOException {
        JSONObject jsonTemplate = fetchJSONTemplate(name);
        convert_json_to_csvFile(filepath, jsonTemplate);
    }

    /**
     * @param jsonObject
     * @return
     * @throws JSONException
     * @throws IOException
     * JSON -> CSV
     * input: json that has to be converted,
     * output: string including \n that has to be written to file
     * it includes headers in hierarchical format
     */
    public String convert_json_to_csv(JSONObject jsonObject) throws JSONException, IOException {
        JsonToCsv jsonToCsv = new JsonToCsv();
        List<String> values  = jsonToCsv.convertJSONtoCSV(jsonObject);
        List<String> header = get_header_lines(jsonObject);
        String csv = "";
        for(String s : header){
            csv +=s;    csv += "\n";
        }
        for(String s : values){
            csv += s + "\n";
        }
        return csv;
    }

    public void convert_json_to_csvFile(String filepath, JSONObject jsonObject) throws JSONException, IOException {
        String csv = convert_json_to_csv(jsonObject);
        BufferedWriter bw = new BufferedWriter(new FileWriter(filepath));
        bw.write(csv, 0, csv.length());
        bw.close();
    }

    /**
     * @param csv_file
     * @param jsonName
     * @return
     * @throws JSONException
     * @throws IOException
     * convert_csv_to_json converts a given file into a list of json objects
     * fetches json template from db by jsonName
     * It validates the datatypes of provided values to those of the values present in jsonTemplate
     */
    public List<JSONObject> convert_csv_to_json(File csv_file, String jsonName)
            throws JSONException, IOException, JsonValidationException, SQLException {
        if(!validate_file_type(csv_file)){
            throw new JsonValidationException("provided file is not csv");
        }
        JSONObject jsonTemplate = fetchJSONTemplate(jsonName);
        CsvToJson csvToJson = new CsvToJson();
        csvToJson.set_template(jsonTemplate);
        csvToJson.know_headers(jsonTemplate);
        String[] headers = csvToJson.getKeyStr().split(",");
        List<JSONObject> jsonObj = csvToJson.convertCSVtoJson(csv_file, headers, get_header_lines(jsonTemplate).size());
        if(!validate_json_objects(jsonTemplate, jsonObj)){
            throw new JsonValidationException("provided values in csv are not of same type as in template ");
        }else{
            System.out.println("validation success.. :) ");
            return jsonObj;
        }
    }

    /**
     *
     * @param jsonTemplate
     * @throws JSONException
     */
    private static List<String> get_header_lines(JSONObject jsonTemplate)throws JSONException{
        JsonToCsv jsonToCsv = new JsonToCsv();
        return jsonToCsv.get_header_lines(jsonTemplate);
    }

    /**
     *
     * @param jsonTemplate
     * @param resultObjects
     * @return
     * @throws JSONException
     */
    private static boolean validate_json_objects(JSONObject jsonTemplate, List<JSONObject> resultObjects) throws JSONException{
        for( JSONObject result : resultObjects ) {
            Iterator<String> it = jsonTemplate.keys();
            while (it.hasNext()) {
                String key = it.next();
                Object val1 = jsonTemplate.get(key);
                Object val2 = result.get(key);
                if((val1.getClass() != val2.getClass())){
                    if((val1 instanceof Integer) && (val2 instanceof String)){
                        try {
                            Integer.parseInt(val2.toString());
                        }catch( NumberFormatException e) {
                            return false;
                        }
                    }
                }
                if(val2 instanceof JSONArray || val2 instanceof JSONObject){
                    List<JSONObject> temp  = new ArrayList<JSONObject>();
                    if(val2 instanceof JSONObject){
                        temp.add((JSONObject)val2);
                    }else {
                        for (int i = 0; i < ((JSONArray) val2).length(); i++) {
                            temp.add((JSONObject) ((JSONArray) val2).get(i));
                        }
                    }
                    if(val1 instanceof JSONObject) {
                        if(!validate_json_objects((JSONObject) val1, temp))
                            return false;
                    }else if(val1 instanceof JSONArray){
                        if(!validate_json_objects((JSONObject)(((JSONArray) val1).get(0)), temp))
                            return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean validate_file_type(File file){
        return file.getName().toUpperCase().endsWith(".CSV");
    }
}
