package com.ostj.dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SQLAccess {
    private static Logger log = LoggerFactory.getLogger(SQLAccess.class);

    private Connection conn;

    public SQLAccess(String jdbcUrl, String username, String password) throws SQLException {
        this.conn = DriverManager.getConnection(jdbcUrl, username, password);
    }

    public List<Map<String, Object>> query( String sql, List<Object> parameters) throws SQLException
    {
        log.debug("Start query: {}", sql);
        List<Map<String, Object>> results = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try
        {
            ps = this.conn.prepareStatement(sql);
            int i = 0;
            for (Object parameter : parameters)
            {
                ps.setObject(++i, parameter);
            }
            rs = ps.executeQuery();
            results = map(rs);
        }
        finally
        {
            close(rs);
            close(ps);
        }
        return results;
    }

    public List<Map<String, Object>> map(ResultSet rs) throws SQLException
    {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        try
        {
            if (rs != null)
            {
                ResultSetMetaData meta = rs.getMetaData();
                int numColumns = meta.getColumnCount();
                while (rs.next())
                {
                    Map<String, Object> row = new HashMap<String, Object>();
                    for (int i = 1; i <= numColumns; ++i)
                    {
                        String name = meta.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(name, value);
                    }
                    results.add(row);
                }
            }
        }
        finally
        {
            close(rs);
        }
        return results;
    }

    public  int update( String sql, List<Object> parameters) throws SQLException
    {
        int numRowsUpdated = 0;
        PreparedStatement ps = null;
        log.debug("Start update query: {}", sql);
        try
        {
            ps = this.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int i = 0;
            for (Object parameter : parameters)
            {
                ps.setObject(++i, parameter);
            }
            numRowsUpdated = ps.executeUpdate();
            if (numRowsUpdated > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    numRowsUpdated = rs.getInt(1);
                }
            }
        }
        finally
        {
            close(ps);
        }
        return numRowsUpdated;
    }


    public void close()
    {
        try
        {
           this.conn.close();
        }
        catch (SQLException e)
        {
            log.error("Error close Statement {}", e);
        }
    }


    public void close(Statement st)
    {
        try
        {
            if (st != null)
            {
                st.close();
            }
        }
        catch (SQLException e)
        {
            log.error("Error close Statement {}", e);
        }
    }

    public void close(ResultSet rs)
    {
        try
        {
            if (rs != null)
            {
                rs.close();
            }
        }
        catch (SQLException e)
        {
            log.error("Error close ResultSet {}", e);
        }
    }

    public void rollback()
    {
        try
        {
            this.conn.rollback();
        }
        catch (SQLException e)
        {
            log.error("Error rollback {}", e);
        }
    }
}