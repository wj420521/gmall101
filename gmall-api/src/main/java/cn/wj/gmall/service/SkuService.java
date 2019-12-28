package cn.wj.gmall.service;

import cn.wj.gmall.bean.PmsSearchSkuInfo;
import cn.wj.gmall.bean.PmsSkuInfo;

import java.math.BigDecimal;
import java.util.List;


public interface SkuService {
    String saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuInfoById(String skuId);

    List<PmsSkuInfo> getSkuInfoBySpu(String productId);

    List<PmsSkuInfo> getSkuAll(String catalog3Id);

    boolean checkPrice(String productSkuId, BigDecimal price);
}
