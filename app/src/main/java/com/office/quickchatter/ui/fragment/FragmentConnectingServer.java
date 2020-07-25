package com.office.quickchatter.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.office.quickchatter.R;
import com.office.quickchatter.network.bluetooth.basic.BEClient;
import com.office.quickchatter.network.bluetooth.basic.BETransmitter;
import com.office.quickchatter.navigation.Router;
import com.office.quickchatter.presenter.BasePresenter;
import com.office.quickchatter.presenter.BasePresenterDelegate;
import com.office.quickchatter.ui.common.CommonToast;
import com.office.quickchatter.utilities.Logger;

public class FragmentConnectingServer extends Fragment implements BasePresenterDelegate.ConnectingServer {
    private Router.Primary _router;

    private BasePresenter.ConnectingServer _presenter;

    private TextView _label;

    public static @NonNull FragmentConnectingServer build(@NonNull Router.Primary router, @NonNull BasePresenter.ConnectingServer presenter) {
        FragmentConnectingServer fragment = new FragmentConnectingServer();
        fragment._router = router;
        fragment._presenter = presenter;
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Logger.message(this, "onCreateView()");

        View root = inflater.inflate(R.layout.fragment_connecting, container, false);

        setupFirstTimeUI(root);

        return root;
    }

    private void setupFirstTimeUI(@NonNull View root) {
        _label = root.findViewById(R.id.label);
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

    // # PresenterDelegate.ConnectingServer

    @Override
    public void displayBluetoothIsOffAlert() {
        CommonToast.showBluetoothIsOff(getContext());
    }

    @Override
    public void updateClientInfo(@NonNull String name) {
        _label.setText("Pairing with '" + name  + "'...");
    }

    @Override
    public void navigateToChatScreen(@NonNull BEClient client,
                                     @NonNull BETransmitter.ReaderWriter transmitter,
                                     @NonNull BETransmitter.Service transmitterService) {
        try {
            _router.navigateToChatScreen(client, transmitter, transmitterService);
        } catch (Exception e) {
            Logger.error(this, "Internal error while navigating to chat: " + e);

            _router.navigateBack();

            showError("Error", "Internal error");
        }
    }

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
