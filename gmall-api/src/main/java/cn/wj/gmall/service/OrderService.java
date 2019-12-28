package cn.wj.gmall.service;

import cn.wj.gmall.bean.OmsOrder;

public interface OrderService {
    String genTradeCode(String memberId);

    String checkTradeCode(String memberId,String tradeCode);

    void saveOrder(OmsOrder omsOrder);

    OmsOrder getOrderByOrderSn(String outOrderSn);

    void updateOrderByMq(String orderSn);
}
