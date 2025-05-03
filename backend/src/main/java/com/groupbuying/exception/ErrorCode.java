package com.groupbuying.exception;

public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    BUSINESS_ERROR(500, "业务错误"),
    SYSTEM_ERROR(500, "系统错误"),
    INVALID_PARAMETER(400, "参数错误"),
    USERNAME_ALREADY_EXISTS(400, "用户名已存在"),
    PHONE_ALREADY_EXISTS(400, "手机号已存在"),
    USER_NOT_FOUND(4041, "用户不存在"),
    PRODUCT_NOT_FOUND(404, "商品不存在"),
    INSUFFICIENT_STOCK(400, "库存不足"),
    INSUFFICIENT_BALANCE(400, "余额不足"),
    INVALID_PAYMENT_PASSWORD(400, "支付密码错误"),
    FILE_EMPTY(400, "文件为空"),
    FILE_UPLOAD_FAILED(500, "文件上传失败"),
    POST_NOT_FOUND(4042, "帖子不存在"),
    COMMENT_NOT_FOUND(404, "评论不存在"),
    CART_ITEM_NOT_FOUND(404, "购物车项不存在"),
    GROUP_NOT_FOUND(404, "拼团不存在"),
    INVALID_CREDENTIALS(401, "无效的凭证"),
    ORDER_NOT_FOUND(404, "订单不存在"),
    INVALID_STATUS(400, "无效的状态"),
    GROUP_FULL(400, "拼团已满"),
    GROUP_EXPIRED(400, "拼团已过期"),
    GROUP_FAILED(400, "拼团失败"),
    SERVER_ERROR(500, "服务器内部错误"),
    GROUP_BUY_NOT_FOUND(4043, "拼团不存在"),
    GROUP_BUY_NOT_AVAILABLE(4044, "拼团已结束或已满"),
    GROUP_BUY_ALREADY_JOINED(4045, "您已经参加过该拼团"),
    GROUP_BUY_EXPIRED(4046, "拼团已过期");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
} 