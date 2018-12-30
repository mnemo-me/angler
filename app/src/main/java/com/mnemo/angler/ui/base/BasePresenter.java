package com.mnemo.angler.ui.base;


public abstract class BasePresenter {

    private BaseView baseView;

    public void attachView(BaseView baseView){

        if (this.baseView == null) {
            this.baseView = baseView;
        }
    }

    public void deattachView(){
        baseView = null;
    }

    protected BaseView getView() {
        return baseView;
    }
}
