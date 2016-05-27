package imobile.panorama.util;

/**
 * 热点类
 * @author chix
 */
public class HotSpot {
	// 热点名称
	public String hsName;
	// 热点ID
	public String hsID;
	// 热点显示位置（0~360°之间，暂设定为整数值）
	public float  hsPanoOrien;
	// 热点目标（下一个场景ID）
	public int  hsNextSceneID;
	
	public HotSpot(){
		
	}
	
	public String getXmlStr(){
		String str = "";
		
		str += "\t\t<hotspot>\n";
		str += "\t\t\t<hsname>"+hsName+"</hsname>\n";
		str += "\t\t\t<hsnextsceneid>"+hsNextSceneID+"</hsnextsceneid>\n";
		str += "\t\t\t<hspanoorien>"+hsPanoOrien+"</hspanoorien>\n";
		str += "\t\t</hotspot>\n";
		
		return str;
	}
}
