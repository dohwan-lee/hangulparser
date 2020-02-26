package com.extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.subtlelib.poi.api.sheet.SheetContext;
import org.subtlelib.poi.api.workbook.WorkbookContext;
import org.subtlelib.poi.impl.workbook.WorkbookContextFactory;

import com.google.common.io.Files;

public class HangulContentExtractor {

	private final WorkbookContextFactory ctxFactory;
	
	public HangulContentExtractor(WorkbookContextFactory ctxFactory) {
		this.ctxFactory = ctxFactory;
	}
	
    /**
     * find *.properties, *.jsp, *.java, 
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
    	
    	HangulContentExtractor hce = new HangulContentExtractor(WorkbookContextFactory.useXlsx());
    	WorkbookContext workbookCtx = hce.ctxFactory.createWorkbook();
    	
    	SheetContext sheetCtx = workbookCtx.createSheet("HangulTranslate");
    	
    	sheetCtx.nextRow()
    		.skipCell()
    		.header("파일 경로").setColumnWidth(80).setRowHeight(28)
    		.header("파일명").setColumnWidth(50)
    		.header("위치").setColumnWidth(10)
    		.header("시작 위치").setColumnWidth(5)
    		.header("종료 위치").setColumnWidth(5)
    		.header("추출 문자열").setColumnWidth(100);
    		//.header("추출 노드");
    	
    	
        String isDir = "/Users/joseph/projects_all/ohprint-backoffice";
                
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
        		        		
	        	if(((strs[1].contains("jsp") || strs[1].contains("properties")) && !strs[0].contains("target"))) {
	        		System.out.println("**************************************************************************");
	        		System.out.println("| 파일명 : " +info.getAbsoluteFile());
	        		
	        		List<FileInfo> findHangulList = findHangul(info.getAbsoluteFile());
	        		
	        		findHangulList
	        			.stream()
	        			.forEach(data -> {
	        				        					        			
			        		sheetCtx.nextRow()
			        			.skipCell()
			        			.text(!ObjectUtils.isEmpty(info.getAbsoluteFile()) ? info.getAbsoluteFile().toString() : "").setRowHeight(28)
			        			.text(info.getName())
			        			.number(data.getLineNumber())
			        			.number(data.getStartPos())
			        			.number(data.getEndPos())
			        			.text(data.getContent());
			        			//.text(data.data);
	        			});
	        		
	        		System.out.println("**************************************************************************");
	        	}
	        		        	
        	}
        	
        	        	
        }
        
        Files.write(workbookCtx.toNativeBytes(), new File(isDir + File.separator + "RequestTranslateFile.xlsx"));
        
        // 하위의 모든 디렉토리
        for (File info : FileUtils.listFilesAndDirs(new File(isDir), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
            if(info.isDirectory()) {
                //System.out.println(info.getName());
            }
        }
        
    }
    
    public static List<FileInfo> findHangul(File file) throws Exception {
    	
    	AtomicInteger count = new AtomicInteger(0);
    	    	
    	Pattern pattern = Pattern.compile("(?=((?<!(\\*.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\/\\/.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\<\\!\\-\\-.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\<\\%\\-\\-.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\-.{0,100}))([가-힣ㄱ-ㅎ]+))).*");
    	Pattern pattern2 = Pattern.compile("([가-힣ㄱ-ㅎ]+)");
    	
    	List<FileInfo> list = new ArrayList<>();
    	list.clear();
    	
    	StringBuffer sb = new StringBuffer();
    	
		FileInputStream fis = new FileInputStream(file);
		
		try(BufferedReader bis = new BufferedReader(new InputStreamReader(fis))) {
			String line = "";
			int crement = 1;
			while( (line = bis.readLine()) != null) {
				
				Matcher matcher = pattern2.matcher(line);
				boolean isKor = false;
				FileInfo fi = new FileInfo();
				crement = count.getAndIncrement();
				
				while(matcher.find()) {
										
					if( !(line.contains("/*") || line.contains("*/") || line.contains("//") || line.contains("* ") || line.contains("<!--") || line.contains("-->")
							|| line.contains("console.log"))) {
						
						
						fi.setStartPos(matcher.start());
												
						System.out.println(matcher.group());
						sb.append(matcher.group() +" ");
						isKor = true;
						
						fi.setEndPos(matcher.end());
						
					}
					
				}
				
				if(isKor == true) {
					fi.setLineNumber(crement+1);
//					if(crement > 1) {
//						fi.setEndPos(fi.getStartPos() + sb.toString().length());
//					}
					fi.setContent(sb.toString() + "\r\n");
					list.add(fi);
					sb.setLength(0);
				}
							
				
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return list;
		
    }
    
    

}
