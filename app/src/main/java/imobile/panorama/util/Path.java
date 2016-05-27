package imobile.panorama.util;


/**
 * 惯性导航生成的路径，即两个场景之间的路径
 * @author Administrator
 */
public class Path {
	public int   prevID;
	public int   nextID;
	public float distance;
	public float direction;
	
	public Path(){
	}
	public Path(float _dis, float _dir){
		distance = _dis;
		direction = _dir;
	}
	
	//
	public float getDist(){
		return distance;
	}
	public void setDist(float _dist){
		distance = _dist;
	}
	public float getDirec(){
		return direction;
	}
	public void setDirec(float _dir){
		direction = _dir;
	}
}
