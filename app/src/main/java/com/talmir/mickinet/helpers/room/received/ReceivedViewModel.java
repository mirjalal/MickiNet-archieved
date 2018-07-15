package com.talmir.mickinet.helpers.room.received;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * @author miri
 * @since 7/15/2018
 */
public class ReceivedViewModel extends AndroidViewModel {

    private ReceivedRepository mRepository;
    private List<Received> mAllReceiveds;

    public ReceivedViewModel(@NonNull Application application) {
        super(application);
        mRepository = new ReceivedRepository(application);
        mAllReceiveds = mRepository.getAllReceiveds();
    }

    List<Received> getAllReceiveds() { return mAllReceiveds; }

    public void insert(Received received) { mRepository.insert(received); }
}
