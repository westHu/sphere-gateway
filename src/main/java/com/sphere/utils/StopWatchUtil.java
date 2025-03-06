package com.sphere.utils;

import com.sphere.exception.GatewayException;
import com.sphere.exception.GatewayExceptionCode;
import org.springframework.util.StopWatch;

import java.text.NumberFormat;

public class StopWatchUtil {

    private StopWatchUtil() {
        throw new GatewayException("Utility classes should not have public constructors");
    }

    public static String prettyPrint(StopWatch stopWatch) {
        StringBuilder sb = new StringBuilder(stopWatch.shortSummary());
        sb.append('\n');

        int taskCount = stopWatch.getTaskCount();
        if (taskCount == 0) {
            sb.append("No task info kept");
        } else {
            sb.append("---------------------------------------------\n");
            sb.append("ms      %     Task name\n");
            sb.append("---------------------------------------------\n");
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMinimumIntegerDigits(6);
            nf.setGroupingUsed(false);
            NumberFormat pf = NumberFormat.getPercentInstance();
            pf.setMinimumIntegerDigits(3);
            pf.setGroupingUsed(false);
            StopWatch.TaskInfo[] var4 = stopWatch.getTaskInfo();

            for (StopWatch.TaskInfo task : var4) {
                sb.append(nf.format(task.getTimeMillis())).append("  ");
                sb.append(pf.format((double) task.getTimeMillis() / (double) stopWatch.getTotalTimeMillis()))
                        .append("  ");
                sb.append(task.getTaskName()).append("\n");
            }
        }
        return sb.toString();
    }

}
