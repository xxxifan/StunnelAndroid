package com.xxxifan.stunnelandroid;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.xxxifan.devbox.library.helpers.ActivityConfig;
import com.xxxifan.devbox.library.helpers.FieldChecker;
import com.xxxifan.devbox.library.tools.FileUtils;
import com.xxxifan.devbox.library.tools.ViewUtils;
import com.xxxifan.devbox.library.ui.BaseActivity;
import com.xxxifan.stunnelandroid.model.ServerInfo;
import com.xxxifan.stunnelandroid.utils.Commander;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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
    @Bind(R.id.cert_file_name)
    TextView mCertPathText;
    @Bind(R.id.service_progress)
    ProgressBar mProgressBar;

    private Commander mCommander;
    private ServerInfo mServerInfo;
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkEnvironment();
    }

    @Override
    protected void onCreateFragment(List<Fragment> savedFragments) {
        if (savedFragments != null) {
            setContainerFragment(new ShowLogFragment());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == -1) {
            try {
                mCertPath = FileUtils.getPath(getContext(), data.getData());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            if (TextUtils.isEmpty(mCertPath)) {
                Toast.makeText(getContext(), "Invalid cert file", Toast.LENGTH_LONG).show();
            } else {
                mCertPathText.setText(new File(mCertPath).getName());
            }
        }
    }

    @OnClick(R.id.cert_btn)
    public void onChooseCert(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/pem");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }

    @OnClick(R.id.save_btn)
    public void onSaveClick(View view) {
        EditText[] fields = {
                mServerText,
                mPortText,
                mLocalPortText
        };
        int empty = FieldChecker.checkEmptyField(fields);
        if (empty < 0) {
            mLoadingDialog = ViewUtils.getLoadingDialog(getContext());
            mLoadingDialog.show();

            mProgressBar.setVisibility(View.VISIBLE);
            mServerInfo.server = mServerText.getText().toString();
            mServerInfo.serverPort = mPortText.getText().toString();
            mServerInfo.localPort = mLocalPortText.getText().toString();

            if (mCommander == null) {
                mCommander = new Commander();
            }
            try {
                mCommander.saveConfig(mServerInfo, mCertPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ViewUtils.dismissDialog(mLoadingDialog);
        } else {
            fields[empty].requestFocus();
            Toast.makeText(getContext(), "Please input server info", Toast.LENGTH_LONG).show();
        }

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
                        initServerInfo();

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

    private void initServerInfo() {
        mServerInfo = new ServerInfo();
        mServerInfo.loadInfo();
        if (TextUtils.isEmpty(mServerText.getText()) && TextUtils.isEmpty(mPortText.getText()) &&
                TextUtils.isEmpty(mLocalPortText.getText())) {
            mServerText.setText(mServerInfo.server);
            mPortText.setText(mServerInfo.serverPort);
            mLocalPortText.setText(mServerInfo.localPort);
        }
    }

    @Override
    public String getSimpleName() {
        return getClass().getSimpleName();
    }
}
