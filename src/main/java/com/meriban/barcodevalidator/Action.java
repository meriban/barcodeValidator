package com.meriban.barcodevalidator;

public enum Action{
    REMOVE (-1),
    ADD (1),
    TO_LOAN(2),
    TO_REF (3);
    private int actionValue;

    Action(int i) {
        this.actionValue=i;
    }
    public int getActionValue(){
        return actionValue;
    }
}