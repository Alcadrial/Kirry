package com.alcadrial.renderengine.audio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.fmod.FMOD;
import org.lwjgl.fmod.FMOD_CREATESOUNDEXINFO;
import org.lwjgl.fmod.FMOD_ERRORCALLBACK_INFO;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.alcadrial.renderengine.audio.StreamUtils.EchoInputStream;
import com.alcadrial.renderengine.audio.StreamUtils.ForwardInputStream;
import com.alcadrial.renderengine.audio.StreamUtils.SeekerInputStream;
import com.alcadrial.renderengine.audio.StreamUtils.ShellStream;
import com.alcadrial.renderengine.audio.bindings.FModChannel;
import com.alcadrial.renderengine.audio.bindings.FModSound;
import com.alcadrial.renderengine.audio.bindings.FModSystem;

public class Testa {
	
	public static class InputStreamStream extends InputStream {
		
		private final BlockingQueue<InputStream> bufferedInputStream;
		private InputStream stream;
		private boolean ended;
		
		public InputStreamStream(BlockingQueue<InputStream> bufferedInputStream)
		{
			this.bufferedInputStream = bufferedInputStream;
			stream = null;
		}
		
		@Override
		public int read() throws IOException
		{
			boolean interrupted = false;
			try
			{
				
				boolean taken = false;
				while (stream == null && !taken)
				{
					try
					{
						stream = bufferedInputStream.take();
						taken = true;
					}
					catch (InterruptedException e)
					{
						interrupted = true;
					}
				}
				while (stream != null)
				{
					int i = stream.read();
					if (i != -1)
					{
						ended = false;
						return i;
					}
					if (ended)
					{
						taken = false;
						stream.close();
						while (!taken)
						{
							try
							{
								stream = bufferedInputStream.take();
								taken = true;
							}
							catch (InterruptedException e)
							{
								interrupted = true;
							}
						}
						continue;
					}
					ended = true;
					return -1;
				}
				return -1;
			}
			finally
			{
				if (interrupted) Thread.currentThread().interrupt();
			}
		}
	}
	
	private static class NodeList {
		
		Node head;
		Node tail;
		
		public void add(long channel)
		{
			if (head == null)
			{
				head = tail = new Node(channel);
				return;
			}
			tail.next = new Node(channel);
			tail.next.previous = tail;
			tail = tail.next;
		}
		
		public Node head()
		{
			return head;
		}
		
		public Node tail()
		{
			return tail;
		}
		
		public Node cursor()
		{
			return new Cursor(head);
		}
		
		public class Node {
			
			Node next;
			Node previous;
			long channel;
			
			public Node(long channel)
			{
				this.channel = channel;
			}
			
			public long getChannel()
			{
				return channel;
			}
			
			public void setChannel(long channel)
			{
				this.channel = channel;
			}
		}
		
		private class Cursor extends Node {
			
			public Cursor(Node next)
			{
				super(0);
				this.next = next;
			}
			
			@Override
			public long getChannel()
			{
				return next.getChannel();
			}
			
			@Override
			public void setChannel(long channel)
			{
				next.setChannel(channel);
			}
		}
	}
	
	public static final InputStream[] SYSTEM_INPUT = new InputStream[1];
	
