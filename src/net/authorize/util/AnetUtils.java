package net.authorize.util;


import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import net.authorize.data.swiperdata.SwiperData;

public class AnetUtils {

	public static String getAnetSDKVersion(){
		
		
		//TODO fix this
		String version = null;
		try{
			Class clazz = SwiperData.class;
			String className = clazz.getSimpleName() + ".class";
			String classPath = clazz.getResource(className).toString();
			if (!classPath.startsWith("jar")) {
			  // Class not from JAR
				version = "Version Info Not Available";
			}
			else{
				String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + 
			    "/META-INF/MANIFEST.MF";
			Manifest manifest = new Manifest(new URL(manifestPath).openStream());
			Attributes attr = manifest.getMainAttributes();
			version = attr.getValue("Implementation-Version");
			}						
			
					
			 if(StringUtils.isEmpty(version))
				 version = "Version Info Not Available";
		}
		catch(Exception ex){
			version = "Version Info Not Available";
		}
		
		return version;
	}
}
