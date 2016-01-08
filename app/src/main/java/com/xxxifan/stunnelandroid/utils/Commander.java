package com.xxxifan.stunnelandroid.utils;

import android.content.Context;
import android.os.Build;
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

    private static final String ASSET_NAME = "stunnel";

    public static boolean checkRootPermission() {
        return execCommand("echo root", true, false).result == 0;
    }

    public static void chmod(int mode, String path) {
        execCommand("chmod " + mode + " " + path, true, false);
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

    public void initEnvironment() throws IOException {
        File target = new File("/system/bin/stunnel");
        if (!target.exists() || target.isDirectory()) {
            InputStream stream = App.get().getAssets().open(ASSET_NAME);
            Commander.extractAssetTo(stream, target);
            chmod(4755, target.getPath());
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
        }, true, false);
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
