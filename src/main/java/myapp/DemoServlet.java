package myapp;

import java.io.IOException;

import java.io.*;
import javax.servlet.http.*;
import java.sql.*;

import com.google.appengine.api.utils.SystemProperty;

public class DemoServlet extends HttpServlet {
    public ResultSet getPeople() {
        String url = null;
        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
          // Connecting from App Engine.
          // Load the class that provides the "jdbc:google:mysql://"
          // prefix.
          Class.forName("com.mysql.jdbc.GoogleDriver");
          url = "jdbc:google:mysql://blissful-answer-826:sql-test?user=root";
        } else {
          // You may also assign an IP Address from the access control
          // page and use it to connect from an external network.
        	url = "mysq://root:@localhost/test";
        }

        Connection conn = DriverManager.getConnection(url);
        ResultSet rs = conn.createStatement().executeQuery(
        "SELECT first, last FROM person");
        return rs;
    }
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
    	
    	try {
    	ResultSet rs = getPeople();
    	String results = "";
    	while (rs.next()) {
    		results +="{ \"name\": \"" + rs.getString("first") + ", " + rs.getString("last") + "\" },\n";
    	}
    	results = "[" + results + "]";
        resp.setContentType("text/plain");
        resp.getWriter().println(results);
    	} catch (Exception e ) {
        resp.setContentType("text/plain");
        resp.getWriter().println("{\"error\": \"" + e.getMessage() +"\"}");
    		
    	}
    }
}
