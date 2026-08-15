package com.alcadrial.kirry.render.font;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_BitmapGlyph;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FreeType;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferMemoryBarrier;

import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.LinearComputer;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.buffer.writer.BufferWriter;
import com.alcadrial.kirry.render.buffer.writer.DirectBufferWriter;
import com.alcadrial.kirry.render.commandbuffer.CommandBuffer;
import com.alcadrial.kirry.render.memory.MemoryManager;
import com.alcadrial.kirry.render.shader.PreparedShader;
import com.alcadrial.kirry.render.shader.ShaderInputType;
import com.alcadrial.kirry.render.shader.ShaderVariable;
import com.alcadrial.kirry.render.texture.Atlas;
import com.alcadrial.kirry.render.texture.TextureBuffer;
import com.alcadrial.kirry.render.texture.TextureRegion;
import com.alcadrial.kirry.render.util.FreeTypeErrorUtils;
import com.alcadrial.kirry.render.util.exception.FreeTypeException;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class Font implements AutoCloseable {
	
	public static final ShaderVariable GLYPH_SIZE = ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_2, "size");
	public static final ShaderVariable GLYPH_BEARING = ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_2, "bearing");
	public static final ShaderVariable GLYPH_UV_START = ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_2, "uvStart");
	public static final ShaderVariable GLYPH_UV_END = ShaderVariable.of(ShaderInputType.VECTOR_FLOAT_2, "uvEnd");
	public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890.,:'?!\"()][}{<>¡!~";
	public static final String LATIN_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890àèìùòäëïöü.,:'?!\"";
	
	public static final String FONT_VERTEX_SHADER_NAME = "font.vert";
	public static final String FONT_VERTEX_SHADER_PATHNAME = KirryVulkan.SHADERS_PATH + FONT_VERTEX_SHADER_NAME;
	public static final String FONT_GEOMETRY_SHADER_NAME = "font.geom";
	public static final String FONT_GEOMETRY_SHADER_PATHNAME = KirryVulkan.SHADERS_PATH + FONT_GEOMETRY_SHADER_NAME;
	public static final String FONT_FRAGMENT_SHADER_NAME = "font.frag";
	public static final String FONT_FRAGMENT_SHADER_PATHNAME = KirryVulkan.SHADERS_PATH + FONT_FRAGMENT_SHADER_NAME;
	
	public static class GlyphData {
		
		public static final int SIZE = 12;
		
		private int index;
		private float width;
		private float height;
		private float advanceX;
		private float advanceY;
		private float horizontalBearingX;
		private float horizontalBearingY;
		private float verticalBearingX;
		private float verticalBearingY;
		private float startU;
		private float startV;
		private float endU;
		private float endV;
		
		public int getIndex()
		{
			return index;
		}
		
		public float getWidth()
		{
			return width;
		}
		
		public float getHeight()
		{
			return height;
		}
		
		public float getAdvanceX()
		{
			return advanceX;
		}
		
		public float getAdvanceY()
		{
			return advanceY;
		}
		
		public float getHorizontalBearingX()
		{
			return horizontalBearingX;
		}
		
		public float getHorizontalBearingY()
		{
			return horizontalBearingY;
		}
		
		public float getVerticalBearingX()
		{
			return verticalBearingX;
		}
		
		public float getVerticalBearingY()
		{
			return verticalBearingY;
		}
		
		public float getStartU()
		{
			return startU;
		}
		
		public float getStartV()
		{
			return startV;
		}
		
		public float getEndU()
		{
			return endU;
		}
		
		public float getEndV()
		{
			return endV;
		}
		
		public boolean isEmpty()
		{
			return startU == endU;
		}
		
		private void load(BufferWriter writer)
		{
			writer.put(
				width, height,
				advanceX, advanceY,
				horizontalBearingX, horizontalBearingY,
				verticalBearingX, verticalBearingY,
				startU, startV,
				endU, endV
			);
		}
		
		@Override
		public String toString()
		{
			return "GlyphData{index=" + index + "}";
		}
	}
	
	private static final int BLOCK_SIZE = 0x10000;
	
	private final Path path;
	private final long faceIndex;
	private final FT_Face face;
	private final Arena fileDataArena;
	
	private String alphabet;
	private Atlas atlas;
	private final MemoryManager memoryManager;
	
	private int nextDataIndex;
	private VulkanBuffer localFontData;
	private DirectBufferWriter localFontDataWriter;
	private VulkanBuffer remoteFontData;
	private final LinearComputer linearComputer;
	
	private GlyphData emptyGlyphData;
	private GlyphData[] hotData;
	private int hotDataRangeStart;
	private int hotDataRangeEnd;
	private MutableIntObjectMap<GlyphData> loadedGlyphs;
	
	public Font(Device device, Path path, long faceIndex, int dataBufferUsage, MemoryManager memoryManager, PreparedShader computeShader, @Nullable VulkanBuffer uniformBuffer) throws VulkanException, FreeTypeException, IOException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			this.path = path;
			this.faceIndex = faceIndex;
			fileDataArena = Arena.ofConfined();
			
			try (FileChannel channel = FileChannel.open(path))
			{
				PointerBuffer p = stack.mallocPointer(1);
				MemorySegment segment = fileDataArena.allocate(channel.size());
				channel.read(segment.asByteBuffer());
				
				if (FreeType.FT_New_Memory_Face(KirryVulkan.getFTLibrary(), segment.asByteBuffer(), faceIndex, p) != 0) throw new IOException("Failed to load font file");
				face = FT_Face.create(p.get(0));
			}
			
			alphabet = ALPHABET;
			atlas = new Atlas(device, VK10.VK_FORMAT_B8G8R8A8_SRGB, VK10.VK_IMAGE_USAGE_SAMPLED_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT, -1);
			atlas.setOnStructuralChange(this::updateUvs);
			setSize(16);
			
			this.memoryManager = memoryManager;
			localFontData = memoryManager.allocateHost(BLOCK_SIZE, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
			localFontDataWriter = new DirectBufferWriter(localFontData);
			remoteFontData = memoryManager.allocateHost(BLOCK_SIZE, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | dataBufferUsage);
			nextDataIndex = 0;
			
			hotDataRangeStart = 32;
			hotDataRangeEnd = 128;
			hotData = new GlyphData[hotDataRangeEnd - hotDataRangeStart];
			emptyGlyphData = new GlyphData();
			emptyGlyphData.index = 0;
			loadedGlyphs = IntObjectMaps.mutable.of();
			
			linearComputer = LinearComputer.ofUpdate(device, computeShader, uniformBuffer, ShaderVariable.MATRIX, remoteFontData, ShaderVariable.UVUV);
			linearComputer.init();
			linearComputer.setInputBuffer(remoteFontData);
			linearComputer.setOffset(0);
			linearComputer.setStride(4 * 2 * 4);
			linearComputer.setSize(6 * 2 * 4);
		}
	}
	
	public void setSize(int size) throws FreeTypeException
	{
		FreeTypeErrorUtils.assertNoError(FreeType.FT_Set_Pixel_Sizes(face, size, size));
		atlas.clear(size * 8, size * 8);
	}
	
	public void setSize(int width, int height) throws FreeTypeException
	{
		FreeTypeErrorUtils.assertNoError(FreeType.FT_Set_Pixel_Sizes(face, width, height));
		atlas.clear(width * 8, height * 8);
	}
	
	public float getSize()
	{
		return atlas.getWidth() / 8F;
	}
	
	public void setAlphabet(String alphabet)
	{
		this.alphabet = alphabet;
	}
	
	public float getHeight()
	{
		return FreeTypeErrorUtils.nonNull(face.size(), "Unknown size, call #setSize").metrics().height() >> 6;
	}
	
	public float getAscender()
	{
		return face.ascender() >> 6;
	}
	
	public float getDescender()
	{
		return face.descender() >> 6;
	}
	
	public int getCharIndex(int codepoint)
	{
		return FreeType.FT_Get_Char_Index(face, codepoint);
	}
	
	public Atlas getAtlas()
	{
		return atlas;
	}
	
	public void prepare(CommandBuffer commandBuffer) throws VulkanException
	{
		loadAlphabet();
		if (atlas.isStructurallyModified()) atlas.tryFit();
		atlas.draw(commandBuffer);
		commandBuffer.copyBuffer(localFontData, 0, remoteFontData, 0, remoteFontData.getSize());
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			commandBuffer.pipelineBarrier(VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, 0, null, VkBufferMemoryBarrier.malloc(1, stack)
				.sType$Default()
				.pNext(0)
				.srcAccessMask(VK10.VK_ACCESS_MEMORY_WRITE_BIT)
				.dstAccessMask(VK10.VK_ACCESS_SHADER_READ_BIT)
				.srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
				.dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
				.buffer(remoteFontData.getVk())
				.offset(0)
				.size(remoteFontData.getSize()), null);
		}
		float ws = 1F / atlas.getWidth();
		float hs = 1F / atlas.getHeight();
		linearComputer.setMatrix(new Matrix4f(new float[] {ws, 0, 0, 0, 0, hs, 0, 0, 0, 0, ws, 0, 0, 0, 0, hs}));
		linearComputer.dispatch(commandBuffer, nextDataIndex);
	}
	
	public void loadAlphabet() throws VulkanException
	{
		for (char c : alphabet.toCharArray())
		{
			try
			{
				getData(c);
			}
			catch (FreeTypeException _)
			{}
		}
	}
	
	public void loadGlyphs(String glyphs) throws VulkanException
	{
		for (char c : glyphs.toCharArray())
		{
			try
			{
				getData(c);
			}
			catch (FreeTypeException _)
			{}
		}
	}
	/*
	 * TODO use resources
	public void reload() throws FreeTypeException, VulkanException, IOException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			FreeType.FT_Done_Face(face);
			atlas.close();
			PointerBuffer p = stack.mallocPointer(1);
			if (FreeType.FT_New_Face(KirryVulkan.getFTLibrary(), path.toRealPath().toString(), faceIndex, p) != 0) throw new IOException("Failed to load font file");
			face = FT_Face.create(p.get(0));
			atlas = new Atlas(device, VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_SAMPLED_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
			setSize(16);
			regions.clear();
			
		}
	}
	*/
	
	@Override
	public void close()
	{
		FreeType.FT_Done_Face(face);
		fileDataArena.close();
		atlas.close();
	}
	
	@Override
	public String toString()
	{
		return "Font{name=" + FreeType.FT_Get_Postscript_Name(face) + ", path=" + path + ", faceIndex=" + faceIndex + "}";
	}
	
	public VulkanBuffer getLocalData()
	{
		return localFontData;
	}
	
	public VulkanBuffer getRemoteData()
	{
		return remoteFontData;
	}
	
	public GlyphData getData(int codepoint) throws FreeTypeException, VulkanException
	{
		if (codepoint >= hotDataRangeStart && codepoint < hotDataRangeEnd)
		{
			GlyphData glyphData = hotData[codepoint - hotDataRangeStart];
			if (glyphData != null) return glyphData;
			glyphData = loadGlyph(codepoint);
			if (glyphData != null)
			{
				hotData[codepoint - hotDataRangeStart] = glyphData;
				return glyphData;
			}
			hotData[codepoint - hotDataRangeStart] = emptyGlyphData;
			return emptyGlyphData;
		}
		GlyphData glyphData = loadedGlyphs.get(codepoint);
		if (glyphData != null) return glyphData;
		glyphData = loadGlyph(codepoint);
		
		if (glyphData != null)
		{
			loadedGlyphs.put(codepoint, glyphData);
			return glyphData;
		}
		return emptyGlyphData;
	}
	
	public int getGlyphIndex(int codepoint) throws FreeTypeException, VulkanException
	{
		return getData(codepoint).index;
	}
	
	private @Nullable GlyphData loadGlyph(int codepoint) throws FreeTypeException, VulkanException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			int glyphIndex = FreeType.FT_Get_Char_Index(face, codepoint);
			
			FreeTypeErrorUtils.assertNoError(FreeType.FT_Load_Glyph(face, glyphIndex, FreeType.FT_LOAD_NO_BITMAP));
			FT_GlyphSlot slot = FreeTypeErrorUtils.nonNull(face.glyph());
			
			PointerBuffer p = stack.mallocPointer(1);
			FreeTypeErrorUtils.assertNoError(FreeType.FT_Get_Glyph(slot, p));
			FreeTypeErrorUtils.assertNoError(FreeType.FT_Glyph_To_Bitmap(p, FreeType.FT_RENDER_MODE_NORMAL, null, true));
			FT_BitmapGlyph bitmapGlyph = FT_BitmapGlyph.create(p.get(0));
			
			FT_Bitmap bitmap = bitmapGlyph.bitmap();
			
			//empty glyph
			if (bitmap.rows() == 0)
			{
				GlyphData glyphData = new GlyphData();
				glyphData.index = nextDataIndex++;
				glyphData.width = slot.metrics().width() / 64F;
				glyphData.height = slot.metrics().height() / 64F;
				glyphData.advanceX = slot.metrics().horiAdvance() / 64F;
				glyphData.advanceY = slot.metrics().vertAdvance() / 64F;
				glyphData.horizontalBearingX = slot.metrics().horiBearingX() / 64F;
				glyphData.horizontalBearingY = slot.metrics().horiBearingY() / 64F;
				glyphData.verticalBearingX = slot.metrics().vertBearingX() / 64F;
				glyphData.verticalBearingY = slot.metrics().vertBearingY() / 64F;
				glyphData.startU = 0;
				glyphData.startV = 0;
				glyphData.endU = 0;
				glyphData.endV = 0;
				glyphData.load(localFontDataWriter);
				return glyphData;
			}
			
			VulkanBuffer buffer = memoryManager.allocateHost(bitmap.width() * bitmap.rows() * 4, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
			buffer.setName("Font buffer " + glyphIndex);
			DirectBufferWriter writer = new DirectBufferWriter(buffer);
			
			ByteBuffer data = FreeTypeErrorUtils.nonNull(bitmap.buffer(bitmap.rows() * bitmap.pitch()));
			switch (bitmap.pixel_mode())
			{
				case FreeType.FT_PIXEL_MODE_BGRA -> {
					writer.put(data).flush();
				}
				case FreeType.FT_PIXEL_MODE_GRAY -> {
					for (int i = 0; i < data.limit(); i++)
					{
						int a = data.get() & 0xFF;
						if (a == 0) writer.put(0);
						else writer.put((a << 24) | 0x00FFFFFF);
					}
				}
				default -> {
					throw new FreeTypeException("Unsupported format (pixel mode) " + bitmap.pixel_mode());
				}
			}
			TextureRegion region = atlas.add(TextureBuffer.create(buffer, bitmap.pitch(), bitmap.rows(), codepoint));
			if (region == null)
			{
				//TODO: try to remove old/unused glyphs
				buffer.close();
				System.out.println("Failed to draw char into atlas");
				return null;
			}
			GlyphData glyphData = new GlyphData();
			glyphData.index = nextDataIndex++;
			glyphData.width = slot.metrics().width() / 64F;
			glyphData.height = slot.metrics().height() / 64F;
			glyphData.advanceX = slot.metrics().horiAdvance() / 64F;
			glyphData.advanceY = slot.metrics().vertAdvance() / 64F;
			glyphData.horizontalBearingX = slot.metrics().horiBearingX() / 64F;
			glyphData.horizontalBearingY = slot.metrics().horiBearingY() / 64F;
			glyphData.verticalBearingX = slot.metrics().vertBearingX() / 64F;
			glyphData.verticalBearingY = slot.metrics().vertBearingY() / 64F;
			glyphData.startU = region.getXStart();
			glyphData.startV = region.getYStart();
			glyphData.endU = region.getXEnd();
			glyphData.endV = region.getYEnd();
			glyphData.load(localFontDataWriter);
			return glyphData;
		}
	}
	
	private void updateUvs() throws VulkanException
	{
		for (int i = 0; i < hotData.length; i++)
		{
			if (updateGlyph(hotData[i], i + hotDataRangeStart)) hotData[i] = null;
		}
		for (IntObjectPair<GlyphData> p : loadedGlyphs.keyValuesView())
		{
			if (updateGlyph(p.getTwo(), p.getOne())) loadedGlyphs.remove(p.getOne());
		}
	}
	
	private boolean updateGlyph(@Nullable GlyphData glyphData, int codepoint) throws VulkanException
	{
		TextureRegion region = atlas.getRegion(codepoint);
		if (region == null) return true;
		if (glyphData == null)
		{
			try
			{
				loadGlyph(codepoint);
			}
			catch (FreeTypeException _)
			{
				atlas.freeRegion(region);
			}
			return false;
		}
		glyphData.startU = region.getXStart();
		glyphData.startV = region.getYStart();
		glyphData.endU = region.getXEnd();
		glyphData.endV = region.getYEnd();
		
		localFontDataWriter.setCursor(glyphData.index * 12 + 8).put(
			glyphData.startU, glyphData.startV,
			glyphData.endU, glyphData.endV
		);
		localFontDataWriter.setCursor(nextDataIndex * 12);
		return false;
	}
}
