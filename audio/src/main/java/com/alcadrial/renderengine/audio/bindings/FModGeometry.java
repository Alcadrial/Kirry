package com.alcadrial.renderengine.audio.bindings;

import org.lwjgl.fmod.FMOD;

public class FModGeometry {

	public FModGeometry()
	{

	}

	/**
	 *@see FMOD#FMOD_Geometry_SetActive
	 */
	public static int setActive(long arg0, int arg1)
	{
		return FMOD.FMOD_Geometry_SetActive(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Geometry_Release
	 */
	public static int release(long arg0)
	{
		return FMOD.FMOD_Geometry_Release(arg0);
	}

	/**
	 *@see FMOD#FMOD_Geometry_GetRotation
	 */
	public static int getRotation(long arg0, org.lwjgl.fmod.FMOD_VECTOR arg1, org.lwjgl.fmod.FMOD_VECTOR arg2)
	{
		return FMOD.FMOD_Geometry_GetRotation(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Geometry_SetRotation
	 */
	public static int setRotation(long arg0, org.lwjgl.fmod.FMOD_VECTOR arg1, org.lwjgl.fmod.FMOD_VECTOR arg2)
	{
		return FMOD.FMOD_Geometry_SetRotation(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Geometry_GetPosition
	 */
	public static int getPosition(long arg0, org.lwjgl.fmod.FMOD_VECTOR arg1)
	{
		return FMOD.FMOD_Geometry_GetPosition(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Geometry_AddPolygon
	 */
	public static int addPolygon(long arg0, float arg1, float arg2, int arg3, org.lwjgl.fmod.FMOD_VECTOR.Buffer arg4, java.nio.IntBuffer arg5)
	{
		return FMOD.FMOD_Geometry_AddPolygon(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	/**
	 *@see FMOD#FMOD_Geometry_SetScale
	 */
	public static int setScale(long arg0, org.lwjgl.fmod.FMOD_VECTOR arg1)
	{
		return FMOD.FMOD_Geometry_SetScale(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Geometry_GetActive
	 */
	public static int getActive(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_Geometry_GetActive(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Geometry_SetPosition
	 */
	public static int setPosition(long arg0, org.lwjgl.fmod.FMOD_VECTOR arg1)
	{
		return FMOD.FMOD_Geometry_SetPosition(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Geometry_GetScale
	 */
	public static int getScale(long arg0, org.lwjgl.fmod.FMOD_VECTOR arg1)
	{
		return FMOD.FMOD_Geometry_GetScale(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Geometry_Save
	 */
	public static int save(long arg0, java.nio.ByteBuffer arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_Geometry_Save(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Geometry_GetNumPolygons
	 */
	public static int getNumPolygons(long arg0, java.nio.IntBuffer arg1)
	{
		return FMOD.FMOD_Geometry_GetNumPolygons(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Geometry_GetPolygonAttributes
	 */
	public static int getPolygonAttributes(long arg0, int arg1, java.nio.FloatBuffer arg2, java.nio.FloatBuffer arg3, java.nio.IntBuffer arg4)
	{
		return FMOD.FMOD_Geometry_GetPolygonAttributes(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_Geometry_GetPolygonVertex
	 */
	public static int getPolygonVertex(long arg0, int arg1, int arg2, org.lwjgl.fmod.FMOD_VECTOR arg3)
	{
		return FMOD.FMOD_Geometry_GetPolygonVertex(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_Geometry_SetPolygonAttributes
	 */
	public static int setPolygonAttributes(long arg0, int arg1, float arg2, float arg3, int arg4)
	{
		return FMOD.FMOD_Geometry_SetPolygonAttributes(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 *@see FMOD#FMOD_Geometry_SetPolygonVertex
	 */
	public static int setPolygonVertex(long arg0, int arg1, int arg2, org.lwjgl.fmod.FMOD_VECTOR arg3)
	{
		return FMOD.FMOD_Geometry_SetPolygonVertex(arg0, arg1, arg2, arg3);
	}

	/**
	 *@see FMOD#FMOD_Geometry_GetMaxPolygons
	 */
	public static int getMaxPolygons(long arg0, java.nio.IntBuffer arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_Geometry_GetMaxPolygons(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Geometry_GetPolygonNumVertices
	 */
	public static int getPolygonNumVertices(long arg0, int arg1, java.nio.IntBuffer arg2)
	{
		return FMOD.FMOD_Geometry_GetPolygonNumVertices(arg0, arg1, arg2);
	}

	/**
	 *@see FMOD#FMOD_Geometry_SetUserData
	 */
	public static int setUserData(long arg0, long arg1)
	{
		return FMOD.FMOD_Geometry_SetUserData(arg0, arg1);
	}

	/**
	 *@see FMOD#FMOD_Geometry_GetUserData
	 */
	public static int getUserData(long arg0, org.lwjgl.PointerBuffer arg1)
	{
		return FMOD.FMOD_Geometry_GetUserData(arg0, arg1);
	}

}
