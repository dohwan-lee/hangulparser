# hangulparser
Find Korean and create an Excel document.

혼자서 다국어 처리를 하게 되었다. 물론 도움을 요청해야 겠지만 프로그램을 통해서 다국어 처리를 할 수 있다면 비용을 아낄 수 있다.
스프링의 i18n을 이용한 프로퍼티 파일 활용과 {spring:message}로 적용하도록 하고 싶다.

선택된 디렉토리의 파일 목록 리스트를 얻는다.
파일 목록 리스트에서 특정 파일명 또는 확장자를 무시하도록 한다.
파일에서 한글이 위치한 행과 시작위치, 끝위치를 저장한다.
파싱된 정보는 엑셀로 저장하고 프로퍼티로도 저장하도록 한다.
프로퍼티의 키는 특정 네이밍 룰로 {패키지.파일.순번}으로 생성한다.
