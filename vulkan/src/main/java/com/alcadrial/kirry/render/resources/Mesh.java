package com.alcadrial.kirry.render.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.alcadrial.kirry.core.resources.ResourceManager;
import com.alcadrial.kirry.core.resources.ResourceType;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.memory.MemoryTransferState;
import com.alcadrial.kirry.render.texture.Atlas;
import com.alcadrial.kirry.render.texture.TextureBuffer;
import com.alcadrial.kirry.render.texture.TextureRegion;
import com.alcadrial.kirry.render.util.VulkanErrorUtils;
import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.util.future.CompletableFutureUtils;

public class Mesh extends AbstractVulkanResource<Mesh> {
	
	public static final ResourceType<Mesh> TYPE = ResourceType.of(Mesh.class);
	
	public record Properties(
		int vertexCount,
		boolean useTextureUv,
		boolean useNormals,
		int vertexesSize,
		int indicesSize,
		int textureUvSize,
		int normalsSize
	) {}
	
	public record TextureData(
		TextureResource resource,
		TextureRegion region,
		int vertexOffset,
		int vertexCount
	) {}
	
	public record Material(
		@Nullable TextureData colorTexture
	) {}
	
	private volatile boolean isLoaded = false;
	private volatile int reloadCounter;
	private Loader loader;
	private @Nullable MutableList<Material> materials;
	
	public Mesh(Path path, ResourceManager resourceManager)
	{
		this(path, path.getParent(), path.getParent(), resourceManager);
	}
	
	public Mesh(Path path, Path materialsPath, Path texturesPath, ResourceManager resourceManager)
	{
		this(path, materialsPath, texturesPath, resourceManager, (p, mp, tp) -> {
			if (p.getFileName().toString().endsWith(".obj")) return new ObjLoader(resourceManager, p, mp, tp);
			return null;
		});
	}
	
	public Mesh(Path path, Path materialsPath, Path texturesPath, ResourceManager resourceManager, Loader.Factory<?> loader)
	{
		super(path, resourceManager);
		this.loader = VulkanErrorUtils.nonNull(loader.create(path, materialsPath, texturesPath), "Cannot load resource " + path + ", cannot determine a valid loader");
		reloadCounter = 0;
	}
	
	@Override
	protected void loadData(InputStreamSupplier stream) throws IOException
	{
		loader.load();
		MutableList<Loader.Index> indices = loader.indices;
		if (indices != null)
		{
			MutableList<CompletableFuture<?>> futures = Lists.mutable.of();
			for (Loader.Index index : indices)
			{
				TextureResource colorTexture = index.material.colorTextureResource;
				if (colorTexture != null) futures.add(colorTexture.load());
			}
			CompletableFutureUtils.ofAll(futures).join();
		}
		isLoaded = true;
	}
	
	/**
	 * Before submitting the {@link CommandBuffer} it must be used with {@link Atlas#draw(CommandBuffer)}
	 * 
	 * @param memoryManager
	 * @param commandBuffer
	 * @param atlas
	 * @throws VulkanException
	 */
	public void flushMaterials(MemoryManager memoryManager, CommandBuffer commandBuffer, Atlas atlas) throws VulkanException
	{
		if (materials != null) return;
		
		MutableList<Loader.Index> indices = loader.indices;
		if (indices == null) return;
		
		MutableList<Material> materials = Lists.mutable.ofInitialCapacity(indices.size());
		for (int i = 0; i < indices.size(); i++)
		{
			Loader.Index materialIndex = indices.get(i);
			TextureResource textureResource = materialIndex.material.colorTextureResource;
			if (textureResource != null)
			{
				TextureResource.Properties properties = textureResource.getProperties();
				try (MemoryTransferState transfer = memoryManager.allocateForTransfer(commandBuffer, properties.size()))
				{
					VulkanBuffer buffer = transfer.getLocal(properties.size());
					textureResource.copy(buffer);
					TextureRegion region = atlas.add(TextureBuffer.create(buffer, properties.width(), properties.height(), textureResource));
					materials.add(new Material(new TextureData(textureResource, region, 0, materialIndex.indices.size())));
				}
			}
		}
		this.materials = materials;
	}
	
