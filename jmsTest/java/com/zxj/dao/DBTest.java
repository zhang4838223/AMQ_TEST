package com.zxj.dao;

import com.zxj.comm.DBThread;
import com.zxj.mybatis.map.Employee;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhang4838223 on 2016/7/9.
 */
public class DBTest {

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-database.xml");
        EmployeeDao dao = (EmployeeDao)ctx.getBean("emplyoeeDao");
        SqlDao sqlDao = (SqlDao)ctx.getBean("sqlDao");

//        selectUpdateData(dao);
//        updateState(sqlDao);

        insertDataBatch(sqlDao);
    }

    private static void listTest(){

    }

    private static void insertData(EmployeeDao dao) {
        for (int i = 0; i <100000 ; i++) {
            Employee emp = new Employee(i,"test"+i,"test_",7369,new Date(),100.0,10.0,20,0);
            dao.insert(emp);
            System.out.println("insert "+(i)+"th"+" record");
        }
    }

    private static void updateState(final SqlDao sqlDao) {
        final List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        if (!CollectionUtils.isEmpty(list)){
            DBThread.execute(new Runnable() {
                public void run() {
                    try {
                        sqlDao.updateEmpState(list);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private static void insertDataBatch(SqlDao sqlDao) {
        List<Employee> list = new ArrayList<Employee>();
        for (int i = 0; i <300000 ; i++) {
            Employee emp = new Employee(i,"test"+i,"test_",7369,new Date(),100.0,10.0,20,0);
            list.add(emp);
        }

        sqlDao.inserEmps(list);
    }

    private static void selectUpdateData(EmployeeDao dao) throws InterruptedException {
        while (true) {
            Map<String, Integer> map = new HashMap<String, Integer>();
            map.put("state", 1);
            int count = dao.findTotalCountNotSend(map);

            System.out.println(count);
            TimeUnit.SECONDS.sleep(5);
        }
    }

}
