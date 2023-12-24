package com.alcadrial.renderengine.render.shader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.util.FileUtils;
import com.google.gson.stream.JsonReader;

public class ShaderInfo {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	public static ShaderInfo fromJson(File json) throws FileNotFoundException, IOException
	{
		FileUtils.assertExtension(json, "json");
		return fromJson(new FileInputStream(json));
	}
	
	/**
	 * The json must be as follows:
	 * <pre>
	 * {
	 *     "entryPoint": "main",                      // The starting point of the shader                            [REQUIRED]
	 *     "type": "{vert|tesc|tese|geom|frag|comp}",                                                                [REQUIRED]
	 *     "inputs": [                                // A list of 0 or more inputs                                  [REQUIRED]
	 *         {
	 *             "location": 0,                     // The location of the shader's input variable                 [REQUIRED]
	 *             "type": "vec4",                    // The type of the shader's input variable                     [REQUIRED]
	 *             "name": "color"                    // The name of the shader's input variable                     [REQUIRED]
	 *         }
	 *     ],
	 *     "uniformsBlocks": [                        // A list of 0 or more uniform blocks                          [REQUIRED]
	 *         {
	 *             "binding": 0,                      // The binding of the block                                    [REQUIRED]
	 *             "name": "block"                    // The name of the block                                       [OPTIONAL]
	 *             "variables": [                     // A list of uniform variables                                 [REQUIRED]
	 *                 {
	 *                     "type": "mat4",            // The type of the uniform variable                            [REQUIRED]
	 *                     "name": "TRANSFORMATION"   // The name of the uniform variable                            [REQUIRED]
	 *                 }
	 *             ]
	 *         }
	 *     ],
	 *     "bindingsDescriptorTypes": {               // A map between the bindings and the descriptor type required [REQUIRED]
	 *         "0": "uniformBuffer"                   // The binding value and the descriptor type                   [REQUIRED]
	 *     }
	 * }
	 * </pre>
	 * 
	 * @param json
	 * @throws IOException
	 */
	public static ShaderInfo fromJson(InputStream json) throws IOException
	{
		ShaderType type = null;
		String entryPoint = null;
		Map<ShaderInput, Integer> inputs = null;
		
		List<ShaderUniformBlock> uniformBlocks = null;
		
		Map<Integer, ShaderDescriptorType> bindingDescriptorTypeMap = null;
		
		try (JsonReader reader = new JsonReader(new InputStreamReader(json)))
		{
			reader.beginObject();
			while (reader.hasNext())
			{
				switch (reader.nextName())
				{
					case "entryPoint" -> entryPoint = reader.nextString();
					case "type" -> type = ShaderType.fromExtension(reader.nextString());
					case "inputs" ->
					{
						if (inputs != null) inputs.clear();
						inputs = new LinkedHashMap<>();
						
						reader.beginArray();
						while (reader.hasNext())
						{
							Integer location = null;
							ShaderInputType inputType = null;
							String name = null;
							
							reader.beginObject();
							while (reader.hasNext())
							{
								switch (reader.nextName())
								{
									case "location" -> location = reader.nextInt();
									case "type" -> inputType = ShaderInputType.of(reader.nextString());
									case "name" -> name = reader.nextString();
									default -> reader.skipValue();
								}
							}
							if (location == null) throw new IOException(missing(reader, "location"));
							if (inputType == null) throw new IOException(missing(reader, "type"));
							if (name == null) throw new IOException(missing(reader, "name"));
							reader.endObject();
							
							inputs.put(ShaderInput.of(inputType, name), location);
						}
						reader.endArray();
					}
					case "uniformBlocks" ->
					{
						if (uniformBlocks != null) uniformBlocks.clear();
						uniformBlocks = new LinkedList<>();
						
						reader.beginArray();
						while (reader.hasNext())
						{
							Integer binding = null;
							String name = null;
							List<ShaderUniform> uniforms = null;
							
							reader.beginObject();
							while (reader.hasNext())
							{
								switch (reader.nextName())
								{
									case "binding" -> binding = reader.nextInt();
									case "name" -> name = reader.nextString();
									case "variables" ->
									{
										if (uniforms != null) uniforms.clear();
										uniforms = new LinkedList<>();
										
										reader.beginArray();
										while (reader.hasNext())
										{
											ShaderInputType inputType = null;
											String variableName = null;
											
											reader.beginObject();
											while (reader.hasNext())
											{
												switch (reader.nextName())
												{
													case "type" -> inputType = ShaderInputType.of(reader.nextString());
													case "name" -> variableName = reader.nextString();
													default -> reader.skipValue();
												}
											}
											if (inputType == null) throw new IOException(missing(reader, "type"));
											if (variableName == null) throw new IOException(missing(reader, "name"));
											reader.endObject();
											
											uniforms.add(new ShaderUniform(inputType, variableName));
										}
										reader.endArray();
									}
									default -> reader.skipValue();
								}
							}
							if (binding == null) throw new IOException(missing(reader, "binding"));
							if (uniforms == null) throw new IOException(missing(reader, "uniforms"));
							reader.endObject();
							
							uniformBlocks.add(new ShaderUniformBlock(binding, name, Collections.unmodifiableSet(new LinkedHashSet<>(uniforms))));
						}
						reader.endArray();
					}
					case "bindingsDescriptorTypes" ->
					{
						if (uniformBlocks == null) throw new IOException(missing(reader, "uniformBlocks definition"));
						if (bindingDescriptorTypeMap != null) bindingDescriptorTypeMap.clear();
						bindingDescriptorTypeMap = new HashMap<>();
						
						reader.beginObject();
						while (reader.hasNext())
						{
							String bindingString = reader.nextName();
							String descriptorTypeString = reader.nextString();
							int binding;
							try
							{
								binding = Integer.parseInt(bindingString);
							}
							catch (NumberFormatException e)
							{
								throw new IOException(invalid(reader, "integer"));
							}
							ShaderDescriptorType descriptorType = ShaderDescriptorType.of(descriptorTypeString);
							if (descriptorType == null) throw new IOException(invalid(reader, "descriptor type"));
							if (bindingDescriptorTypeMap.putIfAbsent(binding, descriptorType) != null) throw new IOException("Duplicated binding descriptor type");
						}
						reader.endObject();
					}
					default -> reader.skipValue();
				}
			}
			reader.endObject();
		}
		catch (IOException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}
		return new ShaderInfo(require(type, "type"), require(entryPoint, "entryPoint"), require(inputs, "inputs"), require(uniformBlocks, "uniformBlocks"), bindingDescriptorTypeMap);
	}
	
