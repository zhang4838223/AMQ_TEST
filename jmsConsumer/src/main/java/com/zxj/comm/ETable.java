package com.zxj.comm;

import java.util.List;

/**
 * Created by zhang4838223 on 2016/7/15.
 *
 * describe the xml info
 */
public class ETable {
    //表名称
    private String name;
    //更新状态操作sql
    private String updateSql;
    //查询操作sql
    private String querySql;
    //查询数量sql
    private String queryCountSql;
    //插入数据sql
    private String insertSql;
    //更新数据sql
    private String updateSqlA;

    //列信息
    List<EColumn> columns;

    public ETable() {
    }

    public ETable(String updateSql, String name, List<EColumn> columns,
                  String querySql, String queryCountSql,
                  String insertSql, String updateSqlA) {
        this.updateSql = updateSql;
        this.name = name;
        this.columns = columns;
        this.querySql = querySql;
        this.queryCountSql = queryCountSql;
        this.insertSql = insertSql;
        this.updateSqlA = updateSqlA;
    }

    public String getInsertSql() {
        return insertSql;
    }

    public void setInsertSql(String insertSql) {
        this.insertSql = insertSql;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUpdateSql() {
        return updateSql;
    }

    public void setUpdateSql(String updateSql) {
        this.updateSql = updateSql;
    }

    public String getQuerySql() {
        return querySql;
    }

    public void setQuerySql(String querySql) {
        this.querySql = querySql;
    }

    public List<EColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<EColumn> columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "ETable{" +
                "name='" + name + '\'' +
                ", updateSql='" + updateSql + '\'' +
                ", querySql='" + querySql + '\'' +
                ", columns=" + columns +
                ", queryCountSql=" + queryCountSql +
                ", insertSql=" + insertSql +
                ", updateSqlA=" + updateSqlA +
                '}';
    }

    public String getQueryCountSql() {
        return queryCountSql;
    }

    public void setQueryCountSql(String queryCountSql) {
        this.queryCountSql = queryCountSql;
    }

    public String getUpdateSqlA() {
        return updateSqlA;
    }

    public void setUpdateSqlA(String updateSqlA) {
        this.updateSqlA = updateSqlA;
    }
}
