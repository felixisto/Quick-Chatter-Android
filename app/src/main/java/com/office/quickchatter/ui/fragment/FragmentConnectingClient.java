package com.office.quickchatter.ui.fragment;

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

public class FragmentConnectingClient extends Fragment implements BasePresenterDelegate.ConnectingClient {
    private Router.Primary _router;

    private BasePresenter.ConnectingClient _presenter;

    private TextView _label;

    public static @NonNull FragmentConnectingClient build(@NonNull Router.Primary router, @NonNull BasePresenter.ConnectingClient presenter) {
        FragmentConnectingClient fragment = new FragmentConnectingClient();
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

    // # PresenterDelegate.ConnectingClient

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
        _router.navigateToChatScreen(client, transmitter, transmitterService);
    }
}

