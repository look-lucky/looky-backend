-- [시드 데이터] local, dev 환경에서만 실행됨 (prod 제외)
-- R__ prefix: 파일 내용 변경시 자동 적용

SET FOREIGN_KEY_CHECKS = 0;

/* Universities */
INSERT IGNORE INTO university (university_id, email_domain, name) VALUES
    (1, 'snu.ac.kr', '서울대학교'),
    (2, 'yonsei.ac.kr', '연세대학교'),
    (3, 'korea.ac.kr', '고려대학교'),
    (4, 'sogang.ac.kr', '서강대학교'),
    (5, 'hanyang.ac.kr', '한양대학교'),
    (6, 'jbnu.ac.kr', '전북대학교');

/* Users */
INSERT IGNORE INTO user (
    user_id, created_at, modified_at, created_by, last_modified_by,
    username, password, gender, birth_date, role, social_type, social_id, email, deleted, deleted_at
) VALUES
    (1, '2026-03-01 09:00:00', '2026-03-01 09:00:00', 'seed', 'seed', 'admin', '$2a$10$wT.f/h.d3M9Zq8vQZ3.8eu.Y.xG7c./UvT/F3mX.s.', 2, '1990-01-15', 'ROLE_ADMIN', 'LOCAL', NULL, 'admin@looky.local', 0, NULL),
    (2, '2026-02-10 10:00:00', '2026-02-10 10:00:00', 'seed', 'seed', 'minjun.jbnu', '$2a$10$wT.f/h.d3M9Zq8vQZ3.8eu.Y.xG7c./UvT/F3mX.s.', 0, '2001-03-12', 'ROLE_STUDENT', 'LOCAL', NULL, 'minjun@jbnu.ac.kr', 0, NULL),
    (3, '2026-02-11 10:00:00', '2026-02-11 10:00:00', 'seed', 'seed', 'soeun.jbnu', '$2a$10$wT.f/h.d3M9Zq8vQZ3.8eu.Y.xG7c./UvT/F3mX.s.', 1, '2000-08-24', 'ROLE_STUDENT', 'GOOGLE', 'google-student-3', 'soeun@jbnu.ac.kr', 0, NULL),
    (4, '2026-02-12 10:00:00', '2026-02-12 10:00:00', 'seed', 'seed', 'hyunwoo.snu', '$2a$10$wT.f/h.d3M9Zq8vQZ3.8eu.Y.xG7c./UvT/F3mX.s.', 0, '1999-11-03', 'ROLE_STUDENT', 'APPLE', 'apple-student-4', 'hyunwoo@snu.ac.kr', 0, NULL),
    (5, '2026-02-13 10:00:00', '2026-02-13 10:00:00', 'seed', 'seed', 'yebin.ku', '$2a$10$wT.f/h.d3M9Zq8vQZ3.8eu.Y.xG7c./UvT/F3mX.s.', 1, '2001-05-30', 'ROLE_STUDENT', 'KAKAO', 'kakao-student-5', 'yebin@korea.ac.kr', 0, NULL),
    (6, '2026-02-14 10:00:00', '2026-02-14 10:00:00', 'seed', 'seed', 'seojun.hyu', '$2a$10$wT.f/h.d3M9Zq8vQZ3.8eu.Y.xG7c./UvT/F3mX.s.', 0, '2002-01-17', 'ROLE_STUDENT', 'LOCAL', NULL, 'seojun@hanyang.ac.kr', 0, NULL),
    (7, '2026-02-15 10:00:00', '2026-02-15 10:00:00', 'seed', 'seed', 'chaeon.yonsei', '$2a$10$wT.f/h.d3M9Zq8vQZ3.8eu.Y.xG7c./UvT/F3mX.s.', 1, '2001-09-09', 'ROLE_STUDENT', 'GOOGLE', 'google-student-7', 'chaeon@yonsei.ac.kr', 0, NULL),
    (8, '2026-02-16 10:00:00', '2026-02-16 10:00:00', 'seed', 'seed', 'dowon.jbnu', '$2a$10$wT.f/h.d3M9Zq8vQZ3.8eu.Y.xG7c./UvT/F3mX.s.', 2, '2003-12-01', 'ROLE_STUDENT', 'APPLE', 'apple-student-8', 'dowon@jbnu.ac.kr', 0, NULL),
    (9, '2026-02-17 10:00:00', '2026-02-17 10:00:00', 'seed', 'seed', 'owner.kim', '$2a$10$wT.f/h.d3M9Zq8vQZ3.8eu.Y.xG7c./UvT/F3mX.s.', 1, '1988-07-04', 'ROLE_OWNER', 'LOCAL', NULL, 'owner.kim@looky.local', 0, NULL),
    (10, '2026-02-18 10:00:00', '2026-02-18 10:00:00', 'seed', 'seed', 'owner.park', '$2a$10$wT.f/h.d3M9Zq8vQZ3.8eu.Y.xG7c./UvT/F3mX.s.', 0, '1986-04-11', 'ROLE_OWNER', 'KAKAO', 'kakao-owner-10', 'owner.park@looky.local', 0, NULL),
    (11, '2026-02-19 10:00:00', '2026-02-19 10:00:00', 'seed', 'seed', 'owner.lee', '$2a$10$wT.f/h.d3M9Zq8vQZ3.8eu.Y.xG7c./UvT/F3mX.s.', 0, '1989-02-14', 'ROLE_OWNER', 'GOOGLE', 'google-owner-11', 'owner.lee@looky.local', 0, NULL),
    (12, '2026-02-20 10:00:00', '2026-02-20 10:00:00', 'seed', 'seed', 'owner.choi', '$2a$10$wT.f/h.d3M9Zq8vQZ3.8eu.Y.xG7c./UvT/F3mX.s.', 1, '1991-10-22', 'ROLE_OWNER', 'LOCAL', NULL, 'owner.choi@looky.local', 0, NULL),
    (13, '2026-02-21 10:00:00', '2026-02-21 10:00:00', 'seed', 'seed', 'council.jbnu', '$2a$10$wT.f/h.d3M9Zq8vQZ3.8eu.Y.xG7c./UvT/F3mX.s.', 0, '1998-06-13', 'ROLE_COUNCIL', 'LOCAL', NULL, 'council@jbnu.ac.kr', 0, NULL),
    (14, '2026-02-22 10:00:00', '2026-02-22 10:00:00', 'seed', 'seed', 'council.snu', '$2a$10$wT.f/h.d3M9Zq8vQZ3.8eu.Y.xG7c./UvT/F3mX.s.', 1, '1997-03-28', 'ROLE_COUNCIL', 'LOCAL', NULL, 'council@snu.ac.kr', 0, NULL),
    (15, '2026-02-23 10:00:00', '2026-02-23 10:00:00', 'seed', 'seed', 'harin.sogang', '$2a$10$wT.f/h.d3M9Zq8vQZ3.8eu.Y.xG7c./UvT/F3mX.s.', 1, '2002-07-15', 'ROLE_STUDENT', 'KAKAO', 'kakao-student-15', 'harin@sogang.ac.kr', 0, NULL);

