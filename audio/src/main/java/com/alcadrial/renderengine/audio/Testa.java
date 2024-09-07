package com.alcadrial.renderengine.audio;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.lwjgl.PointerBuffer;
import org.lwjgl.fmod.FMOD;
import org.lwjgl.fmod.FMOD_CREATESOUNDEXINFO;
import org.lwjgl.fmod.FMOD_VECTOR;
import org.lwjgl.system.MemoryStack;

import com.alcadrial.renderengine.audio.bindings.FModChannel;
import com.alcadrial.renderengine.audio.bindings.FModChannelGroup;
import com.alcadrial.renderengine.audio.bindings.FModSound;
import com.alcadrial.renderengine.audio.bindings.FModSystem;

public class Testa {
	
	public static void main(String[] args)
	{
		ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(4);
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer p = stack.mallocPointer(10);
			IntBuffer ip = stack.mallocInt(10);
			LongBuffer lp = stack.mallocLong(10);
			
			FModErrorUtils.assertNoError(FModSystem.create(p, FMOD.FMOD_VERSION), "Failed to create system");
			long system = p.get(0);
			
			FModErrorUtils.assertNoError(FModSystem.init(system, 16, FMOD.FMOD_INIT_NORMAL, 0), "Failed to init system");
			
			FModErrorUtils.assertNoError(FModSystem.getSoftwareFormat(system, ip, null, null), "Failed to retrive software format");
			int sampleRate = ip.get(0);
			
			FMOD_CREATESOUNDEXINFO createSoundInfo = FMOD_CREATESOUNDEXINFO.calloc(stack);
			createSoundInfo.cbsize(FMOD_CREATESOUNDEXINFO.SIZEOF);
			
			FModErrorUtils.assertNoError(FModSystem.createSound(system, stack.UTF8("sound0.ogg"), FMOD.FMOD_LOOP_NORMAL | FMOD.FMOD_3D, createSoundInfo, p), "Failed to create sound0");
			long sound0 = p.get(0);
			FModErrorUtils.assertNoError(FModSystem.createSound(system, stack.UTF8("sound1.ogg"), FMOD.FMOD_LOOP_NORMAL | FMOD.FMOD_3D, createSoundInfo, p), "Failed to create sound1");
			long sound1 = p.get(0);
			
			FModSound.getLength(sound0, ip, FMOD.FMOD_TIMEUNIT_MS);
			int sound0Length = ip.get(0);
			
			FModSound.getLength(sound1, ip, FMOD.FMOD_TIMEUNIT_MS);
			int sound1Length = ip.get(0);
			
			FModSound.setLoopPoints(sound0, 0, FMOD.FMOD_TIMEUNIT_MS, sound0Length, FMOD.FMOD_TIMEUNIT_MS);
			FModSound.setLoopPoints(sound1, 0, FMOD.FMOD_TIMEUNIT_MS, sound1Length, FMOD.FMOD_TIMEUNIT_MS);
			
			FModErrorUtils.assertNoError(FModSystem.createChannelGroup(system, "Group 1", p), "Failed to create channel group");
			long channelGroup = p.get(0);
			
			FModErrorUtils.assertNoError(FModSystem.playSound(system, sound0, channelGroup, 0, p), "Failed to play sound");
			long channel0 = p.get(0);
			FModErrorUtils.assertNoError(FModSystem.playSound(system, sound1, channelGroup, 1, p), "Failed to play sound");
			long channel1 = p.get(0);
			
			FMOD_VECTOR up = FMOD_VECTOR.calloc(stack).set(0, 1, 0);
			FMOD_VECTOR forward = FMOD_VECTOR.calloc(stack).set(0, 0, 1);
			FMOD_VECTOR pos = FMOD_VECTOR.calloc(stack).set(0, 0, 0);
			FMOD_VECTOR vel = FMOD_VECTOR.calloc(stack).set(1, 0, 0);
			
			FModSystem.set3DListenerAttributes(system, 0, pos, vel, forward, up);
			
			FModChannel.set3DAttributes(channel0, pos, vel);
			FModChannel.set3DAttributes(channel1, pos, vel);
			
			FModChannelGroup.setVolume(channelGroup, 2);
			
			int state = 0;
			long curretChannel = channel0;
			
			FModChannelGroup.isPlaying(channelGroup, ip);
			while (ip.get(0) == 1)
			{
				FModChannel.getPosition(curretChannel, ip, FMOD.FMOD_TIMEUNIT_MS);
				int position = ip.get(0);
				
				switch (state)
				{
					case 0 -> {
						if (position > 0.02F * sound0Length)
						{
							state = 1;
							
							FModChannel.getDSPClock(channel0, null, lp);
							long clock = lp.get(0);
							
							FModChannel.addFadePoint(channel0, clock, 1);
							FModChannel.addFadePoint(channel0, (clock + 5 * sampleRate), 0);
							FModChannel.setDelay(channel0, 0, (clock + 5 * sampleRate), 1);
						}
					}
					case 1 -> {
						FModChannel.isPlaying(channel0, ip);
						if (ip.get(0) == 0)
						{
							state = 2;
							
							FModChannel.getDSPClock(channel1, null, lp);
							long clock = lp.get(0);
							
							FModChannel.addFadePoint(channel1, clock, 0);
							FModChannel.addFadePoint(channel1, (clock + 5 * sampleRate), 1);
							FModChannel.setPaused(channel1, 0);
							curretChannel = channel1;
						}
					}
					case 2 -> {
						if (position > 0.5F * sound1Length)
						{
							state = 3;
							
							FModChannel.getDSPClock(channel1, null, lp);
							long clock = lp.get(0);
							
							FModChannel.addFadePoint(channel1, clock, 1);
							FModChannel.addFadePoint(channel1, (clock + 5 * sampleRate), 0);
							FModChannel.setDelay(channel1, 0, (clock + 5 * sampleRate), 1);
						}
					}
				}
				
				printFraction(curretChannel);
				
				FModSystem.update(system);
				Thread.sleep(100);
				FModChannelGroup.isPlaying(channelGroup, ip);
			}
			
			printFraction(curretChannel);
			
			FModErrorUtils.assertNoError(FModSystem.close(system), "Failed to close system");
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
			
			FModChannel.getCurrentSound(channel, pp);
			long sound = pp.get(0);
			FModChannel.getPosition(channel, ip, FMOD.FMOD_TIMEUNIT_MS);
			int position = ip.get(0);
			FModSound.getLength(sound, ip, FMOD.FMOD_TIMEUNIT_MS);
			int length = ip.get(0);
			int sl = length / 1000 % 60;
			int ml = length / 60000 % 60;
			int hl = length / 3600000;
			int sp = position / 1000 % 60;
			int mp = position / 60000 % 60;
			int hp = position / 3600000;
			
			int pre = position * max / length;
			int post = max - pre;
			
			System.out.print("<");
			System.out.print("=".repeat(pre));
			System.out.print("-".repeat(post));
			System.out.print("> ");
			
			if (hl > 0) System.out.print(hp + ":");
			String sm = mp + "";
			sm = "0".repeat(2 - sm.length()) + sm;
			String ss = sp + "";
			ss = "0".repeat(2 - ss.length()) + ss;
			
			System.out.print(sm);
			System.out.print(":");
			System.out.print(ss);
			System.out.print("/");
			
			if (hl > 0) System.out.print(hl + ":");
			sm = ml + "";
			sm = "0".repeat(2 - sm.length()) + sm;
			ss = sl + "";
			ss = "0".repeat(2 - ss.length()) + ss;
			
			System.out.print(sm);
			System.out.print(":");
			System.out.print(ss);
			System.out.print("\r");
		}
	}
}
