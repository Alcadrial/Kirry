package com.alcadrial.renderengine.render.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.alcadrial.renderengine.core.component.AbstractComponent;
import com.alcadrial.renderengine.core.component.ComponentInitParams;
import com.alcadrial.renderengine.core.component.ComponentProperties;
import com.alcadrial.renderengine.core.component.ComponentType;
import com.alcadrial.renderengine.render.RenderEngine;
import com.alcadrial.renderengine.render.buffer.VulkanBuffer;

public class MeshComponent extends AbstractComponent<MeshComponent.Params, MeshComponent.Properties> {
	
	public enum Type implements ComponentType<MeshComponent> {
		
		INSTANCE;
		
		@Override
		public int getId()
		{
			return 0;
		}
	}
	
	public record Params(
		@Nullable VulkanBuffer vertexBuffer, int vertexStride, int vertexOffset,
		@Nullable VulkanBuffer textureUvBuffer, int textureUvStride, int textureUvOffset,
		@Nullable VulkanBuffer normalBuffer, int normalStride, int normalOffset,
		@Nullable VulkanBuffer indexBuffer
	) implements ComponentInitParams {}
	
	public record Properties(
		int vertexCount,
		boolean useTextureUv,
		boolean useNormals,
		int vertexesSize,
		int indicesSize,
		int textureUvSize,
		int normalsSize
	) implements ComponentProperties {}
	
	public static MeshComponent ofFileObj(File file)
	{
		return new MeshComponent(new ObjLoader(file));
	}
	
	private Loader loader;
	
	public MeshComponent(Loader loader)
	{
		this.loader = loader;
	}
	
	@Override
	public CompletableFuture<?> reload()
	{
		CompletableFuture<?> future = new CompletableFuture<>();
		RenderEngine.submitAsync(() -> {
			try
			{
				loader.load();
				future.complete(null);
				reloadCounter++;
			}
			catch (Throwable e)
			{
				future.completeExceptionally(e);
			}
			return null;
		});
		return future;
	}
	
	@Override
	public void reinit(Params params)
	{
		MutableList<float[]> vertexes = loader.vertexes;
		MutableList<Integer[]> indices = loader.indices;
		MutableList<float[]> textureUv = loader.textureUv;
		MutableList<float[]> normals = loader.normals;
		int vertexLength = loader.vertexLength;
		int textureUvLength = loader.textureUvLength;
		if (indices == null || vertexes == null) return;
		
		VulkanBuffer vertexBuffer = params.vertexBuffer;
		int vertexStride = params.vertexStride;
		int vertexOffset = params.vertexOffset;
		VulkanBuffer textureUvBuffer = params.textureUvBuffer;
		int textureUvStride = params.textureUvStride;
		int textureUvOffset = params.textureUvOffset;
		VulkanBuffer normalBuffer = params.normalBuffer;
		int normalStride = params.normalStride;
		int normalOffset = params.normalOffset;
		VulkanBuffer indexBuffer = params.indexBuffer;
		
		boolean useVertexes = vertexBuffer != null;
		boolean useTextureUv = textureUv != null && textureUvBuffer != null;
		boolean useNormals = normals != null && normalBuffer != null;
		
		FloatBuffer vertexWriter = vertexBuffer != null ? vertexBuffer.asBuffer().asFloatBuffer() : null;
		FloatBuffer textureUvWriter = textureUvBuffer != null ? textureUvBuffer.asBuffer().asFloatBuffer() : null;
		FloatBuffer normalWriter = normalBuffer != null ? normalBuffer.asBuffer().asFloatBuffer() : null;
		
		if (indexBuffer == null)
		{
			if (useVertexes || useTextureUv || useNormals)
			{
				for (int i = 0; i < indices.size(); i++)
				{
					Integer[] index = indices.get(i);
					if (useVertexes) ((@NonNull FloatBuffer) vertexWriter)
						.position(i * vertexStride + vertexOffset)
						.put(vertexes.get(index[0]), 0, vertexLength);
					if (useTextureUv) ((@NonNull FloatBuffer) textureUvWriter)
						.position(i * textureUvStride + textureUvOffset)
						.put(((@NonNull MutableList<float[]>) textureUv).get(index[1]), 0, textureUvLength);
					if (useNormals) ((@NonNull FloatBuffer) normalWriter)
						.position(i * normalStride + normalOffset)
						.put(((@NonNull MutableList<float[]>) normals).get(index[2]));
				}
			}
		}
		else
		{
			IntBuffer indexWriter = indexBuffer.asBuffer().asIntBuffer();
			if (useVertexes || useTextureUv || useNormals)
			{
				for (int i = 0; i < indices.size(); i++)
				{
					Integer[] index = indices.get(i);
					if (useVertexes) ((@NonNull FloatBuffer) vertexWriter)
						.position(i * vertexStride + vertexOffset)
						.put(vertexes.get(index[0]), 0, vertexLength);
					if (useTextureUv) ((@NonNull FloatBuffer) textureUvWriter)
						.position(i * textureUvStride + textureUvOffset)
						.put(((@NonNull MutableList<float[]>) textureUv).get(index[1]), 0, textureUvLength);
					if (useNormals) ((@NonNull FloatBuffer) normalWriter)
						.position(i * normalStride + normalOffset)
						.put(((@NonNull MutableList<float[]>) normals).get(index[2]));
					indexWriter.put(i);
				}
			}
		}
		
		clear();
	}
	
