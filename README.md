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

## 태블릿 AOD 앱

갤럭시 탭용 AOD 앱은 [`aod-app/`](aod-app/README.md)에 있습니다.

made by 정산찬
