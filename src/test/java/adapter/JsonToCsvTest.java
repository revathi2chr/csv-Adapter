package adapter;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by chinta.revathi on 26/04/16.
 */
public class JsonToCsvTest {

    private JsonToCsv jsonToCsv;

    JSONObject nullJson = new JSONObject();
    JSONObject simpleJson = new JSONObject();
    JSONObject nestedJson = new JSONObject();

    @Before
    public void setUp() throws JSONException{
        jsonToCsv = new JsonToCsv();
        simpleJson.put("a", "A");       simpleJson.put("b", "B");
        nestedJson.put("c", "C");       nestedJson.put("d", simpleJson);
    }

    /**
     *  Method : get_header_lines
     */

    @Test
    public void shouldReturnNoRowsIfJsonIsNull() throws JSONException {
        List<String> expected_response = new ArrayList<>();
        expected_response.add("");
        assertThat(jsonToCsv.get_header_lines(nullJson), is(expected_response));
    }

    @Test
    public void shouldReturnListOfStringsForASimlpeJson() throws JSONException{
        List<String> expected_response = new ArrayList<>();
        expected_response.add("a,b");
        assertThat(jsonToCsv.get_header_lines(simpleJson), is(expected_response));
    }

    @Test
    public void shouldReturnListOfStringsForANestedJson() throws JSONException {
        List<String> expected_response = new ArrayList<>();
        expected_response.add("c,d,,");       expected_response.add(",a,b");
        assertThat(jsonToCsv.get_header_lines(nestedJson), is(expected_response));
    }

    /**
     * Method: convertJSONtoCSV
     */

    @Test
    public void shouldNotCallRecursivelyForSimpleDataTypes() throws JSONException {
        simpleJson.put("d", "D");
        List<String> expected_response = new ArrayList<String>();
        expected_response.add("A,B,D,");
        assertThat(jsonToCsv.convertJSONtoCSV(simpleJson), is(expected_response));
    }

    @Test
    public void shouldCallRecursivelyForJsonObjectAndMerge() throws JSONException {
        List<String> expected_response = new ArrayList<String>();
        expected_response.add("C,A,B,");
        assertThat(jsonToCsv.convertJSONtoCSV(nestedJson), is(expected_response));
    }

    @Test
    public void shouldCallRecursivelyForJsonArrayAndMerge() throws JSONException {
        List<JSONObject> array = new ArrayList<JSONObject>();
        array.add(simpleJson);  array.add(simpleJson);
        nestedJson.put("d", array);
        nestedJson.put("z","Z");
        List<String> expected_response = new ArrayList<String>();
        expected_response.add("C,A,B,Z,");    expected_response.add(",A,B,,");
        List<String> arr = jsonToCsv.convertJSONtoCSV(nestedJson);
        System.out.println(arr.get(0)); System.out.println(arr.get(1));
        assertThat(jsonToCsv.convertJSONtoCSV(nestedJson), is(expected_response));
    }
}
