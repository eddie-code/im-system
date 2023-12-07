package com.learn.im.service.message.service;

import com.learn.im.common.model.MessageContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author lee
 * @description
 */
@Slf4j
@Service
public class P2PMessageService {

    public void process(MessageContent messageContent) {

        //前置校验
        //这个用户是否被禁言 是否被禁用
        //发送方和接收方是否是好友

    }

}
