package com.alcadrial.kirry.render.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.alcadrial.kirry.core.util.Pair;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.shader.Shader;
import com.alcadrial.kirry.render.shader.ShaderType;
import com.alcadrial.kirry.render.test.util.RenderTest;
import com.alcadrial.kirry.render.util.Version;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class CompileShaders extends RenderTest {

	public static void main(String[] args)
	{
		try
		{
			KirryVulkan.initialize("compiler", new Version(0, 1, 0, 0));
		}
		catch (VulkanException e)
		{
			System.out.println("Cannot initialize Kirry");
			e.printStackTrace();
			return;
		}

		List<Pair<Path, IOException>> failures = new LinkedList<>();
		try
		{
			FileVisitor<Path> visitor = new FileVisitor<>() {

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
				{
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException
				{
					if (Shader.isShaderFile(path))
					{
						File file = path.toFile();
						String name = file.getName();
						Shader shader = new Shader(path);
						ShaderType type = ShaderType.fromExtension(name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : name);
						shader.compile(type);
						if (!shader.isCompilationSuccessfull())
						{
							throw new IOException("Shader " + shader.getPath() + " has errors: \n" + Arrays.stream(shader.getCode()).collect(Collectors.joining("\n")));
						}
						saveShaderBinary(shader);
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
				{
					failures.add(new Pair<>(file, exc));
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
				{
					return FileVisitResult.CONTINUE;
				}
			};
			Files.walkFileTree(new File("src/test/resources").toPath(), visitor);
			Files.walkFileTree(new File("src/main/resources").toPath(), visitor);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}

		if (!failures.isEmpty())
		{
			System.out.println("Some shaders have failed");
			for (Pair<Path, IOException> p : failures)
			{
				System.out.println(p);
			}
		}
		KirryVulkan.terminate();
	}
}
