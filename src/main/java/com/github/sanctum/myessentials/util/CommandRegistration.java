/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.util;

import com.github.sanctum.labyrinth.Labyrinth;
import com.github.sanctum.myessentials.api.CommandData;
import com.github.sanctum.myessentials.model.CommandBuilder;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.bukkit.plugin.Plugin;

public final class CommandRegistration {

	/**
	 * Look for any compatible object types representative of {@link CommandBuilder} within
	 * a desired package location and automatically instantiate each of them
	 * individually if possible for registration.
	 *
	 * @param packageName The location to query.
	 * @throws IllegalStateException if the plugin jar cannot be accessed
	 */
	public static void compileFields(Plugin instance, String packageName) throws IllegalStateException {
		final Set<Class<? extends CommandBuilder>> classes = new HashSet<>();
		final JarFile jarFile;
		try {
			jarFile = new JarFile(URLDecoder.decode(instance.getClass().getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8"));
		} catch (IOException e) {
			throw new IllegalStateException("Unable to inspect " + instance.getName() + " jar file!", e);
		}
		for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
			final String className = jarEntry.getName().replace("/", ".");
			if (className.startsWith(packageName) && className.endsWith(".class")) {
				final Class<? extends CommandBuilder> clazz;
				try {
					//noinspection unchecked
					clazz = (Class<? extends CommandBuilder>) Class.forName(className.substring(0, className.length() - 6));
				} catch (ClassNotFoundException e) {
					Labyrinth.getInstance().getLogger().severe(() -> "- Unable to find class" + className + "! Double check package location. See the error below for more information.");
					break;
				} catch (ClassCastException e) {
					Labyrinth.getInstance().getLogger().severe(() -> "- Unable to verify extension of CommandBuilder! Excluding " + className + ".");
					break;
				}
				if (CommandBuilder.class.isAssignableFrom(clazz)) {
					classes.add(clazz);
				}
			}
		}
		for (Class<?> aClass : classes) {
			try {
				aClass.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				Labyrinth.getInstance().getLogger().severe(() -> "- Unable to instantiate " + aClass.getName() + ". This may mean that you are not extending CommandBuilder in your command class.");
				e.printStackTrace();
				break;
			}
		}
	}

	/**
	 * Instantiate and register a specified command builder class.
	 *
	 * @param command The class to register.
	 */
	public static void inject(Class<? extends CommandBuilder> command) {
		try {
			command.newInstance();
		} catch (IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		}
	}

}

