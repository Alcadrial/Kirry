package com.alcadrial.kirry.render.texture;

import org.eclipse.collections.api.list.MutableList;

import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.util.functional.VulkanSupplier;

public interface TextureManager extends AutoCloseable {

	static TextureManager getDefault(VulkanSupplier<Atlas> atlasFactory)
	{
		return new DefaultTextureManager(atlasFactory);
	}
	
	int addTexture(Texture texture);
	int addAtlas(Atlas atlas);
	TextureRegion addToAtlas(Texture texture, Object id) throws VulkanException;
	TextureRegion addToAtlas(TextureBuffer texture) throws VulkanException;
	TextureRegion addToAtlas(TextureBuffer texture, Object id) throws VulkanException;
	
	Texture getTexture(int index);
	Atlas getAtlas(int index);
	
	int getTextureSize();
	int getAtlasSize();
	
	int textureIndexToGlobal(int index);
	int atlasIndexToGlobal(int index);
	
	MutableList<Texture> getTextures();
	MutableList<Atlas> getAtlases();
	
	@Override
	void close();
}
