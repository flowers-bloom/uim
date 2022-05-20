package io.github.flowersbloom.udp.transfer;

public class TransferFuture {
    private volatile boolean success;
    private FutureListener listener;

    public void addListener(FutureListener futureListener) {
        this.listener = futureListener;
    }

    public boolean isSuccess() {
        return success;
    }

    public void complete(boolean result) {
        this.success = result;
        listener.callback(this);
    }
}
