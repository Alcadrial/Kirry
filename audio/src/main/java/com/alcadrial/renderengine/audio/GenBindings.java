package com.alcadrial.renderengine.audio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.lwjgl.fmod.FMOD;

public class GenBindings {
	
	public GenBindings()
	{}
	
	public static class Data {
		
		private RandomAccessFile clazzFile;
		
		private Map<String, StringBuilder> functions;
		
		Data(RandomAccessFile clazzFile, Map<String, StringBuilder> functions)
		{
			this.clazzFile = clazzFile;
			this.functions = functions;
		}
		
		public RandomAccessFile getClazzFile()
		{
			return clazzFile;
		}
		
		public Map<String, StringBuilder> getFunctions()
		{
			return functions;
		}
	}
	
	public static final Map<String, String> NAME_MAP = Map.ofEntries(
			Map.entry("offsettype", "offsetType"),
			Map.entry("insideconeangle", "insideConeAngle"),
			Map.entry("outsideconeangle", "outsideConeAngle"),
			Map.entry("outsidevolume", "outsideVolume"),
			Map.entry("numpoints", "numPoints"),
			Map.entry("lengthtype", "lengthType"),
			Map.entry("loopcount", "loopCount"),
			Map.entry("loopstart", "loopStart"),
			Map.entry("loopstarttype", "loopStartType"),
			Map.entry("loopend", "loopEnd"),
			Map.entry("loopendtype", "loopEndType"),
			Map.entry("namelen", "nameLen"),
			Map.entry("numsubsounds", "numSubSounds"),
			Map.entry("numsyncpoints", "numSyncPoints"),
			Map.entry("numtags", "numTags"),
			Map.entry("numtagsupdated", "numTagsUpdated"),
			Map.entry("openstate", "openState"),
			Map.entry("percentbuffered", "percentBuffered"),
			Map.entry("diskbusy", "diskBusy"),
			Map.entry("soundgroup", "soundGroup"),
			Map.entry("subsound", "subSound"),
			Map.entry("parentsound", "parentSound"),
			Map.entry("maxaudible", "maxAudible")
	);
	
