package com.talmir.mickinet.helpers.ui;

import android.view.View;

/**
 * @author miri
 * @since 4/29/2017
 */
public interface IRecyclerItemClickListener {
    void onClick(View view, int position);
    void onLongClick(View view, int position);
}