INSERT IGNORE INTO student_profile (user_id, nickname, university_id, is_club_member) VALUES
    (2, '전주점심헌터', 6, 1),
    (3, '카페수집가', 6, 0),
    (4, '관악산책러', 1, 1),
    (5, '쿠폰먼저보는편', 3, 0),
    (6, '헬시푸드파인더', 5, 1),
    (7, '야식탐험대', 2, 0),
    (8, '리뷰는신중하게', 6, 0),
    (15, '비건브런치러버', 4, 1);

INSERT IGNORE INTO owner_profile (user_id, name) VALUES
    (9, '김윤서'),
    (10, '박준형'),
    (11, '이도현'),
    (12, '최서연');

INSERT IGNORE INTO council_profile (user_id, university_id) VALUES
    (13, 6),
    (14, 1);

/* Organizations */
INSERT IGNORE INTO organization (
    organization_id, created_at, modified_at, created_by, last_modified_by,
    category, name, expires_at, university_id, parent_id, user_id
) VALUES
    (1, '2026-03-05 09:00:00', '2026-03-05 09:00:00', 'seed', 'seed', 'UNIVERSITY_COUNCIL', '전북대학교 총학생회', '2027-02-28 23:59:59', 6, NULL, 13),
    (2, '2026-03-05 09:05:00', '2026-03-05 09:05:00', 'seed', 'seed', 'COLLEGE', '전북대학교 공과대학 학생회', '2027-02-28 23:59:59', 6, 1, 13),
    (3, '2026-03-05 09:10:00', '2026-03-05 09:10:00', 'seed', 'seed', 'DEPARTMENT', '전북대학교 소프트웨어공학과 학생회', '2027-02-28 23:59:59', 6, 2, 13),
    (4, '2026-03-05 09:15:00', '2026-03-05 09:15:00', 'seed', 'seed', 'CLUB_ASSOCIATION', '전북대학교 중앙동아리연합회', '2026-12-31 23:59:59', 6, 1, 13),
    (5, '2026-03-05 09:20:00', '2026-03-05 09:20:00', 'seed', 'seed', 'UNIVERSITY_COUNCIL', '서울대학교 총학생회', '2027-02-28 23:59:59', 1, NULL, 14);

INSERT IGNORE INTO user_organization (user_organization_id, user_id, organization_id) VALUES
    (1, 2, 3),
    (2, 3, 4),
    (3, 8, 1),
    (4, 15, 4);

/* Stores */
INSERT IGNORE INTO store (
    store_id, created_at, modified_at, created_by, last_modified_by,
    name, branch, biz_reg_no, road_address, jibun_address, latitude, longitude,
    store_phone, representative_name, need_to_check, check_reason, introduction,
    operating_hours, profile_image_url, store_status, is_suspended, clover_grade, user_id
) VALUES
    (1, '2026-03-06 11:00:00', '2026-03-06 11:00:00', 'seed', 'seed', '백미담 백반연구소', '전북대 북문점', '204-81-10001', '전북특별자치도 전주시 덕진구 명륜3길 12', '전북특별자치도 전주시 덕진구 금암동 634-12', 35.846210, 127.128530, '063-271-1001', '김윤서', 0, NULL, '점심 피크타임 회전이 빠르고 혼밥 좌석과 단체석을 모두 갖춘 학생식당형 한식 매장입니다.', '{"weekday":{"open":"11:00","close":"20:30","break":"15:00-17:00"},"sat":{"open":"11:30","close":"20:00"},"sun":"CLOSED"}', 'https://cdn.looky.dev/stores/1/profile.jpg', 'ACTIVE', 0, 'SPROUT', 9),
    (2, '2026-03-06 11:10:00', '2026-03-06 11:10:00', 'seed', 'seed', '모노그레이 로스터스', '구정문점', '204-81-10002', '전북특별자치도 전주시 덕진구 명륜2길 8', '전북특별자치도 전주시 덕진구 금암동 642-7', 35.845730, 127.126820, '063-271-1002', '박준형', 0, NULL, '원두 산미와 좌석 간격에 신경 쓴 로스터리 카페로, 조용한 카공 수요가 많은 매장입니다.', '{"weekday":{"open":"08:00","close":"22:00"},"weekend":{"open":"10:00","close":"22:30"}}', 'https://cdn.looky.dev/stores/2/profile.jpg', 'ACTIVE', 0, 'THREE_LEAF', 10),
    (3, '2026-03-06 11:20:00', '2026-03-06 11:20:00', 'seed', 'seed', '별밤포차', '신정문점', '204-81-10003', '전북특별자치도 전주시 덕진구 명륜1길 19', '전북특별자치도 전주시 덕진구 금암동 651-2', 35.844120, 127.127910, '063-271-1003', '이도현', 0, NULL, '야식과 모임 수요가 많은 포차형 매장으로 늦은 시간에도 간단한 식사가 가능합니다.', '{"sun-thu":{"open":"17:00","close":"01:00"},"fri-sat":{"open":"17:00","close":"02:00"}}', 'https://cdn.looky.dev/stores/3/profile.jpg', 'ACTIVE', 0, 'SEED', 11),
    (4, '2026-03-06 11:30:00', '2026-03-06 11:30:00', 'seed', 'seed', '리듬존 코인노래연습장', '전북대점', '204-81-10004', '전북특별자치도 전주시 덕진구 백제대로 567', '전북특별자치도 전주시 덕진구 금암동 478-14', 35.847950, 127.123770, '063-271-1004', '최서연', 0, NULL, '시험기간 새벽 수요가 높아 심야 운영을 유지하는 엔터테인먼트 매장입니다.', '{"daily":{"open":"10:00","close":"02:00"},"examPeriodClose":"03:00"}', 'https://cdn.looky.dev/stores/4/profile.jpg', 'ACTIVE', 1, 'SPROUT', 12),
    (5, '2026-03-06 11:40:00', '2026-03-06 11:40:00', 'seed', 'seed', '온기피부랩', '캠퍼스점', '204-81-10005', '전북특별자치도 전주시 덕진구 가련산로 24', '전북특별자치도 전주시 덕진구 덕진동1가 1312-1', 35.849110, 127.126010, '063-271-1005', '최서연', 0, NULL, '학생 대상 저자극 케어와 피부진단 프로그램을 운영하는 뷰티 헬스 매장입니다.', '{"weekday":{"open":"10:30","close":"20:00"},"lunch":"13:00-14:00","sun":"CLOSED"}', 'https://cdn.looky.dev/stores/5/profile.jpg', 'ACTIVE', 0, 'SPROUT', 12),
    (6, '2026-03-06 11:50:00', '2026-03-06 11:50:00', 'seed', 'seed', '전북국수 정문점', NULL, NULL, '전북특별자치도 전주시 덕진구 명륜4길 5', '전북특별자치도 전주시 덕진구 금암동 660-9', 35.843490, 127.129140, NULL, NULL, 1, '지도 기반 자동 등록 매장으로 영업 정보와 대표자 확인이 필요합니다.', '자동 등록된 매장으로 보이며 학생들이 혼밥 장소로 자주 언급합니다.', '{"weekday":{"open":"10:30","close":"19:30"},"break":"15:00-16:00","sun":"CLOSED"}', 'https://cdn.looky.dev/stores/6/profile.jpg', 'UNCLAIMED', 0, 'SEED', NULL),
    (7, '2026-03-06 12:00:00', '2026-03-06 12:00:00', 'seed', 'seed', '캠퍼스 샐러드 바', '중앙도서관점', '204-81-10007', '전북특별자치도 전주시 덕진구 명륜2길 18', '전북특별자치도 전주시 덕진구 금암동 640-4', 35.846980, 127.125960, '063-271-1007', '박준형', 0, NULL, '비건 옵션과 단백질 토핑을 강화한 샐러드 브런치 매장으로 여성 고객 재방문율이 높습니다.', '{"weekday":{"open":"09:00","close":"21:00"},"weekend":{"open":"10:00","close":"20:00"}}', 'https://cdn.looky.dev/stores/7/profile.jpg', 'ACTIVE', 0, 'THREE_LEAF', 10),
    (8, '2026-03-06 12:10:00', '2026-03-06 12:10:00', 'seed', 'seed', '만화카페 아지트', '관악점', '204-81-10008', '서울특별시 관악구 대학길 7', '서울특별시 관악구 봉천동 1598-4', 37.460820, 126.951240, '02-871-1008', '이도현', 0, NULL, '장시간 체류 고객이 많았던 매장이지만 현재 운영정책 위반 이력이 있어 노출 제한 상태입니다.', '{"daily":{"open":"11:00","close":"23:00"}}', 'https://cdn.looky.dev/stores/8/profile.jpg', 'BANNED', 1, 'SEED', 11);

