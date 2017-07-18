package com.talmir.mickinet.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * checkout: https://stackoverflow.com/a/22498307/4057688
 */
public class CountdownService extends Service {
    public CountdownService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