	public void invalidateMaterials()
	{
		materials = null;
	}
	
	public void flush(@Nullable VulkanBuffer vertexBuffer, int vertexStride, int vertexOffset, @Nullable VulkanBuffer textureUvBuffer, int textureUvStride, int textureUvOffset, @Nullable VulkanBuffer normalBuffer, int normalStride, int normalOffset, @Nullable VulkanBuffer indexBuffer) throws VulkanException
	{
		MutableList<float[]> vertexes = loader.vertexes;
		MutableList<Loader.Index> indices = loader.indices;
		MutableList<float[]> textureUv = loader.textureUv;
		MutableList<float[]> normals = loader.normals;
		int vertexLength = loader.vertexLength;
		int textureUvLength = loader.textureUvLength;
		if (indices == null || vertexes == null) return;
		
		boolean useVertexes = vertexBuffer != null;
		boolean useTextureUv = textureUv != null && textureUvBuffer != null;
		boolean useNormals = normals != null && normalBuffer != null;
		
		FloatBuffer vertexWriter = vertexBuffer != null ? vertexBuffer.asBuffer().asFloatBuffer() : null;
		FloatBuffer textureUvWriter = textureUvBuffer != null ? textureUvBuffer.asBuffer().asFloatBuffer() : null;
		FloatBuffer normalWriter = normalBuffer != null ? normalBuffer.asBuffer().asFloatBuffer() : null;
		
		if (useTextureUv)
		{
			int jOffset = 0;
			for (int i = 0; i < indices.size(); i++)
			{
				Loader.Index materialIndex = indices.get(i);
				TextureResource texture = materialIndex.material.colorTextureResource;
				if (texture != null)
				{
					if (indexBuffer == null)
					{
						for (int j = jOffset; j - jOffset < materialIndex.indices.size(); j++)
						{
							Integer[] index = materialIndex.indices.get(j);
							if (useVertexes) ((@NonNull FloatBuffer) vertexWriter)
								.position(j * vertexStride + vertexOffset)
								.put(vertexes.get(index[0]), 0, vertexLength);
							((@NonNull FloatBuffer) textureUvWriter)
								.position(j * textureUvStride + textureUvOffset)
								.put(((@NonNull MutableList<float[]>) textureUv).get(index[1]), 0, textureUvLength);
							if (useNormals) ((@NonNull FloatBuffer) normalWriter)
								.position(j * normalStride + normalOffset)
								.put(((@NonNull MutableList<float[]>) normals).get(index[2]));
						}
						jOffset += materialIndex.indices.size();
					}
					else
					{
						IntBuffer indexWriter = indexBuffer.asBuffer().asIntBuffer();
						for (int j = jOffset; j - jOffset < materialIndex.indices.size(); j++)
						{
							Integer[] index = materialIndex.indices.get(j);
							if (useVertexes) ((@NonNull FloatBuffer) vertexWriter)
								.position(j * vertexStride + vertexOffset)
								.put(vertexes.get(index[0]), 0, vertexLength);
							((@NonNull FloatBuffer) textureUvWriter)
								.position(j * textureUvStride + textureUvOffset)
								.put(((@NonNull MutableList<float[]>) textureUv).get(index[1]), 0, textureUvLength);
							if (useNormals) ((@NonNull FloatBuffer) normalWriter)
								.position(j * normalStride + normalOffset)
								.put(((@NonNull MutableList<float[]>) normals).get(index[2]));
							indexWriter.put(j);
						}
						jOffset += materialIndex.indices.size();
					}
				}
			}
		}
		else
		{
			
			if (indexBuffer == null)
			{
				if (useVertexes || useNormals)
				{
					for (Loader.Index materialIndex : indices)
					{
						for (int i = 0; i < materialIndex.indices.size(); i++)
						{
							Integer[] index = materialIndex.indices.get(i);
							if (useVertexes) ((@NonNull FloatBuffer) vertexWriter)
								.position(i * vertexStride + vertexOffset)
								.put(vertexes.get(index[0]), 0, vertexLength);
							if (useNormals) ((@NonNull FloatBuffer) normalWriter)
								.position(i * normalStride + normalOffset)
								.put(((@NonNull MutableList<float[]>) normals).get(index[2]));
						}
					}
				}
			}
			else
			{
				IntBuffer indexWriter = indexBuffer.asBuffer().asIntBuffer();
				if (useVertexes || useNormals)
				{
					for (Loader.Index materialIndex : indices)
					{
						for (int i = 0; i < materialIndex.indices.size(); i++)
						{
							Integer[] index = materialIndex.indices.get(i);
							if (useVertexes) ((@NonNull FloatBuffer) vertexWriter)
								.position(i * vertexStride + vertexOffset)
								.put(vertexes.get(index[0]), 0, vertexLength);
							if (useNormals) ((@NonNull FloatBuffer) normalWriter)
								.position(i * normalStride + normalOffset)
								.put(((@NonNull MutableList<float[]>) normals).get(index[2]));
							indexWriter.put(i);
						}
					}
				}
			}
		}
		
		//loader.clear();
	}
	
