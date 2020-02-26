package com.extractor;

import lombok.Data;

@Data
public class FileInfo {
	private String filePath;
	private String fileName;
	private Integer lineNumber;
	private String content;
	private String help;
	private Integer startPos;
	private Integer endPos;
}
