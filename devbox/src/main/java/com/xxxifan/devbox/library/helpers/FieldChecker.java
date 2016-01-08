package com.xxxifan.devbox.library.helpers;

import android.text.TextUtils;
import android.widget.EditText;

/**
 * Created by xifan on 15-7-24.
 */
public class FieldChecker {

    private FieldChecker() {

    }

    public static boolean isEmptyField(EditText item) {
        return checkEmptyField(item) > -1;
    }

    /**
     * @return first empty field position, -1 if no empty field
     */
    public static int checkEmptyField(EditText... items) {
        for (int i = 0, s = items.length; i < s; i++) {
            if (items[i] == null || TextUtils.isEmpty(items[i].getText())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return first empty field position, -1 if no empty field
     */
    public static int checkField(EditText[] items, CheckFilter filter) {
        if (filter == null) {
            // 憋逗
            return -1;
        }

        for (int i = 0, s = items.length; i < s; i++) {
            boolean success = filter.onCheck(items[i].getText());
            if (!success) {
                return i;
            }
        }
        return -1;
    }

    public static boolean checkEqualField(EditText... items) {
        boolean result = true;
        String str1, str2;
        for (int i = items.length - 1; i >= 0; i--) {
            if (i - 1 < 0) {
                break;
            }

            str1 = items[i].getText().toString();
            str2 = items[i - 1].getText().toString();
            if (TextUtils.isEmpty(str1) || TextUtils.isEmpty(str2) || !str1.equals(str2)) {
                result = false;
                break;
            }
        }

        return result;
    }

    public abstract class CheckFilter {
        /**
         * @return true if success
         */
        public abstract boolean onCheck(CharSequence str);
    }

}
