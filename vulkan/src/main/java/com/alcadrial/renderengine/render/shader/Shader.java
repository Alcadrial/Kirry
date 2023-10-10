package com.alcadrial.renderengine.render.shader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.alcadrial.renderengine.render.util.FileUtils;
import com.alcadrial.renderengine.render.util.Properties;
import com.alcadrial.renderengine.render.util.VulkanErrorUtils;
import com.google.gson.stream.JsonReader;

public class Shader {
	
	public static final File GLSLC = new File(new File(Properties.SHADER_COMPILER_PATH.getValue()), "glslc");
	
	private File file;
	private ShaderType type;
	private String[] code;
	private CompiledShader compiled;
	
	private String entryPoint;
	private Map<ShaderInput, Integer> inputs;
	
	private List<ShaderUniformBlock> uniformBlocks;
	
	/**
	 * DataFile must be a json file as follows:
	 * <pre>
	 * {
	 *     "entryPoint": "main",                      // The starting point of the shader            [REQUIRED]
	 *     "type": "{vert|tcon|teva|geom|frag|comp}",                                                [OPTIONAL]
	 *     "inputs": [                                // A list of 0 or more inputs                  [REQUIRED]
	 *         {
	 *             "location": 0,                     // The location of the shader's input variable [REQUIRED]
	 *             "type": "vec4",                    // The type of the shader's input variable     [REQUIRED]
	 *             "name": "color"                    // The name of the shader's input variable     [REQUIRED]
	 *         }
	 *     ],
	 *     "uniformsBlocks": [                        // A list of 0 or more uniform blocks          [REQUIRED]
	 *         {
	 *             "binding": 0,                      // The binding of the block                    [REQUIRED]
	 *             "name": "block"                    // The name of the block                       [OPTIONAL]
	 *             "variables": [                     // A list of uniform variables                 [REQUIRED]
	 *                 {
	 *                     "type": "mat4",            // The type of the uniform variable            [REQUIRED]
	 *                     "name": "TRANSFORMATION"   // The name of the uniform variable            [REQUIRED]
	 *                 }
	 *             ]
	 *         }
	 *     ]
	 * }
	 * </pre>
	 * 
	 * @param file
	 * @param dataFile
	 * @throws IOException
	 */
	public Shader(File file, File dataFile) throws IOException
	{
		this.file = VulkanErrorUtils.nonNull(file);
		code = Files.readAllLines(file.toPath()).toArray(String[]::new);
		FileUtils.assertExtension(dataFile, "json");
		try (JsonReader reader = new JsonReader(new FileReader(dataFile)))
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
							ShaderInputType type = null;
							String name = null;
							
							reader.beginObject();
							while (reader.hasNext())
							{
								switch (reader.nextName())
								{
									case "location" -> location = reader.nextInt();
									case "type" -> type = ShaderInputType.of(reader.nextString());
									case "name" -> name = reader.nextString();
									default -> reader.skipValue();
								}
							}
							if (location == null) throw new IOException(missing(reader, "location"));
							if (type == null) throw new IOException(missing(reader, "type"));
							if (name == null) throw new IOException(missing(reader, "name"));
							reader.endObject();
							
							inputs.put(ShaderInput.of(type, name), location);
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
											ShaderInputType type = null;
											String variableName = null;
											
											reader.beginObject();
											while (reader.hasNext())
											{
												switch (reader.nextName())
												{
													case "type" -> type = ShaderInputType.of(reader.nextString());
													case "name" -> variableName = reader.nextString();
													default -> reader.skipValue();
												}
											}
											if (type == null) throw new IOException(missing(reader, "type"));
											if (variableName == null) throw new IOException(missing(reader, "name"));
											reader.endObject();
											
											uniforms.add(new ShaderUniform(type, variableName));
										}
										reader.endArray();
									}
									default -> reader.skipValue();
								}
							}
							if (binding == null) throw new IOException(missing(reader, "binding"));
							if (uniforms == null) throw new IOException(missing(reader, "uniforms"));
							reader.endObject();
							
							uniformBlocks.add(new ShaderUniformBlock(binding, name, Set.copyOf(uniforms)));
						}
						reader.endArray();
					}
					default -> reader.skipValue();
				}
			}
			reader.endObject();
		}
		
		if (type != null)
		{
			if (type != ShaderType.fromExtension(FileUtils.getExtension(file))) throw new IOException("Shader " + file + " extension doesn't match the type (" + type.getExtension() + ") in " + dataFile);
		}
		else type = ShaderType.fromExtension(FileUtils.getExtension(file));
		entryPoint = require(entryPoint, "entryPoint");
		inputs = require(inputs, "inputs");
		uniformBlocks = require(uniformBlocks, "uniformBlocks");
	}
	
	//public Shader(String[] code, Reader data) throws IOException
	
	private <T> T require(T t, String name) throws IOException
	{
		if (t == null) throw new IOException("Missing " + name);
		return t;
	}
	
	private String missing(JsonReader reader, String object)
	{
		String path = reader.getPath();
		return "Missing " + object + " at " + path.substring(0, path.lastIndexOf("."));
	}
	
	public CompiledShader compile()
	{
		try
		{
			File binary = new File(file + "" + Thread.currentThread().getId() + ".bin");
			new ProcessBuilder("\"" + GLSLC + "\" \"" + file + "\" -O -o \"" + binary + "\"").inheritIO().start().waitFor();
			if (binary.exists())
			{
				compiled = CompiledShader.ofSuccess(code, Files.readAllBytes(binary.toPath()));
				binary.delete();
			}
			else
			{
				compiled = CompiledShader.ofFail();
				System.out.println("Shader compilation failed");
			}
		}
		catch (InterruptedException | IOException e)
		{
			e.printStackTrace(System.out);
			compiled = CompiledShader.ofFail();
		}
		return compiled;
	}
	
	public CompiledShader getOrCompile()
	{
		if (compiled == null) compile();
		return compiled;
	}
	
	public Optional<CompiledShader> getLastCompilation()
	{
		return Optional.ofNullable(compiled);
	}
	
	public boolean isCompilationSuccessfull()
	{
		return !getOrCompile().hasErrors();
	}
	
	public File getFile()
	{
		return file;
	}
	
	public ShaderType getType()
	{
		return type;
	}
	
	public String[] getCode()
	{
		return code;
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
}
