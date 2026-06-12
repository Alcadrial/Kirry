package com.alcadrial.kirry.render.test.filesystem;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class FileSystemTest {
	
	public static void main(String[] args)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			FileSystem fileSystem = FileSystems.newFileSystem(URI.create("rem:main"), Map.of("sources", List.of(
				"src/test/resources",
				"src/main/resources"
			)));
			
			Path root = fileSystem.getPath("");
			Path p1 = fileSystem.getPath("assets");
			Path p2 = root.resolve("assets");
			Path p3 = root.resolve("assets/kirry/ARIAL.TTF");
			Path p4 = root.resolve("assets/kirry/shaderGL.vert");
			Path p5 = root.resolve("assets/kirry/shader0.vert");
			Path p6 = root.resolve("assets/kirry/models/model1.obj");
			
			int i = 0;
			for (Path p : List.of(root, p1, p2, p3, p4, p5, p6))
			{
				System.out.println("Path " + i + ": " + p);
				System.out.println("Real " + i + ": " + p.toRealPath());
				System.out.println();
				i++;
			}
			ByteBuffer buffer = stack.malloc(4096);
			
			for (Path p : List.of(p4, p5, p6))
			{
				
				System.out.println("Content of file " + p);
				
				try (FileChannel channel = FileChannel.open(p, StandardOpenOption.READ))
				{
					int r;
					while ((r = channel.read(buffer)) != -1)
					{
						System.out.print(MemoryUtil.memUTF8(buffer.slice(0, r)));
						buffer.rewind();
					}
					System.out.println();
				}
			}
			
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(root.resolve("assets/kirry/shaders")))
			{
				System.out.println("Files in " + root.resolve("assets/kirry/shaders"));
				for (Path path : stream)
				{
					System.out.println("\t" + path);
				}
			}
			
			WatchService watchService = fileSystem.newWatchService();
			
			root.resolve("assets/kirry").register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.OVERFLOW);
			
			Thread.startVirtualThread(() -> {
				while (true)
				{
					WatchKey key;
					try
					{
						key = watchService.take();
					}
					catch (InterruptedException | ClosedWatchServiceException _)
					{
						return;
					}
					
					for (WatchEvent<?> event : key.pollEvents())
					{
						System.out.println(event.kind() + " at " + event.context());
					}
					if (!key.reset())
					{
						System.out.println("Deleted folder");
						return;
					}
				}
			});
			
			System.in.read();
			watchService.close();
			
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
	}
}
