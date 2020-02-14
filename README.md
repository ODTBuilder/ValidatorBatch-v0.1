[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)
[![Korean](https://img.shields.io/badge/language-Korean-blue.svg)](#korean)


<a name="korean"></a>
ValidatorBatch-v0.1 (공간자료 검증 배치파일 v0.1) 
=======
이 프로젝트는 국토공간정보연구사업 중 [공간정보 SW 활용을 위한 오픈소스 가공기술 개발]과제의 5차년도 연구성과 입니다.<br>
본 프로그램은 로컬기반의 검수를 수행하기 위한 배치파일로써 [OpenGDS-Desktop-QgisPlugin](https://github.com/ODTBuilder/OpenGDS-Desktop-QgisPlugin) 검수를 지원하기 위해 개발되었습니다.<br>
배치파일은 3, 4차년도 때 Web기반으로 개발된 공간정보 검수도구인 OpenGDS(https://github.comODTBuilderOpenGDS-Builder-Javascript )와 Validator(https://github.comODTBuilderValidator )로 개발되었습니다.
<br>Geoserver 연동 없이 로컬파일을 직접 읽어 검수함으로써 기존 이슈였던 대용량 지원 및 고속검수가 가능합니다.


감사합니다.<br>
공간정보기술(주) 연구소 <link>http://www.git.co.kr/<br>
OpenGeoDT 팀



Getting Started
=====
### 1. 환경 ###
- Java - OpenJDK 1.8.0.111 64 bit, JDK 1.8.0_192

### 2. 연동방법 ###
 - 연동방법은 [배치파일_인터페이스설계서](https://github.com/ODTBuilder/ValidatorBatch-v0.1/blob/master/%EB%B0%B0%EC%B9%98%ED%8C%8C%EC%9D%BC_%EC%9D%B8%ED%84%B0%ED%8E%98%EC%9D%B4%EC%8A%A4%EC%84%A4%EA%B3%84%EC%84%9C.docx)를 참고하시길 바랍니다.


특징
=====
- QGIS 플러그인에서 Java 클래스를 외부에서 호출이 가능하게 Batch 파일로 연동합니다. 
- 검수 내용은 [공간자료 검증도구v1.0](https://github.com/ODTBuilder/Validator-v1.0) 에 의거합니다.



파라미터
=====
```css
*  --basedir       : 최상위 폴더
*  --filetype      : 파일타입
                     dxf
                     ngi
                     shp
*  --cidx          : 옵션타입 
                     1 - 수치지도  1.0
                     2 - 수치지도  2.0
                     3 - 지하시설물 1.0
                     4 - 지하시설물 2.0
                     5 - 임상도
*  --layerdefpath  : 레이어 정의 옵션 경로
*  --valoptpath    : 검수 옵션 경로
*  --objfilepath   : 검수 대상  경로
*  --crs           : 좌표계
```

프로세스구조
====

<img width="710" alt="2018-08-23 1 25 12" src="https://user-images.githubusercontent.com/13480171/44504711-2e9d2f00-a6d8-11e8-89fc-6371d15ae403.png">

연구기관
=====
- 세부 책임 : 부산대학교 <link>http://www.pusan.ac.kr/<br>
- 연구 책임 : 국토연구원 <link>http://www.krihs.re.kr/


Mail
=====
Developer : SG.LEE
ghre55@git.co.kr
