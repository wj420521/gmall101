package cn.wj.gmall.service;

import cn.wj.gmall.bean.OmsCartItem;

import java.util.List;


public interface CartService {
    OmsCartItem getCartItemByMemberIdAndSkuId(String memberId,String skuId);

    void addCart(OmsCartItem omsCartItem);

    void flushCartCache(String memberId);

    void updateCart(OmsCartItem omsCartItemFromDb);

    List<OmsCartItem> cartList(String memberId);

    void checkCart(OmsCartItem omsCartItem);

    void deleteBySkuId(String productSkuId);
}
