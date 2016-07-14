package com.zxj.dao;

import com.zxj.model.Emp;
import oracle.jdbc.OracleConnection;
import oracle.sql.ArrayDescriptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhang4838223 on 2016/7/14.
 */
public class DBTest {

    public static void main(String[] args){
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-db.xml");
        JdbcTemplate jdbcTemplate = (JdbcTemplate)ctx.getBean("jdbcTemplate");

//        try {
//            Connection conn = jdbcTemplate.getDataSource().getConnection();
//            org.apache.commons.dbcp.DelegatingConnection del = new org.apache.commons.dbcp.DelegatingConnection(conn.getMetaData().getConnection());
//            Connection delegate = del.getInnermostDelegate();
//
//            OracleConnection oracleConnection = (OracleConnection) delegate;
//            ArrayDescriptor tabDesc = ArrayDescriptor.createDescriptor("BUT_UKBNOV_EMP_TAB",
//                    oracleConnection);
//            System.out.println(oracleConnection);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

        SqlDao sqlDao = (SqlDao)ctx.getBean("sqlDao");

        List<Emp> list = new ArrayList<Emp>();

        for (int i = 0; i < 1000; i++) {
            list.add(new Emp(i,"test_"+i,"test",20,new Date(),100.00,100.00,1,0));
        }
        sqlDao.insertEmpsWithPro(list);
    }
}
