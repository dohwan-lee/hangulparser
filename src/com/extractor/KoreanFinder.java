package com.extractor;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KoreanFinder {
    public static List<FileInfo> findHangul(File file) throws Exception {

        AtomicInteger count = new AtomicInteger(0);

        Pattern pattern =
                Pattern.compile("(?=((?<!(\\*.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\/\\/.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\<\\!\\-\\-.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\<\\%\\-\\-.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\-.{0,100}))([가-힣ㄱ-ㅎ]+))).*");
        Pattern pattern2 = Pattern.compile("([가-힣ㄱ-ㅎ]+)");

        List<FileInfo> list = new ArrayList<>();
        list.clear();

        List<Position> posList = new ArrayList<>();
        posList.clear();

        FileInputStream fis = new FileInputStream(file);

        try (BufferedReader bis = new BufferedReader(new InputStreamReader(fis))) {
            String line = "";
            int crement = 1;
            String ignoreBuf = "";
            while ((line = bis.readLine()) != null) {

                crement = count.getAndIncrement();
                Matcher matcher = pattern2.matcher(line);
                boolean isKor = false;
                FileInfo fi = new FileInfo();
                int endPos = 0;
                int startPos = 0;

                if((line.contains("/*") && line.contains("*/"))) {      // ignore st
                    ignoreBuf = "";
                    continue;
                } else if(line.contains("/*")) {
                    ignoreBuf = "/*";
                } else if(line.contains("*/")) {
                    ignoreBuf = "";
                    continue;
                }

                if((line.contains("<!--") && line.contains("-->"))) {     // ignore st
                    ignoreBuf = "";
                    continue;
                } else if(line.contains("<!--")) {     // ignore st
                    ignoreBuf = "<!--";
                } else if(line.contains("-->")) {
                    ignoreBuf = "";
                    continue;
                }

                if((line.contains("<%--") && line.contains("-->"))) {     // ignore st
                    ignoreBuf = "";
                    continue;
                } else if(line.contains("<%--")) {
                    ignoreBuf = "<%--";
                } else if(line.contains("-->")) {
                    ignoreBuf = "";
                    continue;
                }

                if(!StringUtils.isEmpty(ignoreBuf)) {
                    continue;
                }

                while (matcher.find()) {

                    if (!(line.contains("//") || line.contains("* ") || line.contains("console.log") || line.contains("logger"))) {

                        System.out.println(matcher.group());

                        Position position = new Position();
                        startPos = matcher.start();
                        position.setStart(startPos);

                        //System.out.println(line.substring(fi.getStartPos(), fi.getEndPos() - 1));
                        //sb.append(matcher.group() + " ");
                        isKor = true;
                        endPos = matcher.end();

                        position.setEnd(endPos);

                        posList.add(position);
                    }

                }

                if (isKor == true) {

                    if(posList.size() > 0) {
                        Position sp = posList.get(0);
                        Position ep = posList.get(posList.size() - 1);
                        fi.setStartPos(sp.getStart());
                        fi.setEndPos(ep.getEnd());

                    }

                    fi.setLineNumber(count.get());
//					if(crement > 1) {
//						fi.setEndPos(fi.getStartPos() + sb.toString().length());
//					}
                    fi.setContent(line.substring(fi.getStartPos(), fi.getEndPos())); //  + "\r\n"
                    list.add(fi);

                    posList.clear();

                }


            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;

    }
}

class Position {
    private Integer start;
    private Integer end;

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }
}
