package com.learn.im.tcp.feign;

import com.learn.im.common.ResponseVO;
import com.learn.im.common.model.message.CheckSendMessageReq;
import feign.Headers;
import feign.RequestLine;

/**
 * @author lee
 * @description
 */
public interface FeignMessageService {

    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @RequestLine("POST /message/checkSend")
    ResponseVO checkSendMessage(CheckSendMessageReq checkSendMessageReq);

}
