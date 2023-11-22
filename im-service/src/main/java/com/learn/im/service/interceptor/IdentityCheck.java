package com.learn.im.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.learn.im.common.BaseErrorCode;
import com.learn.im.common.config.AppConfig;
import com.learn.im.common.constant.Constants;
import com.learn.im.common.enums.GateWayErrorCode;
import com.learn.im.common.exception.ApplicationExceptionEnum;
import com.learn.im.common.utils.SigAPI;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.learn.im.service.user.service.ImUserService;

import java.util.concurrent.TimeUnit;

/**
 * @author lee
 * @description 身份检查
 */
@Slf4j
@Component
public class IdentityCheck {

    @Autowired
    ImUserService imUserService;

    //10000 123456 10001 123456789
    @Autowired
    AppConfig appConfig;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public ApplicationExceptionEnum checkUserSig(String identifier, String appId, String userSig) {

        // 去缓存获取是否校验过的记录，如果有就往下判断过期时间
        String cacheUserSig = stringRedisTemplate.opsForValue().get(appId + ":" + Constants.RedisConstants.userSign + ":" + identifier + userSig);

        if (!StringUtils.isBlank(cacheUserSig) && Long.valueOf(cacheUserSig) > System.currentTimeMillis() / 1000) {
            // 过期时间大于系统时间就通过, 证明还没有过期
            return BaseErrorCode.SUCCESS;
        }

        //获取秘钥
        String privateKey = appConfig.getPrivateKey();

        //根据appid + 秘钥创建sigApi
//        SigAPI sigAPI = new SigAPI(Long.valueOf(appId), privateKey);

        //调用sigApi对userSig解密
        JSONObject jsonObject = SigAPI.decodeUserSig(userSig);

        //取出解密后的appid 和 操作人 和 过期时间做匹配，不通过则提示错误
        Long expireTime = 0L;
        Long expireSec = 0L;
        String decoerAppId = "";
        String decoderidentifier = "";

        try {
            decoerAppId = jsonObject.getString("TLS.appId");
            decoderidentifier = jsonObject.getString("TLS.identifier");
            String expireStr = jsonObject.get("TLS.expire").toString();
            String expireTimeStr = jsonObject.get("TLS.expireTime").toString();
            expireSec = Long.valueOf(expireStr);
            expireTime = Long.parseLong(expireTimeStr) + expireSec;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("checkUserSig-error:{}", e.getMessage());
        }

        // 用户签名与操作人不匹配
        if (!decoderidentifier.equals(identifier)) {
            return GateWayErrorCode.USERSIGN_OPERATE_NOT_MATE;
        }

        // 用户签名不正确
        if (!decoerAppId.equals(appId)) {
            return GateWayErrorCode.USERSIGN_IS_ERROR;
        }

        // 用户签名已过期
        if (expireSec == 0L) {
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }

        // 用户签名已过期
        if (expireTime < System.currentTimeMillis() / 1000) {
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }

        // appid + "xxx" + userId + sign
        String key = appId + ":" + Constants.RedisConstants.userSign + ":" + identifier + userSig;

        Long etime = expireTime - System.currentTimeMillis() / 1000;
        stringRedisTemplate.opsForValue().set(
                key, expireTime.toString(), etime, TimeUnit.SECONDS
        );

        return BaseErrorCode.SUCCESS;

    }


}
