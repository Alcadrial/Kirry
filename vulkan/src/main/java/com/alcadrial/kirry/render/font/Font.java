package com.alcadrial.kirry.render.font;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_BitmapGlyph;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FreeType;
import org.lwjgl.vulkan.VK10;

import com.alcadrial.kirry.render.Device;
import com.alcadrial.kirry.render.KirryVulkan;
import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.buffer.writer.DirectBufferWriter;
import com.alcadrial.kirry.render.texture.Atlas;
import com.alcadrial.kirry.render.texture.TextureBuffer;
import com.alcadrial.kirry.render.texture.TextureRegion;
import com.alcadrial.kirry.render.util.FreeTypeErrorUtils;
import com.alcadrial.kirry.render.util.exception.FreeTypeException;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class Font implements AutoCloseable {
	
	private Path path;
	private long faceIndex;
	private FT_Face face;
	private Device device;
	private Atlas atlas;
	//private MemoryManager memoryManager;
	private HashMap<Integer, TextureRegion> regions;
	//private MutableList<VulkanBuffer> glyphDataBuffers;
	
	public Font(Device device, Path path, long faceIndex/*, MemoryManager memoryManager*/) throws VulkanException, FreeTypeException, IOException
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			this.device = device;
			this.path = path;
			this.faceIndex = faceIndex;
			PointerBuffer p = stack.mallocPointer(1);
			if (FreeType.FT_New_Face(KirryVulkan.getFTLibrary(), path.toRealPath().toString(), faceIndex, p) != 0) throw new IOException("Failed to load font file");
			face = FT_Face.create(p.get(0));
			atlas = new Atlas(device, VK10.VK_FORMAT_B8G8R8A8_SRGB, VK10.VK_IMAGE_USAGE_SAMPLED_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
			setSize(16);
			regions = new HashMap<>();
		//	this.memoryManager = memoryManager;
		//	glyphDataBuffers = Lists.mutable.of();
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
	
	public TextureRegion getCharRegion(int codepoint) throws FreeTypeException, VulkanException
	{
		return drawCharBitmap(FreeType.FT_Get_Char_Index(face, codepoint));
	}
	
	private TextureRegion drawCharBitmap(int glyphIndex) throws FreeTypeException, VulkanException
	{
		if (glyphIndex == 0) return new TextureRegion(null, 0, 0, 0, 0);
		{
			TextureRegion region = atlas.getRegion(glyphIndex);
			if (region != null) return region;
		}
		
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer p = stack.mallocPointer(1);
			FreeTypeErrorUtils.assertNoError(FreeType.FT_Load_Glyph(face, glyphIndex, FreeType.FT_LOAD_NO_BITMAP));
			FT_GlyphSlot slot = FreeTypeErrorUtils.nonNull(face.glyph());
			FreeTypeErrorUtils.assertNoError(FreeType.FT_Get_Glyph(slot, p));
			FreeTypeErrorUtils.assertNoError(FreeType.FT_Glyph_To_Bitmap(p, FreeType.FT_RENDER_MODE_NORMAL, null, true));
			FT_BitmapGlyph bitmapGlyph = FT_BitmapGlyph.create(p.get(0));
			
			FT_Bitmap bitmap = bitmapGlyph.bitmap();
			
			if (bitmap.rows() == 0)
			{
				return new TextureRegion(null, 0, 0, 0, 0);
			}
			
			ByteBuffer data = FreeTypeErrorUtils.nonNull(bitmap.buffer(bitmap.rows() * bitmap.pitch()));
			//memoryManager.allocateHost(bitmap.width() * bitmap.rows() * 4, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
			VulkanBuffer buffer = new VulkanBuffer(device, bitmap.width() * bitmap.rows() * 4, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);
			buffer.setName("Font buffer " + glyphIndex);
			DirectBufferWriter writer = new DirectBufferWriter(buffer);
			switch (bitmap.pixel_mode())
			{
				case FreeType.FT_PIXEL_MODE_BGRA -> {
					writer.put(data).flush();
				}
				case FreeType.FT_PIXEL_MODE_GRAY -> {
					for (int i = 0; i < data.limit(); i++)
					{
						int a = data.get();
						if (a == 0) writer.put(0);
						else writer.put(0xFF000000 | (a << 16) | (a << 8) | a);
					}
				}
				default -> {
					throw new FreeTypeException("Unsupported format (pixel mode) " + bitmap.pixel_mode());
				}
			}
			return atlas.add(TextureBuffer.create(buffer, bitmap.width(), bitmap.rows(), glyphIndex));
		}
	}
	
	public Atlas getAtlas()
	{
		return atlas;
	}
	
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
	
	@Override
	public void close()
	{
		FreeType.FT_Done_Face(face);
		atlas.close();
	}
	
	@Override
	public String toString()
	{
		return "Font{name=" + FreeType.FT_Get_Postscript_Name(face) + ", path=" + path + ", faceIndex=" + faceIndex + "}";
	}
}
