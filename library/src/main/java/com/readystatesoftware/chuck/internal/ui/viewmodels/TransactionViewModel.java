package com.readystatesoftware.chuck.internal.ui.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.readystatesoftware.chuck.internal.data.HttpTransaction;

public class TransactionViewModel extends ViewModel {

    public MutableLiveData<HttpTransaction> transaction = new MutableLiveData<>();
}
