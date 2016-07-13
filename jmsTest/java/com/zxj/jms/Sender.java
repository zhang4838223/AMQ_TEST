package com.zxj.jms;

import com.google.gson.Gson;
import com.zxj.comm.DBThread;
import com.zxj.comm.JMSConstants;
import com.zxj.comm.LogicThread;
import com.zxj.dao.EmployeeDao;
import com.zxj.dao.SqlDao;
import com.zxj.mybatis.map.Employee;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.util.CollectionUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhang4838223 on 2016/7/10.
 */
public class Sender {

    private final static Log logger = LogFactory.getLog(Producer.class);
    public static ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-jms.xml");
    public static JmsTemplate template =(JmsTemplate)context.getBean("jmsTemplate");
//    public static JmsTemplate template1 =(JmsTemplate)context.getBean("jmsTemplate1");
//    public static JmsTemplate template2 =(JmsTemplate)context.getBean("jmsTemplate2");
//    public static JmsTemplate template3 =(JmsTemplate)context.getBean("jmsTemplate3");
//    public static JmsTemplate recTemplate =(JmsTemplate)context.getBean("recTemplate");
    public static ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-database.xml");
    private static EmployeeDao dao = (EmployeeDao)ctx.getBean("emplyoeeDao");
    private final static SqlDao sqlDao = (SqlDao)ctx.getBean("sqlDao");

    static {
        template.setSessionTransacted(false);
        template.setDeliveryPersistent(false);
//        recTemplate.setSessionTransacted(false);
//        recTemplate.setDeliveryPersistent(false);

    }
    public static void main(String[] args) throws Exception {
//        sendByPage();
//        sendDirectly();
//        sendByPageRes();
        sendDirecWithMultiQueue();
//        sendWithMultiQueue();
//        timerTest();
    }

    public static void timerTestByPage() throws Exception {
        while (true){
            System.out.println("logic queue size:"+ LogicThread.getLogicQueueSize()+"; db size: "+ DBThread.getQueueSize());

            boolean flag = true;
            if (DBThread.getQueueSize() == 0 && flag) {//首次默认可以发送
                sendByPageRes();
                flag = false;
            }
            TextMessage msg = (TextMessage) template.receive();
            String text = msg.getText();

            if("RESTART".equals(text)){
                flag = true;
            }

            if ("BUSY".equals(text)){
                flag = false;
                sendMes("RESTART");
            }
//            TimeUnit.SECONDS.sleep(15);
        }
    }

    /**
     * 分页查询出数据并发送，收到响应后再发送下一批分页查询数据
     */
    public static void sendByPageRes() {
/*
        Map<String,Integer> map = new HashMap<String, Integer>();

        Integer startIndex = 0;
        Integer endIndex = 0;
        Integer pageSize = 10000;//每次查询一万条

        final List<Integer> ids = new ArrayList<Integer>();
        int count = -1;

        try {
            while (true) {
                System.out.println("DBThread task nunm is:  "+ DBThread.getQueueSize());
                if(DBThread.getQueueSize() > 0){//等待当前数据保存完毕
                    System.out.println("DBThread.getQueueSize() = " + DBThread.getQueueSize());
                    TimeUnit.SECONDS.sleep(15);
                    startIndex = 0;
                    endIndex = 0;
                    count = -1;
//                    ids.clear();
                    map.clear();
                    continue;
                }

                map.put("state", 1);

                if(count == -1) {//新一轮发送开始，重新查询
                    count = dao.findTotalCountNotSend(map);
                    System.out.println("the new count is : "+ count+"; map= "+ map);
                }

                if (count <= 0){
                    count = -1;
                    System.out.println("===========waiting for new data=========="+count);
                    TimeUnit.SECONDS.sleep(15);
                    startIndex = 0;
                    endIndex = 0;
                    continue;
                }

                int totalPages =  (count + pageSize -1) / pageSize;
                //        CountDownLatch cdl = new CountDownLatch(totalPages);

                map.clear();
                map.put("state", 1);

                startIndex = endIndex == 0 ? 0 : endIndex + 1;
                endIndex = endIndex + pageSize;

                map.put("startIndex", startIndex);
                map.put("endIndex", endIndex);
                List<Employee> list = dao.findPageList(map);
                map.clear();



                final String data = toJson(list);
                //发送数据java.io.IOException: Wire format negotiation timeout: peer did not send his wire format.
                System.out.println("begin to send mes!");
                sendMes(data);
    //            cdl.countDown();

                for (Employee emp : list) {
                    ids.add(emp.getEmpno());
                }


                System.out.println("begin to receive the response");
                TextMessage msg = (TextMessage) recTemplate.receive();

                String text = msg.getText();
                System.out.println("the response is ---" + text);
                if ("SUCC".equals(text)) {
                    map.put("state", 1);

//                    startIndex = endIndex + 1;
//                    endIndex = endIndex + pageSize;

//                    map.put("startIndex", startIndex);
//                    map.put("endIndex", endIndex);

                    if (startIndex >= count && startIndex != 0 && count != -1) {//本轮发送结束
                        System.out.println("===========begin to the next==========");
                        startIndex = 0;
                        endIndex = 0;
                        map.clear();
                        //更改为已经发送状态
                        saveDataAsyn(ids);
                        ids.clear();
                        continue;
                    }
                }

                if ("RESTART".equals(text)){
                    System.out.println("===========begin to restart==========");
                    startIndex = 0;
                    endIndex = 0;
                    map.clear();
                    //更改为已经发送状态
                    saveDataAsyn(ids);
                    ids.clear();
                    continue;
                }
    //          cdl.await();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            saveDataAsyn(ids);
        }*/
    }

