package com.xxxifan.stunnelandroid;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.xxxifan.devbox.library.helpers.ActivityConfig;
import com.xxxifan.devbox.library.tools.ViewUtils;
import com.xxxifan.devbox.library.ui.BaseActivity;
import com.xxxifan.stunnelandroid.utils.Commander;

import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseActivity {

    @Bind(R.id.server_port_text)
    EditText mPortText;
    @Bind(R.id.cert_btn)
    Button mChooseCertBtn;
    @Bind(R.id.service_status)
    TextView mServiceStatusText;
    @Bind(R.id.service_start_btn)
    ImageView mStartBtn;
    @Bind(R.id.server_text)
    EditText mServerText;
    @Bind(R.id.local_port_text)
    EditText mLocalPortText;
    @Bind(R.id.service_progress)
    ProgressBar mProgressBar;

    private Commander mCommander;
    private String mCertPath;

    private MaterialDialog mLoadingDialog;

    @Override
    protected void onConfigureActivity(ActivityConfig config) {
        config.setShowHomeAsUpKey(false)
                .setIsDarkToolbar(false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView(View rootView) {
        rootView.post(this::checkEnvironment);
    }

    @Override
    protected void onCreateFragment(List<Fragment> savedFragments) {
        if (savedFragments != null) {
            setContainerFragment(new ShowLogFragment());
        }
    }

    @OnClick(R.id.save_btn)
    public void onSaveClick(View view) {
        mLoadingDialog = ViewUtils.getLoadingDialog(getContext());
        mLoadingDialog.show();

        mProgressBar.setVisibility(View.VISIBLE);
        String server = mServerText.getText().toString();
        String serverPort = mPortText.getText().toString();
        String localPort = mLocalPortText.getText().toString();

        if (mCommander == null) {
            mCommander = new Commander();
        }
        try {
            mCommander.saveConfig(server, serverPort, localPort, mCertPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ViewUtils.dismissDialog(mLoadingDialog);
    }

    private void checkEnvironment() {
        mLoadingDialog = ViewUtils.getLoadingDialog(getContext());
        mLoadingDialog.show();

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

                        // TODO: 16-1-8 load config

                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(null);
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
                .subscribe(
                        o -> ViewUtils.dismissDialog(mLoadingDialog),
                        throwable -> {
                            ViewUtils.dismissDialog(mLoadingDialog);
                            ViewUtils.getAlertDialog(getContext(), throwable.getMessage());
                        }
                );
    }

    @Override
    public String getSimpleName() {
        return getClass().getSimpleName();
    }
}