	@Override
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
	
	@Override
	public void close()
	{
		clear();
	}
	
	private void clear()
	{
		if (loader.vertexes != null) loader.vertexes.clear();
		loader.vertexes = null;
		if (loader.indices != null) loader.indices.clear();
		loader.indices = null;
		if (loader.textureUv != null) loader.textureUv.clear();
		loader.textureUv = null;
		if (loader.normals != null) loader.normals.clear();
		loader.normals = null;
	}
	
	@Override
	public String toString()
	{
		return loader.toString();
	}
	
	private static abstract class Loader {
		
		@Nullable
		MutableList<float[]> vertexes;
		@Nullable
		MutableList<@Nullable Integer[]> indices;
		@Nullable
		MutableList<float[]> textureUv;
		@Nullable
		MutableList<float[]> normals;
		
		int vertexCount;
		int vertexLength;
		int textureUvLength;
		boolean useNormals;
		boolean useTextureUv;
		
		public abstract void load() throws IOException;
	}
	
	private static class ObjLoader extends Loader {
		
		File file;
		
		public ObjLoader(File file)
		{
			this.file = file;
		}
		
		public void load() throws IOException
		{
			try (BufferedReader reader = new BufferedReader(new FileReader(file)))
			{
				MutableList<float[]> vertexes = Lists.mutable.of();
				MutableList<float[]> textureUv = Lists.mutable.of();
				MutableList<float[]> normals = Lists.mutable.of();
				MutableList<Integer[][]> faces = Lists.mutable.of();
				
				int vertexLength = 3;
				int textureUvLength = 1;
				
				int quadsCount = 0;
				
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
							if (i < 3 || scanner.hasNext()) throw new IOException("Failed to parse file " + file);
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
							if (i < 1 || scanner.hasNext()) throw new IOException("Failed to parse file " + file);
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
							if (i < 3 || scanner.hasNext()) throw new IOException("Failed to parse file " + file);
							normals.add(fs);
						}
						case "f" -> {
							String[] tokens = scanner.tokens().limit(5).toArray(String[]::new);
							if (tokens.length > 4) throw new IOException("Failed to parse file " + file);
							if (tokens.length < 3) throw new IOException("Failed to parse file " + file);
							
							Integer[][] ref = new Integer[tokens.length][3];
							Integer[][] oldRef = faces.getFirstOptional().orElse(null);
							if (oldRef != null)
							{
								for (int i = 0; i < tokens.length; i++)
								{
									ref[i] = parseVertexRef(file, tokens[i]);
									for (int j = 0; j < 3; j++) if ((ref[i] == null) != (oldRef[i] == null)) throw new IOException("Invalid geometry (non uniform faces format)");
								}
							}
							else
							{
								for (int i = 0; i < tokens.length; i++) ref[i] = parseVertexRef(file, tokens[i]);
							}
							if (tokens.length == 4) quadsCount++;
							faces.add(ref);
						}
					}
				}
				if (faces.isEmpty()) return;
				if (faces.getAny()[0][0] == null) throw new IOException("Invalid geometry (missing vertex value)");
				
				MutableList<Integer[]> indices = Lists.mutable.ofInitialCapacity((faces.size() + quadsCount) * 3);
				
				for (Integer[][] face : faces)
				{
					if (face.length == 4)
					{
						indices.add(face[0]);
						indices.add(face[1]);
						indices.add(face[2]);
						indices.add(face[0]);
						indices.add(face[2]);
						indices.add(face[3]);
					}
					else
					{
						indices.add(face[0]);
						indices.add(face[1]);
						indices.add(face[2]);
					}
				}
				
				vertexCount = indices.size();
				useTextureUv = faces.getAny()[0][1] != null;
				useNormals = faces.getAny()[0][2] != null;
				if (!useTextureUv) textureUv.clear();
				if (!useNormals) normals.clear();
				this.vertexes = vertexes;
				this.indices = indices;
				this.textureUv = textureUv;
				this.normals = normals;
				this.vertexLength = vertexLength;
				this.textureUvLength = textureUvLength;
			}
		}
		
		private static Integer[] parseVertexRef(File file, String s) throws IOException
		{
			String[] ss = s.split("/");
			if (ss.length > 3) throw new IOException("Failed to parse file " + file);
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
		public String toString()
		{
			return "ObjFile{" + file + "}";
		}
	}
}