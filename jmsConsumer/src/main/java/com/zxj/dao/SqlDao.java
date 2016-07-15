package com.zxj.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import com.zxj.comm.utils.UtilFuns;
import com.zxj.model.Emp;

import oracle.jdbc.OracleConnection;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;
import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

/**
 * Created by zhang4838223 on 2016/7/4.
 */
public class SqlDao {
    private final static Log log = LogFactory.getLog(SqlDao.class);
    private UtilFuns utilFuns = new UtilFuns();
    private JdbcTemplate jdbcTemplate;
    private Connection conn ;
    private org.apache.commons.dbcp.DelegatingConnection del;
    private Connection delegate;
    private OracleConnection oracleConnection;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) throws SQLException {
        this.jdbcTemplate = jdbcTemplate;
        conn = jdbcTemplate.getDataSource().getConnection();
        del = new org.apache.commons.dbcp.DelegatingConnection(conn.getMetaData().getConnection());
        delegate = del.getInnermostDelegate();

        oracleConnection = (OracleConnection) delegate;
    }

    //返回单值
    public String getSingleValue(String sql){
        log.debug(sql);
        StringBuffer sBuf = new StringBuffer();
        List jlist = jdbcTemplate.queryForList(sql);
        Iterator ite = jlist.iterator();
        while(ite.hasNext()){
            Map map = (Map)ite.next();
            for(Object o:map.keySet()){
                sBuf.append(String.valueOf(map.get(o))).append(",");
            }
        }
        if(sBuf!=null && sBuf.length()>1){
            sBuf.delete(sBuf.length()-1, sBuf.length());	//del last char
        }
        return sBuf.toString();
    }

    public String getSingleValue(String sql, Object[] objs){
        log.debug(sql);
        StringBuffer sBuf = new StringBuffer();
        List jlist = null;
        if(utilFuns.arrayValid(objs)){
            jlist = jdbcTemplate.queryForList(sql, objs);
        } else {
            jlist = jdbcTemplate.queryForList(sql);
        }
        Iterator ite = jlist.iterator();
        while(ite.hasNext()){
            Map map = (Map)ite.next();
            for(Object o:map.keySet()){
                sBuf.append(String.valueOf(map.get(o))).append(",");
            }
        }
        if(sBuf!=null && sBuf.length()>1){
            sBuf.delete(sBuf.length()-1, sBuf.length());	//del last char
        }
        return sBuf.toString();
    }

    public String[] toArray(String sql){
        log.debug(sql);
        String[] strs = null;
        List aList = this.executeSQL(sql);
        if(aList.size()>0){
            int count = aList.size();
            strs = new String[ count ];
            for(int i=0; i<count; i++) {
                strs[ i ] = String.valueOf(aList.get(i));
            }
            return strs;
        }else{
            return null;
        }
    }

    public List executeSQL(String sql){
        log.debug(sql);
        List<String> aList = new ArrayList();
        List jlist = jdbcTemplate.queryForList(sql);
        Iterator ite = jlist.iterator();
        while(ite.hasNext()){
            Map map = (Map)ite.next();
            for(Object o:map.keySet()){
                if(map.get(o.toString())==null){
                    aList.add("");		//对象不存在时，写空串
                }else{
                    aList.add(map.get(o.toString()).toString());
                }
            }
        }
        return aList;
    }

    public List executeSQL(String sql, Object[] objs){
        log.debug(sql);
        List aList = new ArrayList();
        List jlist = null;
        if(utilFuns.arrayValid(objs)){
            jlist = jdbcTemplate.queryForList(sql, objs);
        } else {
            jlist = jdbcTemplate.queryForList(sql);
        }
        Iterator ite = jlist.iterator();
        while(ite.hasNext()){
            Map map = (Map)ite.next();
            for(Object o:map.keySet()){
                aList.add((String)map.get(o));
            }
        }
        return aList;
    }

    public List executeSQLForList(String sql, Object[] objs){
        log.debug(sql);
        List aList = new ArrayList();
        List jlist = null;
        if(utilFuns.arrayValid(objs)){
            jlist = jdbcTemplate.queryForList(sql, objs);
        } else {
            jlist = jdbcTemplate.queryForList(sql);
        }

        Iterator ite = jlist.iterator();

        List list;

        while(ite.hasNext()){
            Map map = (Map)ite.next();
            list = new ArrayList();

            for(Object o:map.keySet()){
                list.add(map.get(o));
            }
            aList.add(list.toArray());
        }

        return aList;
    }

    public int updateSQL(String sql){
        log.debug(sql);
        int i = jdbcTemplate.update(sql);
        return i;
    }

    public int updateSQL(String sql, Object[] objs){
        log.debug(sql);
        int i = jdbcTemplate.update(sql, objs);
        return i;
    }

    public int[] batchSQL(String[] sql){
        log.debug(sql);
        return jdbcTemplate.batchUpdate(sql);
    }

    public void inserEmps(List<Emp> list){
        String sql="insert into emp(EMPNO,ENAME,JOB,MGR,HIREDATE,SAL,COMM,DEPTNO,STATE)values(?,?,?,?,?,?,?,?,?)";
        final List<Emp> datas = list;
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int empno = datas.get(i).getEmpno();
                String ename = datas.get(i).getEname();
                String job = datas.get(i).getJob();
                int mgr = datas.get(i).getMgr();
                Date hiredate = datas.get(i).getHiredate();
                double sal = datas.get(i).getSal();
                double comm = datas.get(i).getComm();
                int deptno = datas.get(i).getDeptno();
                int state = datas.get(i).getState();

                ps.setInt(1, empno);
                ps.setString(2, ename);
                ps.setString(3, job);
                ps.setInt(4, mgr);
                ps.setDate(5, new java.sql.Date(hiredate.getTime()));
                ps.setDouble(6, sal);
                ps.setDouble(7, comm);
                ps.setInt(8, deptno);
                ps.setInt(9, state);

            }

            public int getBatchSize() {
                return datas.size();
            }
        });
    }

    public void updateEmps(List<Emp> list){
        String sql="UPDATE EMP SET ENAME = ? ,JOB = ? ,MGR = ? ,HIREDATE = ?, SAL = ?,COMM = ?, DEPTNO = ?,STATE = ? WHERE EMPNO = ?";
        final List<Emp> datas = list;
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter()
        {
            public void setValues(PreparedStatement ps,int i)throws SQLException
            {
                int empno = datas.get(i).getEmpno();
                String ename = datas.get(i).getEname();
                String job = datas.get(i).getJob();
                int mgr = datas.get(i).getMgr();
                Date hiredate = datas.get(i).getHiredate();
                double sal = datas.get(i).getSal();
                double comm = datas.get(i).getComm();
                int deptno = datas.get(i).getDeptno();

                ps.setString(1, ename);
                ps.setString(2, job);
                ps.setInt(3, mgr);
                ps.setDate(4, new java.sql.Date(hiredate.getTime()));
                ps.setDouble(5, sal);
                ps.setDouble(6, comm);
                ps.setDouble(7, deptno);

//                System.out.println(i + ":" + datas.get(i));
                ps.setInt(8, datas.get(i).getState());
                ps.setInt(9, empno);
            }
            public int getBatchSize()
            {
                return datas.size();
            }
        });
    }

    /**
     * 调用存储过程
     * @param list
     */
    public void insertEmpsWithPro(List<Emp> list){
        CallableStatement cstmt = null;
        try {
//            Connection conn = jdbcTemplate.getDataSource().getConnection();
//            oracle.jdbc.OracleConnection conn = (oracle.jdbc.OracleConnection)connection.getMetaData().getConnection();
//            org.apache.commons.dbcp.DelegatingConnection del = new org.apache.commons.dbcp.DelegatingConnection(conn.getMetaData().getConnection());
//            Connection delegate = del.getInnermostDelegate();
//
//            OracleConnection oracleConnection = (OracleConnection) delegate;
            ArrayDescriptor tabDesc = ArrayDescriptor.createDescriptor("BUT_UKBNOV_EMP_TAB",
                    oracleConnection);
//            Object[] o = list.toArray();
//            ARRAY vArray = new ARRAY(tabDesc, oracleConnection, o);

            ARRAY vArray = getObjArray("BUT_UKBNOV_EMP_TAB",list);
            cstmt = oracleConnection.prepareCall("{call bulkInsertEmp(?)}");
            cstmt.setArray(1, vArray);
            cstmt.execute();
            oracleConnection.commit();
        } catch (SQLException e) {
            System.out.println("========================");
            System.out.println(list);
            e.printStackTrace();
        }
    }

    private ARRAY getObjArray(String oracleList, List<Emp> list) throws SQLException {
        ARRAY vArray = null;

        if(!CollectionUtils.isEmpty(list)){

            StructDescriptor descriptor = new StructDescriptor("BUT_UKBNOV_EMP_REC",oracleConnection);
            STRUCT[] structs = new STRUCT[list.size()];

            for (int i = 0; i < list.size(); i++) {
                Object[] record = new Object[9];

                record[0] = list.get(i).getEmpno();
                record[1] = list.get(i).getEname();
                record[2] = list.get(i).getJob();
                record[3] = list.get(i).getMgr();
                record[4] = new java.sql.Date(list.get(i).getHiredate().getTime());
                record[5] = list.get(i).getSal();
                record[6] = list.get(i).getComm();
                record[7] = list.get(i).getDeptno();
                record[8] = list.get(i).getState();
                STRUCT item = new STRUCT(descriptor, oracleConnection, record);
                structs[i] = item;
            }

//            ArrayDescriptor tabDesc = ArrayDescriptor.createDescriptor("BUT_UKBNOV_EMP_TAB", oracleConnection);
            ArrayDescriptor tabDesc = ArrayDescriptor.createDescriptor(oracleList, oracleConnection);

            vArray = new ARRAY(tabDesc, oracleConnection, structs);
        }
        return vArray;
    }

}