    /**
     * 多个队列无响应发送
     */

    public static void sendByQueues(){

/*        Map<String,Integer> map = new HashMap<String, Integer>();

        Integer startIndex = 0;
        Integer endIndex = 0;
        Integer pageSize = 10000;//每次查询一万条

        final List<Integer> ids = new ArrayList<Integer>();
        int count = -1;

        try {
            while (true) {
                System.out.println("DBThread task nunm is:  "+ DBThread.getQueueSize());
                if(DBThread.getQueueSize() > 0){//等待当前数据保存完毕
                    System.out.println("DBThread.getQueueSize() = " + DBThread.getQueueSize());
                    TimeUnit.SECONDS.sleep(15);
                    startIndex = 0;
                    endIndex = 0;
                    count = -1;
//                    ids.clear();
                    map.clear();
                    continue;
                }

                map.put("state", 1);

                if(count == -1) {//新一轮发送开始，重新查询
                    count = dao.findTotalCountNotSend(map);
                    System.out.println("the new count is : "+ count+"; map= "+ map);
                }

                if (count <= 0){
                    count = -1;
                    System.out.println("===========waiting for new data=========="+count);
                    TimeUnit.SECONDS.sleep(15);
                    startIndex = 0;
                    endIndex = 0;
                    continue;
                }

                int totalPages =  (count + pageSize -1) / pageSize;
                //        CountDownLatch cdl = new CountDownLatch(totalPages);

                map.clear();
                map.put("state", 1);

                // 0-10001-20001-30001
                startIndex = endIndex == 0 ? 0 : endIndex + 1;
                endIndex = endIndex + pageSize;

                map.put("startIndex", startIndex);
                map.put("endIndex", endIndex);
                List<Employee> list = dao.findPageList(map);
                map.clear();



                final String data = toJson(list);
                //发送数据java.io.IOException: Wire format negotiation timeout: peer did not send his wire format.
                System.out.println("begin to send mes!");

                int var = endIndex / pageSize;

                int index = var % 4;

                switch (index){
                    case 0:
                        sendMes(data);
                        break;
                    case 1:
                        sendMesWithDes(data,template1);
                        break;
                    case 2:
                        sendMesWithDes(data,template2);
                        break;
                    case 3:
                        sendMesWithDes(data,template3);
                        break;
                    default:
                        sendMesWithDes(data,template1);
                        break;

                }
//                sendMes(data);
                //            cdl.countDown();

                for (Employee emp : list) {
                    ids.add(emp.getEmpno());
                }


//                System.out.println("begin to receive the response");
//                TextMessage msg = (TextMessage) recTemplate.receive();
//
//                String text = msg.getText();
//                System.out.println("the response is ---" + text);
//                if ("SUCC".equals(text)) {
//                    map.put("state", 1);

//                    startIndex = endIndex + 1;
//                    endIndex = endIndex + pageSize;

//                    map.put("startIndex", startIndex);
//                    map.put("endIndex", endIndex);

                    if (startIndex >= count && startIndex != 0 && count != -1) {//本轮发送结束
                        System.out.println("===========begin to the next=========="+startIndex);
                        startIndex = 0;
                        endIndex = 0;
                        map.clear();
                        //更改为已经发送状态
                        saveDataAsyn(ids);
                        ids.clear();
//                        return;
                        continue;
                    }
//                }

//                if ("RESTART".equals(text)){
//                    System.out.println("===========begin to restart==========");
//                    startIndex = 0;
//                    endIndex = 0;
//                    map.clear();
//                    //更改为已经发送状态
//                    saveDataAsyn(ids);
//                    ids.clear();
//                    continue;
//                }
                //          cdl.await();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            saveDataAsyn(ids);
        }*/
    }

