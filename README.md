# DeptManagement_BackEnd
- **부서 금전 관리 페이지**는 회사원들 본인이 필요한 비품을 **신청**, **저장** 및 **관리**할 수 있는 시스템 페이지입니다.
- **직책**은 계층 구조로 **사원**, **센터장**, **관리자**로 이루어져 있으며 각각의 내역은 **상위 계층에서 처리**합니다.
- **정해진 결재 라인**에 따라 **승인**되거나 **반려** 처리 할 수 있도록 체계적으로 구성되어 있어 **투명하고 일관된 관리**가 가능합니다.

# 기술 스택
**FrontEnd**
> - HTML, CSS
> - JavaScript
> - TypeScript
> - React

**BackEnd**
> - Java
> - SpringBoot

**DB**
> - PostgreSQL

# 기능
![image](https://github.com/user-attachments/assets/c2c0f030-9256-48da-999e-978a8430bf2f)

**사원**
> 1. 신청 내역 등록(**대기중**으로 등록) - 대기 조회
> 2. **대기중**인 신청 내역 **상신** - 대기 조회
> 3. **자신**의 모든 신청 내역 **조회** - 주문 현황

**팀장**
> 1. 신청 내역 등록(**대기중**으로 등록) - 대기 조회
> 2. **대기중**인 신청 내역 **상신** - 대기 조회
> 3. **자신을 포함한 팀**의 모든 신청 내역 **조회** - 주문 현황
> 4. **팀원**이 **상신**한 신청 내역을 **승인** 및 **반려** - 상신 조회

**센터장**
> 1. 모든 신청 내역 **조회** - 주문 현황
> 2. **팀장**이 **상신**한 신청 내역을 최종 **승인** 및 **반려** - 상신 조회

# 페이지 구성
**로그인**
> - 아이디, 비밀번호를 통해 로그인

**대기 조회** 
> - 자신의 신청 내역을 **대기중** 상태로 등록 및 관리
> - **대기중** 상태로 등록된 신청 내역을 **상신**

**주문 현황**
> - 직책에 따라서 현재 **대기중** 상태가 아닌 신청 내역 조회
> - **부서**, **사원**, **처리 현황**을 선택해서 세부적으로 조회

**상신 조회**
> - **상신**한 신청 내역을 **승인** 및 **반려** 처리

# API 명세서
![명세서1](https://github.com/user-attachments/assets/8daad291-f630-4d58-ab31-4401220e8737)
![명세서2](https://github.com/user-attachments/assets/96bbffeb-7a4b-41c6-b8da-3eb1fd3d883f)

# 참조
- [노션](https://ritzy-hisser-df2.notion.site/15b71ec8238349c8bf0ea82e9fca0817)
- [피그마](https://www.figma.com/design/Z1c764VvTFJOyaH4kzaQhz/SJ?node-id=0-1)
- [FrontEnd_Github](https://github.com/SunJinInternShip/DeptManagement_FrontEnd)
- [BackEnd_Github](https://github.com/SunJinInternShip/DeptManagement_BackEnd)
