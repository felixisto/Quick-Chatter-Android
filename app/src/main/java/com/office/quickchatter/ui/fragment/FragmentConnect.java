package com.office.quickchatter.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.office.quickchatter.R;
import com.office.quickchatter.network.bluetooth.basic.BEClient;
import com.office.quickchatter.navigation.Router;
import com.office.quickchatter.presenter.BasePresenter;
import com.office.quickchatter.presenter.BasePresenterDelegate;
import com.office.quickchatter.ui.adapters.AdapterData;
import com.office.quickchatter.ui.adapters.BEClientsListAdapter;
import com.office.quickchatter.ui.common.CommonToast;
import com.office.quickchatter.utilities.Logger;

public class FragmentConnect extends Fragment implements BasePresenterDelegate.Connect {
    private Router.Primary _router;
    private BasePresenter.Connect _presenter;
    private LayoutInflater _inflater;

    private ToggleButton _buttonScan;
    private ListView _listClients;

    private BEClientsListAdapter _adapter;

    public static @NonNull FragmentConnect build(@NonNull Router.Primary router, @NonNull BasePresenter.Connect presenter) {
        FragmentConnect fragment = new FragmentConnect();
        fragment._router = router;
        fragment._presenter = presenter;
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Logger.message(this, "onCreateView()");

        _inflater = inflater;

        View root = inflater.inflate(R.layout.fragment_connect, container, false);

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
        _buttonScan = root.findViewById(R.id.buttonScan);
        _listClients = root.findViewById(R.id.listClients);

        _buttonScan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startScan();
                } else {
                    stopScan();
                }
            }
        });

        _listClients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                _presenter.pickItem(position);
            }
        });
    }

    // # Actions

    private void startScan() {
        _presenter.startScan();
    }

    private void stopScan() {
        _presenter.stopScan();
    }

    // # PresenterDelegate.ConnectScan

    @Override
    public void displayBluetoothIsOffAlert() {
        CommonToast.showBluetoothIsOff(getContext());
    }

    @Override
    public void updateClientsListData(@NonNull AdapterData<BEClient> data) {
        _adapter = new BEClientsListAdapter(_inflater, data);
        _listClients.setAdapter(_adapter);
        _adapter.notifyDataSetChanged();
    }

    @Override
    public void navigateToConnectingScreen(final @NonNull BEClient client) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
        builder1.setMessage("Connect as:");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Server",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        _router.navigateToConnectingAsServer(client);
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "Client",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        _router.navigateToConnectingAsClient(client);
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    @Override
    public void onScanStarted() {

    }

    @Override
    public void onScanEnded() {
        if (buttonScanIsScanning()) {
            setButtonScanToScanMode(false);
        }
    }

    private boolean buttonScanIsScanning() {
        return _buttonScan.isChecked();
    }

    private void setButtonScanToScanMode(boolean scanning) {
        if (buttonScanIsScanning() != scanning) {
            _buttonScan.performClick();
        }
    }
}
