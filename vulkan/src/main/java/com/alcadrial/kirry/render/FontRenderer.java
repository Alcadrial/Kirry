package com.alcadrial.kirry.render;

import java.io.File;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Pointer;
import org.lwjgl.util.freetype.FT_BBox;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_BitmapGlyph;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_Glyph;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FT_Matrix;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.FreeType;
import org.lwjgl.vulkan.VK10;

import com.alcadrial.kirry.render.buffer.VulkanBuffer;
import com.alcadrial.kirry.render.buffer.writer.DirectBufferWriter;
import com.alcadrial.kirry.render.texture.Texture;
import com.alcadrial.kirry.render.util.exception.VulkanException;

public class FontRenderer implements AutoCloseable {
	
	static
	{
		KirryVulkan.assertLoaded();
	}
	
	private Device device;
	private @Nullable FT_Face face;
	private @Nullable Texture texture;
	
	public FontRenderer(Device device)
	{
		this.device = device;
	}
	
	public void loadFont(File fontFile, long faceIndex)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer p = stack.mallocPointer(1);
			check(FreeType.FT_New_Face(KirryVulkan.getFTLibrary(), fontFile.getPath(), faceIndex, p), "Failed to load font file");
			face = FT_Face.create(p.get(0));
		}
	}
	
	public long getFacesCount(File fontFile)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer p = stack.mallocPointer(1);
			check(FreeType.FT_New_Face(KirryVulkan.getFTLibrary(), fontFile.getPath(), -1, p), "Failed to load font file");
			FT_Face face = FT_Face.create(p.get(0));
			long n = face.num_faces();
			FreeType.FT_Done_Face(face);
			return n;
		}
	}
	
	public void setSize(int width, int height)
	{
		check(FreeType.FT_Set_Pixel_Sizes(getFace(), width, height), "Unsupported size");
	}
	
	public void renderText0(int x, int y, CharSequence text) throws VulkanException
	{
		if (text.isEmpty()) return;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer p = stack.mallocPointer(1);
			FT_Face face = getFace();
			
			Texture texture = Texture.create2dTexture(device, 1000, 200, VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_SAMPLED_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
			
			VulkanBuffer buffer = new VulkanBuffer(device, texture.getLength() * 4, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_CACHED_BIT, 0);
			IntBuffer bufferData = buffer.getMemory().mapMemory().getBuffer().asIntBuffer();
			MemorySegment segment = MemorySegment.ofBuffer(buffer.getMemory().mapMemory().getBuffer());
			
			DirectBufferWriter writer = new DirectBufferWriter(buffer);
			segment.fill((byte) 0);
			
			boolean useKerning = FreeType.FT_HAS_KERNING(face);
			FT_Vector delta = FT_Vector.malloc(stack);
			
			int previous = 0;
			
			int posX = 50;
			int posY = 150;
			
			//	int minX = posX;
			//	int minY = posY;
			//	int maxX = posX;
			//	int maxY = posY;
			
			for (int i = 0; i < text.length(); i++)
			{
				int glyphIndex = FreeType.FT_Get_Char_Index(face, text.charAt(i));
				
				if (useKerning && previous != 0 && glyphIndex != 0)
				{
					if (FreeType.FT_Get_Kerning(face, previous, glyphIndex, FreeType.FT_KERNING_DEFAULT, delta) != 0) continue;
					posX += delta.x() >> 6;
				}
				
				if (FreeType.FT_Load_Glyph(face, glyphIndex, FreeType.FT_LOAD_NO_BITMAP) != 0) continue;
				FT_GlyphSlot slot = face.glyph();
				if (slot == null) continue;
				if (FreeType.FT_Get_Glyph(slot, p) != 0) continue;
				if (FreeType.FT_Glyph_To_Bitmap(p, FreeType.FT_RENDER_MODE_NORMAL, null, true) != 0) continue;
				
				FT_BitmapGlyph bitmapGlyph = FT_BitmapGlyph.create(p.get(0));
				
				FT_Bitmap bitmap = bitmapGlyph.bitmap();
				ByteBuffer data = ((@NonNull ByteBuffer) bitmap.buffer(bitmap.rows() * bitmap.pitch()));
				
				int glyphX = posX + bitmapGlyph.left();
				int glyphY = posY - bitmapGlyph.top();
				
				for (int j = 0; j < bitmap.rows(); j++)
				{
					writer.setCursor(glyphX + (glyphY + j) * texture.getWidth());
					data.position(j * bitmap.pitch());
					bufferData.position(glyphX + (glyphY + j) * texture.getWidth());
					for (int k = 0; k < bitmap.pitch(); k++)
					{
						int s = data.get();
						int d = bufferData.get();
						float sa = s != 0 ? 1 : 0;
						float da = ((d >>> 24) & 0xFF) / 255F;
						int r = (int) ((255 * sa + ((d >>> 0) & 0xFF) * (1 - sa)));
						int g = (int) ((255 * sa + ((d >>> 8) & 0xFF) * (1 - sa)));
						int b = (int) ((255 * sa + ((d >>> 16) & 0xFF) * (1 - sa)));
						int a = (int) ((1 - (1 - sa) * (1 - da)) * 255);
						writer.put((r << 0) | (g << 8) | (b << 16) | (a << 24));
					}
				}
				
				posX += bitmapGlyph.root().advance().x() >> 16;
				posY += bitmapGlyph.root().advance().y() >> 16;
			}
			
			device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), commandBuffer -> {
				texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
				commandBuffer.copyBufferToImage(buffer, texture);
				texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
			}).join();
			
			buffer.getMemory().unmapMemory();
			buffer.close();
			if (this.texture != null) this.texture.close();
			this.texture = texture;
		}
	}
	
	public void renderText(int x, int y, CharSequence text) throws VulkanException
	{
		if (text.isEmpty()) return;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer p = stack.mallocPointer(1);
			FT_BitmapGlyph.Buffer glyphs = FT_BitmapGlyph.create(stack.ncalloc(Pointer.POINTER_SIZE, FT_BitmapGlyph.SIZEOF, text.length()), text.length());
			FT_BBox bbox = FT_BBox.malloc(stack);
			
			FT_Face face = getFace();
			
			boolean useKerning = FreeType.FT_HAS_KERNING(face);
			FT_Vector delta = FT_Vector.malloc(stack);
			FT_Vector origin = FT_Vector.malloc(stack).x(0).y(0);
			FT_Matrix matrix = FT_Matrix.malloc(stack).set(0x11000, 0x6250, 0, 0x10000);
			
			int previous = 0;
			
			int posX = 0;
			int posY = 0;
			
			int minX = posX;
			int minY = posY;
			int maxX = posX;
			int maxY = posY;
			
			for (int i = 0; i < text.length(); i++)
			{
				int glyphIndex = FreeType.FT_Get_Char_Index(face, text.charAt(i));
				
				if (useKerning && previous != 0 && glyphIndex != 0)
				{
					if (FreeType.FT_Get_Kerning(face, previous, glyphIndex, FreeType.FT_KERNING_DEFAULT, delta) != 0) continue;
					posX += delta.x() >> 6;
				}
				
				if (FreeType.FT_Load_Glyph(face, glyphIndex, FreeType.FT_LOAD_NO_BITMAP) != 0) continue;
				if (FreeType.FT_Get_Glyph((@NonNull FT_GlyphSlot) face.glyph(), p) != 0) continue;
				FT_Glyph glyph = FT_Glyph.create(p.get(0));
				FreeType.FT_Glyph_Transform(glyph, matrix, null);
				FreeType.FT_Glyph_Get_CBox(glyph, FreeType.FT_GLYPH_BBOX_PIXELS, bbox);
				
				if (FreeType.FT_Glyph_To_Bitmap(p, FreeType.FT_RENDER_MODE_NORMAL, origin, false) != 0)
				{
					FreeType.FT_Done_Glyph(glyph);
					continue;
				}
				glyphs.put(i, FT_BitmapGlyph.create(p.get(0)));
				
				minX = (int) Math.min(minX, bbox.xMin() + posX);
				maxX = (int) Math.max(maxX, bbox.xMax() + posX);
				minY = (int) Math.min(minY, bbox.yMin() + posY);
				maxY = (int) Math.max(maxY, bbox.yMax() + posY);
				
				posX += glyph.advance().x() >> 16;
				posY += glyph.advance().y() >> 16;
			}
			
			Texture texture = Texture.create2dTexture(device, maxX - minX, maxY - minY, VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_USAGE_SAMPLED_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
			
			VulkanBuffer buffer = new VulkanBuffer(device, texture.getLength() * 4, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_CACHED_BIT, 0);
			IntBuffer bufferData = buffer.getMemory().mapMemory().getBuffer().asIntBuffer();
			MemorySegment segment = MemorySegment.ofBuffer(buffer.getMemory().mapMemory().getBuffer());
			
			DirectBufferWriter writer = new DirectBufferWriter(buffer);
			segment.fill((byte) 0);
			
			posX = -minX;
			posY = maxY;
			
			for (int i = 0; i < text.length(); i++)
			{
				FT_BitmapGlyph glyph = glyphs.get(i);
				if (glyph == null) continue;
				
				int glyphIndex = FreeType.FT_Get_Char_Index(face, text.charAt(i));
				
				if (useKerning && previous != 0 && glyphIndex != 0)
				{
					FreeType.FT_Get_Kerning(face, previous, glyphIndex, FreeType.FT_KERNING_DEFAULT, delta);
					posX += delta.x() >> 6;
				}
				
				FT_Bitmap bitmap = glyph.bitmap();
				ByteBuffer data = (@NonNull ByteBuffer) bitmap.buffer(bitmap.rows() * bitmap.pitch());
				
				int glyphX = posX + glyph.left();
				int glyphY = posY - glyph.top();
				
				for (int j = 0; j < bitmap.rows(); j++)
				{
					writer.setCursor(glyphX + (glyphY + j) * texture.getWidth());
					data.position(j * bitmap.pitch());
					bufferData.position(glyphX + (glyphY + j) * texture.getWidth());
					for (int k = 0; k < bitmap.pitch(); k++)
					{
						int s = data.get();
						int d = bufferData.get();
						float sa = s != 0 ? 1 : 0;
						float da = ((d >>> 24) & 0xFF) / 255F;
						int r = (int) ((255 * sa + ((d >>> 0) & 0xFF) * (1 - sa)));
						int g = (int) ((255 * sa + ((d >>> 8) & 0xFF) * (1 - sa)));
						int b = (int) ((255 * sa + ((d >>> 16) & 0xFF) * (1 - sa)));
						int a = (int) ((1 - (1 - sa) * (1 - da)) * 255);
						writer.put((r << 0) | (g << 8) | (b << 16) | (a << 24));
					}
				}
				
				posX += glyph.root().advance().x() >> 16;
				posY -= glyph.root().advance().y() >> 16;
			}
			
			device.configureAndSubmitSingleUseCommandBuffer(device.getGraphicsQueue(), commandBuffer -> {
				texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
				commandBuffer.copyBufferToImage(buffer, texture);
				texture.changeLayout(commandBuffer, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
			}).join();
			
			buffer.getMemory().unmapMemory();
			buffer.close();
			if (this.texture != null) this.texture.close();
			this.texture = texture;
		}
	}
	
	public @Nullable Texture getTexture()
	{
		return texture;
	}
	
	public void drawFontAtlas()
	{
		
	}
	
	@Override
	public void close()
	{
		if (face != null) FreeType.FT_Done_Face(face);
	}
	
	private FT_Face getFace()
	{
		return check(face, "A Font must be loaded");
	}
	
	private void check(int error, String message)
	{
		if (error != 0)
		{
			throw new IllegalStateException(message + ": " + error);
		}
	}
	
	private <T> @NonNull T check(@Nullable T obj, String message)
	{
		if (obj == null) throw new IllegalStateException(message);
		return obj;
	}
}
