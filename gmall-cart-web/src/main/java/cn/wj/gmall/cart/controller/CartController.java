package cn.wj.gmall.cart.controller;

import cn.wj.gmall.annotations.LoginRequired;
import cn.wj.gmall.bean.OmsCartItem;
import cn.wj.gmall.bean.PmsSkuInfo;
import cn.wj.gmall.service.CartService;
import cn.wj.gmall.service.SkuService;
import cn.wj.gmall.util.CookieUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {

    @Reference
    private CartService cartService;
    @Reference
    private SkuService skuService;





    @RequestMapping("addToCart")
    @LoginRequired(loginSuccess = false)
    public String addToCart(String skuId, int num, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        //获取添加购物车时传递来的参数 ，pmsSkuInfo
        PmsSkuInfo pmsSkuInfo = skuService.getSkuInfoById(skuId);
        //创建购物车对象,并将skuInfo 信息转成购物车 信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(pmsSkuInfo.getPrice());
        omsCartItem.setProductCategoryId(pmsSkuInfo.getCatalog3Id());
        omsCartItem.setProductId(pmsSkuInfo.getProductId());
        omsCartItem.setProductName(pmsSkuInfo.getSkuName());
        omsCartItem.setProductPic(pmsSkuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuId(skuId);
        //商品数量默认为1
        omsCartItem.setQuantity(new BigDecimal(num));
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        //先定义一个用户 ，默认为空 就是没登陆
        String memberId = (String)request.getAttribute("memberId");
        //添加购物车时 判断是否登陆
        if (StringUtils.isBlank(memberId)) {
            //用户为空 没登陆。。走Coookie 分支
            //先获取cookie
            String cartList = CookieUtil.getCookieValue(request, "cartListCookie", true);
            //如果cookie为空则说明 此时 购物车没有数据
            if (StringUtils.isBlank(cartList)) {
                //直接添加cookie
                omsCartItems.add(omsCartItem);
            } else {
                //有cookie则说明 购物车有商品，判断 购物车中的商品 和正要添加的 是否有重复的
                omsCartItems = JSON.parseArray(cartList, OmsCartItem.class);
                boolean exist = if_exist(omsCartItems, omsCartItem);
                if (exist) {
                    //存在 相同的商品
                    //修改数量
                    for (OmsCartItem cartItem : omsCartItems) {
                        if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                            cartItem.setQuantity(omsCartItem.getQuantity().add(cartItem.getQuantity()));
                            //    cartItem.setPrice(cartItem.getPrice().add(omsCartItem.getPrice()));
                        }
                    }
                } else {
                    //没有则 。
                    omsCartItems.add(omsCartItem);
                }
            }
            //更新cookie
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 60 * 60 * 72, true);
        } else {
            //登陆时
            //判断购物车是否有相同商品 查询数据库
            OmsCartItem omsCartItemFromDb = cartService.getCartItemByMemberIdAndSkuId(memberId, skuId);
            //如果有相同商品
            if (omsCartItemFromDb != null) {
                //更新此商品的数量和价格
                omsCartItemFromDb.setQuantity(omsCartItemFromDb.getQuantity().add(omsCartItem.getQuantity()));
                //  omsCartItemFromDb.setTotalPrice();
                //更新数据库
                cartService.updateCart(omsCartItemFromDb);
            } else {
                //没有此商品 则添加此商品
                omsCartItem.setMemberId(memberId);
                cartService.addCart(omsCartItem);

            }
            //更新购物车缓存
            cartService.flushCartCache(memberId);
        }
        modelMap.put("skuNum", num);
        modelMap.put("skuInfo", pmsSkuInfo);
        return "redirect:/success.html";
    }

    public boolean if_exist(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        boolean flag = false;
        if (omsCartItems != null) {
            for (OmsCartItem cartItem : omsCartItems) {
                if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                    //有一样的商品
                    flag = true;
                    return flag;
                }
            }
        }
        return flag;
    }

    @RequestMapping("cartList")
    @LoginRequired(loginSuccess = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        String memberId = (String)request.getAttribute("memberId");
        if (StringUtils.isBlank(memberId)) {
            //如果为空 ，则没有登陆 从cookie取数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)) {
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
            }
        } else {
            //不为空 则已登陆 从db 缓存取数据
            omsCartItems = cartService.cartList(memberId);
        }
        //总价
        BigDecimal totalAmount = getTotalAmount(omsCartItems);
        modelMap.put("totalAmount", totalAmount);

        modelMap.put("cartList", omsCartItems);
        return "cartList";
    }

    @RequestMapping("checkCart")
    @LoginRequired(loginSuccess = false)
    public String checkCart(String skuId, String isChecked, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        //查询数据库 修改数据
        String memberId = (String)request.getAttribute("memberId");
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setIsChecked(isChecked);
        cartService.checkCart(omsCartItem);
        //更新页面
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        //总价
        BigDecimal totalAmount = getTotalAmount(omsCartItems);
        modelMap.put("totalAmount", totalAmount);

        modelMap.put("cartList", omsCartItems);

        return "cartListInner";
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
