package com.zxj.jms;

import com.google.gson.reflect.TypeToken;
import com.zxj.comm.DBThread;
import com.zxj.comm.LogicThread;
import com.zxj.comm.utils.TextUtils;
import com.zxj.dao.EmpMapper;
import com.zxj.dao.SqlDao;
import com.zxj.model.Emp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.util.CollectionUtils;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhang4838223 on 2016/7/4.
 */
public class ConsumerListener2 implements MessageListener {

    private static final Log logger = LogFactory.getLog(ConsumerListener2.class);
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private SqlDao sqlDao;

    public void onMessage(Message message) {
        System.out.println("listener[2222] is running!");
        TextMessage msg = (TextMessage) message;
        String text = null;
        try {
            text = msg.getText();
//            emps = gson.fromJson(text, new TypeToken<List<Emp>>(){}.getType());
        } catch (JMSException e) {
            e.printStackTrace();
        }

//        jmsTemplate.setSessionTransacted(false);
//        jmsTemplate.setDeliveryPersistent(false);

        if (text == null || text.length() <= 3) {
            //重新开始
            System.out.println(text);
//            jmsTemplate.send(new MessageCreator() {
//
//                public Message createMessage(Session sen) throws JMSException {
//                    TextMessage msg = sen.createTextMessage("RESTART");
//                    return msg;
//                }
//
//            });
//            System.out.println("response restart");
        }else {

            //DB数据成功消费
//            System.out.println("begin to response succ");
//            jmsTemplate.send(new MessageCreator() {
//
//                public Message createMessage(Session sen) throws JMSException {
//                    TextMessage msg = sen.createTextMessage("SUCC");
//                    return msg;
//                }
//
//            });
            System.out.println("response successfully");
//        batchSaveDataAsyn(emps);
//            batchSaveDataAsyn(text);
            final String data = text;
            LogicThread.execute(new Runnable() {
                public void run() {
                    batchSaveDataAsyn(data);
                }
            });
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
    private void batchSaveDataAsyn(List<Emp> emps) {

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
                    break;
            }
        }

        /*ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-db.xml");
        final SqlDao dao = (SqlDao)ctx.getBean("sqlDao");*/

        logger.info("insert data size:" + insertData.size());
        logger.info("updateData data size:" + updateData.size());
        if (!CollectionUtils.isEmpty(insertData)){
            DBThread.execute(new Runnable() {
                public void run() {
                    try {
                        sqlDao.inserEmps(insertData);
                    }catch (Exception e){
                        logger.error(e);
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
                }catch (Exception e){

                    //TODO 这里保存失败可以做备份处理
                }
            }
        });
    }

    /**
     * 异步批量保存数据
     */
    private void batchSaveDataAsyn(String text) {

        if (text == null || text.length() <= 3)
            return;

        List<Emp> emps = null;
        try {
            emps = TextUtils.gson.fromJson(text, new TypeToken<List<Emp>>() {
            }.getType());
        }catch(Exception e){
//            System.out.println("=================json error=============");
            logger.warn(text);
            logger.error(e);
        }

        if (CollectionUtils.isEmpty(emps)){
//            System.out.println("=================empty data=============");
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

        /*ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-db.xml");
        final SqlDao dao = (SqlDao)ctx.getBean("sqlDao");*/

        logger.info("insert data size:" + insertData.size());
        logger.info("updateData data size:" + updateData.size());
        if (!CollectionUtils.isEmpty(insertData)){
            DBThread.execute(new Runnable() {
                public void run() {
                    try {
                        sqlDao.inserEmps(insertData);
                    }catch (Exception e){
                        logger.error(e);
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
                }catch (Exception e){

                    //TODO 这里保存失败可以做备份处理
                }
            }
        });
    }

}