	public static void handleInput()
	{
		try (ServerSocket server = new ServerSocket(11712))
		{
			BlockingQueue<InputStream> serverInputStream = new LinkedBlockingQueue<>(1);
			SYSTEM_INPUT[0] = new InputStreamStream(serverInputStream);
			BlockingQueue<InputStream> clientInputStream = new LinkedBlockingQueue<>();
			serverInputStream.add(new InputStreamStream(clientInputStream));
			
			while (!Thread.currentThread().isInterrupted())
			{
				try
				{
					Socket client = server.accept();
					Thread.startVirtualThread(() -> {
						try (Socket s = client)
						{
							ByteArrayOutputStream bufferedOutputStream = new ByteArrayOutputStream();
							ShellStream shell = new ShellStream(new SeekerInputStream(new EchoInputStream(client.getInputStream(), bufferedOutputStream)), new PrintStream(client.getOutputStream()));
							
							shell.println("Available commands:");
							shell.println("  next");
							shell.println("  previous");
							shell.println("  pause");
							shell.println("  resume");
							shell.println("  loop");
							shell.println("  stop");
							
							while (!client.isClosed() && !shell.isEnded())
							{
								shell.print("> ");
								shell.readLine();
								bufferedOutputStream.write('\r');
								bufferedOutputStream.write('\n');
								ByteArrayInputStream stream = new ByteArrayInputStream(bufferedOutputStream.toByteArray());
								synchronized (clientInputStream)
								{
									clientInputStream.add(stream);
								}
								bufferedOutputStream.reset();
							}
						}
						catch (SocketException e)
						{}
						catch (IOException e)
						{
							System.out.println("Something when wrong during connection with " + client);
						}
					});
				}
				catch (SocketException e)
				{}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
	}
	
	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Missing argument <path to sounds dir>");
			return;
		}
		
		try
		{
			System.in.close();
		}
		catch (IOException e)
		{}
		System.setIn(new ForwardInputStream(() -> SYSTEM_INPUT[0]));
		Thread inputThread = Thread.startVirtualThread(Testa::handleInput);
		
		ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(4);
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer pp = stack.mallocPointer(10);
			
			FModErrorUtils.assertNoError(FModSystem.create(pp, FMOD.FMOD_VERSION), "Failed to create system");
			long system = pp.get(0);
			
			FModErrorUtils.assertNoError(FModSystem.setCallback(system, (s, type, commandData0, commandData1, userData) -> {
				
				switch (type)
				{
					case FMOD.FMOD_SYSTEM_CALLBACK_ERROR -> {
						FMOD_ERRORCALLBACK_INFO error = FMOD_ERRORCALLBACK_INFO.create(commandData0);
						if (error.result() != 0)
						{
							System.err.println("FMod" + error.functionnameString().replace("::", ".") + "(" + error.functionparamsString() + ") returned " + FModErrorUtils.getErrorString(error.result()) + " (" + error.result() + ")");
						}
					}
				}
				return FMOD.FMOD_OK;
			}, FMOD.FMOD_SYSTEM_CALLBACK_ERROR), "Failed to register callback");
			
			List<Long> sounds = new LinkedList<>();
			
			Path directory = Path.of(args[0]);
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, p -> !Files.isHidden(p)))
			{
				FModErrorUtils.assertNoError(FModSystem.init(system, (int) Files.list(directory).count() + 10, FMOD.FMOD_INIT_NORMAL, 0), "Failed to init system");
				
				FMOD_CREATESOUNDEXINFO createSoundInfo = FMOD_CREATESOUNDEXINFO.calloc(stack);
				createSoundInfo.cbsize(FMOD_CREATESOUNDEXINFO.SIZEOF);
				int i = 0;
				for (Path path : stream)
				{
					if (i++ >= 5) break;
					int error = FModSystem.createSound(system, stack.UTF8(path.toString()), FMOD.FMOD_LOOP_NORMAL | FMOD.FMOD_3D, createSoundInfo, pp);
					switch (error)
					{
						case FMOD.FMOD_OK -> {
							sounds.add(pp.get(0));
							FModSound.setLoopCount(pp.get(0), 0);
							System.out.println("Loaded \"" + path + "\"");
						}
						case FMOD.FMOD_ERR_FILE_BAD -> System.out.println("Failed to load \"" + path + "\": Corrupted file");
						case FMOD.FMOD_ERR_FILE_COULDNOTSEEK -> System.out.println("Failed to load \"" + path + "\": Corrupted file");
						case FMOD.FMOD_ERR_FILE_DISKEJECTED -> System.out.println("Failed to load \"" + path + "\": Disk ejected");
						case FMOD.FMOD_ERR_FILE_ENDOFDATA -> System.out.println("Failed to load \"" + path + "\": Corrupted file");
						case FMOD.FMOD_ERR_FILE_EOF -> System.out.println("Failed to load \"" + path + "\": Corrupted file");
						case FMOD.FMOD_ERR_FILE_NOTFOUND -> System.out.println("Failed to load \"" + path + "\": File not found");
						default -> System.out.println("Failed to load \"" + path + "\": Unknown error " + error);
					}
				}
			}
			catch (NoSuchFileException e)
			{}
			
			if (sounds.isEmpty())
			{
				System.out.println("0 sounds found");
				FModSystem.close(system);
				return;
			}
			
			FModErrorUtils.assertNoError(FModSystem.createChannelGroup(system, "Group 1", pp), "Failed to create channel group");
			long channelGroup = pp.get(0);
			NodeList channels = new NodeList();
			
			NodeList.Node[] cursorP = new NodeList.Node[1];
			
			for (long sound : sounds)
			{
				FModSystem.playSound(system, sound, channelGroup, 1, pp);
				channels.add(pp.get(0));
				
				FModChannel.setCallback(pp.get(0), (channelControl, controlType, callbackType, commandData1, commandData2) -> {
					if (controlType == FMOD.FMOD_CHANNELCONTROL_CHANNEL && callbackType == FMOD.FMOD_CHANNELCONTROL_CALLBACK_END)
					{
						try (MemoryStack stack0 = MemoryStack.stackPush())
						{
							PointerBuffer pp0 = stack0.mallocPointer(1);
							
							NodeList.Node cursor = cursorP[0];
							FModSystem.playSound(system, sound, channelGroup, 1, pp0);
							cursor.setChannel(pp0.get());
							
							cursor.next = cursor.next.next;
							if (cursor.next == null) cursor.next = channels.head();
							FModChannel.setPaused(cursor.getChannel(), 0);
						}
					}
					return FMOD.FMOD_OK;
				});
				
				if (channels.tail.channel == 0)
				{
					ByteBuffer sp = stack.malloc(100);
					FModSound.getName(sound, sp);
					throw new IllegalStateException("Missing channel for sound " + MemoryUtil.memUTF8(sp));
				}
			}
			
			NodeList.Node cursor = cursorP[0] = channels.cursor();
			FModChannel.setPaused(cursor.getChannel(), 0);
			
			System.out.println();
			System.out.println();
			
			ScheduledFuture<?> printFractionFuture = scheduledExecutor.scheduleAtFixedRate(() -> {
				
				printFraction(cursor);
				FModSystem.update(system);
				
			}, 0, 100, TimeUnit.MILLISECONDS);
			
			Thread thread = Thread.startVirtualThread(() -> {
				
				Scanner input = new Scanner(System.in);
				Map<String, Consumer<Scanner>> commands = Map.of(
						"stop", input0 -> {
							printFractionFuture.cancel(false);
							Thread.currentThread().interrupt();
						},
						"next", input0 -> {
							FModChannel.setPaused(cursor.getChannel(), 1);
							cursor.next = cursor.next.next;
							if (cursor.next == null) cursor.next = channels.head();
							FModChannel.setPaused(cursor.getChannel(), 0);
						},
						"previous", input0 -> {
							FModChannel.setPaused(cursor.getChannel(), 1);
							cursor.next = cursor.next.previous;
							if (cursor.next == null) cursor.next = channels.tail();
							FModChannel.setPaused(cursor.getChannel(), 0);
						},
						"pause", input0 -> {
							FModChannel.setPaused(cursor.getChannel(), 1);
						},
						"resume", input0 -> {
							FModChannel.setPaused(cursor.getChannel(), 0);
						},
						"loop", input0 -> {
							
							try (MemoryStack stack0 = MemoryStack.stackPush())
							{
								IntBuffer ip = stack0.mallocInt(10);
								
								long channel = cursor.getChannel();
								FModChannel.getLoopCount(channel, ip);
								FModChannel.setLoopCount(channel, ip.get() == -1 ? 0 : -1);
							}
						}
				);
						
				while (!Thread.interrupted())
				{
					Consumer<Scanner> command = commands.get(input.next().toLowerCase());
					if (command != null) command.accept(input);
				}
			});
			
			thread.join();
			
			FModSystem.release(system);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
		finally
		{
			inputThread.interrupt();
			scheduledExecutor.shutdownNow();
			try
			{
				inputThread.join();
			}
			catch (InterruptedException e)
			{}
		}
	}
	
	public static void printFraction(NodeList.Node node)
	{
		final int max = 30;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer pp = stack.mallocPointer(1);
			IntBuffer ip = stack.mallocInt(1);
			ByteBuffer sp = stack.calloc(100);
			
			long channel = node.getChannel();
			
			FModChannel.isPlaying(channel, ip);
			if (ip.get(0) == 0) return;
			
			FModChannel.getCurrentSound(channel, pp);
			long sound = pp.get(0);
			FModChannel.getPosition(channel, ip, FMOD.FMOD_TIMEUNIT_MS);
			int position = ip.get(0);
			FModChannel.getLoopCount(channel, ip);
			int loopCount = ip.get(0);
			FModSound.getLength(sound, ip, FMOD.FMOD_TIMEUNIT_MS);
			int length = ip.get(0);
			FModSound.getName(sound, sp);
			String name = MemoryUtil.memUTF8(sp);
			
			int secondsp = position / 1000 % 60;
			int minutesp = position / 60000 % 60;
			int hoursp = position / 3600000;
			int secondsl = length / 1000 % 60;
			int minutesl = length / 60000 % 60;
			int hoursl = length / 3600000;
			
			int pre = position * max / length;
			int post = max - pre;
			
			System.out.print("\r\u001B[K\u001B[93m");
			for (char c : name.toCharArray())
			{
				if (c == 0) break;
				System.out.print(c);
			}
			System.out.print("\u001B[m <\u001B[92m");
			System.out.print("=".repeat(pre));
			System.out.print("\u001B[m");
			System.out.print("-".repeat(post));
			System.out.print("> ");
			
			if (hoursl > 0) System.out.print(hoursp + ":");
			String sm = minutesp + "";
			sm = "0".repeat(2 - sm.length()) + sm;
			String ss = secondsp + "";
			ss = "0".repeat(2 - ss.length()) + ss;
			
			System.out.print(sm);
			System.out.print(":");
			System.out.print(ss);
			System.out.print("/");
			
			if (hoursl > 0) System.out.print(hoursl + ":");
			sm = minutesl + "";
			sm = "0".repeat(2 - sm.length()) + sm;
			ss = secondsl + "";
			ss = "0".repeat(2 - ss.length()) + ss;
			
			System.out.print(sm);
			System.out.print(":");
			System.out.print(ss);
			
			if (loopCount == -1) System.out.print(" (loop)");
		}
	}
}
