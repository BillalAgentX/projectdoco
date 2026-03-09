package com.projectdocupro.mobile.canvasview.utils;

public class HistoricalAction {
    protected boolean isOriginalAction; // Move, Undo, Redo

    protected boolean isActivated;

    HistoricalAction(){
        this.isOriginalAction = true;
        this.isActivated = true;
    }
}
