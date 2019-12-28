package cn.wj.gmall.service;

import cn.wj.gmall.bean.PmsProductImage;
import cn.wj.gmall.bean.PmsProductInfo;
import cn.wj.gmall.bean.PmsProductSaleAttr;
import cn.wj.gmall.bean.PmsSkuInfo;

import java.util.List;

public interface SpuService {
    List<PmsProductInfo> spuList(String catalog3Id);

    void saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    List<PmsProductImage> spuImageList(String spuId);

    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku( String productId,String skuId);

    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(PmsSkuInfo pmsSkuInfo);
}
