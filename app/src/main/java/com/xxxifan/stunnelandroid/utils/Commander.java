package com.xxxifan.stunnelandroid.utils;

import android.text.TextUtils;

import com.xxxifan.devbox.library.tools.IOUtils;
import com.xxxifan.devbox.library.tools.Log;
import com.xxxifan.stunnelandroid.App;

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
    private static final String ROOT_PATH = "/data/local/etc/stunnel/";
    private static final String LOG_PATH = "/data/local/var/log/";

    public static boolean checkRootPermission() {
        return execCommand("echo root", true, true).result == 0;
    }

    public static void chmod(int mode, String path) {
        execCommand("chmod " + mode + " " + path, true, true);
    }

    public static CommandResult execCommand(String command, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(new String[]{
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
            Log.e(Commander.class, successMsg.toString());
        } else if (errorMsg != null) {
            Log.e(Commander.class, errorMsg.toString());
        }

        return new CommandResult(result, successMsg == null ? null : successMsg.toString(),
                errorMsg == null ? null : errorMsg.toString());
    }

    public static void extractAssetTo(InputStream inputStream, File target) throws
            IOException {
        IOUtils.saveToDisk(inputStream, target);
    }

    /**
     * extract file to a temp dir that have write permission, and then move to target with root.
     */
    public static void extractAssetTo(InputStream inputStream, File tmpFile, File target) throws
            IOException {
        IOUtils.saveToDisk(inputStream, tmpFile);

        execCommand(new String[]{
                CMD_RW_SYSTEM,
                "cat " + tmpFile.getPath() + " > " + target.getPath(),
                "chmod 0755 " + target.getPath(),
                CMD_RO_SYSTEM
        }, true, true);
    }

    public void initEnvironment() throws IOException {
        File binaryFile = new File(BINARY_TARGET_PATH);
        File root = new File(ROOT_PATH);
        File log = new File(LOG_PATH);
        if (!binaryFile.exists() || binaryFile.isDirectory()) {
            InputStream stream = App.get().getAssets().open(ASSET_STUNNEL);
            Commander.extractAssetTo(stream, binaryFile);
            chmod(4755, binaryFile.getPath());
        }
        if (!root.exists() || !root.isDirectory()) {
            root.mkdirs();
        }
        if (!log.exists() || !log.isDirectory()) {
            log.mkdirs();
        }

//
//
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

    public void saveConfig(String server, String serverPort, String localPort, String mCertPath) throws IOException {
        File certFile = new File(mCertPath);
        File certTargetFile = new File(CERT_TARGET_PATH);
        File confTargetFile = new File(CONF_TARGET_PATH);
        if (confTargetFile.exists()) {
            confTargetFile.delete();
        }

        boolean confResult = IOUtils.saveToDisk(App.get().getAssets().open(ASSET_CONFIG), confTargetFile);
        if (confResult) {
            chmod(4755, confTargetFile.getPath());
            execCommand(
                    String.format(
                            "echo \"accept = 127.0.0.1:%s\\nconnect = %s:%s\\n\" > %s",
                            localPort,
                            server,
                            serverPort,
                            confTargetFile.getPath()
                    ),
                    true,
                    true
            );
        }
        boolean certResult = IOUtils.saveToDisk(Okio.buffer(Okio.source(certFile)), certTargetFile);
        if (certResult) {
            chmod(4755, certTargetFile.getPath());
        }
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
