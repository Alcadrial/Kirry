package com.alcadrial.renderengine.render.test;

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

import com.alcadrial.renderengine.core.util.Pair;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.Shader;
import com.alcadrial.renderengine.render.shader.ShaderInfo;
import com.alcadrial.renderengine.render.test.util.RenderTest;
import com.alcadrial.renderengine.render.util.Version;

public class CompileShaders extends RenderTest {

	public static void main(String[] args)
	{
		if (!RenderEngine.initialize("compiler", new Version(0, 1, 0, 0)))
		{
			System.out.println("Cannot initialize RenderEngine");
			RenderEngine.getLastInitializationException().printStackTrace();
			return;
		}

		List<Pair<Path, IOException>> failures = new LinkedList<>();
		try
		{
			Files.walkFileTree(new File("src/main/resources").toPath(), new FileVisitor<Path>() {

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
				{
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
				{
					if (file.toString().matches(".*(\\.vert|\\.tesc|\\.tese|\\.geom|\\.frag|\\.comp)"))
					{
						Shader shader = new Shader(file.toFile());
						ShaderInfo shaderInfo = ShaderInfo.fromJson(new File(file.toString() + ".json"));
						shader.compile(shaderInfo);
						if (!shader.isCompilationSuccessfull())
						{
							throw new IOException("Shader " + shader.getFile() + " has errors: \n" + Arrays.stream(shader.getCode()).collect(Collectors.joining("\n")));
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
			});
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
		RenderEngine.terminate();
	}
}
