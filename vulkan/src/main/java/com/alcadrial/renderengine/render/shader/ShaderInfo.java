package com.alcadrial.renderengine.render.shader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.shader.layouts.ShaderCombinedImageSamplerLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderInputLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderLocalSizeLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderStorageBufferLayout;
import com.alcadrial.renderengine.render.shader.layouts.ShaderUniformBufferLayout;
import com.alcadrial.renderengine.render.util.FileUtils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class ShaderInfo {
	
	static
	{
		RenderEngine.assertLoaded();
	}
	
	/**
	 * 
	 * @param json
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @see #fromJson(ObjectMapper, InputStream)
	 */
	public static ShaderInfo fromJson(File json) throws FileNotFoundException, IOException
	{
		FileUtils.assertExtension(json, "json");
		return fromJson(RenderEngine.getVulkan().getObjectMapper(), new FileInputStream(json));
	}
	
	/**
	 * The json must be as follows:
	 * <pre>
	 * {
	 *     "type": "{vert|tesc|tese|geom|frag|comp}",                                                          [REQUIRED]
	 *     "entryPoint": "main",                                   // The starting point of the shader         [REQUIRED]
	 *     "layouts": [                                            // A list of 0 or more layouts              [REQUIRED]
	 *         {
	 *             "type": "uniformBuffer",                        // The type of a uniform block              [REQUIRED]
	 *             "binding": 0,                                   // The binding of the block                 [REQUIRED]
	 *             "descriptorCount": "1"                          // The number of copies of this layout      [OPTIONAL (default 1)]
	 *             "name": "uniformBlock"                          // The name of the block                    [OPTIONAL]
	 *             "variables": [                                  // A list of variables                      [REQUIRED]
	 *                 "mat4 TRANSFORMATION"                       // The declaration of a variable            [REQUIRED]
	 *             ]
	 *         },
	 *         {
	 *             "type": "storageBuffer",                        // The type of a storage block              [REQUIRED]
	 *             "binding": 1,                                   // The binding of the block                 [REQUIRED]
	 *             "descriptorCount": "1"                          // The number of copies of this layout      [OPTIONAL (default 1)]
	 *             "name": "storageBlock"                          // The name of the block                    [OPTIONAL]
	 *             "usage": "{input|output|other}"                                                             [REQUIRED]
	 *             "variables": [                                  // A list of variables                      [OPTIONAL]
	 *                 "vec3 position"                             // The declaration of a variable            [REQUIRED]
	 *             ]
	 *         },
	 *         {
	 *             "type": "input",                                // The type of a buffer used in rendering   [REQUIRED]
	 *             "binding": 2,                                   // The binding of the block                 [REQUIRED]
	 *             "name": "inputBlock"                            // The name of the block                    [OPTIONAL]
	 *             "variables": [                                  // A list of variables                      [REQUIRED]
	 *                 {
	 *                     "location": "0",                        // The location of the variable             [REQUIRED]
	 *                     "declaration": "vec3 vertexPosition"    // The declaration of the variable          [REQUIRED]
	 *                 }
	 *             ]
	 *         },
	 *         {
	 *             "type": "localSize",                            // The type of localSize used in computing  [REQUIRED]
	 *             "xSize": 64,                                    // The x size                               [REQUIRED]
	 *             "ySize": 64,                                    // The y size                               [REQUIRED]
	 *             "zSize": 64                                     // The z size                               [REQUIRED]
	 *         }
	 *     ]
	 * }
	 * </pre>
	 * 
	 * The declaration of a variable must be as follows:
	 * <pre>"&lt;type&gt; &lt;name&gt;"</pre>
	 * where &lt;type&gt; is a valid shader type and &lt;name&gt; is blank or a valid identifier, if &lt;name&gt;
	 * is blank then it is interpreted as padding of the same size of the &lt;type&gt;
	 * 
	 * @throws IOException
	 */
	public static ShaderInfo fromJson(ObjectMapper mapper, InputStream stream) throws IOException
	{
		return fromJson(mapper, stream, "$");
	}
	
	public static ShaderInfo fromJson(ObjectMapper mapper, InputStream stream, String location) throws IOException
	{
		JsonNode json = mapper.readTree(stream);
		
		ShaderType shaderType = ShaderType.fromExtension(json.get("type").requireNonNull().stringValue());
		if (shaderType == ShaderType.UNKNOWN) throw new IOException("Shader type is unknown at " + location);
		
		String entryPoint = json.get("entryPoint").requireNonNull().stringValue();
		
		JsonNode layoutsNode = json.get("layouts").requireNonNull();
		List<ShaderLayout> layouts = new LinkedList<>();
		
		for (int i = 0; i < layoutsNode.size(); i++)
		{
			JsonNode layoutNode = layoutsNode.get(i);
			ShaderLayout.Type type = ShaderLayout.Type.of(layoutNode.get("type").requireNonNull().stringValue());
			
			String newLocation = location + ".layouts[" + i + "]";
			layouts.add(switch (type)
			{
				case COMBINED_IMAGE_SAMPLER -> ShaderCombinedImageSamplerLayout.parse(layoutNode, newLocation);
				case UNIFORM_BUFFER -> ShaderUniformBufferLayout.parse(layoutNode, newLocation);
				case STORAGE_BUFFER -> ShaderStorageBufferLayout.parse(layoutNode, newLocation);
				case INPUT -> ShaderInputLayout.parse(layoutNode, newLocation);
				case LOCAL_SIZE -> ShaderLocalSizeLayout.parse(layoutNode, newLocation);
				default -> throw new IOException("Unknown layout type at " + newLocation);
			});
		}
		
		return new ShaderInfo(shaderType, entryPoint, layouts);
	}
	
	private final ShaderType type;
	private final String entryPoint;
	private final ImmutableList<ShaderLayout> layouts;
	
	public ShaderInfo(ShaderType type, String entryPoint, List<ShaderLayout> layouts)
	{
		this.type = type;
		this.entryPoint = entryPoint;
		this.layouts = Lists.immutable.ofAll(layouts);
	}
	
	public ShaderType getType()
	{
		return type;
	}
	
	public String getEntryPoint()
	{
		return entryPoint;
	}
	
	public ImmutableList<ShaderLayout> getLayouts()
	{
		return layouts;
	}
}