	public static void main(String[] args)
	{
		Map<String, Data> classData = new HashMap<>();
		
		Method[] declaredMethods = FMOD.class.getDeclaredMethods();
		Arrays.sort(declaredMethods, (m0, m1) -> m0.getName().compareTo(m1.getName()));
		for (Method method : declaredMethods)
		{
			if ((method.getModifiers() & Modifier.STATIC) == 0) continue;
			if ((method.getModifiers() & Modifier.PUBLIC) == 0) continue;
			if (!method.getName().startsWith("FMOD")) continue;
			
			String methodReturnType = method.getReturnType().toString();
			List<String> methodParameters = new ArrayList<>(method.getParameters().length);
			
			String fullName = method.getName();
			String name = fullName.substring(5);
			int clazzNameEnd = name.indexOf("_");
			if (clazzNameEnd == -1)
			{
				try
				{
					processMethod(classData, method, fullName, "Common", "common", name, methodReturnType, methodParameters);
					continue;
				}
				catch (IOException e)
				{
					e.printStackTrace(System.out);
					break;
				}
			}
			String clazzName = name.substring(0, clazzNameEnd);
			String clazzVarName = clazzName.substring(0, 1).toLowerCase() + clazzName.substring(1);
			String methodName = name.substring(clazzNameEnd + 1);
			methodName = methodName.substring(0, 1).toLowerCase() + methodName.substring(1);
			
			System.out.println(method);
			System.out.println(clazzName + " ".repeat(20 - clazzName.length()) + methodName);
			
			if (!method.getName().equals("FMOD_System_Create")) methodParameters.add(clazzVarName);
			else clazzVarName = null;
			try
			{
				processMethod(classData, method, fullName, clazzName, clazzVarName, methodName, methodReturnType, methodParameters);
			}
			catch (IOException e)
			{
				e.printStackTrace(System.out);
				break;
			}
		}
		
		for (Data data : classData.values())
		{
			try (RandomAccessFile clazzFile = data.getClazzFile())
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
	
	private static void processMethod(Map<String, Data> classData, Method method, String fullName, String clazzName, @Nullable String clazzVarName, String methodName, String methodReturnType, List<String> methodParameters) throws IOException, FileNotFoundException
	{
		RandomAccessFile clazzFile;
		Map<String, StringBuilder> functions;
		
		if (clazzName.equals("Debug") || clazzName.equals("File") || clazzName.equals("Memory") || clazzName.equals("Thread"))
		{
			clazzName = "Common";
		}
		
		if (classData.containsKey(clazzName))
		{
			Data data = classData.get(clazzName);
			clazzFile = data.getClazzFile();
			functions = data.getFunctions();
		}
		else
		{
			Files.createDirectories(Path.of("src/gen/java/com/alcadrial/renderengine/audio/bindings"));
			Files.deleteIfExists(Path.of("src/gen/java/com/alcadrial/renderengine/audio/bindings/FMod" + clazzName + ".java"));
			clazzFile = new RandomAccessFile("src/gen/java/com/alcadrial/renderengine/audio/bindings/FMod" + clazzName + ".java", "rwd");
			functions = new HashMap<>();
			if (clazzName.equals("Channel") || clazzName.equals("ChannelGroup"))
			{
				parseDocs("ChannelControl", functions);
			}
			parseDocs(clazzName, functions);
			String clazzJavadoc = buildParamTag(functions.get("clazz"), 0, null).toString()
				.replaceAll("<p>", "")
				.replaceAll("</p>", "\n * <p>\n * ")
				.replaceAll("<ul>", "<ul>\n * ")
				.replaceAll("</ul>", "</ul>\n * ")
				.replaceAll("<li>", "    <li>")
				.replaceAll("</li>", "</li>\n * ");
			
			clazzFile.write("""
				package com.alcadrial.renderengine.audio.bindings;
				
				import org.lwjgl.fmod.FMOD;
				
				/**
				 * %2$s
				 * @author FMOD team, autogenerated by Alcadrial
				 */
				public class FMod%1$s {
				
					private FMod%1$s()
					{
				
					}
				
				""".formatted(clazzName, clazzJavadoc).getBytes());
			
			classData.put(clazzName, new Data(clazzFile, functions));
		}
		StringBuilder s = functions.get(methodName);
		
		String javadocString = s == null ? "" : buildJavadoc(s, clazzVarName, methodParameters).toString().replaceAll("\\n", "\n\t");
		
		String methodParameterNames = "";
		String methodParameters0 = "";
		
		if (!methodParameters.isEmpty())
		{
			for (int i = 0; i < methodParameters.size() && i < method.getParameters().length; i++) methodParameterNames += methodParameters.get(i) + ", ";
			methodParameterNames = methodParameterNames.substring(0, methodParameterNames.length() - 2);
			for (int i = 0; i < methodParameters.size() && i < method.getParameters().length; i++) methodParameters0 += method.getParameters()[i].getType().getCanonicalName() + " " + methodParameters.get(i) + ", ";
			methodParameters0 = methodParameters0.substring(0, methodParameters0.length() - 2);
		}
		
		clazzFile.write("""
			\t/**%6$s
				 */
				public static %1$s %2$s(%3$s)
				{
					return FMOD.%4$s(%5$s);
				}
			
			""".formatted(methodReturnType, methodName, methodParameters0, fullName, methodParameterNames, javadocString).getBytes());
	}
	
	private static void parseDocs(String clazzName, Map<String, StringBuilder> functions) throws IOException, FileNotFoundException
	{
		//https://d1s9dnlmdewoh1.cloudfront.net/2.03/api/core-api-<clazzName.toLowerCase()>.html
		String path = "docs/FMod" + clazzName + "Docs.html";
		if (!Files.isRegularFile(Path.of(path))) return;
		try (RandomAccessFile docsFile = new RandomAccessFile(path, "rwd"))
		{
			String functionName = "clazz";
			functions.put(functionName, new StringBuilder(0));
			StringBuilder s = new StringBuilder();
			String line = docsFile.readLine();
			while (line != null)
			{
				if (line.contains("<h2"))
				{
					if (functionName != null)
					{
						System.out.println("Found " + functionName);
						functions.put(functionName, s);
					}
					s = new StringBuilder(s.capacity());
					if (line.contains("api=\"function\""))
					{
						if (line.contains("System_Create")) functionName = "create";
						else functionName = line.substring(line.indexOf("::") + 2, line.indexOf("</a>"));
					}
					else functionName = null;
					line = docsFile.readLine();
					continue;
				}
				//	line = line.replaceAll("<a[^>]*>([^:<]+)::([^<]+)</a>", "{@link FMod$1#$2}");
				//	line = line.replaceAll("<a(?:[^>])*href=\\\"([^\"]*)\\\"[^>]*>([^<]*)</a>", "<a href=\"https://www.fmod.com/docs/2.03/api/$1\">$2</a>");
				line = line.replaceAll("\\*/", "{@literal *}/");
				s.append(line);
				line = docsFile.readLine();
			}
			functions.put(functionName, s);
		}
	}
	
	public static StringBuilder buildJavadoc(StringBuilder functionDocs, @Nullable String clazzVarName, List<String> methodParameters)
	{
		StringBuilder javadoc = new StringBuilder();
		StringBuilder javadocParams = clazzVarName != null ? new StringBuilder("\n * @param ").append(clazzVarName) : new StringBuilder();
		StringBuilder line = new StringBuilder();
		
		int tagDeep = 0;
		int removeDeep = Integer.MAX_VALUE;
		boolean paragraph = true;
		
		for (int tagStartIndex = functionDocs.indexOf("<"), dataEndIndex; tagStartIndex != -1; tagStartIndex = dataEndIndex)
		{
			int tagAttributesIndex = functionDocs.indexOf(" ", tagStartIndex + 1);
			int tagEndIndex = functionDocs.indexOf(">", tagStartIndex + 1);
			dataEndIndex = functionDocs.indexOf("<", tagEndIndex + 1);
			
			boolean closeTag = functionDocs.charAt(tagStartIndex + 1) == '/';
			boolean autoCloseTag = functionDocs.charAt(tagEndIndex - 1) == '/';
			boolean hasAttributes = tagAttributesIndex != -1 && tagAttributesIndex < tagEndIndex;
			boolean hasData = tagEndIndex + 1 != dataEndIndex;
			
			int tagStartInternalIndex = tagStartIndex + (closeTag ? 2 : 1);
			int tagEndInternalIndex = tagEndIndex - (autoCloseTag ? 1 : 0);
			
			if (closeTag) tagDeep--;
			else if (!autoCloseTag) tagDeep++;
			
			if (tagDeep >= removeDeep) continue;
			if (tagDeep == removeDeep - 1)
			{
				removeDeep = Integer.MAX_VALUE;
				continue;
			}
			
			String tagName = functionDocs.substring(tagStartInternalIndex, hasAttributes ? tagAttributesIndex : tagEndIndex);
			String tagAttributes;
			String data;
			switch (tagName)
			{
				case "p" -> {
					if (!line.isEmpty())
					{
						javadoc.append("\n * ").append(line);
						javadoc.append("\n * <p>");
						line = new StringBuilder(line.capacity());
						paragraph = true;
					}
					else if (closeTag)
					{
						if (!paragraph) javadoc.append("\n * <p>");
						paragraph = true;
					}
					if (hasData)
					{
						javadoc.append("\n * ").append(functionDocs, tagEndIndex + 1, dataEndIndex != -1 ? dataEndIndex : functionDocs.length());
						paragraph = false;
					}
				}
				case "a" -> {
					if (closeTag)
					{
						if (hasData)
						{
							paragraph = false;
							line.append(functionDocs, tagEndIndex + 1, dataEndIndex);
						}
						continue;
					}
					paragraph = false;
					if (hasData)
					{
						data = functionDocs.substring(tagEndIndex + 1, dataEndIndex);
						if (data.equals("System_Create")) data = "System::create";
						
						int functionNameIndex = data.indexOf("::");
						if (functionNameIndex != -1 && !data.startsWith("FMOD_") && data.indexOf("::", functionNameIndex + 2) == -1)
						{
							appendLink(line, data, functionNameIndex);
						}
						else if (hasAttributes)
						{
							tagAttributes = functionDocs.substring(tagAttributesIndex + 1, tagEndInternalIndex);
							int linkStartIndex = tagAttributes.indexOf("href=\"");
							if (linkStartIndex != -1)
							{
								linkStartIndex += 6;
								int linkEndIndex = tagAttributes.indexOf("\"", linkStartIndex);
								line.append("<a href=\"https://www.fmod.com/docs/2.03/api/").append(tagAttributes, linkStartIndex, linkEndIndex).append("\">").append(data).append("</a>");
							}
						}
					}
				}
				case "span" -> {
					if (hasData)
					{
						paragraph = false;
						line.append(functionDocs, tagEndIndex + 1, dataEndIndex);
					}
				}
				case "dl" -> {
					continue;
				}
				case "dt" -> {
					if (closeTag) continue;
					
					data = functionDocs.substring(tagEndIndex + 1, dataEndIndex).trim();
					int[] next = new int[1];
					StringBuilder internalTag = buildParamTag(functionDocs, dataEndIndex, next);
					methodParameters.add(NAME_MAP.getOrDefault(data, data));
					javadocParams.append("\n * @param ").append(NAME_MAP.getOrDefault(data, data)).append(" ");
					if (!internalTag.isEmpty()) javadocParams.append(internalTag).append(" ");
					dataEndIndex = next[0];
				}
				case "dd" -> {
					if (closeTag) continue;
					int[] next = new int[1];
					javadocParams.append(buildParamTag(functionDocs, tagStartIndex, next));
					dataEndIndex = next[0];
				}
				default -> {
					if (hasAttributes)
					{
						tagAttributes = functionDocs.substring(tagAttributesIndex + 1, tagEndInternalIndex);
						
						if (tagAttributes.contains("class=\"language-selector\"") || tagAttributes.contains("language-cpp") || tagAttributes.contains("language-csharp") || tagAttributes.contains("language-javascript"))
						{
							removeDeep = tagDeep;
							continue;
						}
						
						if (tagAttributes.contains("class=\"highlight language-c\""))
						{
							int[] next = new int[1];
							line.append(buildCode(functionDocs, tagStartIndex, next));
							dataEndIndex = next[0];
							paragraph = false;
							continue;
						}
					}
					paragraph = false;
					line.append(functionDocs, tagStartIndex, dataEndIndex != -1 ? dataEndIndex : functionDocs.length());
				}
			}
		}
		javadoc.append(javadocParams);
		return javadoc;
	}
	
	public static StringBuilder buildCode(StringBuilder functionDocs, int tagStartIndex, int[] nextDataEndIndex)
	{
		StringBuilder line = new StringBuilder();
		
		int tagDeep = 0;
		
		for (int dataEndIndex; tagStartIndex != -1; tagStartIndex = dataEndIndex)
		{
			int tagAttributesIndex = functionDocs.indexOf(" ", tagStartIndex + 1);
			int tagEndIndex = functionDocs.indexOf(">", tagStartIndex + 1);
			dataEndIndex = functionDocs.indexOf("<", tagEndIndex + 1);
			
			boolean closeTag = functionDocs.charAt(tagStartIndex + 1) == '/';
			boolean autoCloseTag = functionDocs.charAt(tagEndIndex - 1) == '/';
			boolean hasAttributes = tagAttributesIndex != -1 && tagAttributesIndex < tagEndIndex;
			
			int tagStartInternalIndex = tagStartIndex + (closeTag ? 2 : 1);
			int tagEndInternalIndex = tagEndIndex - (autoCloseTag ? 1 : 0);
			
			if (closeTag) tagDeep--;
			else if (!autoCloseTag) tagDeep++;
			
			if (tagDeep <= 0)
			{
				nextDataEndIndex[0] = dataEndIndex;
				return line;
			}
			
			String tagName = functionDocs.substring(tagStartInternalIndex, hasAttributes ? tagAttributesIndex : tagEndIndex);
			String tagAttributes;
			String data;
			
			switch (tagName)
			{
				case "pre" -> {
					if (closeTag) line.append("</code></pre>");
					else line.append("<pre><code>\n * ").append(functionDocs, tagEndIndex + 1, dataEndIndex);
				}
				case "span" -> {
					data = functionDocs.substring(tagEndIndex + 1, dataEndIndex);
					
					if (data.equals(");"))
					{
						line.append("\n * ");
						line.append(data);
						line.append("\n * ");
					}
					else
					{
						line.append(data);
						if (data.equals(",") || data.equals("(")) line.append("\n * ");
					}
				}
				case "a" -> {
					data = functionDocs.substring(tagEndIndex + 1, dataEndIndex);
					if (data.equals("System_Create")) data = "System::create";
					
					if (closeTag)
					{
						line.append(data);
						continue;
					}
					tagAttributes = functionDocs.substring(tagAttributesIndex + 1, tagEndInternalIndex);
					
					int functionNameIndex = data.indexOf("::");
					if (functionNameIndex != -1 && !data.startsWith("FMOD_") && data.indexOf("::", functionNameIndex + 2) == -1)
					{
						appendLink(line, data, functionNameIndex);
					}
					else if (hasAttributes)
					{
						int linkStartIndex = tagAttributes.indexOf("href=\"");
						if (linkStartIndex != -1)
						{
							linkStartIndex += 6;
							int linkEndIndex = tagAttributes.indexOf("\"", linkStartIndex);
							line.append("<a href=\"https://www.fmod.com/docs/2.03/api/").append(tagAttributes, linkStartIndex, linkEndIndex).append("\">").append(data).append("</a>");
						}
					}
				}
				default -> {
					line.append(functionDocs, tagEndIndex + 1, dataEndIndex);
				}
			}
		}
		nextDataEndIndex[0] = -1;
		return line;
	}
	
	public static StringBuilder buildParamTag(StringBuilder functionDocs, int tagStartIndex, int @Nullable[] nextDataEndIndex)
	{
		StringBuilder tokens = new StringBuilder();
		StringBuilder line = new StringBuilder();
		
		int tagDeep = 0;
		
		for (int dataEndIndex; tagStartIndex != -1; tagStartIndex = dataEndIndex)
		{
			int tagAttributesIndex = functionDocs.indexOf(" ", tagStartIndex + 1);
			int tagEndIndex = functionDocs.indexOf(">", tagStartIndex + 1);
			dataEndIndex = functionDocs.indexOf("<", tagEndIndex + 1);
			
			boolean closeTag = functionDocs.charAt(tagStartIndex + 1) == '/';
			boolean autoCloseTag = functionDocs.charAt(tagEndIndex - 1) == '/';
			boolean hasAttributes = tagAttributesIndex != -1 && tagAttributesIndex < tagEndIndex;
			
			int tagStartInternalIndex = tagStartIndex + (closeTag ? 2 : 1);
			int tagEndInternalIndex = tagEndIndex - (autoCloseTag ? 1 : 0);
			
			if (closeTag) tagDeep--;
			else if (!autoCloseTag) tagDeep++;
			
			if (tagDeep <= 0)
			{
				if (nextDataEndIndex != null) nextDataEndIndex[0] = dataEndIndex;
				if (!tokens.isEmpty())
				{
					line.append("(<i>").append(tokens, 0, tokens.length() - 2).append("</i>)");
					tokens = new StringBuilder();
				}
				return line;
			}
			
			String tagName = functionDocs.substring(tagStartInternalIndex, hasAttributes ? tagAttributesIndex : tagEndIndex);
			String tagAttributes;
			String data;
			
			switch (tagName)
			{
				case "a" -> {
					data = functionDocs.substring(tagEndIndex + 1, dataEndIndex);
					if (data.equals("System_Create")) data = "System::create";
					
					if (closeTag)
					{
						line.append(data);
						continue;
					}
					tagAttributes = functionDocs.substring(tagAttributesIndex + 1, tagEndInternalIndex);
					
					int functionNameIndex = data.indexOf("::");
					if (functionNameIndex != -1 && !data.startsWith("FMOD_") && data.indexOf("::", functionNameIndex + 2) == -1)
					{
						appendLink(line, data, functionNameIndex);
					}
					else if (hasAttributes)
					{
						if (tagAttributes.contains("class=\"token\""))
						{
							tokens.append(data).append(", ");
						}
						else
						{
							int linkStartIndex = tagAttributes.indexOf("href=\"");
							if (linkStartIndex != -1)
							{
								linkStartIndex += 6;
								int linkEndIndex = tagAttributes.indexOf("\"", linkStartIndex);
								line.append("<a href=\"https://www.fmod.com/docs/2.03/api/").append(tagAttributes, linkStartIndex, linkEndIndex).append("\">").append(data).append("</a>");
							}
						}
					}
				}
				default -> {
					line.append(functionDocs, tagStartIndex, dataEndIndex != -1 ? dataEndIndex : functionDocs.length());
				}
			}
		}
		if (nextDataEndIndex != null) nextDataEndIndex[0] = -1;
		return line;
	}
	
	private static void appendLink(StringBuilder line, String data, int functionNameIndex)
	{
		if (data.startsWith("ChannelControl"))
		{
			line.append("{@link FModChannel#").append(data, functionNameIndex + 2, data.length()).append("}");
			return;
		}
		line.append("{@link FMod").append(data, 0, functionNameIndex).append("#").append(Character.toLowerCase(data.charAt(functionNameIndex + 2))).append(data, functionNameIndex + 3, data.length()).append("}");
	}
}
