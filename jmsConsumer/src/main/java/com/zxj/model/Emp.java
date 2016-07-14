package com.zxj.model;

import oracle.jdbc.OracleTypes;
import oracle.jpub.runtime.MutableStruct;
import oracle.sql.Datum;
import oracle.sql.ORAData;
import oracle.sql.ORADataFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by zhang4838223 on 2016/7/1.
 */
//public class Emp implements ORAData {
public class Emp {

    private int empno;
    private String ename;
    private String job;
    private int mgr;
    private Date hiredate;
    private double sal;
    private double comm;
    private int deptno;
    private int state;

//    public static final String _ORACLE_TYPE_NAME = "BUT_UKBNOV_EMP_REC";
//    static int[] _sqlType = { OracleTypes.NUMBER, OracleTypes.LONGVARCHAR, OracleTypes.LONGVARCHAR,
//                    OracleTypes.NUMBER, OracleTypes.DATE, OracleTypes.NUMBER,
//            OracleTypes.NUMBER,OracleTypes.NUMBER,OracleTypes.INTEGER
//                            };
//    static ORADataFactory[] _factory = new ORADataFactory[_sqlType.length];
//
//    static MutableStruct _struct = new MutableStruct(new Object[_sqlType.length], _sqlType, _factory);
    public Emp() {

    }

    public Emp(int empno, String ename, String job, int mgr, Date hiredate, Double sal, Double comm, int deptno, int state) {
        this.empno = empno;
        this.ename = ename;
        this.job = job;
        this.mgr = mgr;
        this.hiredate = hiredate;
        this.sal = sal;
        this.comm = comm;
        this.deptno = deptno;
        this.state = state;
    }

    public int getEmpno() {
        return empno;
    }

    public void setEmpno(int empno) {
        this.empno = empno;
    }

    public String getEname() {
        return ename;
    }

    public void setEname(String ename) {
        this.ename = ename;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public int getMgr() {
        return mgr;
    }

    public void setMgr(int mgr) {
        this.mgr = mgr;
    }

    public Date getHiredate() {
        return hiredate;
    }

    public void setHiredate(Date hiredate) {
        this.hiredate = hiredate;
    }

    public Double getSal() {
        return sal;
    }

    public void setSal(Double sal) {
        this.sal = sal;
    }

    public Double getComm() {
        return comm;
    }

    public void setComm(Double comm) {
        this.comm = comm;
    }

    public int getDeptno() {
        return deptno;
    }

    public void setDeptno(int deptno) {
        this.deptno = deptno;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "empno=" + empno +
                ", ename='" + ename + '\'' +
                ", job='" + job + '\'' +
                ", mgr=" + mgr +
                ", hiredate=" + hiredate +
                ", sal=" + sal +
                ", comm='" + comm + '\'' +
                ", deptno=" + deptno +
                ", state=" + state +
                '}';
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

//    public Datum toDatum(Connection conn) throws SQLException {
//        _struct.setAttribute(0, this.empno);
//        _struct.setAttribute(1, this.ename);
//        _struct.setAttribute(2, this.job);
//        _struct.setAttribute(3, this.mgr);
//        _struct.setAttribute(4, this.hiredate);
//        _struct.setAttribute(5, this.sal);
//        _struct.setAttribute(6, this.comm);
//        _struct.setAttribute(7, this.deptno);
//        _struct.setAttribute(8, this.state);
//        return _struct.toDatum(conn, _ORACLE_TYPE_NAME);
//    }
}
