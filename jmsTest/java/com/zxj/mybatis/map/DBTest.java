package com.zxj.mybatis.map;

import com.zxj.comm.JMSConstants;
import com.zxj.dao.EmployeeDao;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhang4838223 on 2016/6/30.
 */
public class DBTest {

    public static void main(String[] args){
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-database.xml");
        EmployeeDao dao = (EmployeeDao)ctx.getBean("emplyoeeDao");

        Map<String,Integer> map = new HashMap<String, Integer>();

        Integer startIndex = 0;
        Integer endIndex = 0;
        Integer pageSize = JMSConstants.BATCH_SIZE;

        map.put("state", 1);
        Integer count = dao.findTotalCountNotSend(map);
//        Integer count = 300001 - 2;
        int totalPages =  (count + pageSize -1) / pageSize;

        map.clear();
        map.put("state", 1);

        for (int i=0; i<totalPages;i++) {
            startIndex = endIndex + 1;
            endIndex = endIndex + pageSize;

            map.put("startIndex", startIndex);
            map.put("endIndex", endIndex);
            List<Employee> list = dao.findPageList(map);

            for(Employee employee : list) {
                System.out.println(employee.getEmpno());
            }
        }

    }
}
