package com.zxj.map;

import com.zxj.dao.EmpMapper;
import com.zxj.model.Emp;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Date;
import java.util.List;

/**
 * Created by zhang4838223 on 2016/7/1.
 */
public class DBtest {

    public static void main(String[] args){
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-db.xml");
        EmpMapper empDao = (EmpMapper)ctx.getBean("empDao");
//        List<Employee> li = empDao.selectAll();
        Emp emp = new Emp(8000,"test1","test",1,new Date(),1000.00,1000.00,10,0);
        empDao.insert(emp);
        System.out.println("test is done!");
    }
}
