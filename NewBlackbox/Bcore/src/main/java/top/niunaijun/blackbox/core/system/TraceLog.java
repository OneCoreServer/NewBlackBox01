package top.niunaijun.blackbox.core.system;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import top.niunaijun.blackbox.core.env.BEnvironment;
import top.niunaijun.blackbox.utils.FileUtils;

public class TraceLog {
    private static final String TAG = "TraceLog";
    private static final Object LOCK = new Object();
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

    public static File getTraceFile() {
        File dir = new File(BEnvironment.getExternalVirtualRoot(), "logs");
        FileUtils.mkdirs(dir);
        return new File(dir, "virtual_process_trace.log");
    }

    public static void i(String source, String message) {
        write("I", source, message);
    }

    public static void w(String source, String message) {
        write("W", source, message);
    }

    private static void write(String level, String source, String message) {
        String line = FORMAT.format(new Date()) + " " + level + "/" + source + " " + message;
        synchronized (LOCK) {
            File traceFile = getTraceFile();
            try (FileWriter writer = new FileWriter(traceFile, true)) {
                writer.write(line);
                writer.write('\n');
            } catch (Exception e) {
                Log.e(TAG, "Failed writing trace: " + e.getMessage(), e);
            }
        }
    }
}
