package com.extractor;

import java.io.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.language_translator.v3.LanguageTranslator;
import com.ibm.watson.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.language_translator.v3.model.Translation;
import com.ibm.watson.language_translator.v3.model.TranslationResult;
import com.ibm.watson.language_translator.v3.util.Language;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.ObjectUtils;
import org.subtlelib.poi.api.sheet.SheetContext;
import org.subtlelib.poi.api.workbook.WorkbookContext;
import org.subtlelib.poi.impl.workbook.WorkbookContextFactory;

import com.google.common.io.Files;

public class HangulContentExtractor {

    private final WorkbookContextFactory ctxFactory;

    public HangulContentExtractor(WorkbookContextFactory ctxFactory) {
        this.ctxFactory = ctxFactory;
    }

    private static final String CLIEND_ID = "sAtAb8M3UcWWmCqq77sK";
    private static final String CLIENT_SECRET = "Go0wtIT61X";

    /**
     * [ 한글 번역 자동화 도구 개발 ]
     * 1. find *.properties, *.jsp, *.java,
     * 2. 한글 텍스트 파싱
     *  2-1. 파일명, 경로, 라인, 시작 위치, 종료 위치
     * 3. 엑셀 생성
     *  3-1. 2-1 내용을 파일에 씀
     * 4. 파일 자동 번역 (가능 여부 확인)
     *  4-1. 대상(src)을 지정된 셀(dst)에 번역
     * 5. 전역으로 국가 코드를 지정하여 해당 국가 엑셀 내용으로 *.properties 다국어 파일 생성
     * 6. 리팩토링 및 글로벌 설정
     * 	6-1. 번역 분석 디렉토리를 설정 파일로 설정하도록 변경
     * 	6-2. 자동 분석 예외(ignore) 파일을 설정하도록 변경
     * [ JP 개발용 브랜치 생성 - release 브랜치 사용 ]
     *  jp-www, jp-oround, jp-analytics, jp-framework, jp-backoffice, jp-batch,
     * [ jenkins jp 빌드 구성 ]
     *
     * @throws Exception
     */
    /**
     * https://developers.kakao.com/docs/restapi/translation
     * https://developers.naver.com/docs/labs/translator/
     * (
     * https://developers.naver.com/docs/nmt/reference/
     * https://developers.naver.com/docs/labs/translator/
     * )
     * https://github.com/MicrosoftTranslator/Text-Translation-API-V3-Java/blob/master/Translate/src/main/java/Translate.java
     * https://bakyeono.net/post/2015-08-27-glosbe-api-client.html <-  테스트 중
     * (https://glosbe.com/a-api)
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        HangulContentExtractor hce = new HangulContentExtractor(WorkbookContextFactory.useXlsx());
        WorkbookContext workbookCtx = hce.ctxFactory.createWorkbook();

		// arg로 받을 param
        SheetContext sheetCtx = workbookCtx.createSheet("HangulTranslate" );

        sheetCtx.nextRow()
                .skipCell()
                .header("파일 경로").setColumnWidth(80).setRowHeight(28)
                .header("파일명").setColumnWidth(50)
                .header("위치").setColumnWidth(10)
                .header("시작 위치").setColumnWidth(5)
                .header("종료 위치").setColumnWidth(5)
                .header("추출 문자열").setColumnWidth(70)
                .header("번역 문자열").setColumnWidth(70)
                .header("프로퍼티(kor)").setColumnWidth(50)
                .header("프로퍼티(jpn)").setColumnWidth(50);


		// arg로 받을 param
        //String isDir = "/Users/joseph/projects_all/ohprint-backoffice";
        String isDir = "e:/devGits/ohprintme/ohprint-backoffice";

        // 하위 디렉토리 
        for (File info : new File(isDir).listFiles()) {
            if (info.isDirectory()) {
                //System.out.println(info.getName());
            }
            if (info.isFile()) {
                if (info.getName().contentEquals(".xlsx"))
                    System.out.println(info.getName());
            }
        }

        // 디렉토리 전체 용량
        System.out.println("전체 용량 : " + FileUtils.sizeOfDirectory(new File(isDir)) + "Byte");

        Integer allCount = 0;
        Integer stepCount = 0;
        JsonParser parser = new JsonParser();

        // arg로 받을 param
        Runnable r = new PropertiesCreatator(isDir, "message_kor.properties");
        new Thread(r).start();

        // 하위의 모든 파일
        for (File info : FileUtils.listFiles(new File(isDir), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
            String[] strs = info.getName().split("\\.");

            if (strs.length > 1) {

                if (((strs[1].contains("jsp") || strs[1].contains("properties")) && !strs[0].contains("target"))) {
                    System.out.println("**************************************************************************");
                    System.out.println("| 파일명 : " + info.getAbsoluteFile());

                    List<FileInfo> findHangulList = KoreanFinder.findHangul(info.getAbsoluteFile());

                    String beStr = "";
                    AtomicReference<Integer> fileSameCnt = new AtomicReference<>(0);

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

                                sheetCtx.nextRow()
                                        .skipCell()
                                        .text(!ObjectUtils.isEmpty(info.getAbsoluteFile()) ? info.getAbsoluteFile().toString() : "").setRowHeight(28)
                                        .text(info.getName())
                                        .number(data.getLineNumber())
                                        .number(data.getStartPos())
                                        .number(data.getEndPos())
                                        .text(data.getContent())
                                        //.text(papagoTranslate(data.getContent(), parser));
                                        //.text(ibmTranslate(data.getContent()));
                                        //.text(kakaoTranslate(data.getContent(), parser));
                                        //.text(naverTranslate(data.getContent(), parser));
                                        .text("")
                                        .text(fileName.toUpperCase() + "." + fileSameCnt.get() + "=" + data.getContent())
                                        .text(fileName.toUpperCase() + "." + fileSameCnt.get() + "=");
                            });

                    System.out.println("**************************************************************************");
                }

            }


        }

        Files.write(workbookCtx.toNativeBytes(), new File(isDir + File.separator + "RequestTranslateFile_"
				+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
				+".xlsx"));

        // 하위의 모든 디렉토리
        for (File info : FileUtils.listFilesAndDirs(new File(isDir), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
            if (info.isDirectory()) {
                //System.out.println(info.getName());
            }
        }


    }

    public static String naverTranslate(String transStr, JsonParser parser) {
        String transTxt = "";
        try {
            String text = URLEncoder.encode(transStr, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/language/translate";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Naver-Client-Id", CLIEND_ID);
            con.setRequestProperty("X-Naver-Client-Secret", CLIENT_SECRET);
            // post request
            String postParams = "source=ko&target=ja&text=" + text;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            System.out.println(response.toString());

            if (response.length() > 0) {
                JsonElement element = parser.parse(response.toString());
                transTxt = element.getAsJsonObject().get("message")
                        .getAsJsonObject().get("result")
                        .getAsJsonObject().get("translatedText").getAsString();
            }

            return transTxt;

        } catch (Exception e) {
            System.out.println(e);
        }

        return transTxt;
    }

    /**
     * 5만자 제한 ㅡㅡ;
     *
     * @param tranStr
     * @param parser
     * @return
     */
    public static String kakaoTranslate(String tranStr, JsonParser parser) {
        try {
            String text = URLEncoder.encode(tranStr, "UTF-8");
            //"https://openapi.naver.com/v1/language/translate";
            String apiURL = "https://kapi.kakao.com/v1/translation/translate";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "KakaoAK 32df2dff1a64a736afe8a343aae349bc");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("charset", "utf-8");
            // post request
            String postParams = "src_lang=kr&target_lang=jp&query=" + text;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            System.out.println(response.toString());

            String transTxt = "";

            if (response.length() > 0) {
                JsonElement element = parser.parse(response.toString());
                transTxt = element.getAsJsonObject().get("translated_text").getAsString();
            }
            return transTxt;
        } catch (Exception e) {
            System.out.println(e);
        }
        return "";
    }

    /**
     * https://glosbe.com/a-api
     *
     * @param transTxt
     * @return
     */
    public static String gloseTranslate(String paramTxt) {
        String transTxt = "";
        return transTxt;
    }

    /**
     * 다 좋은데 한국어->일본어 번역 모델이 없다.
     * 과금이 없다.
     *
     * @param text
     * @return
     */
    // https://github.com/watson-developer-cloud/java-sdk/tree/master/language-translator
    public static String ibmTranslate(String text) {
        Authenticator authenticator = new IamAuthenticator("mIAzKC4j3Z5nlpg9XUCr8-dhog4VzCkyIT2B6f8SHfUP");
        LanguageTranslator service = new LanguageTranslator("2018-05-01", authenticator);

        TranslateOptions translateOptions = new TranslateOptions.Builder()
                .addText(text)
                .source(Language.KOREAN)
                .target(Language.JAPANESE)
                .build();
        TranslationResult translationResult = service.translate(translateOptions).execute().getResult();

        System.out.println(translationResult);
        List<Translation> list = translationResult.getTranslations();
        StringBuffer sb = new StringBuffer();
        list.stream()
                .forEach(model -> sb.append(model.getTranslation()));
        return sb.toString();
    }


    /**
     * 하루 10000자 까지만 무료이다.
     *
     * @param tranStr
     * @param parser
     * @return
     */
    public static String papagoTranslate(String tranStr, JsonParser parser) {

        try {
            String text = URLEncoder.encode(tranStr, "UTF-8");
            //"https://openapi.naver.com/v1/language/translate";
            String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Naver-Client-Id", CLIEND_ID);
            con.setRequestProperty("X-Naver-Client-Secret", CLIENT_SECRET);
            // post request
            String postParams = "source=ko&target=ja&text=" + text;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            System.out.println(response.toString());

            String transTxt = "";

            if (response.length() > 0) {
                JsonElement element = parser.parse(response.toString());
                transTxt = element.getAsJsonObject().get("message")
                        .getAsJsonObject().get("result")
                        .getAsJsonObject().get("translatedText").getAsString();
            }
            return transTxt;
        } catch (Exception e) {
            System.out.println(e);
        }
        return "";

    }


}
