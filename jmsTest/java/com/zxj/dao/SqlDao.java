package com.zxj.dao;

import com.zxj.comm.EColumn;
import com.zxj.comm.ERecord;
import com.zxj.mybatis.map.Employee;
import com.zxj.utils.UtilsFun;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by zhang4838223 on 2016/7/7.
 */
public class SqlDao {
    private static Logger log = Logger.getLogger(SqlDao.class);
    private UtilsFun utilFuns = new UtilsFun();
    private JdbcTemplate jdbcTemplate;


    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

    public void inserEmps(List<Employee> list){
        String sql="insert into emp(EMPNO,ENAME,JOB,MGR,HIREDATE,SAL,COMM,DEPTNO,STATE)values(?,?,?,?,?,?,?,?,?)";
        final List<Employee> datas = list;
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int empno = datas.get(i).getEmpno();
                String ename = datas.get(i).getEname();
                String job = datas.get(i).getJob();
                int mgr = datas.get(i).getMgr();
                Date hiredate = datas.get(i).getHiredate();
                Double sal = datas.get(i).getSal();
                Double comm = datas.get(i).getComm();
                int deptno = datas.get(i).getDeptno();

                ps.setInt(1, empno);
                ps.setString(2, ename);
                ps.setString(3, job);
                ps.setInt(4, mgr);
                ps.setDate(5, new java.sql.Date(hiredate.getTime()));
                ps.setDouble(6, sal);
                try {
                    ps.setDouble(7, comm);
                } catch (Exception e) {
                    System.out.println(i + ":" + datas.get(i));
                }
                ps.setInt(8, deptno);
                ps.setInt(9, datas.get(i).getState());
            }

            public int getBatchSize() {
                return datas.size();
            }
        });
    }

    public void updateEmps(List<Employee> list) throws SQLException {

        /*Connection conn = jdbcTemplate.getDataSource().getConnection();
        conn.setAutoCommit(false);
        String sql = "UPDATE emp SET STATE = ? WHERE EMPNO = ?";
        PreparedStatement prest = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        for(int x = 0, y = list.size(); x < y; x++){
            prest.setInt(1, 1);
            prest.setInt(2, list.get(x).getEmpno());
            prest.addBatch();
        }
        prest.executeBatch();
        conn.commit();
        conn.close();*/

        String sql="UPDATE emp SET STATE = ? WHERE EMPNO = ?";
        final List<Employee> datas = list;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter()
        {
            public void setValues(PreparedStatement ps,int i)throws SQLException
            {
                ps.setInt(1, 1);//更改发送状态为1
                ps.setInt(2, datas.get(i).getEmpno());//更改发送状态为1
                datas.get(i).setState(1);
            }
            public int getBatchSize()
            {
                return datas.size();
            }
        });
    }

    public void updateEmpState(List<Integer> ids) throws SQLException {
        String sql="UPDATE emp SET STATE = ? WHERE EMPNO = ?";
        final List<Integer> datas = ids;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter()
        {
            public void setValues(PreparedStatement ps,int i)throws SQLException
            {
                ps.setInt(1, 1);//更改发送状态为1
                ps.setInt(2, datas.get(i));//更改发送状态为1
            }
            public int getBatchSize()
            {
                return datas.size();
            }
        });
    }

    public int queryCountNotSend(String sql){
        int result = jdbcTemplate.queryForObject(sql, Integer.class);
        return result;
    }

    public List<ERecord> queryPageList(String sql, final List<EColumn> columns){

        jdbcTemplate.query(sql, new ResultSetExtractor() {

            public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                for (int i = 0, j = columns.size(); i < j; i++){
                    //ResultSet从1开始
                    EColumn col = columns.get(i);
                    switch (col.getType()) {
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                        case 4:
                            break;
                    }
                }
                return null;
            }
        });
        return null;
    }
}
