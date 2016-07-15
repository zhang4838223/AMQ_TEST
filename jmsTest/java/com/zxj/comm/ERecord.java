package com.zxj.comm;

import java.util.List;

/**
 * Created by zhang4838223 on 2016/7/15.
 * describe the record in database
 */
public class ERecord {

    List<EColumn> columns;

    public ERecord() {
    }

    public ERecord(List<EColumn> columns) {
        this.columns = columns;
    }

    public List<EColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<EColumn> columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "ERecord{" +
                "columns=" + columns +
                '}';
    }
}