	public Properties getProperties()
	{
		int vertexLength = loader.vertexLength;
		int textureUvLength = loader.textureUvLength;
		boolean useTextureUv = loader.useTextureUv;
		boolean useNormals = loader.useNormals;
		int vertexCount = loader.vertexCount;
		
		int vSize = vertexCount * vertexLength * 4;
		int iSize = vertexCount * 4;
		int tSize = useTextureUv ? vertexCount * textureUvLength * 4 : 0;
		int nSize = useNormals ? vertexCount * 3 * 4 : 0;
		
		return new Properties(vertexCount, useTextureUv, useNormals, vSize, iSize, tSize, nSize);
	}
	
	public int getTextureUvLength()
	{
		return loader.textureUvLength;
	}
	
	public MutableList<Material> getMaterials()
	{
		return VulkanErrorUtils.nonNull(materials, "Mesh is not initialized");
	}
	
	public boolean isLoaded()
	{
		return isLoaded;
	}
	
	public int reloadCount()
	{
		return reloadCounter;
	}
	
	@Override
	public void close()
	{
		if (loader.indices != null)
		{
			for (Loader.Index index : loader.indices)
			{
				index.clear();
				if (index.material.colorTextureResource != null) index.material.colorTextureResource.close();
			}
		}
		loader.clear();
	}
	
	public String getDisplayName()
	{
		String name = loader.name;
		if (name != null) return name;
		return loader.getPath().toString();
	}
	
	public @Nullable String getName()
	{
		return loader.name;
	}
	
	@Override
	public Path getPath()
	{
		return loader.getPath();
	}
	
	@Override
	public String toString()
	{
		return loader.toString();
	}
	
	private static abstract class Loader {
		
		@FunctionalInterface
		interface Factory<L extends Loader> {
			
			L create(Path modelPath, Path materialsPath, Path texturesPath);
		}
		
		record Material(
			@Nullable TextureResource colorTextureResource
		) {}
		
		public static Material BLANK = new Material(null);
		
		record Index(
			MutableList<Integer[]> indices,
			Material material
		) {
			
			public void clear()
			{
				indices.clear();
			}
		}
		
		@Nullable
		String name;
		@Nullable
		MutableList<float[]> vertexes;
		@Nullable
		MutableList<float[]> textureUv;
		@Nullable
		MutableList<float[]> normals;
		@Nullable
		MutableList<Index> indices;
		
		int vertexCount;
		int vertexLength;
		int textureUvLength;
		boolean useNormals;
		boolean useTextureUv;
		
		abstract void load() throws IOException;
		abstract Path getPath();
		abstract void clearMaterial(Material material);
		
		@SuppressWarnings("null")
		void clear()
		{
			if (vertexes != null) vertexes.clear();
			if (textureUv != null) textureUv.clear();
			if (normals != null) normals.clear();
			if (indices != null)
			{
				for (Index index : indices)
				{
					index.clear();
					clearMaterial(index.material);
				}
				indices.clear();
			}
		}
	}
	