INSERT IGNORE INTO store_category (store_id, category) VALUES
    (1, 'RESTAURANT'),
    (2, 'CAFE'),
    (3, 'BAR'),
    (3, 'ETC'),
    (4, 'ENTERTAINMENT'),
    (5, 'BEAUTY_HEALTH'),
    (6, 'RESTAURANT'),
    (7, 'CAFE'),
    (7, 'ETC'),
    (8, 'ENTERTAINMENT');

INSERT IGNORE INTO store_mood (store_id, mood) VALUES
    (1, 'SOLO_DINING'),
    (1, 'GROUP_GATHERING'),
    (2, 'SOLO_DINING'),
    (2, 'ROMANTIC'),
    (3, 'GROUP_GATHERING'),
    (3, 'LATE_NIGHT'),
    (4, 'GROUP_GATHERING'),
    (4, 'LATE_NIGHT'),
    (5, 'ROMANTIC'),
    (6, 'SOLO_DINING'),
    (7, 'SOLO_DINING'),
    (7, 'ROMANTIC'),
    (8, 'SOLO_DINING'),
    (8, 'GROUP_GATHERING');

INSERT IGNORE INTO store_university (store_university_id, store_id, university_id) VALUES
    (1, 1, 6),
    (2, 2, 6),
    (3, 2, 1),
    (4, 3, 6),
    (5, 4, 6),
    (6, 5, 6),
    (7, 6, 6),
    (8, 7, 6),
    (9, 7, 5),
    (10, 8, 1);

INSERT IGNORE INTO store_image (store_image_id, store_id, image_url, order_index) VALUES
    (1, 1, 'https://cdn.looky.dev/stores/1/cover.jpg', 0),
    (2, 1, 'https://cdn.looky.dev/stores/1/interior.jpg', 1),
    (3, 2, 'https://cdn.looky.dev/stores/2/cover.jpg', 0),
    (4, 2, 'https://cdn.looky.dev/stores/2/beans.jpg', 1),
    (5, 3, 'https://cdn.looky.dev/stores/3/cover.jpg', 0),
    (6, 3, 'https://cdn.looky.dev/stores/3/menu.jpg', 1),
    (7, 4, 'https://cdn.looky.dev/stores/4/cover.jpg', 0),
    (8, 4, 'https://cdn.looky.dev/stores/4/room.jpg', 1),
    (9, 5, 'https://cdn.looky.dev/stores/5/cover.jpg', 0),
    (10, 5, 'https://cdn.looky.dev/stores/5/care.jpg', 1),
    (11, 6, 'https://cdn.looky.dev/stores/6/cover.jpg', 0),
    (12, 7, 'https://cdn.looky.dev/stores/7/cover.jpg', 0),
    (13, 7, 'https://cdn.looky.dev/stores/7/brunch.jpg', 1),
    (14, 8, 'https://cdn.looky.dev/stores/8/cover.jpg', 0);

INSERT IGNORE INTO store_holiday (store_id, holiday_date) VALUES
    (2, '2026-03-25'),
    (3, '2026-03-30'),
    (5, '2026-04-01'),
    (7, '2026-03-26'),
    (7, '2026-04-02');

