package com.sphere.common.utils;

import com.sphere.common.exception.GatewayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.text.NumberFormat;

/**
 * 性能监控工具类
 * 用于格式化输出StopWatch的性能监控信息
 * 提供美观的表格形式展示任务执行时间和占比
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
public class StopWatchUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private StopWatchUtil() {
        throw new GatewayException("Utility classes should not have public constructors");
    }

    /**
     * 格式化输出StopWatch的性能监控信息
     * 以表格形式展示每个任务的执行时间和占比
     *
     * @param stopWatch Spring的StopWatch对象
     * @return 格式化后的性能监控信息字符串
     */
    public static String prettyPrint(StopWatch stopWatch) {
        if (stopWatch == null) {
            log.warn("StopWatch对象为空");
            return "No StopWatch available";
        }

        StringBuilder sb = new StringBuilder(stopWatch.shortSummary());
        sb.append('\n');

        int taskCount = stopWatch.getTaskCount();
        if (taskCount == 0) {
            sb.append("No task info kept");
        } else {
            // 添加表头
            sb.append("---------------------------------------------\n");
            sb.append("ms      %     Task name\n");
            sb.append("---------------------------------------------\n");

            // 配置数字格式化
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMinimumIntegerDigits(6);
            nf.setGroupingUsed(false);
            NumberFormat pf = NumberFormat.getPercentInstance();
            pf.setMinimumIntegerDigits(3);
            pf.setGroupingUsed(false);

            // 遍历所有任务并格式化输出
            for (StopWatch.TaskInfo task : stopWatch.getTaskInfo()) {
                sb.append(nf.format(task.getTimeMillis())).append("  ");
                sb.append(pf.format((double) task.getTimeMillis() / (double) stopWatch.getTotalTimeMillis()))
                        .append("  ");
                sb.append(task.getTaskName()).append("\n");
            }
        }
        return sb.toString();
    }
}
