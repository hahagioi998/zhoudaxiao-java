package com.yiseven.zhoudaxiao.service.impl;

import com.yiseven.zhoudaxiao.common.Const.Const;
import com.yiseven.zhoudaxiao.common.exception.ExceptionThrow;
import com.yiseven.zhoudaxiao.common.response.Response;
import com.yiseven.zhoudaxiao.common.response.ResponseCode;
import com.yiseven.zhoudaxiao.common.util.MD5Utils;
import com.yiseven.zhoudaxiao.common.util.RedisUtil;
import com.yiseven.zhoudaxiao.entity.UserEntity;
import com.yiseven.zhoudaxiao.mapper.ext.UserEntityMapperExt;
import com.yiseven.zhoudaxiao.service.UserService;
import com.yiseven.zhoudaxiao.web.request.UserRequest;
import com.yiseven.zhoudaxiao.web.result.UserListResult;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @author hdeng
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    UserEntityMapperExt userEntityMapperExt;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public Response addUser(UserRequest userRequest) {
        //1.是否已注册
        UserEntity queryResult = userEntityMapperExt.queryUser(userRequest.getPhone());
        if (queryResult != null) {
            return Response.createByErrorMessage("该手机已经被注册");
        }
        UserEntity userEntity = new UserEntity();
        modelMapper.map(userRequest, userEntity);
        userEntity.setStatus(Const.REVIEW_STATUS);
        userEntity.setCreateDate(new Date());
        userEntity.setLastUpdateDate(new Date());
        userEntity.setPassword(MD5Utils.getMd5Simple(userEntity.getPassword()));
        int result = userEntityMapperExt.insertSelective(userEntity);
        if (Const.INSERT_ONE == result) {
            log.info("用户 {} 注册成功", userEntity.getUsername() + " " + userEntity.getPhone());
            return Response.createBySuccess();
        }
        return Response.createByErrorMessage("用户注册失败");
    }

    @Override
    public Response updateUser(UserEntity userEntity) {
        int resultCount = userEntityMapperExt.updateByPrimaryKeySelective(userEntity);
        ExceptionThrow.cast(ResponseCode.DATABASE_ERROR, 1 != resultCount);
        return Response.createBySuccess();
    }

    @Override
    public Response queryUser(String phone) {
        UserEntity userEntity = userEntityMapperExt.queryUser(phone);
        ExceptionThrow.cast(ResponseCode.RESULT_NULL, null == userEntity);
        return Response.createBySuccess(userEntity);
    }

    @Override
    public UserEntity queryCurrentUser(String token) {
        return (UserEntity) redisUtil.get(token);
    }

    @Override
    public Response queryUserList() {
        UserListResult userListResult = new UserListResult();
        userListResult.setUserList(userEntityMapperExt.queryUserList(Const.ACTIVE_STATUS));
        userListResult.setUnPassList(userEntityMapperExt.queryUserList(Const.REVIEW_STATUS));
        return Response.createBySuccess(userListResult);
    }

    @Override
    public Response delete(int id) {
        ExceptionThrow.cast(ResponseCode.DATABASE_ERROR, 1 != userEntityMapperExt.deleteByPrimaryKey(id));
        return Response.createBySuccess();
    }
}
