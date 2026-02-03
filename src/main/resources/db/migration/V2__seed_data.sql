/* 외래 키 제약 조건 검사를 일시적으로 끕니다 */
SET FOREIGN_KEY_CHECKS = 0;

-- 1. University (전북대학교)
INSERT INTO university (university_id, name, email_domain) VALUES (1, '전북대학교', 'jbnu.ac.kr');

-- 2. Users
-- Password is 'password' (bcrypt hashed) for all users because '0000' hash generation was not possible in this environment. 
-- Please change it after login if needed.
-- Hash: $2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2

-- 2.1 Admin
INSERT INTO user (user_id, created_at, modified_at, username, password, role, deleted) 
VALUES (1, NOW(), NOW(), 'admin', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_ADMIN', 0);

-- 2.2 Owners (5 Owners)
INSERT INTO user (user_id, created_at, modified_at, username, password, role, deleted, name, gender, birth_date, social_type) VALUES 
(10, NOW(), NOW(), 'owner1', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_OWNER', 0, '김사장', 0, '1980-05-05', 'LOCAL'),
(11, NOW(), NOW(), 'owner2', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_OWNER', 0, '이점주', 1, '1985-06-06', 'LOCAL'),
(12, NOW(), NOW(), 'owner3', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_OWNER', 0, '박대표', 0, '1990-07-07', 'LOCAL'),
(13, NOW(), NOW(), 'owner4', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_OWNER', 0, '최오너', 1, '1982-08-08', 'LOCAL'),
(14, NOW(), NOW(), 'owner5', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_OWNER', 0, '정주인', 0, '1978-09-09', 'LOCAL');

INSERT INTO owner_profile (user_id, name, email, phone) VALUES 
(10, '김사장', 'owner1@looky.com', '010-1111-1111'),
(11, '이점주', 'owner2@looky.com', '010-2222-2222'),
(12, '박대표', 'owner3@looky.com', '010-3333-3333'),
(13, '최오너', 'owner4@looky.com', '010-4444-4444'),
(14, '정주인', 'owner5@looky.com', '010-5555-5555');

-- 2.3 Students (20 Students)
INSERT INTO user (user_id, created_at, modified_at, username, password, role, deleted, name, gender, birth_date, social_type, social_id) VALUES 
(101, NOW(), NOW(), 'student1', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생1', 0, '2000-01-01', 'KAKAO', 'kakao_101'),
(102, NOW(), NOW(), 'student2', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생2', 1, '2001-02-02', 'KAKAO', 'kakao_102'),
(103, NOW(), NOW(), 'student3', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생3', 0, '2002-03-03', 'NAVER', 'naver_103'),
(104, NOW(), NOW(), 'student4', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생4', 1, '2000-04-04', 'GOOGLE', 'google_104'),
(105, NOW(), NOW(), 'student5', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생5', 0, '2003-05-05', 'LOCAL', NULL),
(106, NOW(), NOW(), 'student6', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생6', 1, '2001-06-06', 'LOCAL', NULL),
(107, NOW(), NOW(), 'student7', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생7', 0, '2002-07-07', 'KAKAO', 'kakao_107'),
(108, NOW(), NOW(), 'student8', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생8', 1, '2000-08-08', 'NAVER', 'naver_108'),
(109, NOW(), NOW(), 'student9', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생9', 0, '2003-09-09', 'GOOGLE', 'google_109'),
(110, NOW(), NOW(), 'student10', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생10', 1, '2001-10-10', 'LOCAL', NULL),
(111, NOW(), NOW(), 'student11', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생11', 0, '2002-11-11', 'KAKAO', 'kakao_111'),
(112, NOW(), NOW(), 'student12', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생12', 1, '2000-12-12', 'NAVER', 'naver_112'),
(113, NOW(), NOW(), 'student13', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생13', 0, '2003-01-13', 'GOOGLE', 'google_113'),
(114, NOW(), NOW(), 'student14', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생14', 1, '2001-02-14', 'LOCAL', NULL),
(115, NOW(), NOW(), 'student15', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생15', 0, '2002-03-15', 'KAKAO', 'kakao_115'),
(116, NOW(), NOW(), 'student16', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생16', 1, '2000-04-16', 'NAVER', 'naver_116'),
(117, NOW(), NOW(), 'student17', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생17', 0, '2003-05-17', 'GOOGLE', 'google_117'),
(118, NOW(), NOW(), 'student18', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생18', 1, '2001-06-18', 'LOCAL', NULL),
(119, NOW(), NOW(), 'student19', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생19', 0, '2002-07-19', 'KAKAO', 'kakao_119'),
(120, NOW(), NOW(), 'student20', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, '학생20', 1, '2000-08-20', 'NAVER', 'naver_120');

INSERT INTO student_profile (user_id, nickname, university_id) VALUES 
(101, '멋진학생1', 1), (102, '이쁜학생2', 1), (103, '공부왕3', 1), (104, '코딩천재4', 1), (105, '맛집탐방5', 1),
(106, '카페러버6', 1), (107, '전북대짱7', 1), (108, '학점A+8', 1), (109, '졸업하자9', 1), (110, '새내기10', 1),
(111, '복학생11', 1), (112, '휴학생12', 1), (113, '대학원생13', 1), (114, '취준생14', 1), (115, '알바몬15', 1),
(116, '동아리장16', 1), (117, '과대17', 1), (118, '총무18', 1), (119, '인싸19', 1), (120, '아싸20', 1);

-- 3. Organizations
INSERT INTO user (user_id, created_at, modified_at, username, password, role, deleted, name, social_type) VALUES 
(50, NOW(), NOW(), 'jbnu_council', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_COUNCIL', 0, '전북대총학', 'LOCAL');

INSERT INTO council_profile (user_id, university_id) VALUES (50, 1);

INSERT INTO organization (organization_id, created_at, modified_at, university_id, user_id, category, name, parent_id) VALUES 
(1, NOW(), NOW(), 1, 50, 'STUDENT_COUNCIL', '전북대학교 총학생회', NULL);

INSERT INTO organization (organization_id, created_at, modified_at, university_id, user_id, category, name, parent_id) VALUES 
(10, NOW(), NOW(), 1, 50, 'COLLEGE', '공과대학', 1),
(11, NOW(), NOW(), 1, 50, 'COLLEGE', '농업생명과학대학', 1),
(12, NOW(), NOW(), 1, 50, 'COLLEGE', '인문대학', 1),
(13, NOW(), NOW(), 1, 50, 'COLLEGE', '상과대학', 1),
(14, NOW(), NOW(), 1, 50, 'COLLEGE', '예술대학', 1);

INSERT INTO organization (organization_id, created_at, modified_at, university_id, user_id, category, name, parent_id) VALUES 
(100, NOW(), NOW(), 1, 50, 'DEPARTMENT', '소프트웨어공학과', 10),
(101, NOW(), NOW(), 1, 50, 'DEPARTMENT', '컴퓨터공학부', 10),
(102, NOW(), NOW(), 1, 50, 'DEPARTMENT', '기계설계공학부', 10),
(103, NOW(), NOW(), 1, 50, 'DEPARTMENT', '농생물학과', 11),
(104, NOW(), NOW(), 1, 50, 'DEPARTMENT', '영어영문학과', 12),
(105, NOW(), NOW(), 1, 50, 'DEPARTMENT', '경영학과', 13),
(106, NOW(), NOW(), 1, 50, 'DEPARTMENT', '산업디자인학과', 14);

-- 4. Stores (Around Center: 35.846833, 127.12936)
-- 4.1 Store 1: 팀 레스토랑
INSERT INTO store (store_id, created_at, modified_at, name, branch, biz_reg_no, road_address, jibun_address, latitude, longitude, store_phone, store_status, user_id, introduction, operating_hours) VALUES 
(1, NOW(), NOW(), '팀 레스토랑', '전북대점', '111-22-33333', '전북 전주시 덕진구 명륜4길 10', '전북 전주시 덕진구 덕진동1가 1262-4', 35.847133, 127.129060, '063-272-0000', 'ACTIVE', 10, '전북대 오래된 파스타 맛집, 팀입니다.', '{"Mon": "11:00-21:00", "Tue": "11:00-21:00", "Wed": "11:00-21:00", "Thu": "11:00-21:00", "Fri": "11:00-21:00", "Sat": "11:00-21:00", "Sun": "Closed"}');

INSERT INTO store_categories (store_id, category) VALUES (1, 'RESTAURANT'), (1, 'ETC');
INSERT INTO store_moods (store_id, mood) VALUES (1, 'ROMANTIC'), (1, 'GROUP_GATHERING');
INSERT INTO store_university (store_id, university_id) VALUES (1, 1);

INSERT INTO item_category (item_category_id, created_at, modified_at, name, store_id) VALUES (1, NOW(), NOW(), '파스타', 1), (2, NOW(), NOW(), '리조또', 1), (3, NOW(), NOW(), '음료', 1);
INSERT INTO item (item_id, created_at, modified_at, name, price, description, is_sold_out, is_representative, is_hidden, badge, store_id, item_category_id) VALUES 
(1, NOW(), NOW(), '까르보나라', 13000, '진한 크림 소스의 파스타', 0, 1, 0, 'BEST', 1, 1),
(2, NOW(), NOW(), '해산물 토마토 파스타', 14000, '신선한 해산물이 가득', 0, 0, 0, NULL, 1, 1),
(3, NOW(), NOW(), '버섯 크림 리조또', 13500, '풍미 가득한 버섯 리조또', 0, 1, 0, 'HOT', 1, 2),
(4, NOW(), NOW(), '콜라', 2000, '코카콜라 355ml', 0, 0, 0, NULL, 1, 3);

-- 4.2 Store 2: 맘스터치
INSERT INTO store (store_id, created_at, modified_at, name, branch, biz_reg_no, road_address, jibun_address, latitude, longitude, store_phone, store_status, user_id, introduction, operating_hours) VALUES 
(2, NOW(), NOW(), '맘스터치', '전북대본점', '222-33-44444', '전북 전주시 덕진구 명륜3길 15', '전북 전주시 덕진구 덕진동1가 1314-1', 35.846433, 127.129960, '063-271-1234', 'ACTIVE', 11, '빠르고 맛있는 치킨 버거!', '{"Everyday": "10:30-22:00"}');

INSERT INTO store_categories (store_id, category) VALUES (2, 'RESTAURANT');
INSERT INTO store_moods (store_id, mood) VALUES (2, 'SOLO_DINING'), (2, 'LATE_NIGHT');
INSERT INTO store_university (store_id, university_id) VALUES (2, 1);

INSERT INTO item_category (item_category_id, created_at, modified_at, name, store_id) VALUES (4, NOW(), NOW(), '버거', 2), (5, NOW(), NOW(), '치킨', 2), (6, NOW(), NOW(), '사이드', 2);
INSERT INTO item (item_id, created_at, modified_at, name, price, description, is_sold_out, is_representative, is_hidden, badge, store_id, item_category_id) VALUES 
(5, NOW(), NOW(), '싸이버거 세트', 6900, '맘스터치 시그니처', 0, 1, 0, 'BEST', 2, 4),
(6, NOW(), NOW(), '불싸이버거', 4800, '매운맛 싸이버거', 0, 0, 0, NULL, 2, 4),
(7, NOW(), NOW(), '후라이드치킨', 16000, '바삭한 치킨', 0, 0, 0, NULL, 2, 5),
(8, NOW(), NOW(), '감자튀김', 2000, '케이준 스타일', 0, 0, 0, NULL, 2, 6);

-- 4.3 Store 3: 알촌
INSERT INTO store (store_id, created_at, modified_at, name, branch, biz_reg_no, road_address, jibun_address, latitude, longitude, store_phone, store_status, user_id, introduction, operating_hours) VALUES 
(3, NOW(), NOW(), '알촌', '전북대점', '333-44-55555', '전북 전주시 덕진구 권삼득로 333', '전북 전주시 덕진구 금암동 664-14', 35.846033, 127.128560, '063-270-5555', 'ACTIVE', 12, '가성비 최고의 알밥집', '{"Mon-Fri": "10:00-20:00", "Sat-Sun": "11:00-19:00"}');

INSERT INTO store_categories (store_id, category) VALUES (3, 'RESTAURANT');
INSERT INTO store_moods (store_id, mood) VALUES (3, 'SOLO_DINING');
INSERT INTO store_university (store_id, university_id) VALUES (3, 1);

INSERT INTO item_category (item_category_id, created_at, modified_at, name, store_id) VALUES (7, NOW(), NOW(), '메인메뉴', 3);
INSERT INTO item (item_id, created_at, modified_at, name, price, description, is_sold_out, is_representative, is_hidden, badge, store_id, item_category_id) VALUES 
(9, NOW(), NOW(), '약매알밥', 5500, '약간 매운 알밥', 0, 1, 0, 'BEST', 3, 7),
(10, NOW(), NOW(), '매콤알밥', 5800, '매콤한 알밥', 0, 0, 0, 'HOT', 3, 7),
(11, NOW(), NOW(), '짜장알밥', 6000, '짜장 소스 알밥', 0, 0, 0, NULL, 3, 7);

-- 4.4 Store 4: 컴포즈커피
INSERT INTO store (store_id, created_at, modified_at, name, branch, biz_reg_no, road_address, jibun_address, latitude, longitude, store_phone, store_status, user_id, introduction, operating_hours) VALUES 
(4, NOW(), NOW(), '컴포즈커피', '전북대구정문점', NULL, '전북 전주시 덕진구 명륜4길 1', '전북 전주시 덕진구 덕진동1가 1261', 35.847333, 127.129260, '063-222-3333', 'UNCLAIMED', NULL, '대용량 고품질 커피', '{"Everyday": "08:00-23:00"}');

INSERT INTO store_categories (store_id, category) VALUES (4, 'CAFE');
INSERT INTO store_university (store_id, university_id) VALUES (4, 1);
INSERT INTO item_category (item_category_id, created_at, modified_at, name, store_id) VALUES (8, NOW(), NOW(), 'Coffee', 4);
INSERT INTO item (item_id, created_at, modified_at, name, price, description, is_sold_out, is_representative, is_hidden, badge, store_id, item_category_id) VALUES 
(12, NOW(), NOW(), '아메리카노', 1500, '고소한 원두', 0, 1, 0, 'BEST', 4, 8),
(13, NOW(), NOW(), '카페라떼', 2900, '부드러운 우유', 0, 0, 0, NULL, 4, 8);

-- 4.5 Store 5: 슈퍼스타 코인노래방
INSERT INTO store (store_id, created_at, modified_at, name, branch, biz_reg_no, road_address, jibun_address, latitude, longitude, store_phone, store_status, user_id, introduction, operating_hours) VALUES 
(5, NOW(), NOW(), '슈퍼스타 코인노래방', '전주점', '555-66-77777', '전북 전주시 덕진구 명륜4길 20', '전북 전주시 덕진구 덕진동1가 1263-1', 35.846633, 127.129760, '063-111-2222', 'ACTIVE', 14, '최신 시설 깨끗한 코인노래방', '{"Everyday": "11:00-02:00"}');

INSERT INTO store_categories (store_id, category) VALUES (5, 'ENTERTAINMENT');
INSERT INTO store_moods (store_id, mood) VALUES (5, 'GROUP_GATHERING'), (5, 'SOLO_DINING');
INSERT INTO store_university (store_id, university_id) VALUES (5, 1);

INSERT INTO item_category (item_category_id, created_at, modified_at, name, store_id) VALUES (9, NOW(), NOW(), '요금', 5);
INSERT INTO item (item_id, created_at, modified_at, name, price, description, is_sold_out, is_representative, is_hidden, badge, store_id, item_category_id) VALUES 
(14, NOW(), NOW(), '3곡', 1000, '천원에 3곡', 0, 1, 0, NULL, 5, 9),
(15, NOW(), NOW(), '1시간', 5000, '한시간 무제한', 0, 0, 0, 'BEST', 5, 9);

-- 4.6 Store 6: 광장포차
INSERT INTO store (store_id, created_at, modified_at, name, branch, biz_reg_no, road_address, jibun_address, latitude, longitude, store_phone, store_status, user_id, introduction, operating_hours) VALUES 
(6, NOW(), NOW(), '광장포차', NULL, '111-22-33333', '전북 전주시 덕진구 권삼득로 300', '전북 전주시 덕진구 금암동 111-1', 35.845833, 127.128360, '063-777-8888', 'ACTIVE', 10, '대학생들의 성지, 낭만 포차', '{"Everyday": "17:00-05:00"}');

INSERT INTO store_categories (store_id, category) VALUES (6, 'BAR');
INSERT INTO store_moods (store_id, mood) VALUES (6, 'GROUP_GATHERING'), (6, 'LATE_NIGHT');
INSERT INTO store_university (store_id, university_id) VALUES (6, 1);

INSERT INTO item_category (item_category_id, created_at, modified_at, name, store_id) VALUES (10, NOW(), NOW(), '안주', 6), (11, NOW(), NOW(), '주류', 6);
INSERT INTO item (item_id, created_at, modified_at, name, price, description, is_sold_out, is_representative, is_hidden, badge, store_id, item_category_id) VALUES 
(16, NOW(), NOW(), '해물파전', 15000, '오징어가 듬뿍', 0, 1, 0, 'BEST', 6, 10),
(17, NOW(), NOW(), '어묵탕', 12000, '소주 안주로 딱', 0, 0, 0, 'HOT', 6, 10),
(18, NOW(), NOW(), '소주', 5000, '참이슬/처음처럼', 0, 0, 0, NULL, 6, 11);


-- 5. Partnership & Coupons
INSERT INTO partnership (created_at, modified_at, benefit, starts_at, ends_at, store_id, organization_id) VALUES 
(NOW(), NOW(), '전 메뉴 10% 할인 (동반 1인 포함)', '2025-01-01', '2025-12-31', 1, 100),
(NOW(), NOW(), '음료수 서비스', '2025-03-01', '2025-06-30', 3, 101);

INSERT INTO coupon (coupon_id, created_at, modified_at, title, description, issue_starts_at, issue_ends_at, total_quantity, limit_per_user, status, store_id, target_organization_id) VALUES 
(1, NOW(), NOW(), '신학기 1000원 할인 쿠폰', '모든 메뉴에 적용 가능합니다.', '2025-03-01', '2025-03-31', 100, 1, 'ACTIVE', 1, NULL),
(2, NOW(), NOW(), '감자튀김 무료 증정', '세트 메뉴 주문 시 사용 가능', '2025-01-01', '2025-12-31', 50, 1, 'ACTIVE', 2, 100);

INSERT INTO coupon_item (coupon_id, item_id) VALUES (2, 8);

-- 6. Events
INSERT INTO events (event_id, created_at, modified_at, title, description, latitude, longitude, start_date_time, end_date_time, status) VALUES 
(1, NOW(), NOW(), '전북대학교 대동제', '2025년 전북대 대동제에 여러분을 초대합니다!', 35.846833, 127.129360, '2025-05-20 10:00:00', '2025-05-22 23:00:00', 'UPCOMING'),
(2, NOW(), NOW(), '소프트웨어공학과 플리마켓', '다양한 굿즈와 중고 전공서적 판매', 35.846100, 127.129600, '2025-04-01 12:00:00', '2025-04-01 18:00:00', 'UPCOMING');

INSERT INTO event_types (event_id, event_type) VALUES (1, 'SCHOOL_EVENT'), (1, 'PERFORMANCE'), (2, 'FLEA_MARKET'), (2, 'COMMUNITY');

INSERT INTO event_image (created_at, modified_at, event_id, image_url, order_index) VALUES 
(NOW(), NOW(), 1, 'https://example.com/festival1.jpg', 0),
(NOW(), NOW(), 2, 'https://example.com/flea1.jpg', 0);

-- 7. Reviews & Likes
INSERT INTO review (review_id, created_at, modified_at, user_id, store_id, is_verified, rating, content, status, report_count, like_count, is_private) VALUES 
(1, NOW(), NOW(), 101, 1, 1, 5, '진짜 맛있어요! 까르보나라 강추', 'PUBLISHED', 0, 2, 0),
(2, NOW(), NOW(), 102, 1, 0, 4, '분위기 좋고 친절해요', 'PUBLISHED', 0, 1, 0),
(3, NOW(), NOW(), 103, 1, 1, 5, '데이트하기 딱 좋은 곳', 'PUBLISHED', 0, 3, 0);

INSERT INTO review (review_id, created_at, modified_at, user_id, store_id, is_verified, rating, content, status, report_count, like_count, is_private) VALUES 
(4, NOW(), NOW(), 104, 2, 1, 5, '싸이버거는 진리죠', 'PUBLISHED', 0, 5, 0),
(5, NOW(), NOW(), 105, 2, 0, 3, '사람이 너무 많아서 오래 기다렸어요', 'PUBLISHED', 0, 0, 0),
(6, NOW(), NOW(), 106, 2, 1, 4, '감튀 맛집', 'PUBLISHED', 0, 1, 0);

INSERT INTO favorite_store (created_at, modified_at, user_id, store_id) VALUES 
(NOW(), NOW(), 101, 1), (NOW(), NOW(), 101, 2),
(NOW(), NOW(), 102, 1), (NOW(), NOW(), 102, 5),
(NOW(), NOW(), 103, 3), (NOW(), NOW(), 104, 2);

-- 8. Store News
INSERT INTO store_news (id, created_at, modified_at, store_id, title, content, like_count, comment_count) VALUES 
(1, NOW(), NOW(), 1, '신메뉴 출시 안내', '이번 시즌 새로운 메뉴가 출시되었습니다. 많은 관심 부탁드려요!', 10, 2),
(2, NOW(), NOW(), 2, '임시 휴무 공지', '내부 공사로 인해 하루 쉬어갑니다.', 5, 1);

INSERT INTO store_news_comment (created_at, modified_at, store_news_id, user_id, content) VALUES 
(NOW(), NOW(), 1, 101, '오 먹으러 갈게요!'),
(NOW(), NOW(), 1, 102, '무슨 메뉴인가요?'),
(NOW(), NOW(), 2, 104, '헉 헛걸음할뻔');

/* 외래 키 제약 조건 검사를 다시 켭니다 */
SET FOREIGN_KEY_CHECKS = 1;
