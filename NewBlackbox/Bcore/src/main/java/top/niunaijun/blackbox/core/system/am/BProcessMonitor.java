package top.niunaijun.blackbox.core.system.am;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import top.niunaijun.blackbox.core.system.BProcessManagerService;
import top.niunaijun.blackbox.core.system.ProcessRecord;
import top.niunaijun.blackbox.core.system.TraceLog;

public class BProcessMonitor {
    private static final String TAG = "BProcessMonitor";
    private static final BProcessMonitor sMonitor = new BProcessMonitor();
    private final Map<String, ProcessRecord> runningProcesses = new ConcurrentHashMap<>();

    public static BProcessMonitor get() {
        return sMonitor;
    }

    public void onProcessStarted(ProcessRecord record) {
        if (record == null) return;
        runningProcesses.put(key(record.getPackageName(), record.userId), record);
        TraceLog.i(TAG, "onProcessStarted pkg=" + record.getPackageName() + ", userId=" + record.userId + ", pid=" + record.pid);
    }

    public void onProcessDied(ProcessRecord record) {
        if (record == null) return;
        String k = key(record.getPackageName(), record.userId);
        ProcessRecord removed = runningProcesses.remove(k);
        if (removed == null) return;
        TraceLog.w(TAG, "onProcessDied pkg=" + record.getPackageName() + ", userId=" + record.userId + ", pid=" + record.pid);
        BActivityManagerService.get().cleanUpActivities(record.getPackageName(), record.userId);
        if (record.shouldRestart) {
            TraceLog.w(TAG, "restartProcess pkg=" + record.getPackageName() + ", process=" + record.processName + ", userId=" + record.userId);
            BProcessManagerService.get().restartAppProcess(record.getPackageName(), record.processName, record.userId);
        }
    }

    private String key(String packageName, int userId) {
        return packageName + "#" + userId;
    }
}
