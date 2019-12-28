package cn.wj.gmall.payment.controller;

import cn.wj.gmall.annotations.LoginRequired;
import cn.wj.gmall.bean.OmsOrder;
import cn.wj.gmall.bean.PaymentInfo;
import cn.wj.gmall.payment.config.AlipayConfig;
import cn.wj.gmall.service.OrderService;
import cn.wj.gmall.service.PaymentService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Autowired
    AlipayClient alipayClient;
    @Reference
    private OrderService orderService;
    @Reference
    private PaymentService paymentService;
    @RequestMapping("index")
    @LoginRequired(loginSuccess = true)
    public String index(String outOrderSn, BigDecimal totalAmount, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("nickName");
        modelMap.put("outOrderSn", outOrderSn);
        modelMap.put("totalAmount", totalAmount);
        modelMap.put("nickName", nickName);
        return "index";
    }

    //支付宝
    @RequestMapping("alipay/submit")
    @LoginRequired(loginSuccess = true)
    @ResponseBody
    public String alipaySubmit(String outOrderSn, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        String totalAmount = request.getParameter("totalAmount");

        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的req
        Map<String, Object> map = new HashMap<>();
        map.put("out_trade_no", outOrderSn);
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        map.put("total_amount", 0.01);
        map.put("subject", "笔记本电脑");  //订单名称

        alipayRequest.setBizContent(JSON.toJSONString(map));
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址

        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        OmsOrder omsOrder = orderService.getOrderByOrderSn(outOrderSn);

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(outOrderSn);
        paymentInfo.setSubject("电脑一件");
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setTotalAmount(new BigDecimal(totalAmount));
        //生成支付信息
        paymentService.savePaymentInfo(paymentInfo);
        //延迟消息队列
        //检查支付支付结果  初始化发送的次数 超过5次 不管交易是否成功都不管了
        paymentService.sendCheckPaymentResult(outOrderSn,5);
        return form;
    }


    //微信支付
    @RequestMapping("mx/submit")
    @LoginRequired(loginSuccess = true)
    public String mxSubmit(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        return null;
    }

    //支付宝回调
    @RequestMapping("alipay/callback/return")
    @LoginRequired(loginSuccess = true)
    public String alipayCallbackReturn(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        String trade_no = request.getParameter("trade_no");
       String out_trade_no =  request.getParameter("out_trade_no");
        String sign = request.getParameter("sign");
        String subject = request.getParameter("subject");
        String totalAmount =request.getParameter("total_amount");
        String callbackContent = request.getQueryString();
        if(sign!=null){
            //同步回调没有map参数  此处模拟验签
            //验签成功
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setTotalAmount(new BigDecimal(totalAmount));
            paymentInfo.setCallbackContent(callbackContent);
            paymentInfo.setCallbackTime(new Date());
            //更新数据库 支付信息
            paymentService.updatePayment(paymentInfo);
            //启动订单服务 --库存--物流
        }

        return "finish";
    }
}