/* Item categories and items */
INSERT IGNORE INTO item_category (
    item_category_id, created_at, modified_at, created_by, last_modified_by, name, store_id
) VALUES
    (1, '2026-03-07 10:00:00', '2026-03-07 10:00:00', 'seed', 'seed', '식사메뉴', 1),
    (2, '2026-03-07 10:01:00', '2026-03-07 10:01:00', 'seed', 'seed', '사이드', 1),
    (3, '2026-03-07 10:02:00', '2026-03-07 10:02:00', 'seed', 'seed', '커피', 2),
    (4, '2026-03-07 10:03:00', '2026-03-07 10:03:00', 'seed', 'seed', '디저트', 2),
    (5, '2026-03-07 10:04:00', '2026-03-07 10:04:00', 'seed', 'seed', '안주', 3),
    (6, '2026-03-07 10:05:00', '2026-03-07 10:05:00', 'seed', 'seed', '주류', 3),
    (7, '2026-03-07 10:06:00', '2026-03-07 10:06:00', 'seed', 'seed', '이용권', 4),
    (8, '2026-03-07 10:07:00', '2026-03-07 10:07:00', 'seed', 'seed', '케어', 5),
    (9, '2026-03-07 10:08:00', '2026-03-07 10:08:00', 'seed', 'seed', '국수', 6),
    (10, '2026-03-07 10:09:00', '2026-03-07 10:09:00', 'seed', 'seed', '샐러드', 7),
    (11, '2026-03-07 10:10:00', '2026-03-07 10:10:00', 'seed', 'seed', '브런치', 7);

INSERT IGNORE INTO item (
    item_id, created_at, modified_at, created_by, last_modified_by,
    name, price, description, image_url, is_sold_out, item_order, is_representative, is_hidden, badge, store_id, item_category_id
) VALUES
    (1, '2026-03-07 11:00:00', '2026-03-07 11:00:00', 'seed', 'seed', '제육정식', 8500, '점심시간 회전이 빠른 대표 한상 메뉴입니다.', 'https://cdn.looky.dev/items/1.jpg', 0, 1, 1, 0, 'HOT', 1, 1),
    (2, '2026-03-07 11:01:00', '2026-03-07 11:01:00', 'seed', 'seed', '된장찌개 정식', 8000, '자극적이지 않은 국물 맛으로 재주문이 많은 메뉴입니다.', 'https://cdn.looky.dev/items/2.jpg', 0, 2, 0, 0, 'BEST', 1, 1),
    (3, '2026-03-07 11:02:00', '2026-03-07 11:02:00', 'seed', 'seed', '계란말이', 4000, '2인 이상 주문 시 곁들이기 좋은 사이드입니다.', 'https://cdn.looky.dev/items/3.jpg', 0, 3, 0, 0, 'NEW', 1, 2),
    (4, '2026-03-07 11:03:00', '2026-03-07 11:03:00', 'seed', 'seed', '고등어구이', 9500, '당일 소진이 빠른 구이 메뉴입니다.', 'https://cdn.looky.dev/items/4.jpg', 1, 4, 0, 0, NULL, 1, 1),
    (5, '2026-03-07 11:04:00', '2026-03-07 11:04:00', 'seed', 'seed', '플랫화이트', 4500, '원두 산미가 부드럽게 살아있는 시그니처 커피입니다.', 'https://cdn.looky.dev/items/5.jpg', 0, 1, 1, 0, 'BEST', 2, 3),
    (6, '2026-03-07 11:05:00', '2026-03-07 11:05:00', 'seed', 'seed', '아인슈페너', 5500, '크림층과 에스프레소 밸런스가 안정적인 메뉴입니다.', 'https://cdn.looky.dev/items/6.jpg', 0, 2, 0, 0, 'HOT', 2, 3),
    (7, '2026-03-07 11:06:00', '2026-03-07 11:06:00', 'seed', 'seed', '바스크 치즈케이크', 6800, '주말 방문객 비중이 높은 디저트입니다.', 'https://cdn.looky.dev/items/7.jpg', 0, 3, 0, 0, 'NEW', 2, 4),
    (8, '2026-03-07 11:07:00', '2026-03-07 11:07:00', 'seed', 'seed', '휘낭시에 세트', 5900, '행사 기간에만 노출하는 한정 세트입니다.', 'https://cdn.looky.dev/items/8.jpg', 0, 4, 0, 1, NULL, 2, 4),
    (9, '2026-03-07 11:08:00', '2026-03-07 11:08:00', 'seed', 'seed', '닭갈비 철판볶음', 16900, '2인 이상 방문객이 가장 많이 고르는 메인 안주입니다.', 'https://cdn.looky.dev/items/9.jpg', 0, 1, 1, 0, 'HOT', 3, 5),
    (10, '2026-03-07 11:09:00', '2026-03-07 11:09:00', 'seed', 'seed', '국물닭발', 18900, '매운맛 단계 선택이 가능한 인기 안주입니다.', 'https://cdn.looky.dev/items/10.jpg', 0, 2, 0, 0, 'BEST', 3, 5),
    (11, '2026-03-07 11:10:00', '2026-03-07 11:10:00', 'seed', 'seed', '생맥주 500cc', 4500, '단체 주문 비중이 높은 기본 주류입니다.', 'https://cdn.looky.dev/items/11.jpg', 0, 3, 0, 0, NULL, 3, 6),
    (12, '2026-03-07 11:11:00', '2026-03-07 11:11:00', 'seed', 'seed', '청포도하이볼', 6900, '여성 고객 선호도가 높은 상큼한 하이볼입니다.', 'https://cdn.looky.dev/items/12.jpg', 0, 4, 0, 0, 'NEW', 3, 6),
    (13, '2026-03-07 11:12:00', '2026-03-07 11:12:00', 'seed', 'seed', '1시간 이용권', 5000, '짧은 공강 시간에 가장 많이 구매되는 이용권입니다.', 'https://cdn.looky.dev/items/13.jpg', 0, 1, 1, 0, 'HOT', 4, 7),
    (14, '2026-03-07 11:13:00', '2026-03-07 11:13:00', 'seed', 'seed', '3시간 이용권', 12000, '시험기간 패키지로 가장 반응이 좋은 이용권입니다.', 'https://cdn.looky.dev/items/14.jpg', 0, 2, 0, 0, 'BEST', 4, 7),
    (15, '2026-03-07 11:14:00', '2026-03-07 11:14:00', 'seed', 'seed', '학생 피부진단', 10000, '민감도와 유수분 밸런스를 빠르게 확인할 수 있는 체험 상품입니다.', 'https://cdn.looky.dev/items/15.jpg', 0, 1, 1, 0, 'BEST', 5, 8),
    (16, '2026-03-07 11:15:00', '2026-03-07 11:15:00', 'seed', 'seed', '진정 마스크팩', 5000, '학생 할인 적용 시 재구매율이 높은 상품입니다.', 'https://cdn.looky.dev/items/16.jpg', 0, 2, 0, 0, 'NEW', 5, 8),
    (17, '2026-03-07 11:16:00', '2026-03-07 11:16:00', 'seed', 'seed', '눈가 집중 케어', 15000, '시험기간 피로 회복 수요가 높은 집중 케어입니다.', 'https://cdn.looky.dev/items/17.jpg', 0, 3, 0, 0, 'HOT', 5, 8),
    (18, '2026-03-07 11:17:00', '2026-03-07 11:17:00', 'seed', 'seed', '잔치국수', 5000, '가볍게 한 끼 해결하려는 혼밥 수요가 가장 많은 메뉴입니다.', 'https://cdn.looky.dev/items/18.jpg', 0, 1, 1, 0, 'BEST', 6, 9),
    (19, '2026-03-07 11:18:00', '2026-03-07 11:18:00', 'seed', 'seed', '비빔국수', 6000, '매콤한 양념을 선호하는 학생층이 자주 찾는 메뉴입니다.', 'https://cdn.looky.dev/items/19.jpg', 0, 2, 0, 0, 'HOT', 6, 9),
    (20, '2026-03-07 11:19:00', '2026-03-07 11:19:00', 'seed', 'seed', '연어 포케', 12500, '단백질 토핑 선택이 가능한 대표 샐러드입니다.', 'https://cdn.looky.dev/items/20.jpg', 0, 1, 1, 0, 'BEST', 7, 10),
    (21, '2026-03-07 11:20:00', '2026-03-07 11:20:00', 'seed', 'seed', '두부 곡물볼', 9800, '비건 선택지를 찾는 학생층에게 반응이 좋은 메뉴입니다.', 'https://cdn.looky.dev/items/21.jpg', 0, 2, 0, 0, 'VEGAN', 7, 10),
    (22, '2026-03-07 11:21:00', '2026-03-07 11:21:00', 'seed', 'seed', '아보카도 오픈토스트', 8500, '브런치 시간대 판매 비중이 높은 메뉴입니다.', 'https://cdn.looky.dev/items/22.jpg', 0, 3, 0, 0, 'NEW', 7, 11),
    (23, '2026-03-07 11:22:00', '2026-03-07 11:22:00', 'seed', 'seed', '그릭요거트 볼', 6500, '가볍게 먹기 좋은 후식형 메뉴입니다.', 'https://cdn.looky.dev/items/23.jpg', 0, 4, 0, 0, 'HOT', 7, 11),
    (24, '2026-03-07 11:23:00', '2026-03-07 11:23:00', 'seed', 'seed', '캔음료', 2000, '이용권과 함께 가장 많이 추가되는 부가 상품입니다.', 'https://cdn.looky.dev/items/24.jpg', 0, 3, 0, 0, NULL, 4, 7);

