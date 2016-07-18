package com.zxj.test;

import com.zxj.comm.ERecord;
import com.zxj.comm.ETable;
import com.zxj.dao.EmployeeDao;
import com.zxj.dao.SqlDao;
import com.zxj.utils.UtilsFun;
import com.zxj.utils.XMLParseUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhang4838223 on 2016/7/15.
 */
public class XMLDBTest {

    public static void main(String[] args) throws SQLException {

        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-database.xml");
        SqlDao sqlDao = (SqlDao)ctx.getBean("sqlDao");
        List<ETable> etables = XMLParseUtil.getInstance().getEtables();

//        System.out.println(etables);
        int beginIndex = 0;
        int endIndex = 5;
        List<Integer> ids = new ArrayList<Integer>();
        ids.add(10);
        ids.add(20);
        ids.add(30);

        Map<String,List<ERecord>> map = new HashMap<String, List<ERecord>>();
        for (ETable table : etables){
            String sql = table.getQueryCountSql();
            //查询数量
//            int i = sqlDao.queryCountNotSend(sql);
            String querySql = table.getQuerySql();
            //分页查询数据
            String newSql = UtilsFun.format(querySql, endIndex, beginIndex);
            List<ERecord> recordList = sqlDao.queryPageList(newSql, table.getColumns());
            System.out.println(recordList);
//            sqlDao.updateState(ids,updateSql);
//            System.out.println(recordList.size());
        }

    }
}
