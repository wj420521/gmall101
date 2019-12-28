package cn.wj.gmall.payment;

import cn.wj.gmall.mq.ActiveMQUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPaymentServiceApplicationTests {

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Test
    public void contextLoads() throws JMSException {
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();

    }

}
