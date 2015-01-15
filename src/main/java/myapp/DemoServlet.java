package myapp;

import java.io.IOException;

import java.io.*;
import javax.servlet.http.*;
import java.sql.*;

import com.google.appengine.api.utils.SystemProperty;

public class DemoServlet extends HttpServlet {
     private Connection getConnection() {
        String url = null;
        try {
             if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
               // Connecting from App Engine.
               // Load the class that provides the "jdbc:google:mysql://"
               // prefix.
               Class.forName("com.mysql.jdbc.GoogleDriver");
               url = "jdbc:google:mysql://blissful-answer-826:sql-test/test?user=root";
             } else {
               // You may also assign an IP Address from the access control
               // page and use it to connect from an external network.
                  Class.forName("com.mysql.jdbc.Driver");
                  url = "jdbc:mysql://127.0.0.1:3306/test?user=root";
             }
     
             return DriverManager.getConnection(url);
        } catch (Exception e) {
             logException(e);
             return null;
        }
     }
    public ResultSet getPeople() {
         Connection conn = getConnection();
         try {
        ResultSet rs = conn.createStatement().executeQuery(
        "SELECT first, last FROM person");
        return rs;
         } catch (Exception e) {
              logException(e);
              return null;
         }
    }
    private void logException(Exception ex) {
         StringWriter errors = new StringWriter();
         ex.printStackTrace(new PrintWriter(errors));
         System.out.println(errors.toString());
    }
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
         System.out.println("Get hit! " + SystemProperty.environment.value());
         
         try {
         ResultSet rs = getPeople();
         if (rs == null ) {
             resp.setContentType("text/plain");
             resp.getWriter().println("{\"error\": \"cannot get people\"}");
             return;
         }
         String results = "";
         int i = 0;
         while (rs.next()) {
              System.out.println(" get people!");
              if (i > 0) {
                   results += ",\n";
              }
              i++;
              results +="{ \"first\": \"" + rs.getString("first") + "\","  +
                             "\"last\": \"" + rs.getString("last") + "\" }";
         }
         results = "[" + results + "]";
        resp.setContentType("text/plain");
        resp.getWriter().println(results);
         } catch (Exception e ) {
              logException(e);
              
             resp.setContentType("text/plain");
             resp.getWriter().println("{\"error\": \"" + e +"\"}");
         }
    }
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
         System.out.println("Do post!");
         String first = req.getParameter("first");
         String last = req.getParameter("last");
         boolean noFirst = first == null || first.isEmpty();
         boolean noLast = last == null || last.isEmpty();
         if (noFirst || noLast) {
              resp.setContentType("text/html");
              resp.getWriter().println("<html><body>You need to enter both a first and last name</body></html>");
              return;
         }
         Connection conn = null;
         try {
              try {
              conn = getConnection();
              String statement = "INSERT INTO person (first, last) VALUES(?, ?)";
              PreparedStatement stmt = conn.prepareStatement(statement);
              stmt.setString(1, first);
              stmt.setString(2, last);
              if (stmt.executeUpdate() == 1) {
                   resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
                   resp.setHeader("Location", "/");   
              } else {
                   resp.setContentType("text/html");
                   resp.getWriter().println("Failed to insert!");
              }
              } finally {
              conn.close();
              }
         } catch (Exception e) {
              logException(e);
                   resp.setContentType("text/html");
                   resp.getWriter().println("Failed to insert! " + e);
         } 
    }

}
