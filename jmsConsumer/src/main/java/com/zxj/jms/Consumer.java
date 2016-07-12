package com.zxj.jms;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zxj.comm.DBThread;
import com.zxj.comm.utils.TextUtils;
import com.zxj.dao.EmpMapper;
import com.zxj.dao.SqlDao;
import com.zxj.model.Emp;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.util.CollectionUtils;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhang4838223 on 2016/6/30.
 */
public class Consumer {
    public static void main(String[] args) throws Exception {
        receiveMsg();
    }

    private static void saveData(List<Emp> emps) {

        if (emps == null || emps.size() <= 0)
            return;

        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-db.xml");
        EmpMapper dao = (EmpMapper)ctx.getBean("empDao");

        for (int i = 0, j = emps.size(); i < j; i++) {
            Emp employee = emps.get(i);
//            employee.setEmpno(employee.getEmpno()+j);
            dao.insert(employee);
        }
    }

    /**
     * 异步保存数据
     * @param emps
     */
    private static void saveDataAsyn(List<Emp> emps) {

        if (emps == null || emps.size() <= 0)
            return;

        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-db.xml");
        final EmpMapper dao = (EmpMapper)ctx.getBean("empDao");

        for (int i = 0, j = emps.size(); i < j; i++) {
            final Emp employee = emps.get(i);

            DBThread.execute(new Runnable() {
                public void run() {
                    dao.insert(employee);
                }
            });
        }
    }

    /**
     * 异步批量保存数据
     * @param emps
     */
    private static void batchSaveDataAsyn(List<Emp> emps) {

        if (emps == null || emps.size() <= 0)
            return;

        final List<Emp> insertData = new ArrayList<Emp>();
        final List<Emp> updateData = new ArrayList<Emp>();

        for (int i = 0, j = emps.size(); i < j; i++) {
            int state = emps.get(i).getState();
            switch (state){
                case 0://新增
                    insertData.add(emps.get(i));
                    break;
                case 2://修改
                    updateData.add(emps.get(i));
            }
        }

        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-db.xml");
        final SqlDao dao = (SqlDao)ctx.getBean("sqlDao");

        DBThread.execute(new Runnable() {
            public void run() {
                try {
                    dao.inserEmps(insertData);
                }catch (Exception e){
                    //记录日志
                    //这里保存失败可以做备份处理
                }
            }
        });

        DBThread.execute(new Runnable() {
            public void run() {
                try {
//                    dao.updateEmps(updateData);
                }catch (Exception e){
                    //记录日志
                    //这里保存失败可以做备份处理
                }
            }
        });
    }

    private static  void receiveMsg() throws JMSException {
        ApplicationContext context = new ClassPathXmlApplicationContext(
                "applicationContext-jms.xml");
        JmsTemplate jmsTemplate = (JmsTemplate) context.getBean("jmsTemplate");
        jmsTemplate.setSessionTransacted(false);
        jmsTemplate.setDeliveryPersistent(false);

        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-db.xml");
        final SqlDao sqlDao = (SqlDao)ctx.getBean("sqlDao");

        while (true) {
            System.out.println("begin to receive mes!");
            TextMessage msg = (TextMessage) jmsTemplate.receive();
            String text = null;
            try {
                text = msg.getText();
//            emps = gson.fromJson(text, new TypeToken<List<Emp>>(){}.getType());
            } catch (JMSException e) {
                e.printStackTrace();
            }


            if (text == null || text.length() <= 3) {
                //重新开始
                System.out.println(text);
                jmsTemplate.send(new MessageCreator() {

                    public Message createMessage(Session sen) throws JMSException {
                        TextMessage msg = sen.createTextMessage("RESTART");
                        return msg;
                    }

                });
                System.out.println("response restart");
            } else {

                //DB数据成功消费
                System.out.println("begin to response succ");
                jmsTemplate.send(new MessageCreator() {

                    public Message createMessage(Session sen) throws JMSException {
                        TextMessage msg = sen.createTextMessage("SUCC");
                        return msg;
                    }

                });
                System.out.println("response successfully");
//        batchSaveDataAsyn(emps);
                batchSaveDataAsyn1(text, sqlDao);
            }
        }
    }

    private static  String receiveMsgTest() throws JMSException {
        ApplicationContext context = new ClassPathXmlApplicationContext(
                "applicationContext-jms.xml");
        JmsTemplate template = (JmsTemplate) context.getBean("jmsTemplate");
        /*Connection connection = template.getConnectionFactory().createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        session.createProducer()*/
        TextMessage msg = (TextMessage) template.receive();
        String text = msg.getText();
        System.out.println("receive message: "+text);
        return text;
    }

    /**
     * 异步批量保存数据
     */
    private static void batchSaveDataAsyn1(String text,final SqlDao sqlDao) {

        if (text == null || text.length() <= 3)
            return;

        List<Emp> emps = TextUtils.gson.fromJson(text, new TypeToken<List<Emp>>() {
        }.getType());

        if (CollectionUtils.isEmpty(emps)){
            return;
        }

        System.out.println("receive mes size: "+ emps.size());
        final List<Emp> insertData = new ArrayList<Emp>();
        final List<Emp> updateData = new ArrayList<Emp>();

        for (int i = 0, j = emps.size(); i < j; i++) {
            int state = emps.get(i).getState();
            switch (state){
                case 0://新增
                    insertData.add(emps.get(i));
                    break;
                case 2://修改
                    updateData.add(emps.get(i));
                    break;
            }
        }

        if (!CollectionUtils.isEmpty(insertData)){
            DBThread.execute(new Runnable() {
                public void run() {
                    try {
                        sqlDao.inserEmps(insertData);
                    }catch (Exception e){
                        //TODO 这里保存失败可以做备份处理
                    }
                }
            });
        }

        if (CollectionUtils.isEmpty(updateData)){
            return;
        }

        DBThread.execute(new Runnable() {
            public void run() {
                try {
                    sqlDao.updateEmps(updateData);
                } catch (Exception e) {

                    //TODO 这里保存失败可以做备份处理
                }
            }
        });
    }
}
