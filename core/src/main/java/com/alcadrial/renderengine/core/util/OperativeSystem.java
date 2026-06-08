package com.alcadrial.renderengine.core.util;

import java.io.File;

import org.jspecify.annotations.Nullable;

public enum OperativeSystem {
	
	WINDOWS("exe"),
	LINUX(null),
	MACOSX(null),
	SOLARIS(null),
	UNKNOWN(null);
	
	public static final OperativeSystem OS;
	
	static
	{
		String os = System.getenv("OS");
		if (os == null) OS = UNKNOWN;
		else if (os.matches("(?i).*windows.*")) OS = WINDOWS;
		else if (os.matches("(?i).*(linux|unix).*")) OS = LINUX;
		else if (os.matches("(?i).*mac.*")) OS = MACOSX;
		else if (os.matches("(?i).*(sunos|solaris).*")) OS = SOLARIS;
		else OS = UNKNOWN;
	}

	private @Nullable String executableExtension;
	
	OperativeSystem(@Nullable String executableExtension)
	{
		this.executableExtension = executableExtension;
	}
	
	public @Nullable String getExecutableExtension()
	{
		return executableExtension;
	}
	
	public File withExecutableExtension(File file)
	{
		return new File(executableExtension != null ? file.getPath() + "." + executableExtension : file.getPath());
	}
}
