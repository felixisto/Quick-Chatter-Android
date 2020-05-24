package com.office.quickchatter.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.filesystem.model.EntityInfo;
import com.office.quickchatter.filesystem.worker.loader.FileSystemLoaderLocal;
import com.office.quickchatter.navigation.Router;
import com.office.quickchatter.network.bluetooth.basic.BEClient;
import com.office.quickchatter.network.bluetooth.basic.BEConnector;
import com.office.quickchatter.network.bluetooth.basic.BEEmitter;
import com.office.quickchatter.network.bluetooth.basic.BETransmitter;
import com.office.quickchatter.network.bluetooth.bluedroid.BDAdapter;
import com.office.quickchatter.network.bluetooth.bluedroid.connectors.BDClientConnector;
import com.office.quickchatter.network.bluetooth.bluedroid.connectors.BDServerConnector;
import com.office.quickchatter.network.bluetooth.bluedroid.discovery.BDClientScanner;
import com.office.quickchatter.network.bluetooth.bluedroid.discovery.BDPairing;
import com.office.quickchatter.parser.EntityInfoToVMParser;
import com.office.quickchatter.presenter.ChatPresenter;
import com.office.quickchatter.presenter.ConnectPresenter;
import com.office.quickchatter.presenter.ConnectingClientPresenter;
import com.office.quickchatter.presenter.ConnectingServerPresenter;
import com.office.quickchatter.presenter.FileDestinationPickerPresenter;
import com.office.quickchatter.presenter.FilePickerPresenter;
import com.office.quickchatter.presenter.BasePresenter;
import com.office.quickchatter.presenter.ReconnectPresenter;
import com.office.quickchatter.ui.common.FileDestinationDialogHandler;
import com.office.quickchatter.ui.fragment.FragmentChat;
import com.office.quickchatter.ui.fragment.FragmentConnect;
import com.office.quickchatter.ui.fragment.FragmentConnectMenu;
import com.office.quickchatter.ui.fragment.FragmentConnectingClient;
import com.office.quickchatter.ui.fragment.FragmentConnectingServer;
import com.office.quickchatter.ui.fragment.FragmentDirectoryPicker;
import com.office.quickchatter.ui.fragment.FragmentFileDestinationPicker;
import com.office.quickchatter.ui.fragment.FragmentFilePicker;
import com.office.quickchatter.ui.fragment.FragmentReconnect;
import com.office.quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.Parser;
import com.office.quickchatter.utilities.SimpleCallback;
import com.office.quickchatter.utilities.TimeValue;

public class FragmentBuilder {
    private final @NonNull FragmentActivity _context;
    private final @NonNull Router.Primary _router;
    private final @NonNull Router.System _sysRouter;
    private final @NonNull FileSystemLoaderLocal _fileSystemLoader;

    public FragmentBuilder(@NonNull FragmentActivity context, @NonNull Router.Primary router, @NonNull Router.System sysRouter, @NonNull FileSystemLoaderLocal fileSystem) {
        _context = context;
        _router = router;
        _sysRouter = sysRouter;
        _fileSystemLoader = fileSystem;
    }

    public @NonNull Fragment buildConnectMenuScreen() {
        return FragmentConnectMenu.build(_router);
    }

    public @NonNull Fragment buildConnectScreen(@NonNull BDAdapter adapter, @NonNull BDClientScanner scanner, @NonNull BEEmitter emitter) {
        return FragmentConnect.build(_router, new ConnectPresenter(_context, scanner, emitter));
    }

    public @NonNull Fragment buildReconnectScreen(@NonNull BDAdapter adapter, @NonNull BEEmitter emitter) {
        return FragmentReconnect.build(_router, new ReconnectPresenter(_context, emitter, new BDPairing(adapter)));
    }

    public @NonNull Fragment buildConnectingServerScreen(@NonNull BEClient client, @NonNull BDAdapter adapter) {
        BEConnector.Server connector = new BDServerConnector("QuickChat", adapter);

        return FragmentConnectingServer.build(_router, new ConnectingServerPresenter(_context, client, connector));
    }

    public @NonNull Fragment buildConnectingClientScreen(@NonNull BEClient client, @NonNull BDAdapter adapter) {
        BEConnector.Client connector = new BDClientConnector(client, adapter, 10, TimeValue.buildSeconds(1));

        return FragmentConnectingClient.build(_router, new ConnectingClientPresenter(_context, client, connector));
    }

    public @NonNull Fragment buildChatScreen(@NonNull BEClient client,
                                             @NonNull BETransmitter.ReaderWriter transmitter,
                                             @NonNull BETransmitter.Service transmitterService) {
        BasePresenter.Chat presenter = new ChatPresenter(_context, client, transmitter, transmitterService);

        return FragmentChat.build(_router, _sysRouter, presenter);
    }

    public @NonNull Fragment buildPickFileScreen(@NonNull EntityInfo.Directory rootDirectory,
                                                 @NonNull Callback<Path> success,
                                                 @NonNull SimpleCallback failure,
                                                 boolean pickDirectory,
                                                 @NonNull String description) {
        Parser<EntityInfo, FileSystemEntityViewModel> parser = new EntityInfoToVMParser();
        FilePickerPresenter presenter = new FilePickerPresenter(_context, _fileSystemLoader, rootDirectory, parser);

        if (pickDirectory) {
            return FragmentDirectoryPicker.build(_sysRouter, presenter, success, failure, description);
        }

        return FragmentFilePicker.build(_sysRouter, presenter, success, failure, description);
    }

    public @NonNull Fragment buildPickFileDestinationScreen(@NonNull EntityInfo.Directory rootDirectory,
                                                            @NonNull Callback<Path> success,
                                                            @NonNull SimpleCallback failure,
                                                            @NonNull String name,
                                                            @NonNull String description) {
        Parser<EntityInfo, FileSystemEntityViewModel> parser = new EntityInfoToVMParser();
        FileDestinationDialogHandler fileDestDialogHandler = new FileDestinationDialogHandler();

        FileDestinationPickerPresenter presenter = new FileDestinationPickerPresenter(_context, _fileSystemLoader, rootDirectory, parser, fileDestDialogHandler);

        try {
            presenter.setName(name);
        } catch (Exception e) {

        }

        Fragment fragment = FragmentFileDestinationPicker.build(_sysRouter, presenter, fileDestDialogHandler, success, failure, description);

        fileDestDialogHandler.setFragment(fragment);

        return fragment;
    }
}
