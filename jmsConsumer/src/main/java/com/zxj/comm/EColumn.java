package com.zxj.comm;

/**
 * Created by zhang4838223 on 2016/7/15.
 *
 * describe the column in databae
 */
public class EColumn {
    //type of the column in db
    private int type;
    //value of the column in db
    private String value;
    //name of the column in db
    private String name;

    public EColumn() {
    }

    public EColumn(String value, String name, int type) {
        this.value = value;
        this.name = name;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "EColumn{" +
                "type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
