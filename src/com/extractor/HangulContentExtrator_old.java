package com.extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class HangulContentExtrator_old {

	
	
    /**
     * find *.properties, *.jsp, *.java, 
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {

        String isDir = "/Users/joseph/projects_all";
                
        // 하위 디렉토리 
        for (File info : new File(isDir).listFiles()) {
            if (info.isDirectory()) {
                //System.out.println(info.getName());
            }
            if (info.isFile()) {
            	if(info.getName().contentEquals(".xlsx"))
            		System.out.println(info.getName());
            }
        }
        
        // 디렉토리 전체 용량
        System.out.println("전체 용량 : " +  FileUtils.sizeOfDirectory(new File(isDir)) + "Byte");
        
        
        Integer allCount = 0;
        Integer stepCount = 0;
        
        // 하위의 모든 파일
        for (File info : FileUtils.listFiles(new File(isDir), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
        	String[] strs = info.getName().split("\\.");
            
        	if(strs.length > 1) {
        		        		
	        	if(strs[1].contains("jsp") || strs[1].contains("properties")) {
	        		System.out.println("**************************************************************************");
	        		System.out.println("| 파일명 : " +info.getAbsoluteFile());
	        		System.out.println("**************************************************************************");
	        		stepCount = findHangul(info.getAbsoluteFile());
	        		allCount += stepCount;
	        		System.out.println("통합 한글 개수 : " + allCount);
	        		System.out.println("**************************************************************************");
	        	}
	        		        	
        	}
        }
        
        // 하위의 모든 디렉토리
        for (File info : FileUtils.listFilesAndDirs(new File(isDir), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
            if(info.isDirectory()) {
                //System.out.println(info.getName());
            }
        }
        
    }
    
    public static Integer findHangul(File file) throws Exception {
    	
    	Integer count = 0;
    	
    	Pattern pattern = Pattern.compile("(?=((?<!(\\*.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\/\\/.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\<\\!\\-\\-.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\<\\%\\-\\-.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\-.{0,100}))([가-힣ㄱ-ㅎ]+))).*");
    	Pattern pattern2 = Pattern.compile("([가-힣ㄱ-ㅎ]+)");
    	
		FileInputStream fis = new FileInputStream(file);
		try(BufferedReader bis = new BufferedReader(new InputStreamReader(fis))) {
			String line = "";
			while( (line = bis.readLine()) != null) {
				
				Matcher matcher = pattern2.matcher(line);
				
				if(matcher.find()) {
					if( !(line.contains("/*") || line.contains("*/") || line.contains("//") || line.contains("* ") || line.contains("<!--") || line.contains("-->")
							|| line.contains("console.log"))) {
						count++;
						System.out.print(matcher.group() + " [" );
						System.out.print(matcher.start() + "] [");
						System.out.print(matcher.end() + "] : ");
						System.out.println(line);
					}
					
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return count;
		
    }
    
    

}
