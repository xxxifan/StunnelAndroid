package com.xxxifan.devbox.library.callbacks;

import java.io.IOException;

/**
 * Created by xifan on 15-7-30.
 */
public interface CommandCallback {
    void done(String result, IOException e);
}
