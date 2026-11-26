package com.thalesgroup.d1.templates.clicktopay.model;

import com.thalesgroup.gemalto.d1.clicktopay.Status;

public class ClickToPayOperationStatusUpdate {

    private final ClickToPayOperation operation;

    private final Status status;

    public ClickToPayOperation getOperation() {
        return operation;
    }

    public Status getStatus() {
        return status;
    }

    public ClickToPayOperationStatusUpdate(final ClickToPayOperation operation, final Status status){
        this.operation = operation;
        this.status = status;
    }
}
