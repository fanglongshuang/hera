package com.dfire.core.netty.worker;

import com.dfire.common.service.*;
import com.dfire.common.util.NamedThreadFactory;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.job.Job;
import com.dfire.core.netty.HeraChannel;
import com.dfire.core.tool.RunShell;
import com.dfire.core.util.NetUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 11:30 2018/1/10
 * @desc
 */
@Data
@NoArgsConstructor
public class WorkContext {

    public static String host;
    public static Integer cpuCoreNum;
    public String serverHost;
    private HeraChannel serverChannel;
    private Map<String, Job> running = new ConcurrentHashMap<>();
    private Map<String, Job> manualRunning = new ConcurrentHashMap<>();
    private Map<String, Job> debugRunning = new ConcurrentHashMap<>();
    private WorkHandler handler;
    private WorkClient workClient;
    /**
     * 处理web 异步请求
     */
    private ExecutorService workWebThreadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 1L, TimeUnit.MINUTES,
            new SynchronousQueue<>(), new NamedThreadFactory("worker-web"), new ThreadPoolExecutor.AbortPolicy());

    /**
     * 执行任务
     */
    private ExecutorService workExecuteThreadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 1L, TimeUnit.MINUTES,
            new SynchronousQueue<>(), new NamedThreadFactory("worker-execute"), new ThreadPoolExecutor.AbortPolicy());


    private static final String loadStr = "cat /proc/cpuinfo |grep processor | wc -l";

    static {
        host = NetUtils.getLocalAddress();
        if (HeraGlobalEnvironment.isLinuxSystem()) {
            RunShell shell = new RunShell(loadStr);
            Integer exitCode = shell.run();
            if (exitCode == 0) {
                try {
                    cpuCoreNum = Integer.parseInt(shell.getResult());
                } catch (IOException e) {
                    e.printStackTrace();
                    cpuCoreNum = 4;
                }
            }
        } else {
            cpuCoreNum = 4;
        }

    }


    @Autowired
    private HeraDebugHistoryService heraDebugHistoryService;
    @Autowired
    private HeraJobHistoryService heraJobHistoryService;
    @Autowired
    private HeraGroupService heraGroupService;
    @Autowired
    private HeraJobActionService heraJobActionService;
    @Autowired
    private HeraFileService heraFileService;
    @Autowired
    private HeraProfileService heraProfileService;
}
