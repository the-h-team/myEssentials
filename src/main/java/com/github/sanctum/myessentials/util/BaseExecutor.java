package com.github.sanctum.myessentials.util;

import com.github.sanctum.labyrinth.Labyrinth;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.bukkit.plugin.Plugin;

public class BaseExecutor {

	/**
	 * Look for any compatible object types representative of BukkitCommand within a
	 * desired package location and automatically register each of them individually if possible.
	 * @param packageName The location to query.
	 */
	public static void compileFields(Plugin instance, String packageName) {
		Set<Class<?>> classes = Sets.newHashSet();
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(URLDecoder.decode(instance.getClass().getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
			String className = jarEntry.getName().replace("/", ".");
			if (className.startsWith(packageName) && className.endsWith(".class")) {
				Class<?> clazz;
				try {
					clazz = Class.forName(className.substring(0, className.length() - 6));
				} catch (ClassNotFoundException e) {
					Labyrinth.getInstance().getLogger().severe("- Unable to find class" + className + "! Double check package location. See the error below for more information.");
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
				Labyrinth.getInstance().getLogger().severe("- Unable to cast CommandBuilder to the class " + aClass.getName() + ". This likely means you are not extending CommandBuilder for your command class.");
				e.printStackTrace();
				break;
			}
		}
	}


}

