package com.github.sanctum.myessentials.api;

public class InvalidAddonStateException extends Error {
	private static final long serialVersionUID = -3319446608602315912L;

	public InvalidAddonStateException(String message) {
		super(message);
	}
}
