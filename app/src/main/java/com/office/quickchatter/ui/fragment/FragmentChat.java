package com.office.quickchatter.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.office.quickchatter.R;
import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.navigation.Router;
import com.office.quickchatter.presenter.BasePresenter;
import com.office.quickchatter.presenter.BasePresenterDelegate;
import com.office.quickchatter.ui.common.CommonToast;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.Logger;
import com.office.quickchatter.utilities.SimpleCallback;

public class FragmentChat extends Fragment implements BasePresenterDelegate.Chat {
    private Router.Primary _router;
    private Router.System _systemRouter;
    private BasePresenter.Chat _presenter;

    private TextView _textTitle;
    private ScrollView _scrollChat;
    private TextView _textChat;
    private EditText _fieldEnterText;
    private Button _buttonSendFile;

    public static @NonNull FragmentChat build(@NonNull Router.Primary router, @NonNull Router.System systemRouter, @NonNull BasePresenter.Chat presenter) {
        FragmentChat fragment = new FragmentChat();
        fragment._router = router;
        fragment._systemRouter = systemRouter;
        fragment._presenter = presenter;
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Logger.message(this, "onCreateView()");

        View root = inflater.inflate(R.layout.fragment_chat, container, false);

        setupFirstTimeUI(root);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        _presenter.start(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        _presenter.stop();
    }

    private void setupFirstTimeUI(@NonNull View root) {
        _textTitle = root.findViewById(R.id.textTitle);
        _scrollChat = root.findViewById(R.id.scrollChat);
        _textChat = root.findViewById(R.id.textChat);
        _fieldEnterText = root.findViewById(R.id.fieldEnterText);
        _buttonSendFile = root.findViewById(R.id.buttonSendFile);

        _fieldEnterText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    _presenter.sendMessage(_fieldEnterText.getText().toString());
                    return true;
                }
                return false;
            }
        });

        _buttonSendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_presenter.canSendFile()) {
                    Toast toast = Toast.makeText(getContext(), "Already transferring", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                _systemRouter.pickFile(new Callback<Path>() {
                    @Override
                    public void perform(Path path) {
                        _presenter.sendFile(path);
                    }
                }, new SimpleCallback() {
                    @Override
                    public void perform() {

                    }
                }, "Pick file to send");
            }
        });

        _textChat.setText("");
    }

    // # PresenterDelegate.Chat

    @Override
    public void displayBluetoothIsOffAlert() {
        CommonToast.showBluetoothIsOff(getContext());
    }

    @Override
    public void updateClientInfo(@NonNull String name) {
        _textTitle.setText("Chatting with '" + name + "'");

        // Fixes issue with chat being scrolled to the bottom
        _scrollChat.smoothScrollTo(0, 0);
    }

    @Override
    public void updateChat(@NonNull String newLine, @NonNull String fullChat) {
        _textChat.setText(fullChat);
    }

    @Override
    public void clearChatTextField() {
        _fieldEnterText.setText("");
    }

    @Override
    public void onAskToAcceptTransferFile(final @NonNull Callback<Path> accept,
                                          final @NonNull SimpleCallback deny,
                                          final @NonNull String name,
                                          @NonNull String description) {
        // Alert
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Transfer");
        builder.setMessage("Other side wants to send file: " + description);
        builder.setCancelable(false);

        builder.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deny.perform();
            }
        });

        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                _systemRouter.pickFileDestination(new Callback<Path>() {
                    @Override
                    public void perform(Path path) {
                        accept.perform(path);
                    }
                }, new SimpleCallback() {
                    @Override
                    public void perform() {
                        deny.perform();
                    }
                }, name, "Pick destination");
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onConnectionRestored() {

    }

    @Override
    public void onConnectionTimeout(boolean isWarning) {
        if (isWarning) {
            CommonToast.showConnectionSlow(getContext());
        } else {
            _presenter.stop();

            _router.navigateToConnectMenuScreen();

            CommonToast.showConnectionLost(getContext());
        }
    }

    @Override
    public void showError(@NonNull String title, @NonNull String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
