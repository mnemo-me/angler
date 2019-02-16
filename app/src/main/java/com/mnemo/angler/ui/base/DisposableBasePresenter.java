package com.mnemo.angler.ui.base;

import io.reactivex.disposables.Disposable;

public class DisposableBasePresenter extends BasePresenter {

    private Disposable disposable;

    protected void setListener(Disposable disposable){
        this.disposable = disposable;
    }

    @Override
    public void deattachView() {
        super.deattachView();

        if (disposable != null){
            disposable.dispose();
        }
    }
}