/* Coupons */
INSERT IGNORE INTO coupon (
    coupon_id, created_at, modified_at, created_by, last_modified_by,
    title, issue_starts_at, issue_ends_at, valid_days, total_quantity, limit_per_user,
    status, benefit_type, benefit_value, min_order_amount, download_count, store_id
) VALUES
    (1, '2026-03-08 09:00:00', '2026-03-08 09:00:00', 'seed', 'seed', '점심시간 1,500원 할인', '2026-03-18 00:00:00', '2026-04-10 23:59:59', 7, 120, 1, 'ACTIVE', 'FIXED_DISCOUNT', '1500', 8000, 37, 1),
    (2, '2026-03-08 09:10:00', '2026-03-08 09:10:00', 'seed', 'seed', '시그니처 음료 10% 할인', '2026-03-10 00:00:00', '2026-03-31 23:59:59', 5, 50, 1, 'SOLD_OUT', 'PERCENTAGE_DISCOUNT', '10', 4000, 50, 2),
    (3, '2026-03-08 09:20:00', '2026-03-08 09:20:00', 'seed', 'seed', '하이볼 1잔 서비스', '2026-03-01 00:00:00', '2026-03-25 23:59:59', 3, NULL, 1, 'WITHDRAWN_BY_OWNER', 'SERVICE_GIFT', '청포도하이볼 1잔', 20000, 18, 3),
    (4, '2026-03-08 09:30:00', '2026-03-08 09:30:00', 'seed', 'seed', '학생 진정팩 증정', '2026-02-15 00:00:00', '2026-03-05 23:59:59', 10, 80, 1, 'EXPIRED', 'SERVICE_GIFT', '진정 마스크팩 1매', 10000, 29, 5),
    (5, '2026-03-08 09:40:00', '2026-03-08 09:40:00', 'seed', 'seed', '샐러드 12% 할인', '2026-03-19 00:00:00', '2026-04-15 23:59:59', 5, 70, 2, 'ACTIVE', 'PERCENTAGE_DISCOUNT', '12', 9000, 16, 7);

INSERT IGNORE INTO student_coupon (
    student_coupon_id, created_at, modified_at, created_by, last_modified_by,
    verification_code, status, downloaded_at, activated_at, used_at, expires_at, user_id, coupon_id
) VALUES
    (1, '2026-03-19 12:00:00', '2026-03-19 12:00:00', 'seed', 'seed', NULL, 'UNUSED', '2026-03-19 12:00:00', NULL, NULL, '2026-03-26 23:59:59', 2, 1),
    (2, '2026-03-18 18:00:00', '2026-03-20 10:30:00', 'seed', 'seed', 'F7K2', 'ACTIVATED', '2026-03-18 18:00:00', '2026-03-20 10:30:00', NULL, '2026-03-25 23:59:59', 5, 1),
    (3, '2026-03-15 14:00:00', '2026-03-15 19:20:00', 'seed', 'seed', 'P3L9', 'USED', '2026-03-15 14:00:00', '2026-03-15 18:50:00', '2026-03-15 19:20:00', '2026-03-22 23:59:59', 7, 1),
    (4, '2026-03-12 09:40:00', '2026-03-13 15:00:00', 'seed', 'seed', 'Q1W8', 'USED', '2026-03-12 09:40:00', '2026-03-13 14:40:00', '2026-03-13 15:00:00', '2026-03-17 23:59:59', 3, 2),
    (5, '2026-03-01 08:00:00', '2026-03-11 00:00:00', 'seed', 'seed', NULL, 'EXPIRED', '2026-03-01 08:00:00', NULL, NULL, '2026-03-10 23:59:59', 6, 2),
    (6, '2026-02-25 13:00:00', '2026-03-06 00:00:00', 'seed', 'seed', NULL, 'EXPIRED', '2026-02-25 13:00:00', NULL, NULL, '2026-03-05 23:59:59', 8, 4),
    (7, '2026-03-20 09:15:00', '2026-03-20 09:15:00', 'seed', 'seed', NULL, 'UNUSED', '2026-03-20 09:15:00', NULL, NULL, '2026-03-25 23:59:59', 15, 5),
    (8, '2026-03-20 08:20:00', '2026-03-20 08:55:00', 'seed', 'seed', 'N4D6', 'ACTIVATED', '2026-03-20 08:20:00', '2026-03-20 08:55:00', NULL, '2026-03-25 23:59:59', 4, 5);

