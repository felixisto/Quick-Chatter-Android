package com.office.quickchatter.presenter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.network.bluetooth.basic.BEClient;
import com.office.quickchatter.network.bluetooth.basic.BEEmitter;
import com.office.quickchatter.network.bluetooth.basic.BEPairing;
import com.office.quickchatter.network.bluetooth.bluedroid.parser.PairEntityToClientParser;
import com.office.quickchatter.ui.adapters.BEClientsAdapterData;
import com.office.quickchatter.utilities.Logger;
import com.office.quickchatter.utilities.Parser;
import com.office.quickchatter.utilities.SafeMutableArray;

import java.util.ArrayList;
import java.util.List;

public class ReconnectPresenter implements BasePresenter.Reconnect {
    private final @NonNull Context _context;
    private @Nullable BasePresenterDelegate.Reconnect _delegate;

    private final @NonNull BEEmitter _emitter;
    private final @NonNull BEPairing.Database _database;
    private final @NonNull Parser<BEPairing.Entity, BEClient> _parser;

    private final @NonNull SafeMutableArray<BEClient> _clients = new SafeMutableArray<>();
    private @NonNull BEClientsAdapterData _data = new BEClientsAdapterData(_clients.copyData());

    public ReconnectPresenter(@NonNull Context context,
                              @NonNull BEEmitter emitter,
                              @NonNull BEPairing.Database database,
                              @NonNull Parser<BEPairing.Entity, BEClient> parser) {
        _context = context;
        _emitter = emitter;
        _database = database;
        _parser = parser;
    }

    public ReconnectPresenter(@NonNull Context context,
                              @NonNull BEEmitter emitter,
                              @NonNull BEPairing.Database database) {
        this(context, emitter, database, new PairEntityToClientParser());
    }

    // # Presenter.Reconnect

    @Override
    public void start(@NonNull BasePresenterDelegate.Reconnect delegate) {
        if (_delegate != null) {
            return;
        }

        Logger.message(this, "Start.");

        _delegate = delegate;

        if (!_emitter.isBluetoothOn()) {
            delegate.displayBluetoothIsOffAlert();
            return;
        }

        updateData();
    }

    @Override
    public void pickItem(int index) {
        if (index < 0 || index >= _clients.size()) {
            return;
        }

        Logger.message(this, "Clicked item on " + index);

        if (_delegate == null) {
            return;
        }

        BEClient client = _clients.get(index);

        _delegate.navigateToConnectingScreen(client);
    }

    private void updateData() {
        List<BEPairing.Entity> pairedClients = _database.getKnownPairedClients();
        ArrayList<BEClient> clients = new ArrayList<>();

        for (BEPairing.Entity entity : pairedClients) {
            try {
                clients.add(_parser.parse(entity));
            } catch (Exception e) {

            }
        }

        _clients.removeAll();
        _clients.addAll(clients);

        BEClientsAdapterData newData = new BEClientsAdapterData(clients);

        if (_data.equals(newData)) {
            return;
        }

        _data = newData;

        if (_delegate != null) {
            _delegate.updateClientsListData(newData);
        }
    }
}