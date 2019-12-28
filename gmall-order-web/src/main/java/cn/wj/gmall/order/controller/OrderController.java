package cn.wj.gmall.order.controller;

import cn.wj.gmall.annotations.LoginRequired;
import cn.wj.gmall.bean.*;
import cn.wj.gmall.service.CartService;
import cn.wj.gmall.service.OrderService;
import cn.wj.gmall.service.SkuService;
import cn.wj.gmall.service.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
public class OrderController {

    @Reference
    private UserService userService;
    @Reference
    private CartService cartService;
    @Reference
    private OrderService orderService;
    @Reference
    private SkuService skuService;
    @RequestMapping("toTrade")
    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        String memberId = (String)request.getAttribute("memberId");
        String nickName = (String)request.getAttribute("nickName");

        List<UmsMemberReceiveAddress> receiveAddressByMemberId = userService.getReceiveAddressByMemberId(memberId);

        List<OmsCartItem> cartItemList = cartService.cartList(memberId);
        List<OmsOrderItem> omsOrderItems = new ArrayList<>();
        for (OmsCartItem omsCartItem : cartItemList) {
            OmsOrderItem omsOrderItem = new OmsOrderItem();
            omsOrderItem.setProductPic(omsCartItem.getProductPic());
            omsOrderItem.setProductName(omsCartItem.getProductName());
            omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
            omsOrderItems.add(omsOrderItem);
        }

        modelMap.put("userAddressList",receiveAddressByMemberId);
        modelMap.put("orderDetailList",omsOrderItems);
        modelMap.put("totalAmount",getTotalAmount(cartItemList));
        //去结算时生成交易码
        String tradeCode = orderService.genTradeCode(memberId);
        //放到页面
        modelMap.put("tradeCode",tradeCode);
        return "trade";
    }

    @RequestMapping("submitOrder")
    @LoginRequired(loginSuccess = true)
    public String submitOrder(String tradeCode,String receiveAddressId,HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        String memberId = (String)request.getAttribute("memberId");
        String nickName = (String)request.getAttribute("nickName");
    //提交订单时检查交易码
        String success = orderService.checkTradeCode(memberId,tradeCode);
        if(success.equals("success")){
            //检查通过
            //获取用户下单地址id
            //查询购物车被选中的商品
            List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            //生成订单对象
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setCreateTime(new Date()); //订单创建时间
            omsOrder.setMemberId(memberId);
            omsOrder.setAutoConfirmDay(7); //自动确认收获时间
           // 订单编号
            String outOrderSn = "gmall101";
            outOrderSn = outOrderSn + System.currentTimeMillis();
            outOrderSn = outOrderSn + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            omsOrder.setOrderSn(outOrderSn);
            omsOrder.setMemberUsername(nickName);
            omsOrder.setNote("快点发货"); //订单备注
            omsOrder.setOrderType(0); //订单类型：0->正常订单；1->秒杀订单
            omsOrder.setPayAmount(getTotalAmount(omsCartItems));
            //根据收获地址id查询收获信息
            UmsMemberReceiveAddress umsMemberReceiveAddress =  userService.getReceiveAddressById(receiveAddressId);
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());//区
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setSourceType(0); //订单来源：0->PC订单；1->app订单
            omsOrder.setStatus("0"); //订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单

            for (OmsCartItem omsCartItem : omsCartItems) {
                if(omsCartItem.getIsChecked().equals("1")){
                    //校验商品价格
                    //校验库存数量
                    boolean flag = skuService.checkPrice(omsCartItem.getProductSkuId(),omsCartItem.getPrice());
                    if(flag==true){
                        //价格一致
                        //订单详情对象
                        OmsOrderItem omsOrderItem = new OmsOrderItem();
                        omsOrderItem.setProductPrice(omsCartItem.getPrice());
                        omsOrderItem.setProductName(omsCartItem.getProductName());
                        omsOrderItem.setProductPic(omsCartItem.getProductPic());
                        omsOrderItem.setProductAttr(""); //商品详情
                        omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId()); //三级分类id
                        omsOrderItem.setProductId(omsCartItem.getProductId());
                        omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                        omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                        omsOrderItem.setRealAmount(omsCartItem.getTotalPrice()); //优惠后的价格
                        omsOrderItem.setOrderSn(omsOrder.getOrderSn());
                        //omsOrderItem.setOrderId();
                        omsOrderItems.add(omsOrderItem);
                    }else{
                        return "tradeFail" ;
                    }
                }
            }
            omsOrder.setOrderItemList(omsOrderItems);
            //保存订单
            orderService.saveOrder(omsOrder);
            //删除购物车中的此商品（保存订单的时候）
            //重定向到支付系统
            return "redirect:http://payment.gmall.com:8087/index?outOrderSn="+outOrderSn+"&totalAmount="+getTotalAmount(omsCartItems);
        }else{
            //没通过 返回错误页面
            return "tradeFail" ;
        }
    }

    //计算总价格
    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsCartItem cartItem : omsCartItems) {
            //选中状态才计算
            if (cartItem.getIsChecked().equals("1")) {
                totalAmount = totalAmount.add(cartItem.getTotalPrice());
            }
        }
        return totalAmount;
    }
}
