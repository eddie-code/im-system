package com.learn.im.tcp.reciver;

import com.alibaba.fastjson.JSONObject;
import com.learn.im.codec.proto.MessagePack;
import com.learn.im.common.ClientType;
import com.learn.im.common.constant.Constants;
import com.learn.im.common.enums.DeviceMultiLoginEnum;
import com.learn.im.common.enums.command.SystemCommand;
import com.learn.im.tcp.redis.RedisManager;
import com.learn.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;
import com.learn.im.common.model.UserClientDto;

import java.util.List;

/**
 * @description: 多端同步：
 * 1单端登录：一端在线：踢掉除了本clinetType + imel 的设备
 * 2双端登录：允许pc/mobile 其中一端登录 + web端 踢掉除了本clinetType + imel 以外的web端设备
 * 3 三端登录：允许手机+pc+web，踢掉同端的其他imei 除了web
 * 4 不做任何处理
 * @author: lee
 * @version: 1.0
 */
@Slf4j
public class UserLoginMessageListener {

    private Integer loginModel;

    public UserLoginMessageListener(Integer loginModel) {
        this.loginModel = loginModel;
    }

    public void listenerUserLogin() {
        RTopic topic = RedisManager.getRedissonClient().getTopic(Constants.RedisConstants.UserLoginChannel);
        topic.addListener(String.class, new MessageListener<String>() {

            @Override
            public void onMessage(CharSequence charSequence, String msg) {

                log.info("收到用户上线通知：" + msg);

                // 传入的 message
                UserClientDto dto = JSONObject.parseObject(msg, UserClientDto.class);

                // 从本地缓存获取 User Client 的信息
                List<NioSocketChannel> nioSocketChannels = SessionSocketHolder.get(dto.getAppId(), dto.getUserId());

                for (NioSocketChannel nioSocketChannel : nioSocketChannels) {
                    // 单端登录 仅允许 Windows、Web、Android 或 iOS 单端登录
                    if (loginModel == DeviceMultiLoginEnum.ONE.getLoginMode()) {
                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
                        // 踢掉客户端
                        if (!(clientType + ":" + imei).equals(dto.getClientType() + ":" + dto.getImei())) {
                            MessagePack<Object> pack = new MessagePack<>();
                            // 发送端
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            // 下线通知 用于多端互斥 9002
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            // 写入并刷新
                            nioSocketChannel.writeAndFlush(pack);
                        }
                        // 双端登录 允许 Windows、Mac、Android 或 iOS 单端登录，同时允许与 Web 端同时在线
                    } else if (loginModel == DeviceMultiLoginEnum.TWO.getLoginMode()) {
                        // 传入的msg, 判断是否支持web端, 如果是就不再处理
                        if (dto.getClientType() == ClientType.WEB.getCode()) {
                            continue;
                        }
                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                        // 当前端也是web端就不再执行
                        if (clientType == ClientType.WEB.getCode()) {
                            continue;
                        }
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
                        // 踢掉客户端
                        if (!(clientType + ":" + imei).equals(dto.getClientType() + ":" + dto.getImei())) {
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            // 下线通知 用于多端互斥 9002
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            // 写入并刷新
                            nioSocketChannel.writeAndFlush(pack);
                        }
                        // 三端登录 允许 Android 或 iOS 单端登录(互斥)，Windows 或者 Mac 单聊登录(互斥)，同时允许 Web 端同时在线
                    } else if (loginModel == DeviceMultiLoginEnum.THREE.getLoginMode()) {
                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
                        if (dto.getClientType() == ClientType.WEB.getCode()) {
                            continue;
                        }

                        boolean isSameClient = false;
                        // （手机端）判断旧的端和传入的两个新的端, 是否相等, 相等才做处理
                        if ((clientType == ClientType.IOS.getCode() || clientType == ClientType.ANDROID.getCode()) && (dto.getClientType() == ClientType.IOS.getCode() || dto.getClientType() == ClientType.ANDROID.getCode())) {
                            isSameClient = true;
                        }

                        // （电脑端）判断旧的端和传入的两个新的端, 是否相等, 相等才做处理
                        if ((clientType == ClientType.MAC.getCode() || clientType == ClientType.WINDOWS.getCode()) && (dto.getClientType() == ClientType.MAC.getCode() || dto.getClientType() == ClientType.WINDOWS.getCode())) {
                            isSameClient = true;
                        }

                        // 踢掉客户端
                        if (isSameClient && !(clientType + ":" + imei).equals(dto.getClientType() + ":" + dto.getImei())) {
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            // 下线通知 用于多端互斥 9002
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            // 写入并刷新
                            nioSocketChannel.writeAndFlush(pack);
                        }
                    }
                }
            }
        });
    }

}
