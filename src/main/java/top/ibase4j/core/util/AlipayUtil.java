/**
 * 
 */
package top.ibase4j.core.util;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipayUtils;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;

import top.ibase4j.core.support.pay.AliPayConfig;
import top.ibase4j.core.support.pay.vo.RefundResult;

/**
 * 支付宝
 * @author ShenHuaJie
 * @version 2017年10月21日 下午11:59:47
 */
public class AlipayUtil {
    private static final Logger logger = LogManager.getLogger(AlipayUtil.class);

    /**
     * 下单并获取支付签名
     * @param out_trade_no 商户订单号
     * @param subject 交易主题
     * @param body 交易详情
     * @param amount 交易金额
     * @param ip 客户端IP
     * @param timeout 订单失效时间
     * @param callBack 回调地址
     * @return 支付参数
     */
    public static String getSign(String out_trade_no, String subject, String body, BigDecimal amount, String ip,
        String timeout, String callBack) {
        // 实例化客户端
        AlipayClient alipayClient = AliPayConfig.build().getAlipayClient();
        // 实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        // SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setSubject(subject);
        model.setBody(body);
        model.setOutTradeNo(out_trade_no);
        model.setTimeoutExpress(timeout);
        model.setTotalAmount(amount.toString());
        model.setProductCode("QUICK_MSECURITY_PAY");
        request.setBizModel(model);
        request.setNotifyUrl(callBack);
        try {
            // 这里和普通的接口调用不同，使用的是sdkExecute
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
            logger.info(response.getBody());// 就是orderString 可以直接给客户端请求，无需再做处理。
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getSubMsg());
            }
            return response.getBody();
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 退款
     * @param outTradeNo 商户订单号
     * @param tradeNo 支付宝订单号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 支付参数
     */
    public static RefundResult refund(String outTradeNo, String tradeNo, BigDecimal refundAmount, String refundReason) {
        // 实例化客户端
        AlipayClient alipayClient = AliPayConfig.build().getAlipayClient();
        // 实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        // SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(outTradeNo);
        model.setTradeNo(tradeNo);
        model.setRefundAmount(refundAmount.toString());
        model.setRefundReason(refundReason);
        request.setBizModel(model);
        try {
            // 这里和普通的接口调用不同，使用的是sdkExecute
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            logger.info(response.getBody());
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getSubMsg());
            }
            Map<?, ?> result = AlipayUtils.parseJson(response.getBody());
            return new RefundResult((String)result.get("trade_no"), outTradeNo, refundAmount.toString(),
                DateUtil.stringToDate(result.get("gmt_refund_pay").toString()),
                "Y".equals(result.get("fund_change")) ? "1" : "2");
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }
    }
}
