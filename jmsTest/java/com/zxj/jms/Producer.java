package com.zxj.jms;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zxj.comm.DBThread;
import com.zxj.comm.DelayedTask;
import com.zxj.comm.JMSConstants;
import com.zxj.comm.LogicThread;
import com.zxj.dao.EmployeeDao;
import com.zxj.dao.SqlDao;
import com.zxj.mybatis.map.Employee;
import com.zxj.utils.UtilsFun;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.util.CollectionUtils;

import javax.jms.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhang4838223 on 2016/6/30.
 */
public class Producer {

    private final static Log logger = LogFactory.getLog(Producer.class);
    public static ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-jms.xml");
    public static JmsTemplate template =(JmsTemplate)context.getBean("jmsTemplate");
    public static ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-database.xml");
    private static EmployeeDao dao = (EmployeeDao)ctx.getBean("emplyoeeDao");
    private final static SqlDao sqlDao = (SqlDao)ctx.getBean("sqlDao");

    static {
        template.setSessionTransacted(true);
        template.setDeliveryPersistent(false);
    }
    public static void main(String[] args) throws Exception {
//        sendByPage();
//        sendByPageRes();
        timerTestByPage();
//        timerTest();
    }

    public static void jmsTest1() throws Exception {
        System.out.println("producer is runnng!");
        List<Employee> list = getDataFromDb();

        if (CollectionUtils.isEmpty(list)){
            logger.warn("ALL DATA HAS BEEN SENDED!!");
            return;
        }

        long start = System.currentTimeMillis();
        batchSendMes(list);//所有消批量发送
        System.out.println("the cost is:"+ (System.currentTimeMillis()-start));
        System.out.println("send msg successfully!!");
    }

    public static void sendByPage() throws Exception {
        Map<String,Integer> map = new HashMap<String, Integer>();

        Integer startIndex = 0;
        Integer endIndex = 0;
        Integer pageSize = 10000;//每次查询一万条

        map.put("state", 1);
        Integer count = dao.findTotalCountNotSend(map);

        if (count <= 0){
            logger.info("there is no data to send!!");
            return;
        }
        int totalPages =  (count + pageSize -1) / pageSize;

        map.clear();
        map.put("state", 1);

        for (int i=0; i<totalPages;i++) {
            startIndex = endIndex + 1;
            endIndex = endIndex + pageSize;

            map.put("startIndex", startIndex);
            map.put("endIndex", endIndex);
            List<Employee> list = dao.findPageList(map);

            final String data = toJson(list);
            //发送数据java.io.IOException: Wire format negotiation timeout: peer did not send his wire format.
            System.out.println("begin to send mes!");
            sendMes(data);


            DBThread.execute(new Runnable() {
                public void run() {
                    try {
                        final List<Employee> emps = UtilsFun.gson.fromJson(data,
                                new TypeToken<List<Employee>>() {
                                }.getType());
                        sqlDao.updateEmps(emps);
                    } catch (SQLException e) {
                        logger.error(e);
                    }
                }
            });
        }
    }

