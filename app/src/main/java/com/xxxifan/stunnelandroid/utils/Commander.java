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

    private static final String ASSET_STUNNEL_NAME = "stunnel";
    private static final String ASSET_CONFIG_NAME = "stunnel.conf";
    private static final String ASSET_CERT_NAME = "stunnel.pem";

    private static final String BINARY_TARGET_PATH = "/system/bin/stunnel";

    private static LogObserver mLogObserver;

    public static boolean checkRootPermission() throws Exception {
        return execCommand("echo \"check root\"", true, true).result == 0;
    }

    public static void chmod(int mode, String path) throws Exception {
        execCommand("chmod " + mode + " " + path, true, true);
    }

    public static CommandResult execCommand(String command, boolean isRoot,
                                            boolean isNeedResultMsg) throws Exception {
        return execCommand(new String[] {command}, isRoot, isNeedResultMsg);
    }

    public static CommandResult execCommand(String[] commands, boolean isRoot,
                                            boolean isNeedResultMsg) throws Exception {
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
        } finally {
            if (os != null) {
                os.close();
            }
            if (successResult != null) {
                successResult.close();
            }
            if (errorResult != null) {
                errorResult.close();
            }

            if (process != null) {
                process.destroy();
            }
        }

        if (!TextUtils.isEmpty(successMsg)) {
            Log.e(Commander.class, "successMsg : " + successMsg.toString());
        } else if (!TextUtils.isEmpty(errorMsg)) {
            Log.e(Commander.class, "errorMsg : " + errorMsg.toString());
        }

        return new CommandResult(result, successMsg == null ? null : successMsg.toString(),
                errorMsg == null ? null : errorMsg.toString());
    }

    public static void extractAssetTo(InputStream inputStream, File target) throws
            Exception {
        extractAssetTo(inputStream, target, false);
    }

    /**
     * extract file to a temp dir that have write permission, and then move to target with root.
     */
    public static void extractAssetTo(InputStream inputStream, File target, boolean writeSystem) throws
            Exception {
        if (writeSystem) {
            File tmpFile = new File(App.get().getFilesDir(), "extract");

            IOUtils.saveToDisk(inputStream, tmpFile);

            execCommand(new String[] {
                    CMD_RW_SYSTEM,
                    "cat " + tmpFile.getPath() + " > " + target.getPath(),
                    "chmod 777 " + target.getPath(),
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

    public static void log(String log, int type) {
        if (mLogObserver != null) {
            mLogObserver.log(log, type);
        }
    }

    public static boolean isStunnelStarted() {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean startStunnelService() {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.loadInfo();
        if (serverInfo.hasConfig() && serverInfo.start) {
            log("Starting stunnel service", 0);
            String rootPath = "/data/data/com.xxxifan.stunnelandroid/files/stunnel.conf";
            try {
//                CommandResult result = execCommand("stunnel " + rootPath + "/" + ASSET_CONFIG_NAME, false, true);
                CommandResult result = execCommand("stunnel " + rootPath + "/" + ASSET_CONFIG_NAME, false, true);
                App.get().startService(new Intent(App.get(), CoreService.class));
                boolean success = TextUtils.isEmpty(result.successMsg) && TextUtils.isEmpty(result.errorMsg);
                if (success) {
                    log("Start success", 1);
                    return true;
                } else {
                    log("Start failed, checking possible reasons", -1);
                    boolean started = isStunnelStarted();
                    if (started) {
                        log("Stunnel already started :P", 1);
                        return true;
                    } else {
                        log(!TextUtils.isEmpty(result.successMsg) ? result.successMsg :
                                !TextUtils.isEmpty(result.errorMsg) ? result.errorMsg : "Unknown error", -1);
                        return false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static void killStunnelService() {
        try {
            execCommand("busybox pkill stunnel", true, true);
            App.get().stopService(new Intent(App.get(), CoreService.class));
            log("Stunnel service stopped", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void initEnvironment() throws Exception {
        log("checking environment", 0);
        File binaryFile = new File(BINARY_TARGET_PATH);
        if (!binaryFile.exists() || binaryFile.isDirectory()) {
            extractAssetTo(App.get().getAssets().open(ASSET_STUNNEL_NAME), binaryFile, true);
            chmod(777, binaryFile.getPath());
        }
        File busyBoxBin = new File("/system/xbin/busybox");
        if (!busyBoxBin.exists()) {
            extractAssetTo(App.get().getAssets().open("busybox"), busyBoxBin, true);
        }
    }

    public void saveConfig(ServerInfo info, String mCertPath) throws Exception {
        log("Saving config", 0);
        info.save();
        File root = App.get().getFilesDir();
        File certFile = new File(mCertPath);
        File certTargetFile = new File(root, ASSET_CERT_NAME);
        String rootPath = root.getPath();

        String confFilePath = rootPath + File.separator + ASSET_CONFIG_NAME;
        // write config
        execCommand(new String[] {
                        "echo \"client = yes\" > " + confFilePath,
                        "echo \"socket = r:TCP_NODELAY=1\" >> " + confFilePath,
                        "echo \"pid = " + rootPath + "/stunnel.pid\" >> " + confFilePath,
                        "echo \"debug = info\" >> " + confFilePath,
                        "echo \"output = " + rootPath + "/stunnel.log\" >> " + confFilePath,
                        "echo \"[StunnelAndroid]\" >> " + confFilePath,
                        String.format(
                                "echo \"accept = 127.0.0.1:%s\" >> %s",
                                info.localPort,
                                confFilePath
                        ),
                        String.format(
                                "echo \"connect = %s:%s\" >> %s",
                                info.server,
                                info.serverPort,
                                confFilePath
                        ),
                        "echo \"cert = " + rootPath + "/stunnel.pem\" >> " + confFilePath,
                },
                true,
                true
        );
        chmod(777, confFilePath);
        // extra cert
        extractAssetTo(Okio.buffer(Okio.source(certFile)).inputStream(), certTargetFile, true);
        // try to create log file
        execCommand("touch " + rootPath + "/stunnel.log", false, true);
        chmod(777, rootPath + "/stunnel.log");
        log("Config saved", 1);
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
