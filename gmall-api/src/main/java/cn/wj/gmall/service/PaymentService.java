package cn.wj.gmall.service;

import cn.wj.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);

    void sendCheckPaymentResult(String outOrderSn,int count);

    Map<String, Object> checkAlipayPaymentResult(String outOrderSn);
}