	private static <T> T require(T t, String name) throws IOException
	{
		if (t == null) throw new IOException("Missing " + name);
		return t;
	}
	
	private static String missing(JsonReader reader, String object)
	{
		String path = reader.getPath();
		return "Missing " + object + " at " + path.substring(0, path.lastIndexOf("."));
	}
	
	private static String invalid(JsonReader reader, String object)
	{
		String path = reader.getPath();
		return "Invalid " + object + " at " + path.substring(0, path.lastIndexOf("."));
	}
	
	private ShaderType type;
	private String entryPoint;
	
	private Map<ShaderInput, Integer> inputs;
	private List<ShaderUniformBlock> uniformBlocks;
	private Map<Integer, ShaderDescriptorType> bindingDescriptorTypeMap;
	
	public ShaderInfo(ShaderType type, String entryPoint, Map<ShaderInput, Integer> inputs, List<ShaderUniformBlock> uniformBlocks, Map<Integer, ShaderDescriptorType> bindingDescriptorTypeMap)
	{
		this.type = type;
		this.entryPoint = entryPoint;
		this.inputs = Collections.unmodifiableMap(new LinkedHashMap<>(inputs));
		this.uniformBlocks = List.copyOf(uniformBlocks);
		this.bindingDescriptorTypeMap = Map.copyOf(bindingDescriptorTypeMap);
	}
	
	public ShaderType getType()
	{
		return type;
	}
	
	public String getEntryPoint()
	{
		return entryPoint;
	}
	
	public Set<ShaderInput> getInputs()
	{
		return inputs.keySet();
	}
	
	public List<ShaderUniformBlock> getUniformsBlocks()
	{
		return uniformBlocks;
	}
	
	public Map<Integer, ShaderDescriptorType> getBindingDescriptorTypeMap()
	{
		return bindingDescriptorTypeMap;
	}
}
