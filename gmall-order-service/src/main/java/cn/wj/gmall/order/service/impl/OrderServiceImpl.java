package cn.wj.gmall.order.service.impl;

import cn.wj.gmall.bean.OmsOrder;
import cn.wj.gmall.bean.OmsOrderItem;
import cn.wj.gmall.mq.ActiveMQUtil;
import cn.wj.gmall.order.mapper.OmsOrderItemMapper;
import cn.wj.gmall.order.mapper.OmsOrderMapper;
import cn.wj.gmall.service.CartService;
import cn.wj.gmall.service.OrderService;
import cn.wj.gmall.util.RedisUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    private OmsOrderMapper omsOrderMapper;
    @Autowired
    private OmsOrderItemMapper omsOrderItemMapper;
    @Reference
    private CartService cartService;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Override
    public String genTradeCode(String memberId) {
        Jedis jedis = null;
        String tradeCode = "";
        try {
            jedis = redisUtil.getJedis();
            tradeCode = UUID.randomUUID().toString();
            jedis.setex("User:" + memberId + ":tradeCode", 60 * 15, tradeCode);
        } finally {
            jedis.close();
        }
        return tradeCode;
    }

    @Override
    public String checkTradeCode(String memberId, String tradeCode) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String tradeKey = "User:" + memberId + ":tradeCode";
            String tradeCodeFromCache = jedis.get(tradeKey);
            //对比防重删令牌  防止高并发下 一次提交多个订单 lua脚本 发现即删除
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey), Collections.singletonList(tradeCode));
            if (eval != null && eval != 0) {
                return "success";
            } else {
                return "fail";
            }

            //    if(tradeCodeFromCache!=null&&tradeCodeFromCache.equals("tradeCode")){
//                //页面的tradeCode和缓存中的一致
//                //删除缓存
//               jedis.del("User:" + memberId + ":trade");

        } finally {
            jedis.close();
        }
    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {
        omsOrderMapper.insertSelective(omsOrder);
        //主键返回策略 此时就有id了
        List<OmsOrderItem> orderItemList = omsOrder.getOrderItemList();
        for (OmsOrderItem omsOrderItem : orderItemList) {
            omsOrderItem.setOrderId(omsOrder.getId());
            omsOrderItemMapper.insertSelective(omsOrderItem);
            //删除购物车
            //cartService.deleteBySkuId(omsOrderItem.getProductSkuId());
        }


    }

    @Override
    public OmsOrder getOrderByOrderSn(String outOrderSn) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(outOrderSn);
        OmsOrder omsOrder1 = omsOrderMapper.selectOne(omsOrder);
        return omsOrder1;
    }

    @Override
    public void updateOrderByMq(String orderSn) {
        //支付后通过订单号更新订单
        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn",orderSn);
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(orderSn);
        omsOrder.setStatus("1");  //订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单
        omsOrder.setPaymentTime(new Date()); //支付时间

        //通知库存
        ConnectionFactory connectionFactory = null;
        Connection connection = null;
        try {
            connectionFactory = activeMQUtil.getConnectionFactory();
            connection = connectionFactory.createConnection();
        } catch (JMSException e) {
            e.printStackTrace();
        }

        Session session = null;
        try {
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(queue);

            MapMessage mapMessage = new ActiveMQMapMessage();
          //  mapMessage.setString("","");
               //发消息
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            //更新订单和给库存发消息 一起
            omsOrderMapper.updateByExampleSelective(omsOrder,example);
            producer.send(mapMessage);

            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }


    }


}
