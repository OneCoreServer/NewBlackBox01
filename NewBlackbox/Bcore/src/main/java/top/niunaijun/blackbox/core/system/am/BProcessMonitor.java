package top.niunaijun.blackbox.core.system.am;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import top.niunaijun.blackbox.core.system.BProcessManagerService;
import top.niunaijun.blackbox.core.system.ProcessRecord;
import top.niunaijun.blackbox.core.system.TraceLog;

public class BProcessMonitor {
    private static final String TAG = "BProcessMonitor";
    private static final BProcessMonitor sMonitor = new BProcessMonitor();
    private static final long RESTART_WINDOW_MS = 60_000L;
    private static final long RESTART_MIN_GAP_MS = 10_000L;
    private static final int MAX_RESTARTS_IN_WINDOW = 3;
    private final Map<String, ProcessRecord> runningProcesses = new ConcurrentHashMap<>();
    private final Map<String, RestartWindow> restartWindows = new ConcurrentHashMap<>();

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
        if (record.shouldRestart && canRestart(k)) {
            TraceLog.w(TAG, "restartProcess pkg=" + record.getPackageName() + ", process=" + record.processName + ", userId=" + record.userId);
            BProcessManagerService.get().restartAppProcess(record.getPackageName(), record.processName, record.userId);
        } else if (record.shouldRestart) {
            TraceLog.w(TAG, "restartSuppressed pkg=" + record.getPackageName() + ", process=" + record.processName + ", userId=" + record.userId);
        }
    }

    private String key(String packageName, int userId) {
        return packageName + "#" + userId;
    }

    private boolean canRestart(String key) {
        long now = System.currentTimeMillis();
        RestartWindow window = restartWindows.computeIfAbsent(key, k -> new RestartWindow());
        synchronized (window) {
            if (now - window.windowStart > RESTART_WINDOW_MS) {
                window.windowStart = now;
                window.restartCount.set(0);
            }
            if (now - window.lastRestartAt < RESTART_MIN_GAP_MS) {
                return false;
            }
            if (window.restartCount.get() >= MAX_RESTARTS_IN_WINDOW) {
                return false;
            }
            window.lastRestartAt = now;
            window.restartCount.incrementAndGet();
            return true;
        }
    }

    private static final class RestartWindow {
        long windowStart = System.currentTimeMillis();
        long lastRestartAt = 0L;
        AtomicInteger restartCount = new AtomicInteger(0);
    }
}