/* Events */
INSERT IGNORE INTO event (
    event_id, created_at, modified_at, created_by, last_modified_by,
    title, subtitle, description, latitude, longitude, place,
    start_date_time, end_date_time, status, university_id
) VALUES
    (1, '2026-03-09 10:00:00', '2026-03-09 10:00:00', 'seed', 'seed', '전북대 봄맞이 푸드트럭 페스타', '학생회관 앞 3일간 진행', '학생회와 지역 청년상인이 함께하는 봄맞이 먹거리 행사입니다.', 35.847310, 127.128140, '전북대학교 학생회관 앞 광장', '2026-03-27 11:00:00', '2026-03-29 20:00:00', 'UPCOMING', 6),
    (2, '2026-03-09 10:10:00', '2026-03-09 10:10:00', 'seed', 'seed', '중앙도서관 나이트런 콘서트', '공부 끝나고 즐기는 야간 공연', '도서관 야외 계단에서 진행되는 어쿠스틱 공연입니다.', 37.459830, 126.951990, '서울대학교 중앙도서관 야외계단', '2026-03-20 18:00:00', '2026-03-20 21:30:00', 'LIVE', 1),
    (3, '2026-03-09 10:20:00', '2026-03-09 10:20:00', 'seed', 'seed', '동아리 리크루팅 박람회', '1학기 중앙동아리 모집', '공연, 체험, 홍보 부스를 한 번에 둘러볼 수 있는 모집 행사입니다.', 35.846580, 127.127760, '전북대학교 대운동장', '2026-03-12 13:00:00', '2026-03-12 18:00:00', 'ENDED', 6),
    (4, '2026-03-09 10:30:00', '2026-03-09 10:30:00', 'seed', 'seed', '북문 플리마켓 with 로컬 브랜드', '학생 셀러와 로컬 브랜드 팝업', '의류, 리빙, 디저트 셀러가 함께 참여하는 플리마켓입니다.', 35.845970, 127.128880, '전북대 북문 공영주차장 옆 공터', '2026-03-26 12:00:00', '2026-03-26 19:00:00', 'UPCOMING', 6),
    (5, '2026-03-09 10:40:00', '2026-03-09 10:40:00', 'seed', 'seed', '캠퍼스 야외 영화제', '잔디광장 빈백 상영회', '학생 휴게주간 프로그램으로 운영되는 야외 영화 상영 행사입니다.', 37.551900, 126.941710, '연세대학교 노천극장 앞 잔디광장', '2026-03-21 19:00:00', '2026-03-21 22:00:00', 'UPCOMING', 2);

INSERT IGNORE INTO event_type (event_id, event_type) VALUES
    (1, 'FOOD_EVENT'),
    (1, 'STUDENT_EVENT'),
    (2, 'SCHOOL_EVENT'),
    (2, 'PERFORMANCE'),
    (3, 'STUDENT_EVENT'),
    (3, 'SCHOOL_EVENT'),
    (4, 'FLEA_MARKET'),
    (4, 'BRAND_POPUP'),
    (5, 'STUDENT_EVENT'),
    (5, 'PERFORMANCE');

INSERT IGNORE INTO event_image (
    event_image_id, created_at, modified_at, created_by, last_modified_by,
    event_id, image_url, order_index, image_type
) VALUES
    (1, '2026-03-09 11:00:00', '2026-03-09 11:00:00', 'seed', 'seed', 1, 'https://cdn.looky.dev/events/1-banner.jpg', 0, 'BANNER'),
    (2, '2026-03-09 11:01:00', '2026-03-09 11:01:00', 'seed', 'seed', 1, 'https://cdn.looky.dev/events/1-map.jpg', 1, 'GENERAL'),
    (3, '2026-03-09 11:02:00', '2026-03-09 11:02:00', 'seed', 'seed', 2, 'https://cdn.looky.dev/events/2-banner.jpg', 0, 'BANNER'),
    (4, '2026-03-09 11:03:00', '2026-03-09 11:03:00', 'seed', 'seed', 3, 'https://cdn.looky.dev/events/3-booth.jpg', 0, 'GENERAL'),
    (5, '2026-03-09 11:04:00', '2026-03-09 11:04:00', 'seed', 'seed', 4, 'https://cdn.looky.dev/events/4-banner.jpg', 0, 'BANNER'),
    (6, '2026-03-09 11:05:00', '2026-03-09 11:05:00', 'seed', 'seed', 5, 'https://cdn.looky.dev/events/5-banner.jpg', 0, 'BANNER');

/* Partnerships and favorites */
INSERT IGNORE INTO partnership (
    partnership_id, created_at, modified_at, created_by, last_modified_by, benefit, starts_at, ends_at, store_id, organization_id
) VALUES
    (1, '2026-03-10 09:00:00', '2026-03-10 09:00:00', 'seed', 'seed', '공과대 학생회 제휴로 제육정식 1,000원 추가 할인', '2026-03-15', '2026-06-30', 1, 2),
    (2, '2026-03-10 09:10:00', '2026-03-10 09:10:00', 'seed', 'seed', '중앙동아리연합회 회원 대상 음료 사이즈업 무료', '2026-03-18', '2026-05-31', 2, 4),
    (3, '2026-03-10 09:20:00', '2026-03-10 09:20:00', 'seed', 'seed', '총학생회 행사 스태프 대상 샐러드 메뉴 15% 할인', '2026-03-20', '2026-04-30', 7, 1);

