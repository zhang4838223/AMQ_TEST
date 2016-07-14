package com.zxj.manger;

import com.google.gson.reflect.TypeToken;
import com.zxj.comm.DBThread;
import com.zxj.comm.LogicThread;
import com.zxj.comm.utils.TextUtils;
import com.zxj.dao.SqlDao;
import com.zxj.model.Emp;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.CollectionUtils;

import javax.jms.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by zhang4838223 on 2016/7/13.
 */
public class JmsReceiverManager {

    private final static Log logger = LogFactory.getLog(JmsReceiverManager.class);
    private final static JmsReceiverManager instance = new JmsReceiverManager();

    // JDBC执行批量操作
    private SqlDao sqlDao;
    //jms相关组件
    private ConnectionFactory connectionFactory;
    private Connection connection = null;

//    private Session session;
    //根据配置生成指定数量的队列和消费者
    private List<Destination> destinations = new ArrayList<Destination>();

    private List<MessageConsumer> consumers = new ArrayList<MessageConsumer>();
    //属性配置信息
    private Properties props = null;
    //默认队列数量
    private static int queueSize = 1;
    //默认每个队列的消费者数量
    private static int consumerSize = 1;

    private JmsReceiverManager(){
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-db.xml");
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-jms.xml");
        sqlDao = (SqlDao)ctx.getBean("sqlDao");
        connectionFactory = (ConnectionFactory) context.getBean("pooledConnectionFactory");
        try {
            connection = connectionFactory.createConnection();
            connection.start();

            init();
        } catch (IOException e) {
            logger.error(e);
        } catch (JMSException e) {
            logger.error(e);
        }
    }

    /**
     * 初始化队列以及消费者
     * @throws IOException
     * @throws JMSException
     */
    public void init() throws IOException, JMSException {
        destinations.clear();
        consumers.clear();

        props = PropertiesLoaderUtils.loadAllProperties("jms.properties");

        queueSize = Integer.valueOf(props.getProperty("queueSize"));
        consumerSize = Integer.valueOf(props.getProperty("consumerSize"));

        if (consumerSize <= 0){
            consumerSize = 1;
        }

        if (queueSize <= 0){
            queueSize = 1;
        }

        for (int i = 1; i <= queueSize; i++) {
            Session session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
            logger.warn("create queue --->"+("queue_" + i));
            Destination queue = session.createQueue("queue_" + i);
            destinations.add(queue);

            for (int j = 0; j < consumerSize; j++) {
                consumers.add(session.createConsumer(queue));
            }
        }
    }

    public void setListeners() throws JMSException {
        if (CollectionUtils.isEmpty(this.consumers)){
            logger.error("must set consumer first!!");
            return;
        }

        for (int i = 0, j = consumers.size(); i < j; i++) {

            final int k = i;
            consumers.get(i).setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                    System.out.println("listener["+k+"] is running!");
                    TextMessage msg = (TextMessage) message;
                    String text = null;
                    try {
                        text = msg.getText();
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }

                    System.out.println("response successfully");
                    final String data = text;
                    LogicThread.execute(new Runnable() {
                        public void run() {
                            batchSaveDataAsyn(data);
                        }
                    });
                }
            });
        }
    }
    public static JmsReceiverManager getInstance(){
        return instance;
    }
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

//    public Session getSession() {
//        return session;
//    }
//
//    public void setSession(Session session) {
//        this.session = session;
//    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<Destination> destinations) {
        this.destinations = destinations;
    }

    public List<MessageConsumer> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<MessageConsumer> consumers) {
        this.consumers = consumers;
    }

    public static int getQueueSize() {
        return queueSize;
    }

    public static void setQueueSize(int queueSize) {
        JmsReceiverManager.queueSize = queueSize;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
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
            System.out.println("error message: " + text);
            logger.warn(text);
            logger.error(e);
        }

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

        /*ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-db.xml");
        final SqlDao dao = (SqlDao)ctx.getBean("sqlDao");*/

        logger.info("insert data size:" + insertData.size());
        logger.info("updateData data size:" + updateData.size());
        if (!CollectionUtils.isEmpty(insertData)){
            DBThread.execute(new Runnable() {
                public void run() {
                    try {
//                        sqlDao.inserEmps(insertData);
                        sqlDao.insertEmpsWithPro(insertData);
                    } catch (Exception e) {
                        System.out.println(insertData);
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
                } catch (Exception e) {

                    //TODO 这里保存失败可以做备份处理
                }
            }
        });
    }

    public void shutDown(){
        if (null != connection){
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
                logger.error(e);
            }
        }
    }



}
