# SVMS-MOBILE

SVMS(svms.sinokor.co.kr) 화면을 폰(아이폰 사파리) 폭에 맞게 다시 배치하는 북마크용 코드입니다.

- 화면 모양(CSS)만 바꾸고, 조회·저장 같은 SVMS 동작과 데이터는 건드리지 않습니다.
- 화면 폭 1100px 이하(폰, 아이패드 세로)에서만 바뀌고, PC 화면은 그대로입니다.
- 위쪽 ☰ 옆 **PC** 버튼을 누르면 원래 화면, 오른쪽 아래 **모바일** 버튼을 누르면 다시 폰 화면이 됩니다.

## 북마크 코드

사파리 북마크의 주소 칸에 아래 한 줄을 그대로 붙여넣습니다.

```
javascript:void(function(d){var s=d.createElement('script');s.src='https://tanbaalbo.github.io/SVMS-MOBILE/svms-mobile.js?t='+Date.now();d.head.appendChild(s)}(document))
```

북마크는 이 저장소의 `svms-mobile.js`를 불러와 실행합니다. 이 파일을 고치면 북마크를 다시 만들지 않아도 다음에 누를 때 바로 반영됩니다.

made by 정산찬

## 승하선 순번 (crew/)

사관·조리장 승하선 순번표를 SVMS 탭 안에서 띄우는 북마크입니다 (안드로이드 앱과 같은 화면·기능). 같은 사이트 안에서 돌기 때문에 SVMS 조회(명단 업데이트)가 그대로 됩니다.

- 설정·사용법: https://tanbaalbo.github.io/SVMS-MOBILE/crew/setup.html
- 북마크 코드 (사파리 북마크 주소 칸에 붙여넣기):

```
javascript:(function(){var s=document.createElement('script');s.src='https://tanbaalbo.github.io/SVMS-MOBILE/crew/svms.js?'+Date.now();document.body.appendChild(s);})();
```

- 쓰는 법: 사파리에서 SVMS 로그인 → 북마크 "승하선 순번" → 그 탭이 순번표가 됩니다. 명단은 사파리의 SVMS 사이트 저장소에 남습니다.
- 인터넷 없이 보기: https://tanbaalbo.github.io/SVMS-MOBILE/crew/ 를 홈 화면에 추가 (보기 전용). 순번표에서 "오프라인 보기용 복사" → 홈 화면 앱에서 "붙여넣기로 가져오기".
- `crew/` 파일은 순번표 HTML 원본에서 `make_web.py`로 만듭니다 (원본·스크립트는 PC `12. 승하선 순번\웹버전\`).
