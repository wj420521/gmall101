package cn.wj.gmall.service;

import cn.wj.gmall.bean.UmsMember;
import cn.wj.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService{
     List<UmsMember> findAllUser();
     List<UmsMemberReceiveAddress>  getReceiveAddressByMemberId(String memberId);

    UmsMember getUser(UmsMember member);

    void addUserToken(String token, String memeberId);

    void addUserFromSina(UmsMember umsMember);

    UmsMember checkUserUid(String idstr);

    UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId);
}
