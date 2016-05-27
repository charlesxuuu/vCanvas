package imobile.panorama.util;

import java.io.InputStream;
import java.io.StringWriter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

public class VPSFilePullParser implements VPSFileParser {

	@Override
	public void parse(InputStream is) throws Exception {
		// TODO Auto-generated method stub
		
		Scene sc = null;
		HotSpot hs = null;
		XmlPullParser parser= Xml.newPullParser();
		parser.setInput(is, "UTF-8");
		
		int eventType = parser.getEventType();
		while(eventType != XmlPullParser.END_DOCUMENT){
			switch(eventType){
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				if(parser.getName().equals(Constants.VPSNAME)){
					eventType = parser.next();
					VPSFile.setVPSName(parser.getText());
				}else if(parser.getName().equals(Constants.VPSTIME)){
					eventType = parser.next();
					VPSFile.setVPSTime(parser.getText());
				}else if(parser.getName().equals(Constants.SCENE)){
					sc = new Scene();
				}else if(parser.getName().equals(Constants.SCENEID)){
					eventType = parser.next();
					sc.sID = Integer.parseInt(parser.getText());
				}else if(parser.getName().equals(Constants.SCENENAME)){
					eventType = parser.next();
					sc.sName = parser.getText();
				}else if(parser.getName().equals(Constants.SX)){
					eventType = parser.next();
					sc.sX = Float.parseFloat(parser.getText());
				}else if(parser.getName().equals(Constants.SY)){
					eventType = parser.next();
					sc.sY = Float.parseFloat(parser.getText());
				}else if(parser.getName().equals(Constants.ISRELEVANCE)){
					eventType = parser.next();
					sc.isRelevance = Boolean.parseBoolean(parser.getText());
				}else if(parser.getName().equals(Constants.PANOPATH)){
					eventType = parser.next();
					sc.sPanoFile = parser.getText();
				}else if(parser.getName().equals(Constants.HOTSPOT)){
					hs = new HotSpot();
					boolean b = true;
					while(b){
						eventType = parser.next();
						switch(eventType){
						case XmlPullParser.START_TAG:
							if(parser.getName().equals(Constants.HSNAME)){
								eventType = parser.next();
								hs.hsName = parser.getText();
							}else if(parser.getName().equals(Constants.HSNEXTSCENEID)){
								eventType = parser.next();
								hs.hsNextSceneID = Integer.parseInt(parser.getText());
							}else if(parser.getName().equals(Constants.HSPANOORIEN)){
								eventType = parser.next();
								hs.hsPanoOrien = Float.parseFloat(parser.getText());
							}
							break;
						case XmlPullParser.END_TAG:
							if(parser.getName().equals(Constants.HOTSPOT)){
								sc.lstHotS.add(hs);
								hs = null;
								b= false;
							}
							break;
						}
					}
				}
				break;
			case XmlPullParser.END_TAG:
				if(parser.getName().equals(Constants.SCENE)){
					VPSFile.addNewScene(sc, null);
					sc = null;
				}
				break;
			}
			eventType = parser.next();
		}
	}

	@Override
	public String serialize() throws Exception {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		serializer.setOutput(writer);
		serializer.startDocument("UTF-8", true);
		serializer.startTag("", Constants.VPSSPACE);
		
		serializer.startTag("", Constants.VPSNAME);
		serializer.text(VPSFile.getVPSName());
		serializer.endTag("", Constants.VPSNAME);
		
		serializer.startTag("", Constants.VPSTIME);
		serializer.text(VPSFile.getVPSTime()+"");
		serializer.endTag("", Constants.VPSTIME);

		for(Scene sc:VPSFile.copyLstScene()){
			serializer.startTag("", Constants.SCENE);
			
			serializer.startTag("", Constants.SCENEID);
			serializer.text(sc.sID+"");
			serializer.endTag("", Constants.SCENEID);
			
			serializer.startTag("", Constants.SCENENAME);
			serializer.text(sc.sName+"");
			serializer.endTag("", Constants.SCENENAME);
			
			
			serializer.startTag("", Constants.SX);
			serializer.text(sc.sX+"");
			serializer.endTag("", Constants.SX);
			
			serializer.startTag("", Constants.SY);
			serializer.text(sc.sY+"");
			serializer.endTag("", Constants.SY);
			
			serializer.startTag("", Constants.ISRELEVANCE);
			serializer.text(sc.isRelevance+"");
			serializer.endTag("", Constants.ISRELEVANCE);
			
			serializer.startTag("", Constants.PANOPATH);
			serializer.text(sc.sPanoFile+"");
			serializer.endTag("", Constants.PANOPATH);
			
			for(HotSpot hs:sc.lstHotS){
				serializer.startTag("", Constants.HOTSPOT);
				
				serializer.startTag("", Constants.HSNAME);
				serializer.text(hs.hsName+"");
				serializer.endTag("", Constants.HSNAME);
				
				serializer.startTag("", Constants.HSNEXTSCENEID);
				serializer.text(hs.hsNextSceneID+"");
				serializer.endTag("", Constants.HSNEXTSCENEID);
				
				serializer.startTag("", Constants.HSPANOORIEN);
				serializer.text(hs.hsPanoOrien+"");
				serializer.endTag("", Constants.HSPANOORIEN);
				
				serializer.endTag("", Constants.HOTSPOT);
			}
			serializer.endTag("", Constants.SCENE);
		}
		serializer.endTag("", Constants.VPSSPACE);
		serializer.endDocument();
		return writer.toString();
	}
}
