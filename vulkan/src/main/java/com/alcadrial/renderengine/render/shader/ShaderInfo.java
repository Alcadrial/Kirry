package com.alcadrial.renderengine.render.shader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import com.alcadrial.renderengine.render.shader.layouts.ShaderCombinedImageSamplerLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderInputLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLocalSizeLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderStorageBufferLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderUniformBufferLayout;
import com.alcadrial.renderengine.render.util.FileUtils;
import com.alcadrial.renderengine.render.util.JsonUtils;
import com.google.gson.stream.JsonReader;

public class ShaderInfo {
	
	/**
	 * 
	 * @param json
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @see #fromJson(InputStream)
	 */
	public static ShaderInfo fromJson(File json) throws FileNotFoundException, IOException
	{
		FileUtils.assertExtension(json, "json");
		return fromJson(new FileInputStream(json));
	}
	
	/**
	 * The json must be as follows:
	 * <pre>
	 * {
	 *     "type": "{vert|tesc|tese|geom|frag|comp}",                                             [REQUIRED FIRST]
	 *     "entryPoint": "main",                      // The starting point of the shader         [REQUIRED]
	 *     "layouts": [                               // A list of 0 or more layouts              [REQUIRED]
	 *         {
	 *             "type": "uniformBuffer",           // The type of a uniform block              [REQUIRED FIRST]
	 *             "binding": 0,                      // The binding of the block                 [REQUIRED]
	 *             "name": "uniformBlock"             // The name of the block                    [OPTIONAL]
	 *             "variables": [                     // A list of uniform variables              [REQUIRED]
	 *                 {
	 *                     "type": "mat4",            // The type of the uniform variable         [REQUIRED]
	 *                     "name": "TRANSFORMATION"   // The name of the uniform variable         [REQUIRED]
	 *                 }
	 *             ]
	 *         },
	 *         {
	 *             "type": "storageBuffer",           // The type of a storage block              [REQUIRED FIRST]
	 *             "binding": 1,                      // The binding of the block                 [REQUIRED]
	 *             "name": "storageBlock"             // The name of the block                    [OPTIONAL]
	 *             "usage": "{input|output|other}"                                                [REQUIRED]
	 *             "variables": [                     // A list of uniform variables              [OPTIONAL]
	 *                 {
	 *                     "type": "vec3",            // The type of the uniform variable         [REQUIRED]
	 *                     "name": "position"         // The name of the uniform variable         [REQUIRED]
	 *                 }
	 *             ]
	 *         },
	 *         {
	 *             "type": "input",                   // The type of a buffer used in rendering   [REQUIRED FIRST]
	 *             "binding": 2,                      // The binding of the block                 [REQUIRED]
	 *             "name": "inputBlock"               // The name of the block                    [OPTIONAL]
	 *             "variables": [                     // A list of uniform variables              [REQUIRED]
	 *                 {
	 *                     "type": "vec4",            // The type of the shader's input variable  [REQUIRED]
	 *                     "name": "color"            // The name of the shader's input variable  [REQUIRED]
	 *                 }
	 *             ]
	 *         },
	 *         {
	 *             "type": "localSize",               // The type of localSize used in computing   [REQUIRED FIRST]
	 *             "xSize": 64,                       // The x size                                [REQUIRED]
	 *             "ySize": 64,                       // The y size                                [REQUIRED]
	 *             "zSize": 64                        // The z size                                [REQUIRED]
	 *         }
	 *     ]
	 * }
	 * </pre>
	 * 
	 * @param json
	 * @throws IOException
	 */
	public static ShaderInfo fromJson(InputStream json) throws IOException
	{
		ShaderType shaderType = null;
		String entryPoint = null;
		List<ShaderLayout> layouts = null;
		
		try (JsonReader reader = new JsonReader(new InputStreamReader(json)))
		{
			reader.beginObject();
			JsonUtils.require(reader.hasNext(), "type");
			JsonUtils.require(reader.nextName().equals("type"), "type as first item");
			JsonUtils.require((shaderType = ShaderType.fromExtension(reader.nextString())) != ShaderType.UNKNOWN, "Shader type must be a known type");
			
			while (reader.hasNext())
			{
				switch (reader.nextName())
				{
					case "entryPoint" -> entryPoint = reader.nextString();
					case "layouts" -> {
						if (layouts != null) layouts.clear();
						layouts = new LinkedList<>();
						
						reader.beginArray();
						while (reader.hasNext())
						{
							ShaderLayout.Type type;
							reader.beginObject();
							if (!reader.hasNext()) throw new IOException(JsonUtils.missing(reader, "type"));
							if (!reader.nextName().equals("type")) throw new IOException(JsonUtils.missing(reader, "type as first item"));
							type = ShaderLayout.Type.of(reader.nextString());
							
							layouts.add(switch (type)
							{
								case COMBINED_IMAGE_SAMPLER -> ShaderCombinedImageSamplerLayout.parseJson(reader);
								case UNIFORM_BUFFER -> ShaderUniformBufferLayout.parseJson(reader);
								case STORAGE_BUFFER -> ShaderStorageBufferLayout.parseJson(reader);
								case INPUT -> ShaderInputLayout.parseJson(reader);
								case LOCAL_SIZE -> ShaderLocalSizeLayout.parseJson(reader);
								default -> throw new IOException(JsonUtils.invalid(reader, "type"));
							});
							reader.endObject();
						}
						reader.endArray();
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
		return new ShaderInfo(JsonUtils.require(shaderType, "type"), JsonUtils.require(entryPoint, "entryPoint"), JsonUtils.require(layouts, "layouts"));
	}
	
	private ShaderType type;
	private String entryPoint;
	private List<ShaderLayout> layouts;
	
	public ShaderInfo(ShaderType type, String entryPoint, List<ShaderLayout> layouts)
	{
		this.type = type;
		this.entryPoint = entryPoint;
		this.layouts = List.copyOf(layouts);
	}
	
	public ShaderType getType()
	{
		return type;
	}
	
	public String getEntryPoint()
	{
		return entryPoint;
	}
	
	public List<ShaderLayout> getLayouts()
	{
		return layouts;
	}
}