	private static class ObjLoader extends Loader {
		
		ResourceManager resourceManager;
		Path path;
		Path materialsPath;
		Path texturesPath;
		
		ObjLoader(ResourceManager resourceManager, Path path, Path materialsPath, Path texturesPath)
		{
			this.resourceManager = resourceManager;
			this.path = path;
			this.materialsPath = materialsPath;
			this.texturesPath = texturesPath;
		}
		
		@Override
		Path getPath()
		{
			return path;
		}
		
		@Override
		void load() throws IOException
		{
			try (BufferedReader reader = Files.newBufferedReader(path))
			{
				MutableList<float[]> vertexes = Lists.mutable.of();
				MutableList<float[]> textureUv = Lists.mutable.of();
				MutableList<float[]> normals = Lists.mutable.of();
				MutableList<Index> indices = Lists.mutable.of();
				
				MaterialLoader materialLoader = null;
				Material material = BLANK;
				MutableList<Integer[][]> faces = Lists.mutable.of();
				
				int vertexLength = 3;
				int textureUvLength = 1;
				
				boolean useTextureUv = false;
				boolean useNormals = false;
				int trianglesCount = 0;
				int vertexCount = 0;
				
				String line;
				while ((line = reader.readLine()) != null)
				{
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) continue;
					Scanner scanner = new Scanner(line);
					scanner.useLocale(Locale.ENGLISH);
					
					switch (scanner.next())
					{
						case "v" -> {
							float[] fs = new float[4];
							fs[3] = 1;
							int i = 0;
							while (i < 4 && scanner.hasNextFloat())
							{
								fs[i++] = scanner.nextFloat();
							}
							if (i < 3 || scanner.hasNext()) throw new IOException("Failed to parse file " + path);
							if (fs[3] == 0) throw new IOException("Unsupported vertex (w must not be 0)");
							vertexes.add(fs);
							vertexLength = Math.max(vertexLength, i);
						}
						case "vt" -> {
							float[] fs = new float[3];
							int i = 0;
							while (i < 3 && scanner.hasNextFloat())
							{
								fs[i++] = scanner.nextFloat();
							}
							if (i < 1 || scanner.hasNext()) throw new IOException("Failed to parse file " + path);
							textureUv.add(fs);
							textureUvLength = Math.max(textureUvLength, i);
						}
						case "vn" -> {
							float[] fs = new float[3];
							int i = 0;
							while (i < 3 && scanner.hasNextFloat())
							{
								fs[i++] = scanner.nextFloat();
							}
							if (i < 3 || scanner.hasNext()) throw new IOException("Failed to parse file " + path);
							normals.add(fs);
						}
						case "f" -> {
							String[] tokens = scanner.tokens().toArray(String[]::new);
							if (tokens.length < 3) throw new IOException("Failed to parse file " + path);
							
							Integer[][] ref = new Integer[tokens.length][3];
							Integer[][] oldRef = faces.getFirstOptional().orElse(null);
							if (oldRef != null)
							{
								for (int i = 0; i < tokens.length; i++)
								{
									ref[i] = parseVertexRef(path, tokens[i]);
									for (int j = 0; j < 3; j++) if ((ref[i] == null) != (oldRef[i] == null)) throw new IOException("Invalid geometry (non uniform faces format)");
								}
							}
							else
							{
								for (int i = 0; i < tokens.length; i++) ref[i] = parseVertexRef(path, tokens[i]);
							}
							if (tokens.length == 4) trianglesCount += tokens.length - 2;
							faces.add(ref);
						}
						case "o" -> name = scanner.nextLine();
						case "mtllib" -> {
							while (scanner.hasNext())
							{
								String fileName = scanner.next();
								try
								{
									materialLoader = new MaterialLoader(resourceManager, materialsPath, texturesPath, fileName);
								}
								catch (Exception e)
								{
									//TODO: signal error
									materialLoader = null;
								}
							}
						}
						case "usemtl" -> {
							if (materialLoader != null)
							{
								String materialId = scanner.hasNext() ? scanner.next() : null;
								
								if (!faces.isEmpty())
								{
									MutableList<Integer[]> faceIndices = Lists.mutable.ofInitialCapacity(trianglesCount);
									for (Integer[][] face : faces)
									{
										for (int i = 2; i < face.length; i++)
										{
											faceIndices.add(face[0]);
											faceIndices.add(face[i - 1]);
											faceIndices.add(face[i]);
										}
									}
									indices.add(new Index(faceIndices, material));
									faces = Lists.mutable.of();
									vertexCount += trianglesCount * 3;
									trianglesCount = 0;
									useTextureUv |= faces.getAny()[0][1] != null;
									useNormals |= faces.getAny()[0][2] != null;
								}
								
								material = materialLoader.materials.get(materialId);
							}
						}
					}
				}
				if (faces.isEmpty()) return;
				if (faces.getAny()[0][0] == null) throw new IOException("Invalid geometry (missing vertex value)");
				
				MutableList<Integer[]> faceIndices = Lists.mutable.ofInitialCapacity(trianglesCount);
				for (Integer[][] face : faces)
				{
					for (int i = 2; i < face.length; i++)
					{
						faceIndices.add(face[0]);
						faceIndices.add(face[i - 1]);
						faceIndices.add(face[i]);
					}
				}
				indices.add(new Index(faceIndices, material));
				vertexCount += trianglesCount * 3;
				useTextureUv |= faces.getAny()[0][1] != null;
				useNormals |= faces.getAny()[0][2] != null;
				
				this.vertexCount = vertexCount;
				this.useTextureUv = useTextureUv;
				this.useNormals = useNormals;
				if (!useTextureUv) textureUv.clear();
				if (!useNormals) normals.clear();
				this.vertexLength = vertexLength;
				this.textureUvLength = textureUvLength;
				this.indices = indices;
				this.vertexes = vertexes;
				this.textureUv = textureUv;
				this.normals = normals;
			}
		}
		
