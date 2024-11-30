package com.alcadrial.renderengine.audio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class StreamUtils {
	
	public static class EchoInputStream extends InputStream {
		
		private InputStream in;
		private OutputStream out;
		
		public EchoInputStream(InputStream in)
		{
			this(in, System.out);
		}
		
		public EchoInputStream(InputStream in, OutputStream out)
		{
			this.in = in;
			this.out = out;
		}
		
		@Override
		public int read() throws IOException
		{
			int i = in.read();
			if (i >= 0) out.write(i);
			return i;
		}
		
		@Override
		public void close() throws IOException
		{
			in.close();
			out.close();
		}
	}
	
	public static class MappingInputStream extends InputStream {
		
		private InputStream in;
		private String[] map;
		private String bufferedInput;
		
		public MappingInputStream(InputStream in)
		{
			this.in = in;
			map = new String[256];
			bufferedInput = "";
		}
		
		public MappingInputStream addControlCharacters(Function<Character, String> factory)
		{
			for (char i = 0; i < 10; i++) addMapping(i, factory.apply(i));
			for (char i = 11; i < 13; i++) addMapping(i, factory.apply(i));
			for (char i = 14; i < 32; i++) addMapping(i, factory.apply(i));
			for (char i = 127; i < 256; i++) addMapping(i, factory.apply(i));
			return this;
		}
		
		public MappingInputStream addControlCharacters(String to)
		{
			for (char i = 0; i < 10; i++) addMapping(i, to);
			for (char i = 11; i < 13; i++) addMapping(i, to);
			for (char i = 14; i < 32; i++) addMapping(i, to);
			for (char i = 127; i < 256; i++) addMapping(i, to);
			return this;
		}
		
		public void addMapping(char from, String to)
		{
			if (from > 0 && from < map.length) map[from] = to;
		}
		
		public void removeMapping(char from)
		{
			if (from > 0 && from < map.length) map[from] = null;
		}
		
		@Override
		public int read() throws IOException
		{
			if (bufferedInput.length() == 0)
			{
				int i = in.read();
				if (i == -1 || map[i] == null) return i;
				bufferedInput = map[i];
			}
			String t = bufferedInput;
			bufferedInput = bufferedInput.substring(1);
			return t.getBytes(StandardCharsets.ISO_8859_1)[0];
		}
		
		@Override
		public void close() throws IOException
		{
			in.close();
		}
	}
	
	public static class LineSeekerInputStream extends InputStream {
		
		private InputStream in;
		private boolean consumeLineFeed;
		private boolean ended;
		
		public LineSeekerInputStream(InputStream in)
		{
			this.in = in;
			consumeLineFeed = false;
			ended = false;
		}
		
		@Override
		public int read() throws IOException
		{
			consumeLineFeed = false;
			int i = in.read();
			if (i == -1) ended = true;
			return i;
		}
		
		public String readLine() throws IOException
		{
			int current = in.read();
			if (current == '\n' && consumeLineFeed) current = in.read();
			consumeLineFeed = false;
			
			if (current == '\n') return "";
			if (current == '\r')
			{
				consumeLineFeed = true;
				return "";
			}
			if (current == -1)
			{
				ended = true;
				return "";
			}
			
			StringBuilder builder = new StringBuilder();
			while (current != '\n' && current != '\r' && current != -1)
			{
				builder.append((char) current);
				current = in.read();
			}
			if (current == '\r') consumeLineFeed = true;
			if (current == -1) ended = true;
			return builder.toString();
		}
		
		public boolean isEnded()
		{
			return ended;
		}
		
		@Override
		public void close() throws IOException
		{
			in.close();
		}
	}
	
	public static class ConsoleInputStream extends SeekerInputStream {
		
		private OutputStream out;
		
		public ConsoleInputStream()
		{
			this(System.in, System.out);
		}
		
		public ConsoleInputStream(InputStream in, OutputStream out)
		{
			super(in);
			this.out = out;
		}
		
		public String readLine(String prompt) throws IOException
		{
			out.write(prompt.getBytes());
			return super.readLine();
		}
		
		protected <T> T persistRead(Function<String, T> parser, String prompt, String error) throws IOException
		{
			if (prompt.length() > 0) out.write(prompt.getBytes());
			int errors = 0;
			String next = readLineSequence(' ');
			while (true)
			{
				try
				{
					return parser.apply(next);
				}
				catch (Exception e)
				{
					if (error.length() > 0)
					{
						if (available() == 0)
						{
							out.write(error.replace("%n", String.valueOf(errors)).getBytes());
							errors = 0;
						}
						else errors++;
					}
					next = readLineSequence(' ');
				}
			}
		}
		
		public long persistReadLong(String prompt, String error) throws IOException
		{
			return persistRead(Long::parseLong, prompt, error);
		}
		
		public long persistReadLong() throws IOException
		{
			return persistReadLong("", "");
		}
		
		private String checkSubsequence(String s, boolean caseSensitive, Collection<String> validSequences)
		{
			String match = null;
			int matches = 0;
			if (!caseSensitive)
			{
				s = s.toLowerCase(Locale.ENGLISH);
				validSequences = validSequences.stream().map(s0 -> s0.toLowerCase(Locale.ENGLISH)).toList();
			}
			for (String string : validSequences) if (string.startsWith(s))
			{
				matches++;
				match = string;
			}
			if (matches == 1) return match;
			throw new RuntimeException();
		}
		
		public String persistReadSequenceSubstring(String prompt, String error, boolean caseSensitive, Collection<String> validSequences) throws IOException
		{
			return persistRead(s -> checkSubsequence(s, caseSensitive, validSequences), prompt, error);
		}
		
		public String persistReadSequenceSubstring(String prompt, String error, Collection<String> validSequences) throws IOException
		{
			return persistReadSequenceSubstring(prompt, error, false, validSequences);
		}
		
		public String persistReadSequenceSubstring(Collection<String> validSequences) throws IOException
		{
			return persistReadSequenceSubstring("", "", validSequences);
		}
		
		@SuppressWarnings("unchecked")
		private <T> T checkSubsequence(String s, boolean caseSensitive, Map<String, T> validSequences)
		{
			T match = null;
			int matches = 0;
			if (!caseSensitive)
			{
				s = s.toLowerCase(Locale.ENGLISH);
				validSequences = Map.ofEntries(validSequences.entrySet().stream().map(e -> Map.entry(e.getKey().toLowerCase(Locale.ENGLISH), e.getValue())).toArray(Entry[]::new));
			}
			for (Entry<String, T> e : validSequences.entrySet()) if (e.getKey().startsWith(s))
			{
				matches++;
				match = e.getValue();
			}
			if (matches == 1) return match;
			throw new RuntimeException();
		}
		
		public <T> T persistMapReadSequenceSubstring(String prompt, String error, boolean caseSensitive, Map<String, T> validSequences) throws IOException
		{
			return persistRead(s -> checkSubsequence(s, caseSensitive, validSequences), prompt, error);
		}
		
		public <T> T persistMapReadSequenceSubstring(String prompt, String error, Map<String, T> validSequences) throws IOException
		{
			return persistMapReadSequenceSubstring(prompt, error, false, validSequences);
		}
		
		public <T> T persistMapReadSequenceSubstring(Map<String, T> validSequences) throws IOException
		{
			return persistMapReadSequenceSubstring("", "", validSequences);
		}
	}
	
	public static class CharSeekerInputStream extends InputStream {
		
		private InputStream in;
		private char delimiter;
		private boolean ended;
		
		public CharSeekerInputStream(InputStream in)
		{
			this('\0', in);
		}
		
		public CharSeekerInputStream(char delimiter, InputStream in)
		{
			this.in = in;
			this.delimiter = delimiter;
			ended = false;
		}
		
		public void setDelimiter(char delimiter)
		{
			this.delimiter = delimiter;
		}
		
		@Override
		public int read() throws IOException
		{
			int i = in.read();
			if (i == -1) ended = true;
			return i;
		}
		
		public String readSequence() throws IOException
		{
			int current = in.read();
			
			if (current == delimiter) return "";
			if (current == -1)
			{
				ended = true;
				return "";
			}
			
			StringBuilder builder = new StringBuilder();
			while (current != delimiter && current != -1)
			{
				builder.append((char) current);
				current = in.read();
			}
			if (current == -1) ended = true;
			return builder.toString();
		}
		
		public boolean isEnded()
		{
			return ended;
		}
		
		@Override
		public void close() throws IOException
		{
			in.close();
		}
	}
	
	public static class SeekerInputStream extends InputStream {
		
		private InputStream in;
		private boolean consumeLineFeed;
		private boolean ended;
		
		public SeekerInputStream(InputStream in)
		{
			this.in = in;
			consumeLineFeed = false;
			ended = false;
		}
		
		public SeekerInputStream skip() throws IOException
		{
			read();
			return this;
		}
		
		@Override
		public int read() throws IOException
		{
			consumeLineFeed = false;
			int i = in.read();
			if (i == -1) ended = true;
			return i;
		}
		
		public byte[] readBytes(int bytes) throws IOException
		{
			byte[] b = new byte[bytes];
			int r = read(b);
			if (r < 0) return new byte[0];
			return Arrays.copyOf(b, r);
		}
		
		public SeekerInputStream skipLine() throws IOException
		{
			readLine();
			return this;
		}
		
		public String readLine() throws IOException
		{
			int current = in.read();
			if (current == '\n' && consumeLineFeed) current = in.read();
			consumeLineFeed = false;
			
			if (current == '\n') return "";
			if (current == '\r')
			{
				consumeLineFeed = true;
				return "";
			}
			if (current == -1)
			{
				ended = true;
				return "";
			}
			
			StringBuilder builder = new StringBuilder();
			while (current != '\n' && current != '\r' && current != -1)
			{
				builder.append((char) current);
				current = in.read();
			}
			if (current == '\r') consumeLineFeed = true;
			if (current == -1) ended = true;
			return builder.toString();
		}
		
		public String readLineSequence(char delimiter) throws IOException
		{
			int current = in.read();
			if (current == '\n' && consumeLineFeed) current = in.read();
			consumeLineFeed = false;
			
			if (current == '\n') return "";
			if (current == '\r')
			{
				consumeLineFeed = true;
				return "";
			}
			if (current == delimiter) return "";
			if (current == -1)
			{
				ended = true;
				return "";
			}
			
			StringBuilder builder = new StringBuilder();
			while (current != '\n' && current != '\r' && current != delimiter && current != -1)
			{
				builder.append((char) current);
				current = in.read();
			}
			if (current == '\r') consumeLineFeed = true;
			if (current == -1) ended = true;
			return builder.toString();
		}
		
		/**
		 * Continue reading until the char delimiter is found, consuming it.
		 * 
		 * @param delimiter
		 * @return this stream for chaining
		 * @throws IOException
		 */
		public SeekerInputStream skipSequence(char delimiter) throws IOException
		{
			readSequence(delimiter);
			return this;
		}
		
		/**
		 * Continue reading until the char delimiter is found, consuming it without appending it into the returned string.
		 * 
		 * @param delimiter
		 * @return the string of characters until the delimiter (excluded)
		 * @throws IOException
		 */
		public String readSequence(char delimiter) throws IOException
		{
			int current = in.read();
			
			if (current == delimiter) return "";
			if (current == -1)
			{
				ended = true;
				return "";
			}
			
			StringBuilder builder = new StringBuilder();
			while (current != delimiter && current != -1)
			{
				builder.append((char) current);
				current = in.read();
			}
			if (current == -1) ended = true;
			return builder.toString();
		}
		
		public boolean isEnded()
		{
			return ended;
		}
		
		@Override
		public void close() throws IOException
		{
			ended = true;
			in.close();
		}
	}
	
	public static class EndlessSeekerInputStream extends SeekerInputStream {
		
		public EndlessSeekerInputStream(InputStream in)
		{
			super(in);
		}
		
		@Override
		public boolean isEnded()
		{
			return false;
		}
	}
	
	public static class SplitOutputStream extends OutputStream {
		
		private OutputStream[] outs;
		
		public SplitOutputStream(OutputStream... outs)
		{
			this.outs = outs;
		}
		
		@Override
		public void write(int b) throws IOException
		{
			for (OutputStream out : outs) out.write(b);
		}
		
		@Override
		public void flush() throws IOException
		{
			for (OutputStream out : outs) out.flush();
		}
		
		@Override
		public void close() throws IOException
		{
			for (OutputStream out : outs) out.close();
		}
	}
	
	public static class MappingOutputStream extends OutputStream {
		
		private OutputStream out;
		private String[] map;
		
		public MappingOutputStream(OutputStream out)
		{
			this.out = out;
			map = new String[256];
		}
		
		public MappingOutputStream addControlCharacters(Function<Character, String> factory)
		{
			for (char i = 0; i < 10; i++) addMapping(i, factory.apply(i));
			for (char i = 11; i < 13; i++) addMapping(i, factory.apply(i));
			for (char i = 14; i < 32; i++) addMapping(i, factory.apply(i));
			for (char i = 127; i < 256; i++) addMapping(i, factory.apply(i));
			return this;
		}
		
		public MappingOutputStream addControlCharacters(String to)
		{
			for (char i = 0; i < 10; i++) addMapping(i, to);
			for (char i = 11; i < 13; i++) addMapping(i, to);
			for (char i = 14; i < 32; i++) addMapping(i, to);
			for (char i = 127; i < 256; i++) addMapping(i, to);
			return this;
		}
		
		public MappingOutputStream addMapping(char from, String to)
		{
			if (from >= 0 && from < map.length) map[from] = to;
			return this;
		}
		
		public MappingOutputStream removeMapping(char from)
		{
			if (from >= 0 && from < map.length) map[from] = null;
			return this;
		}
		
		@Override
		public void write(int i) throws IOException
		{
			if (map[i & 0xFF] != null) out.write(map[i & 0xFF].getBytes());
			else out.write(i);
		}
		
		@Override
		public void close() throws IOException
		{
			out.close();
		}
	}
	
	public static class FilterInputStream extends InputStream {
		
		private InputStream in;
		private SortedMap<String, String> map;
		private String input;
		private StringBuilder output;
		private boolean isClosed;
		
		public FilterInputStream(InputStream in)
		{
			this.in = in;
			map = new TreeMap<>();
			input = "";
			output = new StringBuilder();
			isClosed = false;
		}
		
		public FilterInputStream addControlCharacters(Function<Character, String> factory)
		{
			for (char i = 0; i < 32; i++) addMapping(factory.apply(i), String.valueOf(i));
			return this;
		}
		
		public FilterInputStream addCharacters(Function<Character, String> factory)
		{
			for (char i = 0; i < 256; i++) addMapping(factory.apply(i), String.valueOf(i));
			return this;
		}
		
		public FilterInputStream addMapping(String token, String replace)
		{
			for (Entry<String, String> e : map.headMap(token.substring(0, token.length() - 1) + (token.charAt(token.length() - 1) + 1)).sequencedEntrySet())
			{
				if (token.compareTo(e.getKey()) == 0)
				{
					map.put(token, replace);
					return this;
				}
				if (e.getKey().startsWith(token) || token.startsWith(e.getKey()))
				{
					map.remove(e.getKey());
					map.put(token, replace);
					return this;
				}
			}
			map.put(token, replace);
			return this;
		}
		
		public FilterInputStream removeMapping(String token)
		{
			map.remove(token);
			return this;
		}
		
		@Override
		public int read() throws IOException
		{
			if (isClosed) return -1;
			if (!output.isEmpty())
			{
				char c = output.charAt(output.length() - 1);
				output.setLength(output.length() - 1);
				return c;
			}
			while (output.isEmpty())
			{
				int i = in.read();
				if (i < 0)
				{
					isClosed = true;
					return -1;
				}
				String base = input;
				input += (char) i;
				
				boolean useless = true;
				
				for (Entry<String, String> e : map.headMap(base + (char) (i + 1)).sequencedEntrySet())
				{
					if (input.compareTo(e.getKey()) == 0)
					{
						output.append(e.getValue());
						input = "";
						useless = false;
						break;
					}
					if (e.getKey().startsWith(input)) useless = false;
				}
				if (useless)
				{
					output.append(input);
					input = "";
				}
			}
			return read();
		}
		
		@Override
		public void close() throws IOException
		{
			in.close();
			isClosed = true;
		}
	}
	
	public static class EventOutputStream extends OutputStream {
		
		private OutputStream output;
		private Consumer<Integer> action;
		
		public EventOutputStream(OutputStream output, Consumer<Integer> action)
		{
			this.output = output;
			this.action = action;
		}
		
		@Override
		public void write(int b) throws IOException
		{
			action.accept(b);
			output.write(b);
		}
	}
	
	public static class ForwardOutputStream extends OutputStream {
		
		private Supplier<OutputStream> next;
		
		public ForwardOutputStream(Supplier<OutputStream> next)
		{
			this.next = next;
		}
		
		@Override
		public void write(int b) throws IOException
		{
			OutputStream s = next.get();
			if (s != null) s.write(b);
		}
	}
	
	public static class ForwardInputStream extends InputStream {
		
		private Supplier<InputStream> next;
		
		public ForwardInputStream(Supplier<InputStream> next)
		{
			this.next = next;
		}
		
		@Override
		public int read() throws IOException
		{
			InputStream s = next.get();
			return s != null ? s.read() : -1;
		}
	}
	
	public static class ShellStream {
		
		private SeekerInputStream input;
		private PrintStream output;
		
		public ShellStream(SeekerInputStream input, PrintStream output)
		{
			this.input = input;
			this.output = output;
		}
		
		public ShellStream skip() throws IOException
		{
			input.skip();
			return this;
		}
		
		public ShellStream skip(long n) throws IOException
		{
			input.skip(n);
			return this;
		}
		
		public byte[] readBytes(int bytes) throws IOException
		{
			return input.readBytes(bytes);
		}
		
		public ShellStream skipLine() throws IOException
		{
			input.skipLine();
			return this;
		}
		
		public String readLine() throws IOException
		{
			return input.readLine();
		}
		
		public String readLineSequence(char delimiter) throws IOException
		{
			return input.readLineSequence(delimiter);
		}
		
		public ShellStream skipSequence(char delimiter) throws IOException
		{
			input.skipSequence(delimiter);
			return this;
		}
		
		public String readSequence(char delimiter) throws IOException
		{
			return input.readSequence(delimiter);
		}
		
		public boolean isEnded()
		{
			return input.isEnded();
		}
		
		public byte[] readNBytes(int len) throws IOException
		{
			return input.readNBytes(len);
		}
		
		public int readNBytes(byte[] b, int off, int len) throws IOException
		{
			return input.readNBytes(b, off, len);
		}
		
		public void flush()
		{
			output.flush();
		}
		
		public void close() throws IOException
		{
			output.close();
			input.close();
		}
		
		public void write(int b)
		{
			output.write(b);
		}
		
		public void writeBytes(byte[] buf)
		{
			output.writeBytes(buf);
		}
		
		public void print(boolean b)
		{
			output.print(b);
		}
		
		public void print(char c)
		{
			output.print(c);
		}
		
		public void print(int i)
		{
			output.print(i);
		}
		
		public void print(long l)
		{
			output.print(l);
		}
		
		public void print(float f)
		{
			output.print(f);
		}
		
		public void print(double d)
		{
			output.print(d);
		}
		
		public void print(char[] s)
		{
			output.print(s);
		}
		
		public void print(String s)
		{
			output.print(s);
		}
		
		public void print(Object obj)
		{
			output.print(obj);
		}
		
		public void println()
		{
			output.println();
		}
		
		public void println(boolean x)
		{
			output.println(x);
		}
		
		public void println(char x)
		{
			output.println(x);
		}
		
		public void println(int x)
		{
			output.println(x);
		}
		
		public void println(long x)
		{
			output.println(x);
		}
		
		public void println(float x)
		{
			output.println(x);
		}
		
		public void println(double x)
		{
			output.println(x);
		}
		
		public void println(char[] x)
		{
			output.println(x);
		}
		
		public void println(String x)
		{
			output.println(x);
		}
		
		public void println(Object x)
		{
			output.println(x);
		}
		
		public ShellStream printf(String format, Object... args)
		{
			output.printf(format, args);
			return this;
		}
		
		public ShellStream printf(Locale l, String format, Object... args)
		{
			output.printf(l, format, args);
			return this;
		}
		
		public ShellStream format(String format, Object... args)
		{
			output.format(format, args);
			return this;
		}
		
		public ShellStream format(Locale l, String format, Object... args)
		{
			output.format(l, format, args);
			return this;
		}
		
		public ShellStream append(CharSequence csq)
		{
			output.append(csq);
			return this;
		}
		
		public ShellStream append(CharSequence csq, int start, int end)
		{
			output.append(csq, start, end);
			return this;
		}
		
		public ShellStream append(char c)
		{
			output.append(c);
			return this;
		}
		
		public SeekerInputStream getInput()
		{
			return input;
		}
		
		public PrintStream getOutput()
		{
			return output;
		}
	}
}
