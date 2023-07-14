package com.alcadrial.renderengine.render.shader;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CompiledShader {
	
	private static final CompiledShader FAIL = new CompiledShader();
	
	static
	{
		FAIL.hasErrors = true;
	}
	
	static CompiledShader ofSuccess(String[] code, byte[] binary)
	{
		CompiledShader compiled = new CompiledShader();
		compiled.hasErrors = false;
		compiled.binary = binary;
		
		Map<ShaderInput, Integer> inputLocations = new HashMap<>();
		for (String line : code)
		{
			line = line.stripLeading();
			if (line.startsWith("layout"))
			{
				String locationString = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
				int li = locationString.indexOf("location");
				if (li >= 0)
				{
					locationString = locationString.substring(li + "location".length() + 1);
					int ei = locationString.indexOf("=");
					if (ei >= 0)
					{
						locationString = locationString.substring(ei + 2).trim();
						try
						{
							int location = Integer.parseInt(locationString);
							line = line.substring(line.indexOf(")") + 1).stripLeading();
							if (line.startsWith("in "))
							{
								String line1 = line.substring(3).stripLeading();
								int i = line1.indexOf(" ");
								if (i != -1 && i >= 1)
								{
									String line2 = line1.substring(i).stripLeading();
									int j0 = line2.indexOf(";");
									if (j0 != -1 && j0 == line2.length() - 1)
									{
										int j1 = line2.indexOf(" ");
										if (j1 == -1 || line2.substring(j1).isBlank())
										{
											String typeName = line1.substring(0, i);
											String inputName = line2.substring(0, j1 == -1 ? j0 : j1);
											inputLocations.put(ShaderInput.of(ShaderInputType.of(typeName), inputName), location);
										}
									}
								}
							}
						}
						catch (NumberFormatException e)
						{}
					}
				}
			}
		}
		compiled.inputLocations = inputLocations;
		return compiled;
	}
	
	public static CompiledShader ofFail()
	{
		return FAIL;
	}
	
	private boolean hasErrors;
	private byte[] binary;
	private Map<ShaderInput, Integer> inputLocations;
	
	public boolean hasErrors()
	{
		return hasErrors;
	}
	
	public byte[] getBinary()
	{
		return binary;
	}
	
	public Set<ShaderInput> getInputs()
	{
		return inputLocations.keySet();
	}
	
	public int getInputLocation(ShaderInput input)
	{
		return inputLocations.get(input);
	}
}
