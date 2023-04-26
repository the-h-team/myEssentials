package com.github.sanctum.myessentials.api;

import com.github.sanctum.panther.util.AbstractClassLoader;
import java.io.File;
import java.io.IOException;

final class EssentialsClassLoader extends AbstractClassLoader<EssentialsAddon> {

	EssentialsClassLoader(File file) throws IOException, InvalidAddonException {
		super(file, MyEssentialsAPI.class.getClassLoader());
		if (getMainClass() == null) throw new InvalidAddonException("Processed jar not an essentials addon!");
	}

	EssentialsClassLoader(File file, EssentialsAddon parent) throws IOException, InvalidAddonException {
		super(file, parent.getClassLoader());
		if (getMainClass() == null) throw new InvalidAddonException("Processed jar not an essentials addon!");
	}

	@Override
	public String toString() {
		return "EssentialsAddonClassLoader{" +
				"addon=" + getMainClass() +
				'}';
	}
}
