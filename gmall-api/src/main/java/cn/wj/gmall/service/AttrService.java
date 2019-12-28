package cn.wj.gmall.service;

import cn.wj.gmall.bean.PmsBaseAttrInfo;
import cn.wj.gmall.bean.PmsBaseAttrValue;
import cn.wj.gmall.bean.PmsBaseSaleAttr;

import java.util.List;
import java.util.Set;

public interface AttrService {
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    List<PmsBaseAttrValue> getAttrValueList(String attrId);


    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseSaleAttr> baseSaleAttrList();

    List<PmsBaseAttrInfo> getAttrValueListByValueId(String valueIdStr);
}
