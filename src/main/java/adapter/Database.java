package adapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;

/**
 * Created by chinta.revathi on 25/04/16.
 */
public class Database{

    private Connection connection;

    public Database(String url, String username, String password){
        connection = getDBConnection(url, username, password);
    }

    public void create_table_in_database() throws JSONException, SQLException {
        Statement stmt;
        stmt = connection.createStatement();
        String drop_sql = "DROP TABLE IF EXISTS json_templates ;";
        String create_Table_sql = "CREATE TABLE json_templates (id INTEGER unsigned NOT NULL AUTO_INCREMENT , name VARCHAR(100) UNIQUE , json LONGTEXT, PRIMARY KEY (id));";
        stmt.execute(drop_sql);
        stmt.execute(create_Table_sql);
        stmt.close();
    }

    protected void insert(String name, String json) throws SQLException{
       execute("INSERT INTO json_templates (name, json) VALUES ('" + name +"','" + json +"');");
    }

    protected void update(String name, String json) throws SQLException{
        execute("UPDATE json_templates SET json='" + json + "' WHERE name='" + name +"'");
    }

    protected void delete(String name)throws SQLException{
        execute("DELETE FROM json_templates where NAME = '" + name + "'");
    }

    protected JSONObject fetch_template_json(String name)
            throws JSONException, SQLException, JsonValidationException {
        Statement stmt = connection.createStatement();
        String sql;
        sql = "SELECT * FROM json_templates WHERE name = '" + name +"'";
        ResultSet rs = stmt.executeQuery(sql);
        if(rs == null){
            throw new JsonValidationException("json template "+name+ " doesnt exist");
        }
        while(rs.next()){
            String json_string = rs.getString("json");
            return new JSONObject(json_string);
        }
        return null;
    }

    private Connection getDBConnection(String url, String username, String password) {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (ClassNotFoundException ex) {
            System.out.println("Error: unable to load driver class!");
            System.exit(1);
        } catch (IllegalAccessException ex) {
            System.out.println("Error: access problem while loading!");
            System.exit(2);
        } catch (InstantiationException ex) {
            System.out.println("Error: unable to instantiate driver!");
            System.exit(3);
        }
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://" + url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    private void execute(String sql) throws SQLException{
        Statement stmt = connection.createStatement();
        stmt.execute(sql);
        stmt.close();
    }

}
