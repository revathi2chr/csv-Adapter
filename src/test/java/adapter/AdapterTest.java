package adapter;


/**
 * Created by chinta.revathi on 26/04/16.
 */

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdapterTest {

    private Adapter adapter;

    private Database database;

    @Mock
    Database Db;

    String url = "";
    String username  = "";
    String password  = "";

    String expectedResponse = "";

    JSONObject nullJson = new JSONObject();
    JSONObject simpleJson = new JSONObject();
    JSONObject nestedJson = new JSONObject();
    JSONObject nested2Json = new JSONObject();
    JSONArray arrayJson = new JSONArray();

    @Before
    public void setUp() throws JSONException {
        adapter = new Adapter(url,username,password);
        Db = new Database(url, username, password);
        //adapter.db = Db;
        Db = Mockito.mock(Database.class);
        adapter.db = Db;
        simpleJson.put("a", "A");       simpleJson.put("b", "B");
        nestedJson.put("c", "C");       nestedJson.put("d", simpleJson);
        nested2Json.put("e", "E");      nested2Json.put("f", nestedJson);
        arrayJson.put(simpleJson);      arrayJson.put(simpleJson);

    }

    /**
     * Testing Method: convert_json_to_csv & convert_csv_to_json
     */

    @Test
    public void shouldReturnCsvStringForSimpleJsonAndResultToJson()
            throws JSONException, IOException, JsonValidationException, SQLException {
        simpleJson.put("d", "D");
        expectedResponse = "a,b,d\nA,B,D,\n";
        String response = adapter.convert_json_to_csv(simpleJson);
        assertThat(response, is(expectedResponse));

        File file = new File("temp.csv");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(response, 0, response.length());       bw.close();
        when(Db.fetch_template_json("simpleJson")).thenReturn(simpleJson);
        List<JSONObject> result = adapter.convert_csv_to_json(file, "simpleJson");
        assertThat(result.get(0).toString(), is(simpleJson.toString()));
    }

    @Test
    public void shouldReturnCsvForNestedJsonAndResultToJson()
            throws JSONException, IOException, JsonValidationException, SQLException{
        expectedResponse = "c,d,,\n,a,b\nC,A,B,\n";
        String response = adapter.convert_json_to_csv(nestedJson);
        assertThat(response, is(expectedResponse));

        File file = new File("temp.csv");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(response, 0, response.length());       bw.close();
        when(Db.fetch_template_json("nestedJson")).thenReturn(nestedJson);
        List<JSONObject> result = adapter.convert_csv_to_json(file, "nestedJson");
        assertThat(result.get(0).toString(), is(nestedJson.toString()));
    }

    @Test
    public void shouldReturnCsvForJsonContainingArrayandConvertResultToJson()
            throws JSONException, IOException, JsonValidationException, SQLException{
        JSONObject object = new JSONObject();
        object.put("def", "DEF");   object.put("xyz", arrayJson);
        expectedResponse = "def,xyz,,\n,a,b\nDEF,A,B,\n,A,B,\n";
        String response = adapter.convert_json_to_csv(object);
        assertThat(response, is(expectedResponse));

        File file = new File("temp.csv");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(response, 0, response.length());       bw.close();
        when(Db.fetch_template_json("object")).thenReturn(object);
        List<JSONObject> result = adapter.convert_csv_to_json(file, "object");
        assertThat(result.get(0).toString(), is(object.toString()));
    }

    @Test
    public void shouldReturnCsvFor2NestedJsonAndConvertBackToJson()
            throws JSONException, IOException, JsonValidationException, SQLException{
        expectedResponse = "e,f,\n,c,d,,\n,,a,b\nE,C,A,B,\n";
        String response = adapter.convert_json_to_csv(nested2Json);
        assertThat(response, is(expectedResponse));

        File file = new File("temp.csv");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(response, 0, response.length());       bw.close();
        when(Db.fetch_template_json("nested2Json")).thenReturn(nested2Json);
        List<JSONObject> result = adapter.convert_csv_to_json(file, "nested2Json");
        assertThat(result.get(0).toString(), is(nested2Json.toString()));
    }

}