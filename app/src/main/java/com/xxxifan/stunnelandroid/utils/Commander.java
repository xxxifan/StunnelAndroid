package com.xxxifan.stunnelandroid.utils;

import android.content.Intent;
import android.text.TextUtils;

import com.xxxifan.devbox.library.tools.IOUtils;
import com.xxxifan.devbox.library.tools.Log;
import com.xxxifan.stunnelandroid.App;
import com.xxxifan.stunnelandroid.model.ServerInfo;
import com.xxxifan.stunnelandroid.service.CoreService;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okio.Okio;

/**
 * Created by xifan on 16-1-8.
 */
public class Commander {
    public static final String CMD_RW_SYSTEM = "mount -o remount,rw /system";
    public static final String CMD_RO_SYSTEM = "mount -o remount,ro /system";

    private static final String COMMAND_SU = "su";
    private static final String COMMAND_SH = "sh";
    private static final String COMMAND_EXIT = "exit\n";
    private static final String COMMAND_LINE_END = "\n";

    private static final String ASSET_STUNNEL = "stunnel";
    private static final String ASSET_CONFIG = "stunnel.conf";

    private static final String CONF_TARGET_PATH = "/data/local/etc/stunnel/stunnel.conf";
    private static final String CERT_TARGET_PATH = "/data/local/etc/stunnel/stunnel.pem";
    private static final String BINARY_TARGET_PATH = "/system/bin/stunnel";
    private static final String ROOT_PATH = "/data/local/etc/stunnel";
    private static final String LOG_PATH = "/data/local/var/log";

    private static LogObserver mLogObserver;

    public static boolean checkRootPermission() {
        return execCommand("echo root", true, true).result == 0;
    }

    public static void chmod(int mode, String path) {
        execCommand("chmod " + mode + " " + path, true, true);
    }

    public static CommandResult execCommand(String command, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(new String[] {
                command
        }, isRoot, isNeedResultMsg);
    }

    public static CommandResult execCommand(String[] commands, boolean isRoot,
                                            boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;

        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (TextUtils.isEmpty(command)) {
                    continue;
                }

                Log.e(Commander.class, "Running : " + command);
                // donnot use os.writeBytes(commmand), avoid chinese charset
                // error
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();

            result = process.waitFor();
            // get command result
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }

        if (successMsg != null) {
            Log.e(Commander.class, "successMsg : " + successMsg.toString());
        } else if (errorMsg != null) {
            Log.e(Commander.class, "errorMsg : " + errorMsg.toString());
        }

