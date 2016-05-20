package adapter;

import org.json.CDL;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.*;

/**
 * Created by chinta.revathi on 11/04/16.
 * works perfect.. congrats :)
 */
class JsonToCsv {

    /**
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    protected List<String> get_header_lines(JSONObject jsonObject) throws JSONException {
        List<String> result = new ArrayList<String>();
        result.add("");
        Iterator<String> it = jsonObject.keys();
        while (it.hasNext()) {
            String key = it.next();
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                List<String> in = get_header_lines((JSONObject) value);
                if (result.get(0).endsWith(",")) {
                    result.set(0, result.get(0) + key);
                } else if (result.get(0) == "") {
                    result.set(0, key);
                } else {
                    result.set(0, result.get(0) + "," + key);
                }
                result = merge_string_lists(result, in);
            } else if (value instanceof JSONArray) {
                List<String> in = get_header_lines(((JSONArray) value).getJSONObject(0));
                if (result.get(0).endsWith(",")) {
                    result.set(0, result.get(0) + key);
                } else if (result.get(0) == "") {
                    result.set(0, key);
                } else {
                    result.set(0, result.get(0) + "," + key);
                }
                result = merge_string_lists(result, in);

            } else {
                if (result.get(0) == "") {
                    result.set(0, key);
                } else if (result.get(0).endsWith(",")) {
                    result.set(0, result.get(0) + key);
                } else {
                    result.set(0, result.get(0) + "," + key);
                }
                for (int i = 1; i < result.size(); i++) {
                    result.set(i, result.get(i) + ",");
                }
            }
        }
        return result;
    }

    /**
     * convertJSONtoCSV method converts all json objects into strings of values present in it.
     * it considers key values in a.b.c format.
     * So, headers have to found separately
     */
    protected List<String> convertJSONtoCSV(JSONObject obj) throws JSONException {
        List<String> lines = new ArrayList<String>();
        Iterator<String> it = obj.keys();
        while (it.hasNext()) {
            Object value = obj.get(it.next());
            if (value instanceof JSONObject) {
                List<String> t = convertJSONtoCSV((JSONObject) value);
                lines = merge_string_arrays(lines, t);
            } else if (value instanceof JSONArray) {
                if (check_for_nested_json(((JSONArray) value).getJSONObject(0))) {
                    List<String> list = new ArrayList<String>();
                    for (int i = 0; i < ((JSONArray) value).length(); i++) {//JSONObject temp : (ArrayList<JSONObject>) value) {
                        List<String> s = convertJSONtoCSV(((JSONArray) value).getJSONObject(i));
                        for (String st : s) {
                            list.add(st);
                        }
                    }
                    lines = merge_string_arrays(lines, list);
                } else {
                    String[] t = CDL.toString((JSONArray) value).split("\\n");
                    List<String> x_lines = Arrays.asList(Arrays.copyOfRange(t, 1, t.length));
                    for (int i = 0; i < x_lines.size(); i++) {
                        x_lines.set(i, x_lines.get(i) + ",");
                    }
                    lines = merge_string_arrays(lines, x_lines);
                }
            } else {
                String temp = "";
                if (!lines.isEmpty()) {
                    temp = lines.get(0);
                }
                temp += value + ",";
                if (!lines.isEmpty()) {
                    lines.set(0, temp);
                    int num = temp.split(",").length;
                    for (int k = 1; k < lines.size(); k++) {
                        String s = "";
                        int comma_present = lines.get(k).split(",").length;
                        for (int z = 0; z < num - comma_present; z++) {
                            s += ",";
                        }
                        lines.set(k, lines.get(k) + s);
                    }
                } else {
                    lines.add(temp);
                }
            }
        }
        return lines;
    }

    private List<String> merge_string_lists(List<String> x, List<String> y) {
        List<String> z = new ArrayList<String>();
        z.add(x.get(0));
        int no_of_fields = y.get(0).split(",").length - 1;
        if (!y.get(0).endsWith(",")) {
            no_of_fields++;
        }
        String com = "";
        for (int i = 0; i < no_of_fields; i++) {
            com += ",";
        }
        z.set(0, z.get(0) + com);
        if (x.size() <= y.size()) {
            for (int i = 1; i < x.size(); i++) {
                z.add("" + x.get(i) + y.get(i - 1));
            }
            int num_of_fields = x.get(0).split(",").length - 1;
            String comma = "";
            for (int i = 0; i < num_of_fields; i++) {
                comma += ",";
            }
            for (int i = x.size(); i <= y.size(); i++) {
                z.add(comma + y.get(i - 1));
            }
        } else if (x.size() > y.size()) {
            for (int i = 1; i <= y.size(); i++) {
                if (x.get(i).endsWith(",")) {
                    z.add("" + x.get(i) + y.get(i - 1));
                } else {
                    z.add("" + x.get(i) + "," + y.get(i - 1));
                }
            }
            int num_of_fields = y.get(0).split(",").length + 1;
            String comma = "";
            for (int i = 0; i < num_of_fields; i++) {
                comma += ",";
            }
            for (int i = y.size() + 1; i < x.size(); i++) {
                z.add(x.get(i) + comma);
            }
        }
        return z;
    }

    private List<String> merge_string_arrays(List<String> x, List<String> y) {
        List<String> z = new ArrayList<String>();
        if (x.size() <= y.size()) {
            for (int i = 0; i < x.size(); i++) {
                z.add("" + x.get(i) + y.get(i));
            }
            String comma = "";
            if (!x.isEmpty()) {
                int num_of_fields = x.get(0).split(",").length;
                for (int i = 0; i < num_of_fields; i++) {
                    comma += ",";
                }
            }
            for (int i = x.size(); i < y.size(); i++) {
                z.add(comma + y.get(i));
            }
        } else if (x.size() > y.size()) {
            for (int i = 0; i < y.size(); i++) {
                z.add("" + x.get(i) + y.get(i));
            }
            int num_of_fields = y.get(0).split(",").length - 1;
            String comma = "";
            for (int i = 0; i < num_of_fields; i++) {
                comma += ",";
            }
            for (int i = y.size(); i < x.size(); i++) {
                z.add(x.get(i) + comma);
            }
        }
        return z;
    }

    private boolean check_for_nested_json(JSONObject obj) throws JSONException {
        Iterator<String> it = obj.keys();
        while (it.hasNext()) {
            Object value = obj.get(it.next());
            if (value instanceof JSONObject || value instanceof JSONArray) {
                return true;
            }
        }
        return false;
    }
}
