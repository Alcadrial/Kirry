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

import com.alcadrial.renderengine.core.util.Pair;

public class GenBindings {
	
	public GenBindings()
	{}
	
	public static void main(String[] args)
	{
		Map<String, Pair<RandomAccessFile, RandomAccessFile>> classData = new HashMap<>();
		
		Method[] declaredMethods = FMOD.class.getDeclaredMethods();
		Arrays.sort(declaredMethods, (m0, m1) -> m0.getName().compareTo(m1.getName()));
		for (Method method : declaredMethods)
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
			String javadoc = """
				/**
				 * @See FMOD#%s
				 */""".formatted(fullName);
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
				
				Pair<RandomAccessFile, RandomAccessFile> data;
				
				if (classData.containsKey(clazzName))
				{
					data = classData.get(clazzName);
				}
				else
				{
					Files.createDirectories(Path.of("src/gen/java/com/alcadrial/renderengine/audio/bindings"));
					Files.deleteIfExists(Path.of("src/gen/java/com/alcadrial/renderengine/audio/bindings/FMod" + clazzName + ".java"));
					RandomAccessFile clazzFile = new RandomAccessFile("src/gen/java/com/alcadrial/renderengine/audio/bindings/FMod" + clazzName + ".java", "rwd");
					RandomAccessFile docsFile = new RandomAccessFile("docs/FMod" + clazzName + "Docs.html", "rwd");
					
					String clazzJavadoc = "/**";
					
					long position = docsFile.getFilePointer();
					String line = docsFile.readLine();
					while (line != null && !line.contains("h2"))
					{
						line = line.replaceFirst("<a.*>.+::(.+)</a>", "{@link #$1}");
						line = line.replaceFirst("<a.*href=\"(.*)\".*>(.*)</a>", "<a href=\"https://www.fmod.com/docs/2.03/api/$1\">$2</a>");
						clazzJavadoc += "\n * " + line;
						position = docsFile.getFilePointer();
						line = docsFile.readLine();
					}
					docsFile.seek(position);
					clazzJavadoc += "\n */";
					
					clazzFile.write("""
						package com.alcadrial.renderengine.audio.bindings;
						
						import org.lwjgl.fmod.FMOD;
						
						%2$s
						public class FMod%1$s {
						
							private FMod%1$s()
							{
						
							}
						
						""".formatted(clazzName, clazzJavadoc).getBytes());
					
					data = new Pair<>(clazzFile, docsFile);
					classData.put(clazzName, data);
				}
				
				javadoc = javadoc.replaceAll("\\n", "\n\t");
				
				data.getLeft().write("""
					\t%6$s
						public static %1$s %2$s(%3$s)
						{
							return FMOD.%4$s(%5$s);
						}
					
					""".formatted(methodReturnType, methodName, methodParameters, fullName, methodParameterNames, javadoc).getBytes());
			}
			catch (IOException e)
			{
				e.printStackTrace(System.out);
				break;
			}
		}
		
		for (Pair<RandomAccessFile, RandomAccessFile> p : classData.values())
		{
			try (RandomAccessFile clazzFile = p.getLeft())
			{
				clazzFile.write("""
					}
					""".getBytes());
			}
			catch (IOException e)
			{
				e.printStackTrace(System.out);
			}
			try
			{
				p.getRight().close();
			}
			catch (IOException e)
			{
				e.printStackTrace(System.out);
			}
		}
	}
	
}
