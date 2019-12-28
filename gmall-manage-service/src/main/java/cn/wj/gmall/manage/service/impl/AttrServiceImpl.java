package cn.wj.gmall.manage.service.impl;

import cn.wj.gmall.bean.PmsBaseAttrInfo;
import cn.wj.gmall.bean.PmsBaseAttrValue;
import cn.wj.gmall.bean.PmsBaseSaleAttr;
import cn.wj.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import cn.wj.gmall.manage.mapper.PmsBaseAttrValueMapper;
import cn.wj.gmall.manage.mapper.PmsBaseSaleAttrMapper;
import cn.wj.gmall.service.AttrService;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    private PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;
    @Autowired
    private PmsBaseAttrValueMapper pmsBaseAttrValueMapper;
    @Autowired
    private PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;

    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);
        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfos) {
            //根据attrId查询属性值
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());
            List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
            baseAttrInfo.setAttrValueList(pmsBaseAttrValues);
        }
        return pmsBaseAttrInfos;
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        return pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
    }

    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {
        String id = pmsBaseAttrInfo.getId();
        if(StringUtils.isBlank(id)){
            //如果id为空则是 添加方法
            //添加属性  会自动 返回 主键值
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo); //insertSelective  值为空则不添加
            //添加属性值
            List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : pmsBaseAttrValues) {
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }
        }else{
            //修改
            //先修改平台属性
            Example e = new Example(PmsBaseAttrInfo.class);
            //根据id修改
            e.createCriteria().andEqualTo("id",pmsBaseAttrInfo.getId());
            //(目标,原对象)
            pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo,e);

            // 属性值修改
            List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrInfo.getAttrValueList();
//          //按照属性id删除所有属性值
            PmsBaseAttrValue p = new PmsBaseAttrValue();
            p.setAttrId(pmsBaseAttrInfo.getId());
            pmsBaseAttrValueMapper.delete(p);
            //将新的属性值插入
            for (PmsBaseAttrValue pmsBaseAttrValue : pmsBaseAttrValues) {
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }
        }
        return null;
    }

    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        return pmsBaseSaleAttrMapper.selectAll();
    }

    @Override
    public List<PmsBaseAttrInfo> getAttrValueListByValueId(String valueIdStr) {
        List<PmsBaseAttrInfo> pmsBaseAttrInfos =  pmsBaseAttrInfoMapper.selectAttrValueListByValueId(valueIdStr);
        return pmsBaseAttrInfos;
    }


}
