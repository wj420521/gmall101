package cn.wj.gmall.order.mq;

import cn.wj.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderServiceMqLisener {

    @Autowired
    private OrderService orderService;
    //订单服务 接收支付服务的通知
    @JmsListener(destination = "PAYMENT_SUCCESS_QUEUE",containerFactory = "jmsQueueListener")
    public void consumePaymentResult(MapMessage mapMessage) throws JMSException {
        String orderSn = mapMessage.getString("orderSn");
        //更新订单
        orderService.updateOrderByMq(orderSn);

    }
}
