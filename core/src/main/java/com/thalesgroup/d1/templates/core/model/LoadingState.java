package com.thalesgroup.d1.templates.core.model;

public class LoadingState {

    private static final int NO_MESSAGE_ID = -1;

    private final boolean isLoading;
    private final int messageId;


    public LoadingState(boolean isLoading){
        this.isLoading = isLoading;
        this.messageId = NO_MESSAGE_ID;
    }

    public LoadingState(final boolean isLoading, final int messageId) {
        this.isLoading = isLoading;
        this.messageId = messageId;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getMessageId(final int defaultMessageId){
        if(messageId != NO_MESSAGE_ID) {
            return messageId;
        } else {
            return defaultMessageId;
        }
    }

    public boolean hasMessage(){
        return messageId != NO_MESSAGE_ID;
    }
}
