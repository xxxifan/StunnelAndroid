package com.xxxifan.stunnelandroid;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.xxxifan.devbox.library.helpers.ActivityConfig;
import com.xxxifan.devbox.library.tools.ViewUtils;
import com.xxxifan.devbox.library.ui.BaseActivity;
import com.xxxifan.stunnelandroid.utils.Commander;

import java.util.List;

import butterknife.Bind;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseActivity {

    @Bind(R.id.server_port_text)
    EditText mServerPortText;
    @Bind(R.id.cert_btn)
    Button mChooseCertBtn;
    @Bind(R.id.service_status)
    TextView mServiceStatusText;
    @Bind(R.id.service_start_btn)
    ImageView mStartBtn;
    @Bind(R.id.server_text)
    EditText mServerText;

    private Commander mCommander;

    @Override
    protected void onConfigureActivity(ActivityConfig config) {
        config.setShowHomeAsUpKey(false).setIsDarkToolbar(false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView(View rootView) {
        rootView.post(this::checkEnvironment);
    }

    private void checkEnvironment() {
        final MaterialDialog dialog = ViewUtils.getLoadingDialog(getContext());
        dialog.show();

        Observable
                .create(subscriber -> {
                    try {
                        boolean hasPerm = Commander.checkRootPermission();
                        if (!hasPerm) {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onError(new Throwable("Operation failed! Please make sure you have root access on this device!"));
                                return;
                            }
                        }

                        if (mCommander == null) {
                            mCommander = new Commander();
                        }
                        mCommander.initEnvironment();

                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onError(e);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(() -> ViewUtils.dismissDialog(dialog))
                .subscribe(o -> {}, throwable -> ViewUtils.getAlertDialog(getContext(), throwable.getMessage()));
    }

    @Override
    protected void onCreateFragment(List<Fragment> savedFragments) {
        if (savedFragments != null) {
            setContainerFragment(new ShowLogFragment());
        }
    }

    @Override
    public String getSimpleName() {
        return getClass().getSimpleName();
    }
}
