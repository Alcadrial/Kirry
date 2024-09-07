package com.alcadrial.renderengine.audio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.lwjgl.fmod.FMOD;

public class GenBindings {
	
	public GenBindings()
	{}
	
	public static void main(String[] args)
	{
		Map<String, RandomAccessFile> classPath = new HashMap<>();
		
		for (Method method : FMOD.class.getDeclaredMethods())
		{
			if ((method.getModifiers() & Modifier.STATIC) == 0) continue;
			if ((method.getModifiers() & Modifier.PUBLIC) == 0) continue;
			if (!method.getName().startsWith("FMOD")) continue;
			
			String fullName = method.getName();
			String name = fullName.substring(5);
			String clazzName = name.substring(0, name.indexOf("_"));
			String methodName = name.substring(name.indexOf("_") + 1);
			methodName = methodName.substring(0, 1).toLowerCase() + methodName.substring(1);
			String methodReturnType = method.getReturnType().toString();
			
			System.out.println(method);
			String methodParameters = Arrays.stream(method.getParameters())
				//	.map(p -> p.getType() + " " + p.getAnnotation(NativeType.class).value().toLowerCase().replaceAll("\\*", "").trim())
				.map(p -> p.getType().getCanonicalName() + " " + p.getName())
				.map(s -> s.replaceAll("\\$", "."))
				.collect(Collectors.joining(", "));
			String methodParameterNames = Arrays.stream(method.getParameters())
				//	.map(p -> p.getAnnotation(NativeType.class).value().toLowerCase())
				.map(Parameter::getName)
				.collect(Collectors.joining(", "));
			
			System.out.println(clazzName + " ".repeat(20 - clazzName.length()) + methodName);
			
			try
			{
				
				RandomAccessFile clazzFile;
				
				if (classPath.containsKey(clazzName))
				{
					clazzFile = classPath.get(clazzName);
				}
				else
				{
					Files.deleteIfExists(Path.of("src/main/java/com/alcadrial/renderengine/audio/bindings/FMod" + clazzName + ".java"));
					clazzFile = new RandomAccessFile("src/main/java/com/alcadrial/renderengine/audio/bindings/FMod" + clazzName + ".java", "rwd");
					clazzFile.write("""
						package com.alcadrial.renderengine.audio.bindings;
						
						import org.lwjgl.fmod.FMOD;
						
						public class FMod%1$s {
						
							public FMod%1$s()
							{
						
							}
						
						""".formatted(clazzName).getBytes());
					classPath.put(clazzName, clazzFile);
				}
				
				clazzFile.write("""
					\t/**
						 *@see FMOD#%4$s
						 */
						public static %1$s %2$s(%3$s)
						{
							return FMOD.%4$s(%5$s);
						}
					
					""".formatted(methodReturnType, methodName, methodParameters, fullName, methodParameterNames).getBytes());
			}
			catch (IOException e)
			{
				e.printStackTrace(System.out);
				break;
			}
		}
		
		for (RandomAccessFile clazzFile : classPath.values())
		{
			try (var v = clazzFile)
			{
				clazzFile.write("""
					}
					""".getBytes());
			}
			catch (IOException e)
			{
				e.printStackTrace(System.out);
			}
		}
	}
	
}
