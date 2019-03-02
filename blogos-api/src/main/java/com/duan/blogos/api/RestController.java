package com.duan.blogos.api;

import com.duan.blogos.service.common.exception.BlogOSException;
import com.duan.blogos.service.common.restful.ResultModel;
import com.duan.blogos.util.CodeMessage;
import com.duan.blogos.util.ExceptionUtil;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created on 2017/12/26.
 *
 * @author DuanJiaNing
 */
public class RestController {

    /**
     * 处理结果为空的情况
     */
    protected ResultModel handlerEmptyResult() {
        return new ResultModel<>("empty result", CodeMessage.COMMON_EMPTY_RESULT.getCode());
    }

    /**
     * 处理操作失败的情况
     */
    protected ResultModel handlerOperateFail() {
        return new ResultModel<>("operate fail", CodeMessage.COMMON_OPERATE_FAIL.getCode());
    }

    /**
     * 统一处理异常，这些异常需要通知API调用者
     */
    @ExceptionHandler(BlogOSException.class)
    @ResponseBody
    // 注解无法继承，所以子类不允许覆盖这些方法
    protected final ResultModel handleException(BlogOSException e) {
        e.printStackTrace();
        return ResultModel.fail(e.getMessage(), e.getCode());
    }

    /**
     * 未进行转化的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    protected final ResultModel handleException(Throwable e) {
        e.printStackTrace();
        return ResultModel.fail(e.getMessage(), CodeMessage.COMMON_UNKNOWN_ERROR.getCode());
    }

    /**
     * 统一处理“请求参数缺失”错误
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    protected final ResultModel handlerException(MissingServletRequestParameterException e) {
        e.printStackTrace();
        return ResultModel.fail(e.getMessage(), CodeMessage.COMMON_MISSING_REQUEST_PARAMETER.getCode());
    }

    /**
     * 统一处理“请求参数与目标参数类型不匹配”错误
     */
    @ExceptionHandler(TypeMismatchException.class)
    @ResponseBody
    protected final ResultModel handlerException(TypeMismatchException e) {
        e.printStackTrace();
        return ResultModel.fail(e.getMessage(), CodeMessage.COMMON_PARAMETER_TYPE_MISMATCH.getCode());
    }

    /**
     * 未指明操作
     */
    @RequestMapping
    protected void defaultOperation() {
        throw ExceptionUtil.get(CodeMessage.COMMON_UNSPECIFIED_OPERATION);
    }

}