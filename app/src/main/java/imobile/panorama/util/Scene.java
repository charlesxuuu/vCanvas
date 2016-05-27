package imobile.panorama.util;

import java.util.ArrayList;
import java.util.List;


/**
 * 场景类，每一张全景图的拍摄场所
 * @author chix
 */
public class Scene {
	// 场景ID
	public int    sID;
	// 场景名称
	public String sName;
	// 室内横纵坐标 (室外时作为经纬度坐标)
	public float sX;	//sLat//	public float sLat;
	public float sY;	//sLon//	public float sLon;
	
	// 该视点是否已经关联全景图
	public boolean isRelevance = false;
	// 关联后的场景图名称
	public String  sPanoFile = "";

	public Scene(){
		lstHotS = new ArrayList<HotSpot>();
	}
	// 包含的热点列表
	public List<HotSpot> lstHotS = null;
	public void addHotSpot(HotSpot _hs){
		lstHotS.add(_hs);
	}
	// 根据nextID值删除HotSpot
	public void deleteHotSpot(int _nid){
		for(int i=0;i<lstHotS.size();i++){
			if(lstHotS.get(i).hsNextSceneID == _nid){
				lstHotS.remove(i);
				break;
			}
		}
	}
	
	// 移动场景
	public void moveLeft(){
		sX -= 5;
	}
	public void moveRight(){
		sX += 5;
	}
	public void moveTop(){
		sY -= 5;
	}
	public void moveBottom(){
		sY += 5;
	}
}