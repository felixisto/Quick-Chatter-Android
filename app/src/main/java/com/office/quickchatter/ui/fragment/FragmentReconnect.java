package com.office.quickchatter.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.office.quickchatter.R;
import com.office.quickchatter.navigation.Router;
import com.office.quickchatter.network.bluetooth.basic.BEClient;
import com.office.quickchatter.presenter.BasePresenter;
import com.office.quickchatter.presenter.BasePresenterDelegate;
import com.office.quickchatter.ui.adapters.AdapterData;
import com.office.quickchatter.ui.adapters.BEClientsListAdapter;
import com.office.quickchatter.ui.common.CommonToast;
import com.office.quickchatter.utilities.Logger;

public class FragmentReconnect extends Fragment implements BasePresenterDelegate.Reconnect {
    private Router.Primary _router;
    private BasePresenter.Reconnect _presenter;
    private LayoutInflater _inflater;

    private ListView _listPairedClients;

    private BEClientsListAdapter _adapter;

    public static @NonNull FragmentReconnect build(@NonNull Router.Primary router, @NonNull BasePresenter.Reconnect presenter) {
        FragmentReconnect fragment = new FragmentReconnect();
        fragment._router = router;
        fragment._presenter = presenter;
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Logger.message(this, "onCreateView()");

        _inflater = inflater;

        View root = inflater.inflate(R.layout.fragment_reconnect, container, false);

        setupFirstTimeUI(root);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        _presenter.start(this);
    }

    private void setupFirstTimeUI(@NonNull View root) {
        _listPairedClients = root.findViewById(R.id.listPairClients);

        _listPairedClients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                _presenter.pickItem(position);
            }
        });
    }

    // # PresenterDelegate.Reconnect

    @Override
    public void displayBluetoothIsOffAlert() {
        CommonToast.showBluetoothIsOff(getContext());
    }

    @Override
    public void updateClientsListData(@NonNull AdapterData<BEClient> data) {
        _adapter = new BEClientsListAdapter(_inflater, data);
        _listPairedClients.setAdapter(_adapter);
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
                        dialog.cancel();

                        try {
                            _router.navigateToConnectingAsServer(client);
                        } catch (Exception e) {
                            showError("Error", "Internal error");
                        }
                    }
                });

        builder1.setNegativeButton(
                "Client",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                        try {
                            _router.navigateToConnectingAsClient(client);
                        } catch (Exception e) {
                            showError("Error", "Internal error");
                        }
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
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