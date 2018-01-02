package com.zenzet;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by C on 16/3/8.
 * HTTPBODY+ERRORCODE
 * ERRORCODE: 2位服务编号 + 2位子模块编码 + 3位自增具体错误代码
 * LSS-GATEWAY服务号为15,子模块编号为00
 */
public class ResponseBody {
    public static final String NO_ERROR                                   = "0";      //没有错误
    public static final String COMPANY_NOT_EXISTS                         = "1501001";//company不存在
    public static final String COMPANY_ID_NOT_SPECIFIED                   = "1501002";//companyID未指定
    public static final String DATA_TYPE_NOT_SPECIFIED                    = "1501003";//dataType未指定
    public static final String API_NOT_SPECIFIED                          = "1501004";//api未指定
    public static final String NO_STRATEGY_MATCHED                        = "1501005";//未匹配到策略
    public static final String INVALID_JSON_DATA                          = "1501006";//json数据非法
    public static final String INVALID_XML_DATA                           = "1501007";//xml数据非法
    public static final String COMPANY_ALREADY_IMPORTED                   = "1501008";//企业重复导入
    public static final String JSON_DATA_IS_NULL                          = "1501009";//json数据为空
    public static final String XML_DATA_IS_NULL                           = "1501010";//xml数据为空
    public static final String CRC_VALIDATION_FAIL_OR_BAD_BASE64          = "1501011";//密文校验失败
    public static final String CRC_VALIDATION_FAIL_OR_NO_STRATEGY_MATCHED = "1501012";//密文校验失败或未匹配到策略
    public static final String API_IS_WRONG                               = "1501013";//api有误
    public static final String APP_DELETED                                = "1501014";//应用已删除
    public static final String API_SURPASS_LIMIT                          = "1501015";//今日免费api调用次数已用完
    public static final String INTERNAL_SERVER_ERROR                      = "1501016";//内部服务器错误
    public static final String ILLEGAL_REQUEST_URL                        = "1501017";//非法请求地址
    public static final String INVALID_ACCESS_TOKEN                       = "1501018";//无效access_token
    public static final String SIGN_VERIFY_ERROR                          = "1501019";//签名验证错误
    public static final String ILLEGAL_REQUEST_BODY                       = "1501020";//非法请求body
    public static final String ILLEGAL_URI_SYNTAX                         = "1501021";//请求地址格式错误
    public static final String HTTP_PARAMETER_POLLUTION                   = "1501022";//HTTP参数污染
    public static final String INVALID_APP_STATUS                         = "1501023";//无效应用状态
    private String msg;//body信息
    private String errorCode;//errorcode

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ResponseBody(String state, String body) {
        this.setErrorCode(state);
        this.setMsg(body);
    }

    @Override
    public String toString() {
        String errorInfo = null;
        if (this.getErrorCode() == COMPANY_NOT_EXISTS) {
            errorInfo = "company不存在";
        } else if (this.getErrorCode() == COMPANY_ID_NOT_SPECIFIED) {
            errorInfo = "companyID未指定";
        } else if (this.getErrorCode() == DATA_TYPE_NOT_SPECIFIED) {
            errorInfo = "dataType未指定";
        } else if (this.getErrorCode() == API_NOT_SPECIFIED) {
            errorInfo = "api未指定";
        } else if (this.getErrorCode() == NO_STRATEGY_MATCHED) {
            errorInfo = "未匹配到策略";
        } else if (this.getErrorCode() == INVALID_JSON_DATA) {
            errorInfo = "json数据非法";
        } else if (this.getErrorCode() == INVALID_XML_DATA) {
            errorInfo = "xml数据非法";
        } else if (this.getErrorCode() == COMPANY_ALREADY_IMPORTED) {
            errorInfo = "企业重复导入";
        } else if (this.getErrorCode() == JSON_DATA_IS_NULL) {
            errorInfo = "JSON数据为空";
        } else if (this.getErrorCode() == XML_DATA_IS_NULL) {
            errorInfo = "XML数据为空";
        } else if (this.getErrorCode() == CRC_VALIDATION_FAIL_OR_BAD_BASE64) {
            errorInfo = "密文校验失败";
        } else if (this.getErrorCode() == CRC_VALIDATION_FAIL_OR_NO_STRATEGY_MATCHED) {
            errorInfo = "密文校验失败或未匹配到策略";
        } else if (this.getErrorCode() == API_IS_WRONG) {
            errorInfo = "api有误";
        } else if (this.getErrorCode() == APP_DELETED) {
            errorInfo = "应用已删除";
        } else if (this.getErrorCode() == API_SURPASS_LIMIT) {
            errorInfo = "今日免费api调用次数已用完";
        }else if (this.getErrorCode() == INTERNAL_SERVER_ERROR) {
            errorInfo = "内部服务器错误";
        } else if (this.getErrorCode() == ILLEGAL_REQUEST_URL) {
            errorInfo = "非法请求地址";
        } else if (this.getErrorCode() == INVALID_ACCESS_TOKEN) {
            errorInfo = "无效access_token";
        } else if (this.getErrorCode() == SIGN_VERIFY_ERROR) {
            errorInfo = "签名验证错误";
        } else if (this.getErrorCode() == ILLEGAL_REQUEST_BODY) {
            errorInfo = "非法请求body";
        } else if (this.getErrorCode() == ILLEGAL_URI_SYNTAX) {
            errorInfo = "请求地址格式错误";
        }else if (this.getErrorCode() == HTTP_PARAMETER_POLLUTION) {
            errorInfo = "HTTP参数污染";
        }else if (this.getErrorCode() == INVALID_APP_STATUS) {
            errorInfo = "无效应用状态";
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorcode", this.getErrorCode());

        try{
            Object jo = JSONObject.parse(this.getMsg());
            jsonObject.put("data", jo);
        }catch (com.alibaba.fastjson.JSONException e){
            jsonObject.put("data", this.getMsg());

        }

        jsonObject.put("errorinfo", errorInfo);
        return jsonObject.toJSONString();
    }
}
