package com.extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KoreanFinder {
    public static List<FileInfo> findHangul(File file) throws Exception {

        AtomicInteger count = new AtomicInteger(0);

        Pattern pattern = Pattern.compile("(?=((?<!(\\*.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\/\\/.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\<\\!\\-\\-.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\<\\%\\-\\-.{0,100}))([가-힣ㄱ-ㅎ]+)))(?=((?<!(\\-.{0,100}))([가-힣ㄱ-ㅎ]+))).*");
        Pattern pattern2 = Pattern.compile("([가-힣ㄱ-ㅎ]+)");

        List<FileInfo> list = new ArrayList<>();
        list.clear();

        StringBuffer sb = new StringBuffer();

        FileInputStream fis = new FileInputStream(file);

        try (BufferedReader bis = new BufferedReader(new InputStreamReader(fis))) {
            String line = "";
            int crement = 1;
            while ((line = bis.readLine()) != null) {

                Matcher matcher = pattern2.matcher(line);
                boolean isKor = false;
                FileInfo fi = new FileInfo();
                crement = count.getAndIncrement();

                while (matcher.find()) {

                    if (!(line.contains("/*") || line.contains("*/") || line.contains("//") || line.contains("* ") || line.contains("<!--") || line.contains("-->")
                            || line.contains("console.log"))) {


                        fi.setStartPos(matcher.start());

                        System.out.println(matcher.group());
                        sb.append(matcher.group() + " ");
                        isKor = true;

                        fi.setEndPos(matcher.end());

                    }

                }

                if (isKor == true) {
                    fi.setLineNumber(crement + 1);
//					if(crement > 1) {
//						fi.setEndPos(fi.getStartPos() + sb.toString().length());
//					}
                    fi.setContent(sb.toString()); //  + "\r\n"
                    list.add(fi);
                    sb.setLength(0);
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;

    }
}
