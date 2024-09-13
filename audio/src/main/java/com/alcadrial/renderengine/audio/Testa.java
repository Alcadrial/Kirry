package com.alcadrial.renderengine.audio;

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
import java.util.concurrent.Executors;
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

import com.alcadrial.renderengine.audio.bindings.FModChannel;
import com.alcadrial.renderengine.audio.bindings.FModSound;
import com.alcadrial.renderengine.audio.bindings.FModSystem;

public class Testa {
	
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
		}
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Missing argument <path to sounds dir>");
			return;
		}
		
		ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(4);
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer pp = stack.mallocPointer(10);
			IntBuffer ip = stack.mallocInt(10);
			
			FModErrorUtils.assertNoError(FModSystem.create(pp, FMOD.FMOD_VERSION), "Failed to create system");
			long system = pp.get(0);
			
			FModErrorUtils.assertNoError(FModSystem.setCallback(system, (s, type, commandData0, commandData1, userData) -> {
				
				switch (type)
				{
					case FMOD.FMOD_SYSTEM_CALLBACK_DEVICELOST -> {
						System.out.println("DeviceLost");
					}
					case FMOD.FMOD_SYSTEM_CALLBACK_MEMORYALLOCATIONFAILED -> {
						System.out.println("MemoryAllocationFailed");
					}
					case FMOD.FMOD_SYSTEM_CALLBACK_THREADCREATED -> {
						System.out.println("ThreadCreated");
					}
					case FMOD.FMOD_SYSTEM_CALLBACK_BADDSPCONNECTION -> {
						System.out.println("BadDspConnection");
					}
					case FMOD.FMOD_SYSTEM_CALLBACK_PREMIX -> {
						//	System.out.println("PreMix");
					}
					case FMOD.FMOD_SYSTEM_CALLBACK_POSTMIX -> {
						//	System.out.println("PostMix");
					}
					case FMOD.FMOD_SYSTEM_CALLBACK_ERROR -> {
						FMOD_ERRORCALLBACK_INFO error = FMOD_ERRORCALLBACK_INFO.create(commandData0);
						if (error.result() != 0)
						{
							System.err.println("FMod" + error.functionnameString().replace("::", ".") + "(" + error.functionparamsString() + ") returned " + FModErrorUtils.getErrorString(error.result()) + " (" + error.result() + ")");
						}
					}
					case FMOD.FMOD_SYSTEM_CALLBACK_MIDMIX -> {
						//	System.out.println("MidMix");
					}
					case FMOD.FMOD_SYSTEM_CALLBACK_THREADDESTROYED -> {
						System.out.println("ThreadDestroyed");
					}
					case FMOD.FMOD_SYSTEM_CALLBACK_PREUPDATE -> {
						//System.out.println("PreUpdate");
					}
					case FMOD.FMOD_SYSTEM_CALLBACK_RECORDLISTCHANGED -> {
						System.out.println("RecordListChanged");
					}
					case FMOD.FMOD_SYSTEM_CALLBACK_BUFFEREDNOMIX -> {
						System.out.println("BufferedMix");
					}
					case FMOD.FMOD_SYSTEM_CALLBACK_DEVICEREINITIALIZE -> {
						System.out.println("RecordPositionChanged");
					}
					case FMOD.FMOD_SYSTEM_CALLBACK_OUTPUTUNDERRUN -> {
						System.out.println("OutputUnderRun");
					}
					case FMOD.FMOD_SYSTEM_CALLBACK_RECORDPOSITIONCHANGED -> {
						System.out.println("RecordPositionChanged");
					}
				}
				return FMOD.FMOD_OK;
			}, FMOD.FMOD_SYSTEM_CALLBACK_ALL ^ (FMOD.FMOD_SYSTEM_CALLBACK_DEVICELISTCHANGED)), "Failed to register callback");
			
			FModSystem.close(34);
			
			List<Long> sounds = new LinkedList<>();
			int max = 5;
			
			Path directory = Path.of(args[0]);
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, p -> !Files.isHidden(p)))
			{
				FModErrorUtils.assertNoError(FModSystem.init(system, (int) Files.list(directory).count() + 10, FMOD.FMOD_INIT_NORMAL, 0), "Failed to init system");
				
				FMOD_CREATESOUNDEXINFO createSoundInfo = FMOD_CREATESOUNDEXINFO.calloc(stack);
				createSoundInfo.cbsize(FMOD_CREATESOUNDEXINFO.SIZEOF);
				
				int i = 0;
				for (Path path : stream)
				{
					if (i++ >= max) break;
					int error = FModSystem.createSound(system, stack.UTF8(path.toString()), FMOD.FMOD_LOOP_NORMAL | FMOD.FMOD_3D, createSoundInfo, pp);
					
					switch (error)
					{
						case FMOD.FMOD_OK -> {
							sounds.add(pp.get(0));
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
			
			for (long sound : sounds)
			{
				FModSound.getLength(sound, ip, FMOD.FMOD_TIMEUNIT_MS);
				int soundLength = ip.get(0);
				//	FModSound.setLoopPoints(sound, 0, FMOD.FMOD_TIMEUNIT_MS, soundLength, FMOD.FMOD_TIMEUNIT_MS);
				FModSystem.playSound(system, sound, 0, 1, pp);
				channels.add(pp.get(0));
				
				FModChannel.setCallback(pp.get(0), (channelControl, controlType, callbackType, commandData1, commandData2) -> {
					if (controlType == FMOD.FMOD_CHANNELCONTROL_CHANNEL && callbackType == FMOD.FMOD_CHANNELCONTROL_CALLBACK_END)
					{
						System.err.println("Ends");
					}
					System.err.println("Mah");
					return FMOD.FMOD_OK;
				});
				
				if (channels.tail.channel == 0)
				{
					ByteBuffer sp = stack.malloc(100);
					FModSound.getName(sound, sp);
					throw new IllegalStateException("Missing channel for sound " + MemoryUtil.memUTF8(sp));
				}
			}
			
			NodeList.Node cursor = channels.cursor();
			
			FModChannel.setPaused(cursor.getChannel(), 0);
			
			ScheduledFuture<?> printFractionFuture = scheduledExecutor.scheduleAtFixedRate(() -> {
				
				//	printFraction(cursor.getChannel());
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
						}
				);
						
				while (!Thread.interrupted())
				{
					Consumer<Scanner> command = commands.get(input.next().toLowerCase());
					if (command != null) command.accept(input);
				}
			});
			
			thread.join();
			
			FModSystem.close(system);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
		finally
		{
			scheduledExecutor.shutdownNow();
		}
	}
	
	public static void printFraction(long channel)
	{
		final int max = 30;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer pp = stack.mallocPointer(1);
			IntBuffer ip = stack.mallocInt(1);
			ByteBuffer sp = stack.calloc(100);
			
			FModChannel.getCurrentSound(channel, pp);
			long sound = pp.get(0);
			FModChannel.getPosition(channel, ip, FMOD.FMOD_TIMEUNIT_MS);
			int position = ip.get(0);
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
			
			System.out.print("\r");
			for (char c : name.toCharArray())
			{
				if (c == 0) break;
				System.out.print(c);
			}
			System.out.print(" <");
			System.out.print("=".repeat(pre));
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
		}
	}
}