    /**
     * 多个队列无响应发送
     */

    public static void sendDirecWithMultiQueue(){

        Map<String,Integer> map = new HashMap<String, Integer>();

        Integer startIndex = 0;
        Integer endIndex = 0;
        Integer pageSize = JMSConstants.getBatchSize();//每次查询一万条

        final List<Integer> ids = new ArrayList<Integer>();
        int count = -1;

        try {
            while (true) {
                System.out.println("DBThread task nunm is:  "+ DBThread.getQueueSize());
                if(DBThread.getQueueSize() > 0){//等待当前数据保存完毕
                    System.out.println("DBThread.getQueueSize() = " + DBThread.getQueueSize());
                    TimeUnit.SECONDS.sleep(15);
                    startIndex = 0;
                    endIndex = 0;
                    count = -1;
//                    ids.clear();
                    map.clear();
                    continue;
                }

                map.put("state", 1);

                if(count == -1) {//新一轮发送开始，重新查询
                    count = dao.findTotalCountNotSend(map);
                    System.out.println("the new count is : "+ count+"; map= "+ map);
                }

                if (count <= 0){
                    count = -1;
                    System.out.println("===========waiting for new data=========="+count);
                    TimeUnit.SECONDS.sleep(15);
                    startIndex = 0;
                    endIndex = 0;
                    continue;
                }

                int totalPages =  (count + pageSize -1) / pageSize;
                //        CountDownLatch cdl = new CountDownLatch(totalPages);

                map.clear();
                map.put("state", 1);

                // 0-10001-20001-30001
                startIndex = endIndex == 0 ? 0 : endIndex + 1;
                endIndex = endIndex + pageSize;

                map.put("startIndex", startIndex);
                map.put("endIndex", endIndex);
                List<Employee> list = dao.findPageList(map);
                map.clear();



                final String data = toJson(list);
                //发送数据java.io.IOException: Wire format negotiation timeout: peer did not send his wire format.
                System.out.println("begin to send mes!");

                int var = endIndex / pageSize;

                int index = var % JMSConstants.getQueueSize();

                sendMesWithDes(data,"queue_"+(index+1));

                for (Employee emp : list) {
                    ids.add(emp.getEmpno());
                }

                if (startIndex >= count && startIndex != 0 && count != -1) {//本轮发送结束
                    System.out.println("===========begin to the next=========="+startIndex);
                    startIndex = 0;
                    endIndex = 0;
                    map.clear();
                    //更改为已经发送状态
                    saveDataAsyn(ids);
                    ids.clear();
//                        return;
                    continue;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            saveDataAsyn(ids);
        }
    }



    /**
     * 无响应发送
     */
    public static void sendDirectly() {

        Map<String,Integer> map = new HashMap<String, Integer>();

        Integer startIndex = 0;
        Integer endIndex = 0;
        Integer pageSize = 10000;//每次查询一万条

        final List<Integer> ids = new ArrayList<Integer>();
        int count = -1;

        try {
            while (true) {
                System.out.println("DBThread task nunm is:  " + DBThread.getQueueSize());
                if (DBThread.getQueueSize() > 0) {//等待当前数据保存完毕
                    System.out.println("DBThread.getQueueSize() = " + DBThread.getQueueSize());
                    TimeUnit.SECONDS.sleep(15);
                    startIndex = 0;
                    endIndex = 0;
                    count = -1;
//                    ids.clear();
                    map.clear();
                    continue;
                }

                map.put("state", 1);

                if (count == -1) {//新一轮发送开始，重新查询
                    count = dao.findTotalCountNotSend(map);
                    System.out.println("the new count is : " + count + "; map= " + map);
                }

                if (count <= 0) {
                    count = -1;
                    System.out.println("===========waiting for new data==========" + count);
                    TimeUnit.SECONDS.sleep(15);
                    startIndex = 0;
                    endIndex = 0;
                    continue;
                }

                int totalPages = (count + pageSize - 1) / pageSize;
                //        CountDownLatch cdl = new CountDownLatch(totalPages);

                map.clear();
                map.put("state", 1);

                startIndex = endIndex == 0 ? 0 : endIndex + 1;
                endIndex = endIndex + pageSize;

                map.put("startIndex", startIndex);
                map.put("endIndex", endIndex);
                List<Employee> list = dao.findPageList(map);
                map.clear();


                final String data = toJson(list);
                //发送数据java.io.IOException: Wire format negotiation timeout: peer did not send his wire format.
                System.out.println("begin to send mes!");
                sendMes(data);
                //            cdl.countDown();

                for (Employee emp : list) {
                    ids.add(emp.getEmpno());
                }


//                System.out.println("begin to receive the response");
//                TextMessage msg = (TextMessage) recTemplate.receive();
//
//                String text = msg.getText();
//                System.out.println("the response is ---" + text);
//                if ("SUCC".equals(text)) {
//                    map.put("state", 1);

//                    startIndex = endIndex + 1;
//                    endIndex = endIndex + pageSize;

//                    map.put("startIndex", startIndex);
//                    map.put("endIndex", endIndex);

                if (startIndex >= count && startIndex != 0 && count != -1) {//本轮发送结束
                    System.out.println("===========begin to the next=========="+startIndex);
                    startIndex = 0;
                    endIndex = 0;
                    map.clear();
                    //更改为已经发送状态
                    saveDataAsyn(ids);
                    ids.clear();
                    continue;
//                    }
//                }

//                if ("RESTART".equals(text)){
//                    System.out.println("===========begin to restart==========");
//                    startIndex = 0;
//                    endIndex = 0;
//                    map.clear();
//                    //更改为已经发送状态
//                    saveDataAsyn(ids);
//                    ids.clear();
//                    continue;
//                }
                    //          cdl.await();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            saveDataAsyn(ids);
        }
    }

    /**
     * 分页查询出数据并发送，收到响应后再发送下一批分页查询数据
     */
    public static void sendWithMultiQueue() {
/*
        Map<String,Integer> map = new HashMap<String, Integer>();

        Integer startIndex = 0;
        Integer endIndex = 0;
        Integer pageSize = 10000;//每次查询一万条

        final List<Integer> ids = new ArrayList<Integer>();
        int count = -1;

        try {
            while (true) {
                System.out.println("DBThread task nunm is:  "+ DBThread.getQueueSize());
                if(DBThread.getQueueSize() > 0){//等待当前数据保存完毕
                    System.out.println("DBThread.getQueueSize() = " + DBThread.getQueueSize());
                    TimeUnit.SECONDS.sleep(15);
                    startIndex = 0;
                    endIndex = 0;
                    count = -1;
//                    ids.clear();
                    map.clear();
                    continue;
                }

                map.put("state", 1);

                if(count == -1) {//新一轮发送开始，重新查询
                    count = dao.findTotalCountNotSend(map);
                    System.out.println("the new count is : "+ count+"; map= "+ map);
                }

                if (count <= 0){
                    count = -1;
                    System.out.println("===========waiting for new data=========="+count);
                    TimeUnit.SECONDS.sleep(15);
                    startIndex = 0;
                    endIndex = 0;
                    continue;
                }

                int totalPages =  (count + pageSize -1) / pageSize;
                //        CountDownLatch cdl = new CountDownLatch(totalPages);

                map.clear();
                map.put("state", 1);

                // 0-10001-20001-30001
                startIndex = endIndex == 0 ? 0 : endIndex + 1;
                endIndex = endIndex + pageSize;

                map.put("startIndex", startIndex);
                map.put("endIndex", endIndex);
                List<Employee> list = dao.findPageList(map);
                map.clear();



                final String data = toJson(list);
                //发送数据java.io.IOException: Wire format negotiation timeout: peer did not send his wire format.
                System.out.println("begin to send mes!");

                int var = endIndex / pageSize;

                int index = var % 4;

                switch (index){
                    case 0:
                        sendMes(data);
                        break;
                    case 1:
//                        sendMesWithDes(data,template1);
                        break;
                    case 2:
//                        sendMesWithDes(data,template2);
                        break;
                    case 3:
//                        sendMesWithDes(data,template3);
                        break;
                    default:
//                        sendMesWithDes(data,template1);
                        break;

                }
//                sendMes(data);
    //            cdl.countDown();

                for (Employee emp : list) {
                    ids.add(emp.getEmpno());
                }


                System.out.println("begin to receive the response");
                TextMessage msg = (TextMessage) recTemplate.receive();

                String text = msg.getText();
                System.out.println("the response is ---" + text);
                if ("SUCC".equals(text)) {
                    map.put("state", 1);

//                    startIndex = endIndex + 1;
//                    endIndex = endIndex + pageSize;

//                    map.put("startIndex", startIndex);
//                    map.put("endIndex", endIndex);

                    if (startIndex >= count && startIndex != 0 && count != -1) {//本轮发送结束
                        System.out.println("===========begin to the next==========");
                        startIndex = 0;
                        endIndex = 0;
                        map.clear();
                        //更改为已经发送状态
                        saveDataAsyn(ids);
                        ids.clear();
                        continue;
                    }
                }

                if ("RESTART".equals(text)){
                    System.out.println("===========begin to restart==========");
                    startIndex = 0;
                    endIndex = 0;
                    map.clear();
                    //更改为已经发送状态
                    saveDataAsyn(ids);
                    ids.clear();
                    continue;
                }
    //          cdl.await();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            saveDataAsyn(ids);
        }*/
    }

    private static void saveDataAsyn(List<Integer> ids){
        final List<Integer> list = new ArrayList<Integer>();
        list.addAll(ids);
        if (!CollectionUtils.isEmpty(ids)){
            DBThread.execute(new Runnable() {
                public void run() {
                    try {
                        System.out.print("update db size:"+ list.size());
                        sqlDao.updateEmpState(list);
                    } catch (SQLException e) {
                        logger.error(e);
                    }
                }
            });
        }
    }

    /**
     * 所有消息一次发送
     * @param json 所要发送的消息
     */
    private static void sendMes(final String json) throws Exception{

        try {
            template.send(new MessageCreator() {

                public Message createMessage(Session sen) throws JMSException {
                    TextMessage msg = sen.createTextMessage(json);
                    return msg;
                }

            });
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e);
            //发送失败则记录，可以后台重发
            throw e;
        }
    }

    /**
     * 所有消息一次发送
     * @param json 所要发送的消息
     */
    private static void sendMesWithDes(final String json, JmsTemplate template) throws Exception{

        try {
            template.send(new MessageCreator() {

                public Message createMessage(Session sen) throws JMSException {
                    TextMessage msg = sen.createTextMessage(json);
                    return msg;
                }

            });
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e);
            //发送失败则记录，可以后台重发
            throw e;
        }
    }
    /**
     * 所有消息一次发送
     * @param json 所要发送的消息
     */
    private static void sendMesWithDes(final String json, String destination) throws Exception{

        try {
            template.send(destination,new MessageCreator() {

                public Message createMessage(Session sen) throws JMSException {
                    TextMessage msg = sen.createTextMessage(json);
                    return msg;
                }

            });
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e);
            //发送失败则记录，可以后台重发
            throw e;
        }
    }

    private static String toJson(List<Employee> list)throws JSONException {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}
