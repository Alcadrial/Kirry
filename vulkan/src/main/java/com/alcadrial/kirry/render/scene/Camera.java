package com.alcadrial.kirry.render.scene;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.alcadrial.kector.Math;
import com.alcadrial.kector.annotation.Angle;
import com.alcadrial.kector.matrix.Matrix4f;
import com.alcadrial.kector.vector.Vector2fr;
import com.alcadrial.kector.vector.Vector3f;
import com.alcadrial.kector.vector.Vector3fr;
import com.alcadrial.kector.vector.quaternion.Quaternionf;
import com.alcadrial.kector.vector.quaternion.Quaternionfr;

public class Camera {
	
	private @Nullable Matrix4f projection;
	private @Angle float fieldOfView;
	private float maxDistance;

	private @Nullable Quaternionf flatViewQ;
	private @Nullable Matrix4f view;
	private Vector3f position;
	private @Angle float xRotation;
	private @Angle float yRotation;
	
	public Camera(@Angle float fieldOfView, float maxDistance)
	{
		this.fieldOfView = fieldOfView;
		this.maxDistance = maxDistance;
		position = Vector3f.of();
		xRotation = 0;
		yRotation = 0;
	}
	
	public Quaternionfr getFlatViewQuaternion()
	{
		if (flatViewQ == null) flatViewQ = Quaternionf.ofAngles(0, -yRotation / 2, 0);
		return (@NonNull Quaternionfr) flatViewQ;
	}
	
	public Matrix4f getView()
	{
		if (view == null) view = new Matrix4f().view(position, xRotation, yRotation);
		return (@NonNull Matrix4f) view;
	}
	
	public boolean isViewDirty()
	{
		return view == null;
	}
	
	public Matrix4f getProjection(float aspectRatio, boolean recalculate)
	{
		if (projection == null || recalculate) projection = new Matrix4f().projectPositiveZ(fieldOfView, maxDistance, 0.1F, -aspectRatio);
		return (@NonNull Matrix4f) projection;
	}
	
	public Matrix4f getProjection(Vector2fr windowSize, boolean recalculate)
	{
		return getProjection(windowSize.x() / windowSize.y(), recalculate);
	}
	
	public boolean isProjectionDirty()
	{
		return projection == null;
	}
	
	public Vector3f getPosition()
	{
		return position;
	}
	
	public void setPosition(Vector2fr position)
	{
		this.position.setAll(position);
		view = null;
	}
	
	public void setPosition(Vector3fr position)
	{
		this.position.setAll(position);
		view = null;
	}
	
	public @Angle float getXRotation()
	{
		return xRotation;
	}
	
	public void setXRotation(@Angle float xRotation)
	{
		this.xRotation = Math.max(-Math.HALF_PIF, Math.min(Math.HALF_PIF, xRotation));
		view = null;
	}
	
	public @Angle float getYRotation()
	{
		return yRotation;
	}
	
	public void setYRotation(@Angle float yRotation)
	{
		this.yRotation = yRotation % Math.DOUBLE_PIF;
		view = null;
		flatViewQ = null;
	}
	
	public @Angle float getFieldOfView()
	{
		return fieldOfView;
	}
	
	public void setFieldOfView(@Angle float fieldOfView)
	{
		this.fieldOfView = fieldOfView;
		projection = null;
	}
	
	public float getMaxDistance()
	{
		return maxDistance;
	}
	
	public void setMaxDistance(float maxDistance)
	{
		this.maxDistance = maxDistance;
		projection = null;
	}
}
