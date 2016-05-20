package adapter;

/**
 * Created by chinta.revathi on 26/04/16.
 */

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(MockitoJUnitRunner.class)
public class CsvToJsonTest {

    @Mock
    private CsvToJson csvToJson;

    private File file;

    private int skip = 2;

    private String[] headers;

    @Before
    public void setUp() throws IOException{
        csvToJson = new CsvToJson();
        file = new File("temp.csv");
        BufferedWriter output = null;
        output = new BufferedWriter(new FileWriter(file));
        output.write("");
        output.write("");
        output.close();
    }

    /**
     * Method:  set_template
     */
    @Test
    public void shouldSetTemplateJson(){
        JSONObject object = new JSONObject();
        csvToJson.set_template(object);
        assertThat(csvToJson.getJson_template(), is(object));
    }
}
