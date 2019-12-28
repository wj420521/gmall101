package cn.wj.gmall.payment.service.impl;

import cn.wj.gmall.bean.PaymentInfo;
import cn.wj.gmall.mq.ActiveMQUtil;
import cn.wj.gmall.payment.mapper.PaymentInfoMapper;
import cn.wj.gmall.service.PaymentService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        PaymentInfo pay = new PaymentInfo();
        pay.setOrderSn(paymentInfo.getOrderSn());
        PaymentInfo paymentInfo1 = paymentInfoMapper.selectOne(pay);
        //幂等性检查
        if (StringUtils.isNotBlank(paymentInfo1.getPaymentStatus()) && paymentInfo1.getPaymentStatus().equals("已支付")) {
            //如果交易状态为已支付,直接返回
            return;
        } else {
            Example example = new Example(PaymentInfo.class);
            example.createCriteria().andEqualTo("orderSn", paymentInfo.getOrderSn());

            //更新支付信息后 通知订单
            ConnectionFactory connectionFactory = null;
            Connection connection = null;
            try {
                connectionFactory = activeMQUtil.getConnectionFactory();
                connection = connectionFactory.createConnection();
                connection.start();
            } catch (JMSException e) {
                e.printStackTrace();
            }
            Session session = null;

            try {
                paymentInfoMapper.updateByExample(paymentInfo, example);
                //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
                session = connection.createSession(true, Session.SESSION_TRANSACTED);
                Queue queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");

                MessageProducer producer = session.createProducer(queue);

                MapMessage mapMessage = new ActiveMQMapMessage();
                mapMessage.setString("orderSn", paymentInfo.getOrderSn());
                //消息设置持久化
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
                //发消息
                producer.send(mapMessage);
                session.commit();
            } catch (Exception e) {
                try {
                    session.rollback();
                } catch (JMSException ex) {
                    ex.printStackTrace();
                }
            } finally {
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void sendCheckPaymentResult(String outOrderSn, int count) {
        Connection connection = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
        } catch (JMSException e) {
            e.printStackTrace();
        }

        Session session = null;
        try {
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue queue = session.createQueue("PAYMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(queue);
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no", outOrderSn);
            mapMessage.setInt("count", count);
            //设置延迟队列时间 60s发送一次
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 1000 * 60);
            producer.send(mapMessage);
            session.commit();
        } catch (JMSException e) {
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public Map<String, Object> checkAlipayPaymentResult(String outOrderSn) {
        //获取支付结果
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, String> map = new HashMap<>();
        map.put("out_trade_no", outOrderSn);
        request.setBizContent(JSON.toJSONString(map));

        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        Map<String, Object> resultMap = new HashMap<>();
        //交易状态：WAIT_BUYER_PAY（交易创建，等待买家付款）、TRADE_CLOSED（未付款交易超时关闭，或支付完成后全额退款）、
        // TRADE_SUCCESS（交易支付成功）、TRADE_FINISHED（交易结束，不可退款）
        if (response.isSuccess()) {
            //调用成功
            resultMap.put("out_trade_no", response.getOutTradeNo());
            resultMap.put("trade_no", response.getTradeNo());
            resultMap.put("trade_status", response.getTradeStatus());
            resultMap.put("total_amount", response.getTotalAmount());
            resultMap.put("callbackContent", response.getMsg());
            System.out.println("调用成功");

        } else {
            //调用失败 可能交易未创建
            System.out.println("调用失败");
        }
        return resultMap;
    }
}
