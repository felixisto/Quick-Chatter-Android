package com.office.quickchatter.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.office.quickchatter.R;
import com.office.quickchatter.navigation.Router;
import com.office.quickchatter.utilities.Logger;

public class FragmentConnectMenu extends Fragment {
    private Router.Primary _router;

    private Button _buttonConnect;
    private Button _buttonReconnect;

    public static @NonNull FragmentConnectMenu build(@NonNull Router.Primary router) {
        FragmentConnectMenu fragment = new FragmentConnectMenu();
        fragment._router = router;
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Logger.message(this, "onCreateView()");

        View root = inflater.inflate(R.layout.fragment_connect_menu, container, false);

        setupFirstTimeUI(root);

        return root;
    }

    private void setupFirstTimeUI(@NonNull View root) {
        _buttonConnect = root.findViewById(R.id.buttonConnect);
        _buttonReconnect = root.findViewById(R.id.buttonReconnect);

        _buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConnectClick();
            }
        });
        _buttonReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReconnectClick();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    private void onConnectClick() {
        Logger.message(this, "onConnectClick()");

        try {
            _router.navigateToConnectScreen();
        } catch (Exception e) {
            showError("Error", "Internal error");
        }
    }

    private void onReconnectClick() {
        Logger.message(this, "onReconnectClick()");

        try {
            _router.navigateToReconnectScreen();
        } catch (Exception e) {
            showError("Error", "Internal error");
        }
    }
}