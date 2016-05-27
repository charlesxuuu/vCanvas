package imobile.panorama.util;

public class Constants {
	
	public static final String PANODIR = "/sdcard/PanoSpace/";
	public static final String VPSFILEDIR = "/sdcard/PanoSpace/VPS/";
	
	//SharedPreference
	public static final String PREF_L_SCENE = "PREF_L_SCENE";
	public static final String PREF_CB_GEOTAG = "PREF_CB_GEOTAG";
	public static final String PREF_CB_VIBRATE = "PREF_CB_VIBRATE";
	public static final String PREF_CB_DRAWGRID = "PREF_CB_DRAWGRID";
	public static final String PREF_CB_AUTO_FOCUS = "PREF_CB_AUTO_FOCUS";
	public static final String PREF_CB_AUTO_FLASH = "PREF_CB_AUTO_FLASH";
	
	
	//XML VPSfile Elements
	public static final String VPSSPACE		= "vpsspace";
	public static final String VPSNAME 		= "vpsname";
	public static final String VPSTIME      = "vpstime";
	public static final String SCENE   		= "scene";
	public static final String SCENEID 		= "sceneid";
	public static final String SCENENAME 	= "scenename";
	public static final String SX 			= "sx";
	public static final String SY 			= "sy";
	public static final String ISRELEVANCE 	= "isrelevance";
	public static final String PANOPATH 	= "panopath";
	public static final String HOTSPOT 		= "hotspot";
	public static final String HSNAME 		= "hsname";
	public static final String HSPANOORIEN 	= "hspanoorien";
	public static final String HSNEXTSCENEID= "hsnextsceneid";
	
	
	// OpenGL 显示参数
	public static final float  R = 30;
	public static final int BMPWIDTH = 2048;
	public static final int BMPHEIGHT = 256;
	public static final float CYLINDER_R =   256f;
	public static final float CYLINDER_L =   84f;
	public static final int CYLINDER_N   =   28800;
	public static final float ROTOTE_PLUS = 90;
	
	// INTENT
	public static final String INTENT_PANOPATH = "PANOPATH";
	public static final String INTENT_FILEDIR  = "FILEDIR";
	public static final String INTENT_SINGLE_PANO = "SINGLE_PANO";
	public static final String INTENT_FILEPANODIR  = "FILEPANODIR";
	// INTENT-FILTER
	public static final String IF_IMAGE_STITCH = "IMAGE_STITCH";
	public static final String INTENT_ISSTITCHFINISH = "IS_FINISH";
	
//	public static final String image_file_name_format = "'IMG'_yyyyMMdd_HHmmss";
	
	// Exif
	public static final String EXIF_LEFT_ANGLE = "LEFT_ANGLE";
	public static final String EXIF_COVERAGE_ANGLE = "COVERAGE_ANGLE";

}