INSERT IGNORE INTO favorite_store (
    favorite_store_id, created_at, modified_at, created_by, last_modified_by, user_id, store_id
) VALUES
    (1, '2026-03-11 08:00:00', '2026-03-11 08:00:00', 'seed', 'seed', 2, 1),
    (2, '2026-03-11 08:05:00', '2026-03-11 08:05:00', 'seed', 'seed', 3, 2),
    (3, '2026-03-11 08:10:00', '2026-03-11 08:10:00', 'seed', 'seed', 5, 7),
    (4, '2026-03-11 08:15:00', '2026-03-11 08:15:00', 'seed', 'seed', 6, 5),
    (5, '2026-03-11 08:20:00', '2026-03-11 08:20:00', 'seed', 'seed', 7, 3),
    (6, '2026-03-11 08:25:00', '2026-03-11 08:25:00', 'seed', 'seed', 15, 7);

/* Claim and report samples */
INSERT IGNORE INTO store_claim (
    store_claim_request_id, created_at, modified_at, created_by, last_modified_by,
    store_id, user_id, biz_reg_no, representative_name, store_name, store_phone,
    license_image_url, status, reject_reason, admin_memo
) VALUES
    (1, '2026-03-18 09:00:00', '2026-03-18 09:00:00', 'seed', 'seed', 6, 9, '204-81-20006', '김윤서', '전북국수 정문점', '063-271-2006', 'https://cdn.looky.dev/claims/1-license.jpg', 'PENDING', NULL, '자동 등록 매장과 제출 서류의 주소 일치 여부 확인 필요'),
    (2, '2026-03-17 14:30:00', '2026-03-19 16:00:00', 'seed', 'seed', 8, 11, '204-81-20008', '이도현', '만화카페 아지트', '02-871-2008', 'https://cdn.looky.dev/claims/2-license.jpg', 'REJECTED', '휴업 상태와 사업자 등록 주소가 일치하지 않습니다.', '추가 서류 제출 전까지 승인 보류');

INSERT IGNORE INTO store_report (
    store_report_id, created_at, modified_at, created_by, last_modified_by, detail, store_id, reporter_id
) VALUES
    (1, '2026-03-19 13:20:00', '2026-03-19 13:20:00', 'seed', 'seed', '지도 위치는 맞지만 실제 영업 여부가 불명확합니다.', 6, 3),
    (2, '2026-03-19 18:40:00', '2026-03-19 18:40:00', 'seed', 'seed', '휴업 안내문이 붙어 있었고 현재 입장이 불가능했습니다.', 8, 2);

INSERT IGNORE INTO store_report_reason (store_report_id, reason) VALUES
    (1, 'LOCATION_MISMATCH'),
    (1, 'INFO_ERROR'),
    (2, 'CLOSED_OR_MOVED');

/* Store news */
INSERT IGNORE INTO store_news (
    store_news_id, created_at, modified_at, created_by, last_modified_by,
    store_id, title, content, like_count, comment_count
) VALUES
    (1, '2026-03-16 09:00:00', '2026-03-16 09:00:00', 'seed', 'seed', 1, '혼밥 손님 위한 1인석 확장', '점심 혼잡 시간에도 빠르게 이용할 수 있도록 창가 1인석 6좌석을 추가했습니다.', 14, 2),
    (2, '2026-03-16 10:00:00', '2026-03-16 10:00:00', 'seed', 'seed', 2, '벚꽃 시즌 한정 딸기라떼 출시', '전북대 벚꽃 시즌 동안만 판매하는 생딸기 라떼와 크럼블 타르트를 함께 운영합니다.', 22, 1),
    (3, '2026-03-16 11:00:00', '2026-03-16 11:00:00', 'seed', 'seed', 3, '금요일 11시까지 연장 영업', '금요일은 하이볼 주문 고객이 많아 기존보다 1시간 연장 영업합니다.', 9, 1),
    (4, '2026-03-16 12:00:00', '2026-03-16 12:00:00', 'seed', 'seed', 4, '시험기간 학생증 인증 1시간 추가', '시험기간 한정으로 3시간권 구매 시 학생증 인증 고객에게 1시간을 추가 제공합니다.', 17, 2),
    (5, '2026-03-16 13:00:00', '2026-03-16 13:00:00', 'seed', 'seed', 5, '민감성 피부 진정 패키지 오픈', '트러블 집중 케어와 진정팩을 묶은 학생 전용 패키지를 새로 구성했습니다.', 6, 0),
    (6, '2026-03-16 14:00:00', '2026-03-16 14:00:00', 'seed', 'seed', 7, '비건 드레싱 2종 추가', '오리엔탈 드레싱과 레몬허브 드레싱 모두 비건 레시피로 전환했습니다.', 18, 2);

INSERT IGNORE INTO store_news_image (
    store_news_image_id, created_at, modified_at, created_by, last_modified_by, store_news_id, image_url, order_index
) VALUES
    (1, '2026-03-16 09:05:00', '2026-03-16 09:05:00', 'seed', 'seed', 1, 'https://cdn.looky.dev/news/1.jpg', 0),
    (2, '2026-03-16 10:05:00', '2026-03-16 10:05:00', 'seed', 'seed', 2, 'https://cdn.looky.dev/news/2.jpg', 0),
    (3, '2026-03-16 12:05:00', '2026-03-16 12:05:00', 'seed', 'seed', 4, 'https://cdn.looky.dev/news/4.jpg', 0),
    (4, '2026-03-16 14:05:00', '2026-03-16 14:05:00', 'seed', 'seed', 6, 'https://cdn.looky.dev/news/6.jpg', 0);

