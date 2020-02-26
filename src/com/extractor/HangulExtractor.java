package com.extractor;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class HangulExtractor {
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("테스트");
		
		Pattern pattern = Pattern.compile("(?=((?<!(\\*.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\/\\/.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\<\\!\\-\\-.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\<\\%\\-\\-.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\-.{0,100}))([가-힣ㄱ-ㅎ]+))).*");
				
		Path path = new File("/Users/joseph/eclipse-w/HangulExtractor/").toPath();
		
		Iterator itr = FileUtils.iterateFiles(new File("/Users/joseph/eclipse-w/HangulExtractor/src/com/extractor/hangul.txt"), null, false);
		while (itr.hasNext()) {
			//itr.next()
			FileInputStream fis = new FileInputStream(new File("/Users/joseph/eclipse-w/HangulExtractor/src/com/extractor/hangul.txt"));
			try(BufferedReader bis = new BufferedReader(new InputStreamReader(fis))) {
				String line = "";
				while( (line = bis.readLine()) != null) {
					
					Matcher matcher = pattern.matcher(line);
					
					if(matcher.find()) {
						
						System.out.println(path.getRoot() +"/" +path.getParent() +"/" +path.getFileName());
						System.out.println(line);
						
					}
				}
				
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
} 