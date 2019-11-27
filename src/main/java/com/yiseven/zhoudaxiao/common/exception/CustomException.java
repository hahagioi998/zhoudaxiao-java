package com.yiseven.zhoudaxiao.common.exception;


import com.yiseven.zhoudaxiao.common.response.ResponseCode;

/**
 * @author hdeng
 */
public class CustomException extends RuntimeException {

    private ResponseCode responseCode;

    public CustomException(ResponseCode responseCode) {
        super("错误代码：" + responseCode.getCode() + "错误信息：" + responseCode.getDesc());
        this.responseCode = responseCode;
    }

    public ResponseCode getResponseCode() {
        return responseCode;
    }
}