    /**
     * 分页查询出数据并发送，收到响应后再发送下一批分页查询数据
     */
    public static void sendByPageRes() {
        Map<String,Integer> map = new HashMap<String, Integer>();

        Integer startIndex = 0;
        Integer endIndex = 0;
        Integer pageSize = 10000;//每次查询一万条

        map.put("state", 1);
        Integer count = dao.findTotalCountNotSend(map);

        if (count <= 0){
            System.out.println("===========restart==========");
            try {
                sendMes("RESTART");//重新开始
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        int totalPages =  (count + pageSize -1) / pageSize;
//        CountDownLatch cdl = new CountDownLatch(totalPages);

        map.clear();
        map.put("state", 1);

        endIndex = endIndex + pageSize;

        map.put("startIndex", startIndex);
        map.put("endIndex", endIndex);
        List<Employee> list = dao.findPageList(map);
        map.clear();

        final List<Integer> ids = new ArrayList<Integer>();

        try {
            final String data = toJson(list);
            //发送数据java.io.IOException: Wire format negotiation timeout: peer did not send his wire format.
            System.out.println("begin to send mes!");
            sendMes(data);
//            cdl.countDown();

            for (Employee emp : list) {
                ids.add(emp.getEmpno());
            }

            while (true) {
                System.out.println("begin to receive the response");
                TextMessage msg = (TextMessage) template.receive();

                String text = msg.getText();
                System.out.println("the response is ---" + text);
                if ("SUCC".equals(text)) {
                    map.put("state", 1);

                    startIndex = endIndex + 1;
                    endIndex = endIndex + pageSize;

                    map.put("startIndex", startIndex);
                    map.put("endIndex", endIndex);

                    if (startIndex >= count) {
                        System.out.println("===========restart==========");
                        sendMes("RESTART");
                        break;
                    }

                    List<Employee> result = dao.findPageList(map);
                    System.out.println("the db result size is ---" + result.size());
                    if (CollectionUtils.isEmpty(result)) {
                        sendMes("RESTART");
                        break;
                    }
                    map.clear();
                    final String mes = toJson(result);
                    //发送数据java.io.IOException: Wire format negotiation timeout: peer did not send his wire format.
                    System.out.println("begin to send mes!======");
                    sendMes(mes);

                    for (Employee emp : result) {
                        ids.add(emp.getEmpno());
                    }
//                    cdl.countDown();
                }
            }

//            cdl.await();
        }catch (Exception e){
            e.printStackTrace();
        }
        //更改为已经发送状态
        if (!CollectionUtils.isEmpty(ids)){
            DBThread.execute(new Runnable() {
                public void run() {
                    try {
                        sqlDao.updateEmpState(ids);
                    } catch (SQLException e) {
                        logger.error(e);
                    }
                }
            });
        }
    }


    public static void timerTestByPage() throws Exception {
        while (true){
            System.out.println("logic queue size:"+LogicThread.getLogicQueueSize()+"; db size: "+DBThread.getQueueSize());

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
    public static void timerTest() throws Exception {
        /*ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
        service.scheduleAtFixedRate(new Runnable() {
            public void run() {
                System.out.println(System.currentTimeMillis());
            }
        },1,2, TimeUnit.SECONDS);*/

        while (true){
            System.out.println("logic queue size:"+LogicThread.getLogicQueueSize()+"; db size: "+DBThread.getQueueSize());
            if (LogicThread.getLogicQueueSize() == 0 && DBThread.getQueueSize() == 0) {
                jmsTest1();
            }
            TimeUnit.MINUTES.sleep(10);
        }

//        LogicThread.init();
       /* LogicThread
                .scheduleTask(new DelayedTask(2, 1000*10) {
                    public void run() {

                        System.out.println("logic queue size:"+LogicThread.getLogicQueueSize()+"; db size: "+DBThread.getQueueSize());
                        if (LogicThread.getLogicQueueSize() == 0 && DBThread.getQueueSize() == 0) {
                            try {
                                jmsTest1();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });*/
    }

    /**
     * 所有消批量发送
     * @param list 所要发送的数据
     */
    private static void batchSendMes(List<Employee> list) throws JSONException {
        if(null == list || list.size()==0){
            return;
        }

        final List<Employee> mes = new ArrayList<Employee>();

        int k = 0;//发送标记
        try {
            for (int i = 0, j = list.size(); i < j; i++) {
                mes.add(list.get(i));

                if (mes.size() % JMSConstants.getBatchSize() == 0) {
//                sendJmsMes(data);
                    final String data = toJson(mes);
                    LogicThread.execute(new Runnable() {
                        public void run() {

                            try {
                                sendMes(data);

                                final List<Employee> emps = UtilsFun.gson.fromJson(data,
                                        new TypeToken<List<Employee>>() {
                                        }.getType());

                                DBThread.execute(new Runnable() {
                                    public void run() {
                                        try {
                                            sqlDao.updateEmps(emps);
                                        } catch (SQLException e) {
                                            logger.error(e);
                                        }
                                    }
                                });

                            } catch (Exception e) {

                            }

                        }
                    });
                    mes.clear();
                    System.out.println("send " + i + "th times!");
                    k = i;//已经发送的记录数
                }
            }

            if (mes.size() > 0) {
                final String data = toJson(mes);
//            sendJmsMes(data);
//                sendMes(data);
//                k = list.size();

                LogicThread.execute(new Runnable() {
                    public void run() {

                        try {
                            sendMes(data);

                            final List<Employee> emps = UtilsFun.gson.fromJson(data,
                                    new TypeToken<List<Employee>>() {
                                    }.getType());

                            DBThread.execute(new Runnable() {
                                public void run() {
                                    try {
                                        sqlDao.updateEmps(emps);
                                    } catch (SQLException e) {
                                        logger.error(e);
                                    }
                                }
                            });

                        } catch (Exception e) {

                        }

                    }
                });
            }

            /*for (int i = 0; i < k; i++) {//更改已经发生成功的数据
                Employee employee = list.get(i);
                //emp.setState(1);
                //update(employee);
                //
            }*/
        }catch (Exception e){

        }
    }

    /**
     * 所有消息一次发送
     * @param json 所要发送的消息
     */
    private static void sendJmsMes(final String json) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-jms.xml");
        JmsTemplate template =(JmsTemplate)context.getBean("jmsTemplate");
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

        }
    }/**
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

    private static String toJson(List<Employee> list)throws JSONException {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }

    private static List<Employee> getDataFromDb() throws JSONException {
//        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-database.xml");
//        EmployeeDao dao = (EmployeeDao)ctx.getBean("emplyoeeDao");

        Map<String,Integer> map = new HashMap<String, Integer>();
        map.put("state",1);
//        Employee result = dao.select(map);
        /*for (int i = 0; i <5000 ; i++) {
            Employee emp = new Employee(10+i,"test","testjob",7369,new Date(),1000.0+i,10.0+i,20);
            dao.insert(emp);
            System.out.println("insert "+(10+i)+"th"+" record");
        }*/
        List<Employee> list = dao.selectAllWithNotSend(map);
        return list;
    }

    private static void baseSend(String data){
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        try {
            connection = template.getConnectionFactory().createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue("monkey");
            Message message = session.createTextMessage(data);
            producer = session.createProducer(destination);

            // 消息为非持久化消息
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            // 设置心跳消息的存活时间，因为会定时发送，所以，没有必要永久存在。
            producer.setTimeToLive(1 * 1000);
            producer.send(message);
            System.out.println("Send Message Completed!");

            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
    private static void jmsTest() throws JSONException {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-jms.xml");
        final JSONObject json = new JSONObject();
        json.put("timestamp", new Date());
        json.put("itemName", "the message is sended!");
        json.put("unitPrice", "5");
        json.put("amount", "20");
        JmsTemplate template =(JmsTemplate)context.getBean("jmsTemplate");
        template.send(new MessageCreator() {

            public Message createMessage(Session sen) throws JMSException {
                TextMessage msg = sen.createTextMessage(json.toString());
                return msg;
            }

        });
    }
}
