/**
 * 
 */
package org.ibase4j.core.util;

import java.util.Map;

import org.ibase4j.core.support.pay.WxPay;
import org.ibase4j.core.support.pay.WxPayment;

import com.baomidou.mybatisplus.toolkit.IdWorker;

/**
 * 
 * @author ShenHuaJie
 * @version 2017年10月21日 下午10:52:22
 */
public class WeChatUtil {
    /**
     * 下单并获取支付签名
     * @param out_trade_no 商户订单号
     * @param detail 交易详情
     * @param amount 交易金额
     * @param ip 客户端IP
     * @param callBack 回调地址
     * @return 支付参数
     */
    public Map<String, String> getSign(String out_trade_no, String detail, String amount, String ip, String callBack) {
        Map<String, String> params = WxPayment.buildUnifiedOrderParasMap(PropertiesUtil.getString("wx.appId"), null,
            PropertiesUtil.getString("wx.mch_id"), null, null, detail, null, null, out_trade_no, amount, ip, callBack,
            "APP", PropertiesUtil.getString("wx.partnerKey"), null);
        String result = WxPay.pushOrder(params);
        Map<String, String> resultMap = WxPayment.xmlToMap(result);
        String return_code = resultMap.get("return_code");
        if (WxPayment.codeIsOK(return_code)) {
            String result_code = resultMap.get("result_code");
            if (WxPayment.codeIsOK(result_code)) {
                String prepay_id = resultMap.get("prepay_id");
                String sign = resultMap.get("sign");
                String mySign = WxPayment.createSign(resultMap, PropertiesUtil.getString("wx.partnerKey"));
                if (mySign.equals(sign)) {
                    resultMap.clear();
                    resultMap.put("appid", PropertiesUtil.getString("wx.appId"));
                    resultMap.put("partnerid", PropertiesUtil.getString("wx.mch_id"));
                    resultMap.put("prepayid", prepay_id);
                    resultMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
                    resultMap.put("noncestr", IdWorker.get32UUID());
                    sign = WxPayment.buildOrderPaySign(resultMap.get("appid"), resultMap.get("partnerid"), prepay_id,
                        "Sign=WXPay", resultMap.get("timestamp"), resultMap.get("noncestr"),
                        PropertiesUtil.getString("wx.partnerKey"));
                    resultMap.put("sign", sign);
                    return resultMap;
                } else {
                    throw new RuntimeException("微信返回数据异常.");
                }
            } else {
                throw new RuntimeException(resultMap.get("err_code_des"));
            }
        } else {
            throw new RuntimeException(resultMap.get("return_msg"));
        }
    }
}
