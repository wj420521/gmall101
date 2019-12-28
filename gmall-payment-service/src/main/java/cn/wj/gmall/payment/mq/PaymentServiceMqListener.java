package cn.wj.gmall.payment.mq;

import cn.wj.gmall.bean.PaymentInfo;
import cn.wj.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Component
public class PaymentServiceMqListener {

    @Autowired
    private PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void customAlipayPaymentResult(MapMessage mapMessage) throws JMSException {
        String outOrderSn = mapMessage.getString("out_trade_no");
        int count = mapMessage.getInt("count");
        //调用支付接口 查询交易状态
        Map<String,Object> resultMap = paymentService.checkAlipayPaymentResult(outOrderSn);
        if(resultMap==null||resultMap.isEmpty()){
            //如果为空 则 调用支付宝失败 交易未创建
            //再次发送检查的消息
            if(count>0){
                count--;
                System.out.println("交易未创建，剩余次数:"+count);
                paymentService.sendCheckPaymentResult(outOrderSn,count);
            }
        }else{
            //不为空 调用成功
            if(resultMap.get("trade_status").equals("TRADE_SUCCESS")){
                //交易成功
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setAlipayTradeNo((String) resultMap.get("trade_no"));
                paymentInfo.setOrderSn((String) resultMap.get("out_trade_no"));
                paymentInfo.setPaymentStatus("已支付");
                paymentInfo.setTotalAmount(new BigDecimal((String) resultMap.get("total_amount")));
                paymentInfo.setCallbackContent((String) resultMap.get("callbackContent"));
                paymentInfo.setCallbackTime(new Date());
                //更新数据库 支付信息
                paymentService.updatePayment(paymentInfo);
                System.out.println("交易成功，更新支付信息，和通知订单");
                return;
            }else{
                if(count>0){
                    count--;
                    //交易已创建，但未支付
                    System.out.println("交易已创建，但未支付，再次调用消息发送检查");
                    paymentService.sendCheckPaymentResult(outOrderSn,count);
                }
            }

        }
    }
}
