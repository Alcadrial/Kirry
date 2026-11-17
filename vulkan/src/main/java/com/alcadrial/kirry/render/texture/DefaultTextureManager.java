package com.alcadrial.kirry.render.texture;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import com.alcadrial.kirry.render.util.exception.VulkanException;
import com.alcadrial.kirry.render.util.functional.VulkanSupplier;

public class DefaultTextureManager implements TextureManager {
	
	private MutableList<Texture> textures;
	private MutableList<Atlas> atlases;
	private VulkanSupplier<Atlas> atlasFactory;
	
	public DefaultTextureManager(VulkanSupplier<Atlas> atlasFactory)
	{
		textures = Lists.mutable.of();
		atlases = Lists.mutable.of();
		this.atlasFactory = atlasFactory;
	}
	
	@Override
	public int addTexture(Texture texture)
	{
		textures.add(texture);
		return textures.size() - 1;
	}
	
	@Override
	public int addAtlas(Atlas atlas)
	{
		atlases.add(atlas);
		return atlases.size() - 1;
	}
	
	@Override
	public TextureRegion addToAtlas(Texture texture, Object id) throws VulkanException
	{
		for (Atlas atlas : atlases)
		{
			TextureRegion region = atlas.add(texture, id);
			if (region != null) return region;
		}
		Atlas atlas = atlasFactory.get();
		atlases.add(atlas);
		TextureRegion region = atlas.add(texture, id);
		if (region != null) return region;
		throw new VulkanException("Texture is too big");
	}
	
	@Override
	public TextureRegion addToAtlas(TextureBuffer texture) throws VulkanException
	{
		for (Atlas atlas : atlases)
		{
			TextureRegion region = atlas.add(texture);
			if (region != null) return region;
		}
		Atlas atlas = atlasFactory.get();
		atlases.add(atlas);
		TextureRegion region = atlas.add(texture);
		if (region != null) return region;
		throw new VulkanException("Texture is too big");
	}
	
	@Override
	public TextureRegion addToAtlas(TextureBuffer texture, Object id) throws VulkanException
	{
		for (Atlas atlas : atlases)
		{
			TextureRegion region = atlas.add(texture, id);
			if (region != null) return region;
		}
		Atlas atlas = atlasFactory.get();
		atlases.add(atlas);
		TextureRegion region = atlas.add(texture, id);
		if (region != null) return region;
		throw new VulkanException("Texture is too big");
	}
	
	@Override
	public int atlasIndexToGlobal(int index)
	{
		return index;
	}
	
	@Override
	public int textureIndexToGlobal(int index)
	{
		return index + getAtlasSize();
	}
	
	@Override
	public Texture getTexture(int index)
	{
		return textures.get(index);
	}
	
	@Override
	public Atlas getAtlas(int index)
	{
		return atlases.get(index);
	}
	
	@Override
	public Texture removeTexture(int index)
	{
		return textures.remove(index);
	}
	@Override
	public Atlas removeAtlas(int index)
	{
		return atlases.remove(index);
	}
	@Override
	public boolean removeTexture(Object id)
	{
		for (Atlas atlas : atlases)
		{
			TextureRegion region = atlas.getRegion(id);
			if (region != null)
			{
				atlas.freeRegion(region);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int getTextureSize()
	{
		return textures.size();
	}
	
	@Override
	public int getAtlasSize()
	{
		return atlases.size();
	}
	
	@Override
	public MutableList<Texture> getTextures()
	{
		return textures;
	}
	
	@Override
	public MutableList<Atlas> getAtlases()
	{
		return atlases;
	}
	
	@Override
	public void close()
	{
		for (Atlas atlas : atlases) atlas.close();
		for (Texture texture : textures) texture.close();
		atlases.clear();
		textures.clear();
	}
}
