package com.github.sanctum.myessentials.util.teleportation;

public final class ExistingTeleportRequestException extends Exception {
    private static final long serialVersionUID = -2989805127393160714L;
    private final TeleportRequest existingRequest;

    protected ExistingTeleportRequestException(TeleportRequest existingRequest) {
        this.existingRequest = existingRequest;
    }

    public TeleportRequest getExistingRequest() {
        return existingRequest;
    }
}
