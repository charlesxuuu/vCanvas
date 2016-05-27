package imobile.panorama.util;

import imobile.panorama.vseditor.VSPathView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VPSFile {
	// 虚拟空间文件的名称
	private static String vpsName = "";	
	// 虚拟空间文件的创建时间
	private static String vpsTime = "";
	// 虚拟空间中对应的场景
	private static List<Scene> lstScene  = new ArrayList<Scene>();
	// 展示的视图
	public static VSPathView vspv = null;
	
	// 构造函数
	public VPSFile(){
	}
	
	// 清空函数
	public static void clear(){
		System.out.println("VPSFile clear()");
		vpsName = "";
		lstScene.clear();
		if(vspv!=null){
			vspv.invalidate();
		}
	}
	
	// 设置入口ID
	
	// 设置VPS名称
	public static void setVPSName(String _name){
		vpsName = _name;
	}
	public static String getVPSName(){
		return vpsName;
	}
	// 设置VPS的创建时间
	public static void setVPSTime(String _time){
		vpsTime = _time;
	}
	public static String getVPSTime(){
		return vpsTime;
	}
	
	/* 对场景（Scene对象）的操作函数 */ 
	public static void addNewScene(Scene sc, Path path){
		lstScene.add(sc);
		
		if(path!=null){
			HotSpot phs = new HotSpot();
			phs.hsNextSceneID = path.nextID;
			phs.hsPanoOrien   = path.direction;
			getSceneByID(path.prevID).addHotSpot(phs);
			
			HotSpot nhs = new HotSpot();
			nhs.hsNextSceneID = path.prevID;
			nhs.hsPanoOrien   = (path.direction+180)%360;
			getSceneByID(path.nextID).addHotSpot(nhs);
		}
		if(vspv!=null){
			vspv.invalidate();
		}
	}
	
	// 增加线段
	public static  void addNewPathBetweenTwoSc(Path path){
		if(path!=null){
			HotSpot phs = new HotSpot();
			phs.hsNextSceneID = path.nextID;
			phs.hsPanoOrien   = path.direction;
			getSceneByID(path.prevID).addHotSpot(phs);
			
			HotSpot nhs = new HotSpot();
			nhs.hsNextSceneID = path.prevID;
			nhs.hsPanoOrien   = (path.direction+180)%360;
			getSceneByID(path.nextID).addHotSpot(nhs);
		}
		if(vspv!=null){
			vspv.invalidate();
		}
	}
	
	public static Scene getSceneByID(int _id){
		Scene temp = null;
		for(int i=0;i<lstScene.size();i++){
			if(lstScene.get(i).sID == _id){
				temp = lstScene.get(i);
				break;
			}
		}
		return temp;
	}
	public static Scene getSceneByI(int i){
		return lstScene.get(i);
	}
	public static void deleteScene(int _id){
		for(int i=0;i<lstScene.size();i++){
			if(lstScene.get(i).sID == _id){
				for(int j=0;j<lstScene.get(i).lstHotS.size();j++){
					HotSpot hs = lstScene.get(i).lstHotS.get(j);
					Scene sc = getSceneByID(hs.hsNextSceneID);
					sc.deleteHotSpot(_id);
				}
				lstScene.remove(i);
				vspv.invalidate();
				break;
			}
		}
	}
	
	public static int getSceneNum(){											// 获取Sc的个数
		return lstScene.size();
	}
	
	public static int getMaxSceneId(){											// 获取最大的场景ID
		int id = 0;
		for(int i=0;i<lstScene.size();i++){
			if(id<lstScene.get(i).sID){
				id = lstScene.get(i).sID;
			}
		}
		return id;
	}
	
	public static List<Scene> copyLstScene(){
		return lstScene;
	}
	// 每个场景关联一幅图，用一下函数关联全景图或者修改关联的全景图
	public static void setScenePano(int _id ,String strPath){//
		for(int i=0;i<lstScene.size();i++){
			Scene temp = lstScene.get(i);
			if(temp.sID == _id){
				if(strPath!=""){												// 如果strPath不为空，则设置
					temp.isRelevance = true;
					temp.sPanoFile   = strPath;
				}else{															// 如果strPath为空，则认为是清空全景图
					temp.isRelevance = false;
					temp.sPanoFile   = strPath;
				}
				vspv.invalidate();
				break;
			}
		}
	}
	// 删除场景关联的全景图
	public static void deleteScenePano(int _id){
		for(int i=0;i<lstScene.size();i++){
			Scene temp = lstScene.get(i);
			if(temp.sID == _id){
				temp.isRelevance = false;
				temp.sPanoFile = "";
				vspv.invalidate();
				break;
			}
		}
	}
	/* 读取文件操作 */
	public static void importFromFile(String fileName){
		clear();
		
		File file = new File(Constants.VPSFILEDIR+fileName);
		if(file.exists()){
			try {
				FileInputStream fis = new FileInputStream(file);
				VPSFilePullParser parser = new VPSFilePullParser();
				parser.parse(fis);
				if(vspv!=null){
					vspv.invalidate();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e){
				
			}
		}
	}
	public static void importWebFile(String fileName){
		clear();
		
		File file = new File(fileName);
		if(file.exists()){
			try {
				FileInputStream fis = new FileInputStream(file);
				VPSFilePullParser parser = new VPSFilePullParser();
				parser.parse(fis);
				if(vspv!=null){
					vspv.invalidate();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e){
				
			}
		}
	}
	
	//当前的VPSFile清空
	public static void exportToFile(){
		
		//检测文件名，如果没有，则创建；如果有，则弹出对话框，是否覆盖源文件
		//对已存在的文件处理
		String strDir = Constants.VPSFILEDIR;
		File fileVPSFile = new File(strDir+vpsName+".xml");
		if(!fileVPSFile.exists()){
			try {
				fileVPSFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(fileVPSFile);
			StringBuffer sb = new StringBuffer();
			VPSFilePullParser parser = new VPSFilePullParser();
			try {
				sb.append(parser.serialize());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			fos.write((sb.toString()).getBytes());
			fos.flush();

			fos.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
