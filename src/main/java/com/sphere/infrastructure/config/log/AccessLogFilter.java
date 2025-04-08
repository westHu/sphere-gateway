package com.sphere.infrastructure.config.log;

import cn.hutool.json.JSONUtil;
import com.sphere.common.constants.GatewayConstant;
import com.sphere.common.exception.GatewayException;
import com.sphere.infrastructure.route.HostConfiguration;
import com.sphere.common.utils.RequestUtil;
import com.sphere.common.utils.StopWatchUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Optional;

/**
 * @author west
 * 自定义Filter里面应该是需要第一位拦截到的，先入日志  -99
 * 自定义Fileter order保持在100以内
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccessLogFilter implements GlobalFilter, Ordered {

    private final LogProperties logProperties;

    @Resource
    ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    HostConfiguration hostConfiguration;

    @Override
    public int getOrder() {
        return -99;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 请求开关
        log.info("accessLogFilter isOpen: {}", hostConfiguration.getOpen());
        if (!Boolean.parseBoolean(hostConfiguration.getOpen())) {
            throw new GatewayException("system is under maintenance and will be restored soon.");
        }

        // 构建日志
        GatewayLog gatewayLog = parseGatewayLog(exchange);
        log.info(">>> gateway request gatewayLog:{}", JSONUtil.toJsonStr(gatewayLog));

        // 计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start(gatewayLog.getRequestPath());

        // 请求
        return chain.filter(exchange.mutate().build()).then(Mono.fromRunnable(() -> {
            stopWatch.stop();
            long executeTime = stopWatch.getTotalTimeMillis();
            gatewayLog.setExecuteTime(executeTime);

            // HttpCode
            int code = Optional.of(exchange).map(ServerWebExchange::getResponse)
                    .map(ServerHttpResponse::getStatusCode)
                    .map(HttpStatusCode::value).orElse(-1);
            gatewayLog.setCode(code);

            // 商户ID
            Object merchantIdObj = exchange.getAttributes().get(GatewayConstant.MERCHANT_ID);
            String merchantId = Optional.ofNullable(merchantIdObj).map(Object::toString).orElse(null);
            gatewayLog.setMerchantId(merchantId);

            // 参数
            Object requestParamObj = exchange.getAttributes().get(GatewayConstant.REQUEST_PARAM);
            String requestParam = Optional.ofNullable(requestParamObj).map(Object::toString).orElse(null);
            gatewayLog.setRequestParam(requestParam);

            // 日志打印
            log.info(">>> payspherepay gatewayLog={}. \nexecute={}", JSONUtil.toJsonStr(gatewayLog),
                    StopWatchUtil.prettyPrint(stopWatch));

            // 异步报告
            threadPoolTaskExecutor.execute(() -> report(gatewayLog));
        }));
    }


    /**
     * 报告 含tg
     */
    private void report(GatewayLog gatewayLog) {
        boolean reported = exceptionReport(gatewayLog);
        if (!reported) {
            slowApiReport(gatewayLog);
        }
    }

    /**
     * 异常报警
     */
    private boolean exceptionReport(GatewayLog gatewayLog) {
        if (gatewayLog.getCode() == HttpStatus.OK.value()) {
            return false;
        }

        LogProperties.ApiAlarmConfiguration apiAlarmConfiguration = logProperties.getFail();
        if (!apiAlarmConfiguration.isAlarm()) {
            return false;
        }

        if (!CollectionUtils.isEmpty(apiAlarmConfiguration.getExclusion())
                && apiAlarmConfiguration.getExclusion().contains(gatewayLog.getCode())) {
            return false;
        }

        String alarmContent = String.format("payspherepay Gateway api exception. MerchantID: [%s]. Request Ip : [%s] " +
                        "Address : " +
                        "[%s] Return code : [%d] Cost time : [%d] ms",
                gatewayLog.getMerchantId(), gatewayLog.getIp(), gatewayLog.getRequestPath(), gatewayLog.getCode(),
                gatewayLog.getExecuteTime());
        log.info(alarmContent);
        return true;
    }


    private void slowApiReport(GatewayLog gatewayLog) {
        LogProperties.SlowApiAlarmConfiguration slowApiAlarmConfiguration = logProperties.getSlow();
        long threshold = slowApiAlarmConfiguration.getThreshold();
        if (gatewayLog.getExecuteTime() < threshold) {
            return;
        }

        if (!slowApiAlarmConfiguration.isAlarm()) {
            log.debug("Slow api alarm disabled.");
            return;
        }

        String slowContent = String.format("payspherepay Api cost time too long. MerchantID: [%s]. Request Ip : [%s]. " +
                        "Address : " +
                        "[%s]. Cost time: [%d] ms",
                gatewayLog.getMerchantId(), gatewayLog.getIp(), gatewayLog.getRequestPath(),
                gatewayLog.getExecuteTime());
        log.info(slowContent);
    }

    /**
     * 获得当前请求分发的路由
     */
    private Route getGatewayRoute(ServerWebExchange exchange) {
        return exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
    }


    /**
     * 构建 GatewayLog
     */
    private GatewayLog parseGatewayLog(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getPath().pathWithinApplication().value();
        Route route = getGatewayRoute(exchange);
        String targetServer = Optional.ofNullable(route).map(Route::getId).orElse(null);
        String ip = RequestUtil.getIpAddress(request);
        String host = RequestUtil.getHost(request);
        String methodValue = request.getMethod().name();

        GatewayLog gatewayLog = new GatewayLog();
        gatewayLog.setTargetServer(targetServer);
        gatewayLog.setHost(host);
        gatewayLog.setRequestPath(requestPath);
        gatewayLog.setMethod(methodValue);
        gatewayLog.setSchema(request.getURI().getScheme());
        gatewayLog.setIp(ip);
        gatewayLog.setRequestTime(new Date());
        return gatewayLog;
    }

}