INSERT IGNORE INTO store_news_comment (
    store_news_comment_id, created_at, modified_at, created_by, last_modified_by, store_news_id, user_id, content
) VALUES
    (1, '2026-03-16 09:20:00', '2026-03-16 09:20:00', 'seed', 'seed', 1, 2, '혼밥 좌석 늘어난 거 좋네요. 점심 줄이 조금 덜 길어졌으면 합니다.'),
    (2, '2026-03-16 09:40:00', '2026-03-16 09:40:00', 'seed', 'seed', 1, 8, '창가 좌석이면 노트북 쓰기에도 괜찮을 것 같아요.'),
    (3, '2026-03-16 10:25:00', '2026-03-16 10:25:00', 'seed', 'seed', 2, 3, '딸기라떼 당도 조절 가능한지도 궁금합니다.'),
    (4, '2026-03-16 11:35:00', '2026-03-16 11:35:00', 'seed', 'seed', 3, 7, '금요일 늦게까지 하면 모임 잡기 편해지겠네요.'),
    (5, '2026-03-16 12:30:00', '2026-03-16 12:30:00', 'seed', 'seed', 4, 4, '시험기간에 자주 갈 것 같아요. 자리 예약은 안 되죠?'),
    (6, '2026-03-16 12:45:00', '2026-03-16 12:45:00', 'seed', 'seed', 4, 15, '학생증만 있으면 적용되는지 확인 부탁드립니다.'),
    (7, '2026-03-16 14:20:00', '2026-03-16 14:20:00', 'seed', 'seed', 6, 6, '레몬허브 드레싱이면 닭가슴살 토핑이랑 잘 어울릴 것 같아요.'),
    (8, '2026-03-16 14:35:00', '2026-03-16 14:35:00', 'seed', 'seed', 6, 15, '비건 옵션 늘어난 점이 가장 반갑습니다.');

/* Reviews */
INSERT IGNORE INTO review (
    review_id, created_at, modified_at, created_by, last_modified_by,
    user_id, store_id, parent_review_id, is_verified, rating, content,
    status, report_count, is_private, like_count
) VALUES
    (1, '2026-03-12 12:10:00', '2026-03-12 12:10:00', 'seed', 'seed', 2, 1, NULL, 1, 5, '점심 피크타임인데도 반찬 회전이 빨라서 깔끔했습니다. 혼밥 자리도 편했습니다.', 'VERIFIED', 0, 0, 12),
    (2, '2026-03-13 18:30:00', '2026-03-13 18:30:00', 'seed', 'seed', 3, 1, NULL, 0, 4, '양이 넉넉해서 좋았고 제육 양념이 과하게 맵지 않아 재방문 의사 있습니다.', 'PUBLISHED', 0, 0, 5),
    (3, '2026-03-13 19:10:00', '2026-03-13 19:10:00', 'seed', 'seed', 9, 1, 2, 0, NULL, '방문 후기 감사합니다. 다음 주부터 계란말이 세트 구성도 조금 바뀔 예정입니다.', 'PUBLISHED', 0, 0, 0),
    (4, '2026-03-14 16:20:00', '2026-03-14 16:20:00', 'seed', 'seed', 5, 2, NULL, 1, 5, '카공하기 좋고 콘센트 좌석도 넉넉합니다. 플랫화이트 밸런스가 안정적입니다.', 'PUBLISHED', 0, 0, 9),
    (5, '2026-03-15 20:15:00', '2026-03-15 20:15:00', 'seed', 'seed', 6, 2, NULL, 0, 3, '대기 없이 들어갔지만 직원 응대가 다소 무심하게 느껴졌습니다.', 'REPORTED', 10, 0, 1),
    (6, '2026-03-15 23:05:00', '2026-03-15 23:05:00', 'seed', 'seed', 7, 3, NULL, 1, 4, '모임하기 좋고 안주 나오는 속도가 빨랐습니다. 하이볼이 생각보다 덜 달아서 좋았습니다.', 'PUBLISHED', 0, 1, 6),
    (7, '2026-03-16 00:10:00', '2026-03-16 00:10:00', 'seed', 'seed', 8, 3, NULL, 1, 5, '늦은 시간에도 음식 퀄리티가 유지돼서 만족했습니다. 닭발이 특히 맛있었습니다.', 'PUBLISHED', 0, 0, 10),
    (8, '2026-03-16 14:10:00', '2026-03-16 14:10:00', 'seed', 'seed', 15, 4, NULL, 0, 4, '시험기간 이벤트가 실용적이고 방음 상태도 나쁘지 않았습니다.', 'PUBLISHED', 0, 0, 4),
    (9, '2026-03-17 11:20:00', '2026-03-17 11:20:00', 'seed', 'seed', 2, 5, NULL, 0, 5, '피부진단 설명이 구체적이어서 좋았습니다. 학생 패키지가 부담되지 않는 가격입니다.', 'VERIFIED', 0, 0, 3),
    (10, '2026-03-18 13:05:00', '2026-03-18 13:05:00', 'seed', 'seed', 4, 7, NULL, 1, 5, '샐러드 양이 예상보다 많고 토핑 추가 선택이 명확해서 자주 이용할 것 같습니다.', 'PUBLISHED', 0, 0, 8),
    (11, '2026-03-18 13:40:00', '2026-03-18 13:40:00', 'seed', 'seed', 3, 7, NULL, 1, 4, '드레싱 선택 폭이 넓고 브런치 메뉴가 가볍게 먹기 좋습니다.', 'PUBLISHED', 0, 0, 2),
    (12, '2026-03-18 18:10:00', '2026-03-18 18:10:00', 'seed', 'seed', 5, 6, NULL, 0, 3, '가격은 괜찮아 보이지만 아직 대표 메뉴 정보가 충분히 정리되진 않은 느낌입니다.', 'PUBLISHED', 0, 0, 1),
    (13, '2026-03-18 14:10:00', '2026-03-18 14:10:00', 'seed', 'seed', 10, 7, 10, 0, NULL, '리뷰 감사합니다. 비건 토핑과 드레싱 안내 표기도 이번 주에 보강했습니다.', 'PUBLISHED', 0, 0, 0);

INSERT IGNORE INTO review_image (review_image_id, review_id, image_url, order_index) VALUES
    (1, 1, 'https://cdn.looky.dev/reviews/1-1.jpg', 0),
    (2, 4, 'https://cdn.looky.dev/reviews/4-1.jpg', 0),
    (3, 7, 'https://cdn.looky.dev/reviews/7-1.jpg', 0),
    (4, 10, 'https://cdn.looky.dev/reviews/10-1.jpg', 0);

INSERT IGNORE INTO review_report (
    review_report_id, created_at, modified_at, created_by, last_modified_by, review_id, reporter_id, reason, detail
) VALUES
    (1, '2026-03-16 09:30:00', '2026-03-16 09:30:00', 'seed', 'seed', 5, 7, 'FRAUDULENT_REVIEW', '직접 방문 후기라기보다 경쟁 매장 비방처럼 보이는 표현이 있습니다.'),
    (2, '2026-03-16 09:35:00', '2026-03-16 09:35:00', 'seed', 'seed', 5, 8, 'IRRELEVANT', '서비스 내용보다 다른 매장 비교가 대부분이라 신고합니다.');

SET FOREIGN_KEY_CHECKS = 1;
