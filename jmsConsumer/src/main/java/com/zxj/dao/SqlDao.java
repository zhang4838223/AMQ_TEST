package com.zxj.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import com.zxj.comm.utils.UtilFuns;
import com.zxj.model.Emp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by zhang4838223 on 2016/7/4.
 */
public class SqlDao {
    private final static Log log = LogFactory.getLog(SqlDao.class);
    private UtilFuns utilFuns = new UtilFuns();
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

}
