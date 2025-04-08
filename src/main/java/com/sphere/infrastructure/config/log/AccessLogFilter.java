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
 * 网关访问日志过滤器
 * 记录所有经过网关的请求和响应信息，包括：
 * 1. 请求基本信息（路径、方法、IP等）
 * 2. 请求头信息
 * 3. 请求参数
 * 4. 响应状态和耗时
 * 5. 异常信息（如果有）
 * 6. 慢请求监控
 * 7. 异常请求告警
 *
 * @author sphere
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccessLogFilter implements GlobalFilter, Ordered {

    /**
     * 日志配置属性
     */
    private final LogProperties logProperties;

    /**
     * 线程池任务执行器
     */
    @Resource
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 主机配置
     */
    @Resource
    HostConfiguration hostConfiguration;

    @Override
    public int getOrder() {
        return -99; // 确保日志过滤器最先执行
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 检查系统是否开放
        log.debug("系统状态检查 - 是否开放: {}", hostConfiguration.getOpen());
        if (!Boolean.parseBoolean(hostConfiguration.getOpen())) {
            log.warn("系统维护中 - 拒绝访问");
            throw new GatewayException("system is under maintenance and will be restored soon.");
        }

        // 构建并记录请求日志
        GatewayLog gatewayLog = parseGatewayLog(exchange);
        log.info("网关请求日志 => {}", JSONUtil.toJsonStr(gatewayLog));

        // 开始计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start(gatewayLog.getRequestPath());

        // 执行过滤器链
        return chain.filter(exchange)
                .doFinally(signalType -> {
                    // 停止计时
                    stopWatch.stop();
                    
                    // 记录响应日志
                    gatewayLog.setExecuteTime(stopWatch.getTotalTimeMillis());
                    
                    // 获取响应状态
                    HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
                    assert statusCode != null;
                    gatewayLog.setCode(statusCode.value());
                    
                    // 获取商户ID
                    Object merchantIdObj = exchange.getAttributes().get(GatewayConstant.MERCHANT_ID);
                    String merchantId = Optional.ofNullable(merchantIdObj).map(Object::toString).orElse(null);
                    gatewayLog.setMerchantId(merchantId);
                    
                    // 获取请求参数
                    Object requestParamObj = exchange.getAttributes().get(GatewayConstant.REQUEST_PARAM);
                    String requestParam = Optional.ofNullable(requestParamObj).map(Object::toString).orElse(null);
                    gatewayLog.setRequestParam(requestParam);
                    
                    // 异步处理日志记录和监控
                    threadPoolTaskExecutor.execute(() -> {
                        // 记录完整日志
                        log.info("网关响应日志 => {}", JSONUtil.toJsonStr(gatewayLog));
                        log.info("请求执行时间 => {}", StopWatchUtil.prettyPrint(stopWatch));
                        
                        // 检查慢请求
                        checkSlowRequest(gatewayLog);
                        
                        // 检查异常请求
                        checkErrorRequest(gatewayLog);
                    });
                });
    }

    /**
     * 检查慢请求
     * 当请求执行时间超过阈值时，记录警告日志
     *
     * @param gatewayLog 网关日志对象
     */
    private void checkSlowRequest(GatewayLog gatewayLog) {
        LogProperties.SlowApiAlarmConfiguration slowConfig = logProperties.getSlow();
        if (slowConfig.isAlarm() && gatewayLog.getExecuteTime() > slowConfig.getThreshold()) {
            log.warn("""
                    慢请求告警 =>
                    商户ID: {}
                    请求路径: {}
                    执行时间: {}ms
                    阈值: {}ms
                    """, 
                    gatewayLog.getMerchantId(),
                    gatewayLog.getRequestPath(),
                    gatewayLog.getExecuteTime(),
                    slowConfig.getThreshold());
        }
    }

    /**
     * 检查异常请求
     * 当响应状态码不是成功状态时，记录错误日志
     *
     * @param gatewayLog 网关日志对象
     */
    private void checkErrorRequest(GatewayLog gatewayLog) {
        if (gatewayLog.getCode() != HttpStatus.OK.value()) {
            LogProperties.ApiAlarmConfiguration errorConfig = logProperties.getFail();
            if (errorConfig.isAlarm() && 
                (CollectionUtils.isEmpty(errorConfig.getExclusion()) || 
                 !errorConfig.getExclusion().contains(gatewayLog.getCode()))) {
                log.error("""
                        异常请求告警 =>
                        商户ID: {}
                        请求路径: {}
                        状态码: {}
                        执行时间: {}ms
                        """, 
                        gatewayLog.getMerchantId(),
                        gatewayLog.getRequestPath(),
                        gatewayLog.getCode(),
                        gatewayLog.getExecuteTime());
            }
        }
    }

    /**
     * 解析网关日志
     * 从请求中提取关键信息构建日志对象
     *
     * @param exchange 请求交换对象
     * @return 网关日志对象
     */
    private GatewayLog parseGatewayLog(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        
        GatewayLog gatewayLog = new GatewayLog();
        
        // 设置基本信息
        gatewayLog.setRequestPath(request.getPath().pathWithinApplication().value());
        gatewayLog.setMethod(request.getMethod().name());
        gatewayLog.setSchema(request.getURI().getScheme());
        gatewayLog.setIp(RequestUtil.getIpAddress(request));
        gatewayLog.setHost(RequestUtil.getHost(request));
        gatewayLog.setRequestTime(new Date());
        
        // 设置目标服务器
        if (route != null) {
            gatewayLog.setTargetServer(route.getId());
        }
        
        // 设置请求头
        if (!CollectionUtils.isEmpty(request.getHeaders())) {
            gatewayLog.setRequestHeader(JSONUtil.toJsonStr(request.getHeaders()));
        }
        
        return gatewayLog;
    }
}

