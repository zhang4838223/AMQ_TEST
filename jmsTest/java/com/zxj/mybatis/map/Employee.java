package com.zxj.mybatis.map;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhang4838223 on 2016/6/30.
 */
public class Employee implements Serializable{

    private int empno;
    private String ename;
    private String job;
    private int mgr;
    private Date hiredate;
    private Double sal;
    private Double comm;
    private int deptno;
    private int state;

    public Employee() {
    }

    public Employee(int empno,String ename, String job, int mgr, Date hiredate, Double sal, Double comm, int deptno, int state) {
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
}
