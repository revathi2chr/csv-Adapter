package adapter;
/**
 * Created by chinta.revathi on 31/03/16.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.util.*;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

class CsvToJson {
    String cvsSplitBy = ",";
    String keySplitBy = "\\.";
    String line = "";
    JSONObject json_template;

    private String keyStr = "";

    protected void set_template(JSONObject obj){
        json_template = new JSONObject();
        json_template = obj;
    }

    /**
     *
     * @param file
     * @param headers
     * @param skip
     * @return
     * @throws IOException
     * @throws JSONException
     */
    protected List<JSONObject> convertCSVtoJson(File file, String[] headers, int skip) throws IOException, JSONException {
        List<JSONObject> result = new ArrayList<JSONObject>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        int skip_line = 0;
        while(skip_line<skip){
            br.readLine();
            skip_line++;
        }
        while((line = br.readLine()) != null) {
            String[] values = line.split(cvsSplitBy, -1);
            if(!(line.startsWith(","))) {
                JSONObject csv_ob = new JSONObject();
                csv_ob = make_json_object(headers, values, csv_ob);
                result.add(csv_ob);
            } else {
                JSONObject csv_ob = result.get(result.size()-1);
                add_to_json_object(csv_ob, headers, line);
            }
        }
        return result;
    }

    /**
     * These methods are to know the headers in the json from the template
     */
    protected void know_headers(JSONObject jsonObject) throws JSONException{
        Iterator<String> keysItr = jsonObject.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = jsonObject.get(key);
            if(value instanceof JSONObject) {
                know_headers((JSONObject) value, key);
            }else if(value instanceof JSONArray || key == "y"){
                know_headers((JSONObject) (((JSONArray) value).get(0)), key);
            } else{
                appendToKeyStr(key);
            }
        }
        return;
    }

    private void know_headers(JSONObject jsonObject, String prefix) throws JSONException{
        Iterator<String> keysItr = jsonObject.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = jsonObject.get(key);
            String key1 = prefix + "." + key;

            if(value instanceof JSONObject) {
                know_headers((JSONObject) value, key1);
            }else if(value instanceof JSONArray || key == "y"){
                know_headers((JSONObject) (((JSONArray) value).get(0)), key1);
            }else {
                appendToKeyStr(key1);
            }
        }
        return;
    }

    private void add_to_json_object(JSONObject obj, String[] headers, String line) throws JSONException {
        String[] values = getValuesFromLine(line);
        int index = 0;
        while (index < values.length) {
            while (values[index].equalsIgnoreCase("")) {
                index++;
            }
            String[] keys = headers[index].split("\\.");
            JSONObject t1 = obj;
            int i;
            for (i = 0; i < keys.length - 2; i++) {
                    Object t = t1.get(keys[i]);
                    if(t instanceof JSONArray){
                        t1 = ((JSONArray) t).getJSONObject(((JSONArray) t).length()-1);
                    }else{
                        t1 = (JSONObject)t;
                    }
            }
            JSONArray arr = new JSONArray();
            JSONObject new_val_obj = set_new_value_object(t1.get(keys[i]), arr);
            new_val_obj.put(keys[++i], values[index]);
            arr.put(new_val_obj);
            index++;
            boolean flag = true;
            while (flag == true && index < headers.length) {
                String[] new_keys = headers[index].split("\\.");
                if (compareKeys(new_keys, keys)) {
                    if (new_keys.length == keys.length) {
                        new_val_obj.put(new_keys[i], values[index]);
                        arr.put(arr.length() - 1, new_val_obj);
                    } else {
                        JSONObject nObj = new_val_obj;
                        if (!nObj.has(new_keys[i])) {
                            JSONObject njson = new JSONObject();
                            njson.put(new_keys[i + 1], values[index]);
                            nObj.put(new_keys[i], njson);
                        } else {
                            Object njson = nObj.get(new_keys[i]);
                            if (njson instanceof JSONObject) {
                                ((JSONObject) njson).put(new_keys[i + 1], values[index]);
                            } else if (njson instanceof JSONArray) {
                                JSONObject a = (JSONObject) ((JSONArray) njson).get(((JSONArray) njson).length() - 1);
                                a.put(new_keys[i + 1], values[index]);
                            }
                        }
                        arr.put(arr.length() - 1, nObj);
                    }
                    index++;
                } else {
                    flag = false;
                }
            }
            add_object_to_array(obj, arr, keys);
        }
    }

    private JSONObject set_new_value_object(Object value, JSONArray arr){
        if (value instanceof JSONObject) {
            arr.put((JSONObject) value);
            return new JSONObject(value);
        } else if (value instanceof JSONArray){
            arr = (JSONArray) value;
            return new JSONObject();
        }
        return null;
    }

    private void add_object_to_array(JSONObject jsonObject, JSONArray jsonArray, String[] keys) throws JSONException{
        JSONObject object = jsonObject;
        int k;
        for (k = 0; k < keys.length - 2; k++) {
            Object temp = object.get(keys[k]);
            if(temp instanceof JSONObject){
                object = (JSONObject)temp;
            }else if(temp instanceof JSONArray){
                object = (JSONObject)((JSONArray) temp).get(((JSONArray) temp).length() - 1);
            }
        }
        object.put(keys[k], jsonArray);
    }

    private JSONObject make_json_object(String[] keys, String[] values, JSONObject csv_ob) throws JSONException{
        for(int i=0;i<keys.length;i++){
            String[] headers = keys[i].split(keySplitBy);
            if(headers.length == 1){
                csv_ob.put(headers[0], values[i]);
            } else {
                try{
                    make_inner_json(headers, csv_ob, values[i]);
                }catch(ArrayIndexOutOfBoundsException e){
                    e.printStackTrace();
                }
            }
        }
        return csv_ob;
    }

    private void make_inner_json(String[] keys, JSONObject obj, String val) throws JSONException{
        JSONObject value;
        if(!obj.has(keys[0])) {
            value = new JSONObject();
            obj.put(keys[0], value);
        } else {
            value = (JSONObject) obj.get(keys[0]);
        }
        if(keys.length != 2) {
            make_inner_json(Arrays.copyOfRange(keys, 1, keys.length), value, val);
        } else {
            value.put(keys[1], val);
        }
    }

    private String[] getValuesFromLine(String line){
        String[] values = ("prefix," + line).split(",");
        values = Arrays.copyOfRange(values, 1, values.length);
        return values;
    }

    private void appendToKeyStr(String key){
                if (keyStr == ""){
                    keyStr = key;
                }else {
                    keyStr = keyStr + "," + key;
                }
    }

    private boolean compareKeys(String[] new_keys, String[] keys){
        return (Arrays.equals(Arrays.copyOfRange(new_keys, 0, keys.length - 1), Arrays.copyOfRange(keys, 0, keys.length - 1)));
    }

    protected JSONObject getJson_template(){
        return json_template;
    }

    protected String getKeyStr(){
        return keyStr;
    }
}