		private static Integer[] parseVertexRef(Path path, String s) throws IOException
		{
			String[] ss = s.split("/");
			if (ss.length > 3) throw new IOException("Failed to parse file " + path);
			Integer[] is = new Integer[3];
			for (int i = 0; i < ss.length; i++)
			{
				if (ss[i].isEmpty()) is[i] = null;
				else is[i] = Integer.parseInt(ss[i]) - 1;
			}
			for (int i = ss.length; i < 3; i++) is[i] = null;
			return is;
		}
		
		@Override
		void clearMaterial(Material material)
		{
			
		}
		
		@Override
		public String toString()
		{
			if (name != null) return name + "{" + path + "}";
			return "ObjFile{" + path + "}";
		}
		
		static class MaterialLoader {
			
			MutableMap<String, Material> materials;
			
			public MaterialLoader(ResourceManager resourceManager, Path folder, Path texturesPath, String fileName) throws IOException
			{
				Path path = folder.resolve(fileName);
				
				materials = Maps.mutable.of();
				try (BufferedReader reader = Files.newBufferedReader(path))
				{
					String materialName = null;
					TextureResource colorTextureResource = null;
					
					String line;
					while ((line = reader.readLine()) != null)
					{
						line = line.trim();
						if (line.isEmpty() || line.startsWith("#")) continue;
						Scanner scanner = new Scanner(line);
						scanner.useLocale(Locale.ENGLISH);
						
						String token = scanner.next();
						if (token.equals("newmtl"))
						{
							if (materialName != null) materials.put(materialName, new Material(colorTextureResource));
							materialName = scanner.next();
							continue;
						}
						if (materialName == null) continue;
						switch (token)
						{
							case "Ka" -> {/*TODO*/}
							case "map_Ka" -> {/*TODO*/}
							case "map_Kd" -> {
								colorTextureResource = resourceManager.getResource(texturesPath.resolve(scanner.next()), TextureResource.TYPE);
							}
						}
					}
				}
			}
		}
	}
}