        return new CommandResult(result, successMsg == null ? null : successMsg.toString(),
                errorMsg == null ? null : errorMsg.toString());
    }

    public static void extractAssetTo(InputStream inputStream, File target) throws
            IOException {
        extractAssetTo(inputStream, target, false);
    }

    /**
     * extract file to a temp dir that have write permission, and then move to target with root.
     */
    public static void extractAssetTo(InputStream inputStream, File target, boolean writeSystem) throws
            IOException {
        if (writeSystem) {
            File tmpFile = new File(App.get().getFilesDir(), "extract");

            IOUtils.saveToDisk(inputStream, tmpFile);

            execCommand(new String[] {
                    CMD_RW_SYSTEM,
                    "cat " + tmpFile.getPath() + " > " + target.getPath(),
                    "chmod 0755 " + target.getPath(),
                    CMD_RO_SYSTEM
            }, true, true);
        } else {
            IOUtils.saveToDisk(inputStream, target);
        }
    }

    public static void registerLogObserver(LogObserver observer) {
        mLogObserver = observer;
    }

    public static void unregisterLogObserver() {
        mLogObserver = null;
    }

    public static boolean isStunnelStarted() {
        CommandResult result = execCommand("ps stunnel", true, true);
        if (!TextUtils.isEmpty(result.successMsg)) {
            String[] strs = result.successMsg.split(" ");
            for (String str : strs) {
                if (str.trim().equals("stunnel")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean startStunnelService() {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.loadInfo();
        if (serverInfo.hasConfig() && serverInfo.start) {
            log("Starting stunnel service", 0);
            CommandResult result = execCommand("stunnel", true, true);
            App.get().startService(new Intent(App.get(), CoreService.class));
            boolean success = TextUtils.isEmpty(result.successMsg) && TextUtils.isEmpty(result.errorMsg);
            if (success) {
                log("Start success", 1);
            } else {
                log("Start failed", -1);
            }

            return success;
        }
        return false;
    }

    public static void log(String log, int type) {
        if (mLogObserver != null) {
            mLogObserver.log(log, type);
        }
    }

    public static void killStunnelService() {
        log("Stunnel is Running", 1);
        execCommand("busybox pkill stunnel", true, true);
        App.get().stopService(new Intent(App.get(), CoreService.class));
    }

    public void initEnvironment() throws IOException {
        log("checking environment", 0);
        File binaryFile = new File(BINARY_TARGET_PATH);
        if (!binaryFile.exists() || binaryFile.isDirectory()) {
            extractAssetTo(App.get().getAssets().open(ASSET_STUNNEL), binaryFile, true);
            chmod(4755, binaryFile.getPath());
        }
        if (!new File(ROOT_PATH).exists()) {
            log("see", -1);
        }
        File busyBoxBin = new File("/system/xbin/busybox");
        if (!busyBoxBin.exists()) {
            extractAssetTo(App.get().getAssets().open("busybox"), busyBoxBin, true);
        }
        execCommand(new String[] {
                "mkdir -p " + ROOT_PATH,
                "chmod -R 0644 " + ROOT_PATH
        }, true, true);
        execCommand(new String[] {
                "mkdir -p " + LOG_PATH,
                "chmod -R 0644 " + LOG_PATH
        }, true, true);

//        Context context = App.get();
//        File busyBoxBin = new File("/system/xbin/busybox");
//        File applet = new File("/system/xbin/find");
//        File fixBin = new File(context.getFilesDir(), "fix_permission");
//
//        if (!busyBoxBin.exists()) {
//            try {
//                File tmp = new File(context.getFilesDir(), "busybox");
//                extractAssetTo(context.getAssets().open("binary/busybox"), tmp, busyBoxBin);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            execCommand(new String[]{
//                    CMD_RW_SYSTEM,
//                    "/system/xbin/busybox --install -s " + "/system/xbin",
//                    CMD_RO_SYSTEM
//            }, true, false);
//        }
//
//        if (!applet.exists()) {
//            execCommand(new String[]{
//                    CMD_RW_SYSTEM,
//                    "/system/xbin/busybox --install -s " + "/system/xbin",
//                    CMD_RO_SYSTEM
//            }, true, false);
//        }
//
//        if (!fixBin.exists()) {
//            try {
//                extractAssetTo(context.getAssets().open("binary/fix_permission"), fixBin);
//                execCommand(new String[]{
//                        "chmod 0755 " + fixBin.getPath(),
//                }, true, false);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    public void saveConfig(ServerInfo info, String mCertPath) throws IOException {
        info.save();
        File certFile = new File(mCertPath);
        File certTargetFile = new File(CERT_TARGET_PATH);
        File confTargetFile = new File(CONF_TARGET_PATH);
        if (confTargetFile.exists()) {
            confTargetFile.delete();
        }

        // extract config
        extractAssetTo(App.get().getAssets().open(ASSET_CONFIG), confTargetFile, true);
        // write config
        execCommand(
                String.format(
                        "echo \"accept = 127.0.0.1:%s\\nconnect = %s:%s\\n\" >> %s",
                        info.localPort,
                        info.server,
                        info.serverPort,
                        confTargetFile.getPath()
                ),
                true,
                true
        );
        // extra cert
        extractAssetTo(Okio.buffer(Okio.source(certFile)).inputStream(), certTargetFile, true);
    }

    /**
     * result of command
     * <ul>
     * <li>{@link CommandResult#result} means result of command, 0 means normal,
     * else means error, same to excute in linux shell</li>
     * <li>{@link CommandResult#successMsg} means success message of command
     * result</li>
     * <li>{@link CommandResult#errorMsg} means error message of command result</li>
     * </ul>
     *
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a>
     *         2013-5-16
     */
    public static class CommandResult {

        /**
         * result of command *
         */
        public int result;
        /**
         * success message of command result *
         */
        public String successMsg;
        /**
         * error message of command result *
         */
        public String errorMsg;

        public CommandResult(int result) {
            this.result = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }
}
