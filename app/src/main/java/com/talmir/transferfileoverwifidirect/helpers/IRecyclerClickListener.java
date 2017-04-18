package com.talmir.transferfileoverwifidirect.helpers;

import android.view.View;

/**
 * @author miri
 * @since 4/18/2017
 */
public interface IRecyclerClickListener {
    void onClick(View view, int position);
    void onLongClick(View view, int position);
}
