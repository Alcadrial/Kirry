package com.alcadrial.renderengine.render.resources.filesystem;

import java.nio.file.CopyOption;
import java.nio.file.Path;

public class MergedCopyOptions {
	
	private MergedCopyOptions()
	{}
	
	public static CopyOption fromSource(Path sourcePath)
	{
		return new FromSourceCopyOption(sourcePath);
	}
	
	public static CopyOption toSource(Path sourcePath)
	{
		return new ToSourceCopyOption(sourcePath);
	}
	
	public static abstract class SourceCopyOption implements CopyOption {
		
		private Path sourcePath;
		
		protected SourceCopyOption(Path sourcePath)
		{
			this.sourcePath = sourcePath;
		}
		
		public Path getSourcePath()
		{
			return sourcePath;
		}
	}
	
	public static final class FromSourceCopyOption extends SourceCopyOption {

		private FromSourceCopyOption(Path sourcePath)
		{
			super(sourcePath);
		}
	}
	
	public static final class ToSourceCopyOption extends SourceCopyOption {

		private ToSourceCopyOption(Path sourcePath)
		{
			super(sourcePath);
		}
	}
}
