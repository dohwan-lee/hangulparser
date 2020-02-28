package com.extractor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 개선점 : 엑셀의 셀 내용을 읽어서 프로퍼티로 생성하도록 변경
 * 이전 작성된 프로퍼티의 히스토리를 새로 생성한 다국어 프로퍼티 파일과 연결이 필요.
 * (이유 : 번역 원본 파일에 한글이 추가될 경우 변역 인덱스 번호가 달라 진다.)
 */
public class PropertiesCreatator implements Runnable {

    private final String CREATE_PATH;
    private final String FILE_NAME;

    public PropertiesCreatator(String create_path, String file_name) {
        CREATE_PATH = create_path;
        FILE_NAME = file_name;
    }

    @Override
    public void run() {

        String beStr = "";
        AtomicReference<Integer> fileSameCnt = new AtomicReference<>(0);

        try (OutputStream output = new FileOutputStream(CREATE_PATH + File.separator + FILE_NAME)) {

            Properties prop = new Properties();

            // set the properties value
            for (File info : FileUtils.listFiles(new File(CREATE_PATH), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
                String[] strs = info.getName().split("\\.");

                if (strs.length > 1) {

                    if (((strs[1].contains("jsp") || strs[1].contains("properties")) && !strs[0].contains("target"))) {
                        List<FileInfo> findHangulList = KoreanFinder.findHangul(info.getAbsoluteFile());

                        findHangulList
                                .stream()
                                .forEach(data -> {

                                    System.out.println(info.getName());
                                    String fileName = info.getName().split("\\.")[0];

                                    if (!fileName.contains(beStr)) {
                                        fileSameCnt.set(0);
                                    } else {
                                        fileSameCnt.getAndSet(fileSameCnt.get() + 1);
                                    }

                                    prop.setProperty(fileName.toUpperCase() + "." + fileSameCnt.get(), data.getContent());

                                });
                    }

                }


            }

            try {
                prop.store(output, null);
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println(prop);

        } catch (IOException io) {
            io.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
