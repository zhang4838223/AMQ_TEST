package com.zxj.test;

import com.zxj.comm.ETable;
import com.zxj.dao.EmployeeDao;
import com.zxj.dao.SqlDao;
import com.zxj.utils.UtilsFun;
import com.zxj.utils.XMLParseUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * Created by zhang4838223 on 2016/7/15.
 */
public class XMLDBTest {

    public static void main(String[] args){

        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-database.xml");
        SqlDao sqlDao = (SqlDao)ctx.getBean("sqlDao");
        List<ETable> etables = XMLParseUtil.getInstance().getEtables();

//        System.out.println(etables);
        int beginIndex = 0;
        int endIndex = 100;
        for (ETable table : etables){
            String sql = table.getQueryCountSql();
            int i = sqlDao.queryCountNotSend(sql);
            String querySql = table.getQuerySql();
            String newSql = UtilsFun.format(querySql, endIndex, beginIndex);
            sqlDao.queryPageList(newSql,table.getColumns());
            System.out.println(i);
        }

    }
}
