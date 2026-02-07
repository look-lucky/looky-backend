/* 외래 키 제약 조건 검사를 일시적으로 끕니다 */
SET FOREIGN_KEY_CHECKS = 0;

-- 1. University (전북대학교, Test University)
INSERT INTO university (university_id, name, email_domain) VALUES 
(1, '전북대학교', 'jbnu.ac.kr'),
(2, '서울대학교', 'snu.ac.kr');

-- 2. Users (Password: 'password')
-- Hash: $2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2

-- 2.1 Admin
INSERT INTO user (user_id, created_at, modified_at, username, password, role, deleted, gender, birth_date, social_type) 
VALUES (1, NOW(), NOW(), 'admin', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_ADMIN', 0, 0, '1990-01-01', 'LOCAL');

-- 2.2 Owners (10 Owners)
INSERT INTO user (user_id, created_at, modified_at, username, password, role, deleted, gender, birth_date, social_type) VALUES 
(10, NOW(), NOW(), 'owner1', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_OWNER', 0, 0, '1980-05-05', 'LOCAL'),
(11, NOW(), NOW(), 'owner2', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_OWNER', 0, 1, '1985-06-06', 'LOCAL'),
(12, NOW(), NOW(), 'owner3', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_OWNER', 0, 0, '1990-07-07', 'LOCAL'),
(13, NOW(), NOW(), 'owner4', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_OWNER', 0, 1, '1982-08-08', 'LOCAL'),
(14, NOW(), NOW(), 'owner5', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_OWNER', 0, 0, '1978-09-09', 'LOCAL'),
(15, NOW(), NOW(), 'owner6', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_OWNER', 0, 1, '1992-01-01', 'LOCAL'), 
(16, NOW(), NOW(), 'owner7', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_OWNER', 0, 0, '1993-02-02', 'LOCAL'),
(17, NOW(), NOW(), 'owner8', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_OWNER', 0, 1, '1994-03-03', 'LOCAL'),
(18, NOW(), NOW(), 'owner9', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_OWNER', 0, 0, '1995-04-04', 'LOCAL'),
(19, NOW(), NOW(), 'owner10', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_OWNER', 0, 1, '1996-05-05', 'LOCAL');

INSERT INTO owner_profile (user_id, name, email, phone) VALUES 
(10, '김사장', 'owner1@looky.com', '010-1111-1111'),
(11, '이점주', 'owner2@looky.com', '010-2222-2222'),
(12, '박대표', 'owner3@looky.com', '010-3333-3333'),
(13, '최오너', 'owner4@looky.com', '010-4444-4444'),
(14, '정주인', 'owner5@looky.com', '010-5555-5555'),
(15, '강사장', 'owner6@looky.com', '010-6666-6666'),
(16, '조점주', 'owner7@looky.com', '010-7777-7777'),
(17, '윤대표', 'owner8@looky.com', '010-8888-8888'),
(18, '장오너', 'owner9@looky.com', '010-9999-9999'),
(19, '임주인', 'owner10@looky.com', '010-1010-1010');

-- 2.3 Students (20 Students)
INSERT INTO user (user_id, created_at, modified_at, username, password, role, deleted, gender, birth_date, social_type, social_id) VALUES 
(101, NOW(), NOW(), 'student1', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 0, '2000-01-01', 'KAKAO', 'kakao_101'),
(102, NOW(), NOW(), 'student2', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 1, '2001-02-02', 'KAKAO', 'kakao_102'),
(103, NOW(), NOW(), 'student3', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 0, '2002-03-03', 'NAVER', 'naver_103'),
(104, NOW(), NOW(), 'student4', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 1, '2000-04-04', 'GOOGLE', 'google_104'),
(105, NOW(), NOW(), 'student5', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 0, '2003-05-05', 'LOCAL', NULL),
(106, NOW(), NOW(), 'student6', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 1, '2001-06-06', 'LOCAL', NULL),
(107, NOW(), NOW(), 'student7', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 0, '2002-07-07', 'KAKAO', 'kakao_107'),
(108, NOW(), NOW(), 'student8', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 1, '2000-08-08', 'NAVER', 'naver_108'),
(109, NOW(), NOW(), 'student9', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 0, '2003-09-09', 'GOOGLE', 'google_109'),
(110, NOW(), NOW(), 'student10', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 1, '2001-10-10', 'LOCAL', NULL),
(111, NOW(), NOW(), 'student11', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 0, '2002-11-11', 'KAKAO', 'kakao_111'),
(112, NOW(), NOW(), 'student12', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 1, '2000-12-12', 'NAVER', 'naver_112'),
(113, NOW(), NOW(), 'student13', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 0, '2003-01-13', 'GOOGLE', 'google_113'),
(114, NOW(), NOW(), 'student14', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 1, '2001-02-14', 'LOCAL', NULL),
(115, NOW(), NOW(), 'student15', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 0, '2002-03-15', 'KAKAO', 'kakao_115'),
(116, NOW(), NOW(), 'student16', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 1, '2000-04-16', 'NAVER', 'naver_116'),
(117, NOW(), NOW(), 'student17', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 0, '2003-05-17', 'GOOGLE', 'google_117'),
(118, NOW(), NOW(), 'student18', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 1, '2001-06-18', 'LOCAL', NULL),
(119, NOW(), NOW(), 'student19', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 0, '2002-07-19', 'KAKAO', 'kakao_119'),
(120, NOW(), NOW(), 'student20', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_STUDENT', 0, 1, '2000-08-20', 'NAVER', 'naver_120');

INSERT INTO student_profile (user_id, nickname, university_id) VALUES 
(101, '멋진학생1', 1), (102, '이쁜학생2', 1), (103, '공부왕3', 1), (104, '코딩천재4', 1), (105, '맛집탐방5', 1),
(106, '카페러버6', 1), (107, '전북대짱7', 1), (108, '학점A+8', 1), (109, '졸업하자9', 1), (110, '새내기10', 1),
(111, '복학생11', 1), (112, '휴학생12', 1), (113, '대학원생13', 1), (114, '취준생14', 1), (115, '알바몬15', 1),
(116, '동아리장16', 1), (117, '과대17', 1), (118, '총무18', 1), (119, '인싸19', 1), (120, '아싸20', 1);

-- 3. Organizations
INSERT INTO user (user_id, created_at, modified_at, username, password, role, deleted, social_type) VALUES 
(50, NOW(), NOW(), 'jbnu_council', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmjMaJJJwal3vSRl0ep2', 'ROLE_COUNCIL', 0, 'LOCAL');

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
-- 4.1 Store 1: 팀 레스토랑 (Active, Partnership, Coupon)
INSERT INTO store (store_id, created_at, modified_at, name, branch, biz_reg_no, road_address, jibun_address, latitude, longitude, store_phone, store_status, user_id, introduction, operating_hours, clover_grade) VALUES 
(1, NOW(), NOW(), '팀 레스토랑', '전북대점', '111-22-33333', '전북 전주시 덕진구 명륜4길 10', '전북 전주시 덕진구 덕진동1가 1262-4', 35.847133, 127.129060, '063-272-0000', 'ACTIVE', 10, '전북대 오래된 파스타 맛집, 팀입니다.', '{"0": [["11:00", "21:00"], ["15:00", "17:00"]], "1": [["11:00", "21:00"], ["15:00", "17:00"]], "2": [["11:00", "21:00"], ["15:00", "17:00"]], "3": [["11:00", "21:00"], ["15:00", "17:00"]], "4": [["11:00", "21:00"], ["15:00", "17:00"]], "5": [["11:00", "21:00"], ["15:00", "17:00"]], "6": [null, null]}', 'THREE_LEAF');
INSERT INTO store_categories (store_id, category) VALUES (1, 'RESTAURANT'), (1, 'ETC');
INSERT INTO store_moods (store_id, mood) VALUES (1, 'ROMANTIC'), (1, 'GROUP_GATHERING');
INSERT INTO store_university (store_id, university_id) VALUES (1, 1);
INSERT INTO store_image (store_id, image_url, order_index) VALUES (1, 'https://example.com/team1.jpg', 0), (1, 'https://example.com/team2.jpg', 1);

-- 4.2 Store 2: 맘스터치 (Active, Coupon)
INSERT INTO store (store_id, created_at, modified_at, name, branch, biz_reg_no, road_address, jibun_address, latitude, longitude, store_phone, store_status, user_id, introduction, operating_hours, clover_grade) VALUES 
(2, NOW(), NOW(), '맘스터치', '전북대본점', '222-33-44444', '전북 전주시 덕진구 명륜3길 15', '전북 전주시 덕진구 덕진동1가 1314-1', 35.846433, 127.129960, '063-271-1234', 'ACTIVE', 11, '빠르고 맛있는 치킨 버거!', '{"0": [["10:30", "22:00"], null], "1": [["10:30", "22:00"], null], "2": [["10:30", "22:00"], null], "3": [["10:30", "22:00"], null], "4": [["10:30", "22:00"], null], "5": [["10:30", "22:00"], null], "6": [["10:30", "22:00"], null]}', 'THREE_LEAF');
INSERT INTO store_categories (store_id, category) VALUES (2, 'RESTAURANT');
INSERT INTO store_moods (store_id, mood) VALUES (2, 'SOLO_DINING'), (2, 'LATE_NIGHT');
INSERT INTO store_university (store_id, university_id) VALUES (2, 1);
INSERT INTO store_image (store_id, image_url, order_index) VALUES (2, 'https://example.com/moms1.jpg', 0);

-- 4.3 Store 3: 알촌 (Active, Partnership)
INSERT INTO store (store_id, created_at, modified_at, name, branch, biz_reg_no, road_address, jibun_address, latitude, longitude, store_phone, store_status, user_id, introduction, operating_hours, clover_grade) VALUES 
(3, NOW(), NOW(), '알촌', '전북대점', '333-44-55555', '전북 전주시 덕진구 권삼득로 333', '전북 전주시 덕진구 금암동 664-14', 35.846033, 127.128560, '063-270-5555', 'ACTIVE', 12, '가성비 최고의 알밥집', '{"0": [["10:00", "20:00"], ["15:00", "17:00"]], "1": [["10:00", "20:00"], ["15:00", "17:00"]], "2": [["10:00", "20:00"], ["15:00", "17:00"]], "3": [["10:00", "20:00"], ["15:00", "17:00"]], "4": [["10:00", "20:00"], ["15:00", "17:00"]], "5": [["11:00", "19:00"], null], "6": [["11:00", "19:00"], null]}', 'THREE_LEAF');
INSERT INTO store_categories (store_id, category) VALUES (3, 'RESTAURANT');
INSERT INTO store_moods (store_id, mood) VALUES (3, 'SOLO_DINING');
INSERT INTO store_university (store_id, university_id) VALUES (3, 1);
INSERT INTO store_image (store_id, image_url, order_index) VALUES (3, 'https://example.com/alchon1.jpg', 0);

-- 4.4 Store 4: 컴포즈커피 (Unclaimed)
INSERT INTO store (store_id, created_at, modified_at, name, branch, biz_reg_no, road_address, jibun_address, latitude, longitude, store_phone, store_status, user_id, introduction, operating_hours, clover_grade) VALUES 
(4, NOW(), NOW(), '컴포즈커피', '전북대구정문점', NULL, '전북 전주시 덕진구 명륜4길 1', '전북 전주시 덕진구 덕진동1가 1261', 35.847333, 127.129260, '063-222-3333', 'UNCLAIMED', NULL, '대용량 고품질 커피', '{"0": [["08:00", "23:00"], null], "1": [["08:00", "23:00"], null], "2": [["08:00", "23:00"], null], "3": [["08:00", "23:00"], null], "4": [["08:00", "23:00"], null], "5": [["08:00", "23:00"], null], "6": [["08:00", "23:00"], null]}', 'SEED');
INSERT INTO store_categories (store_id, category) VALUES (4, 'CAFE');
INSERT INTO store_university (store_id, university_id) VALUES (4, 1);
INSERT INTO store_image (store_id, image_url, order_index) VALUES (4, 'https://example.com/compose1.jpg', 0);

-- 4.5 Store 5: 슈퍼스타 코인노래방 (Active, No Benefits)
INSERT INTO store (store_id, created_at, modified_at, name, branch, biz_reg_no, road_address, jibun_address, latitude, longitude, store_phone, store_status, user_id, introduction, operating_hours, clover_grade) VALUES 
(5, NOW(), NOW(), '슈퍼스타 코인노래방', '전주점', '555-66-77777', '전북 전주시 덕진구 명륜4길 20', '전북 전주시 덕진구 덕진동1가 1263-1', 35.846633, 127.129760, '063-111-2222', 'ACTIVE', 14, '최신 시설 깨끗한 코인노래방', '{"0": [["11:00", "02:00"], null], "1": [["11:00", "02:00"], null], "2": [["11:00", "02:00"], null], "3": [["11:00", "02:00"], null], "4": [["11:00", "02:00"], null], "5": [["11:00", "02:00"], null], "6": [["11:00", "02:00"], null]}', 'THREE_LEAF');
INSERT INTO store_categories (store_id, category) VALUES (5, 'ENTERTAINMENT');
INSERT INTO store_moods (store_id, mood) VALUES (5, 'GROUP_GATHERING'), (5, 'SOLO_DINING');
INSERT INTO store_university (store_id, university_id) VALUES (5, 1);
INSERT INTO store_image (store_id, image_url, order_index) VALUES (5, 'https://example.com/coin1.jpg', 0);

-- 4.6 Store 6: 광장포차 (Active)
INSERT INTO store (store_id, created_at, modified_at, name, branch, biz_reg_no, road_address, jibun_address, latitude, longitude, store_phone, store_status, user_id, introduction, operating_hours, clover_grade) VALUES 
(6, NOW(), NOW(), '광장포차', NULL, '111-22-33333', '전북 전주시 덕진구 권삼득로 300', '전북 전주시 덕진구 금암동 111-1', 35.845833, 127.128360, '063-777-8888', 'ACTIVE', 15, '대학생들의 성지, 낭만 포차', '{"0": [["17:00", "05:00"], null], "1": [["17:00", "05:00"], null], "2": [["17:00", "05:00"], null], "3": [["17:00", "05:00"], null], "4": [["17:00", "05:00"], null], "5": [["17:00", "05:00"], null], "6": [["17:00", "05:00"], null]}', 'THREE_LEAF');
INSERT INTO store_categories (store_id, category) VALUES (6, 'BAR');
INSERT INTO store_moods (store_id, mood) VALUES (6, 'GROUP_GATHERING'), (6, 'LATE_NIGHT');
INSERT INTO store_university (store_id, university_id) VALUES (6, 1);
INSERT INTO store_image (store_id, image_url, order_index) VALUES (6, 'https://example.com/pocha1.jpg', 0);

-- 4.7 Store 7: 금암면옥 (Banned)
INSERT INTO store (store_id, created_at, modified_at, name, branch, biz_reg_no, road_address, jibun_address, latitude, longitude, store_phone, store_status, user_id, introduction, operating_hours, clover_grade) VALUES 
(7, NOW(), NOW(), '금암면옥', '본점', '777-88-99999', '전북 전주시 덕진구 권삼득로 251', '전북 전주시 덕진구 금암동 123-4', 35.840000, 127.130000, '063-999-9999', 'BANNED', 16, '맛있는 냉면', '{"0": [["10:00", "20:00"], null], "1": [["10:00", "20:00"], null], "2": [["10:00", "20:00"], null], "3": [["10:00", "20:00"], null], "4": [["10:00", "20:00"], null], "5": [["10:00", "20:00"], null], "6": [["10:00", "20:00"], null]}', 'SPROUT');
INSERT INTO store_categories (store_id, category) VALUES (7, 'RESTAURANT');
INSERT INTO store_university (store_id, university_id) VALUES (7, 1);

-- 4.8 Store 8: 스타벅스 (Active, Suspended)
INSERT INTO store (store_id, created_at, modified_at, name, branch, biz_reg_no, road_address, jibun_address, latitude, longitude, store_phone, store_status, user_id, introduction, operating_hours, is_suspended, clover_grade) VALUES 
(8, NOW(), NOW(), '스타벅스', '전북대점', '888-99-00000', '전북 전주시 덕진구 명륜4길 25', '덕진동1가 1262-11', 35.847500, 127.129500, '063-888-8888', 'ACTIVE', 17, '스타벅스입니다.', '{"0": [["07:00", "22:00"], null], "1": [["07:00", "22:00"], null], "2": [["07:00", "22:00"], null], "3": [["07:00", "22:00"], null], "4": [["07:00", "22:00"], null], "5": [["07:00", "22:00"], null], "6": [["07:00", "22:00"], null]}', 1, 'SPROUT');
INSERT INTO store_categories (store_id, category) VALUES (8, 'CAFE');
INSERT INTO store_university (store_id, university_id) VALUES (8, 1);

-- 4.9 Store 9: 올리브영 (Active)
INSERT INTO store (store_id, created_at, modified_at, name, branch, biz_reg_no, road_address, jibun_address, latitude, longitude, store_phone, store_status, user_id, introduction, operating_hours, clover_grade) VALUES
(9, NOW(), NOW(), '올리브영', '전북대점', '999-00-11111', '전북 전주시 덕진구 명륜4길 15', '덕진동1가 1261-2', 35.847000, 127.129200, '063-000-0000', 'ACTIVE', 18, '헬스 앤 뷰티', '{"0": [["10:00", "22:00"], null], "1": [["10:00", "22:00"], null], "2": [["10:00", "22:00"], null], "3": [["10:00", "22:00"], null], "4": [["10:00", "22:00"], null], "5": [["10:00", "22:00"], null], "6": [["10:00", "22:00"], null]}', 'SPROUT');
INSERT INTO store_categories (store_id, category) VALUES (9, 'BEAUTY_HEALTH');
INSERT INTO store_university (store_id, university_id) VALUES (9, 1);

-- Items
-- Store 1
INSERT INTO item_category (item_category_id, created_at, modified_at, name, store_id) VALUES (1, NOW(), NOW(), '파스타', 1), (2, NOW(), NOW(), '리조또', 1), (3, NOW(), NOW(), '음료', 1);
INSERT INTO item (item_id, created_at, modified_at, name, price, description, is_sold_out, is_representative, is_hidden, badge, store_id, item_category_id) VALUES 
(1, NOW(), NOW(), '까르보나라', 13000, '진한 크림 소스의 파스타', 0, 1, 0, 'BEST', 1, 1),
(2, NOW(), NOW(), '해산물 토마토 파스타', 14000, '신선한 해산물이 가득', 0, 0, 0, NULL, 1, 1),
(3, NOW(), NOW(), '버섯 크림 리조또', 13500, '풍미 가득한 버섯 리조또', 0, 1, 0, 'HOT', 1, 2),
(4, NOW(), NOW(), '콜라', 2000, '코카콜라 355ml', 0, 0, 0, NULL, 1, 3);

-- Store 2
INSERT INTO item_category (item_category_id, created_at, modified_at, name, store_id) VALUES (4, NOW(), NOW(), '버거', 2), (5, NOW(), NOW(), '치킨', 2), (6, NOW(), NOW(), '사이드', 2);
INSERT INTO item (item_id, created_at, modified_at, name, price, description, is_sold_out, is_representative, is_hidden, badge, store_id, item_category_id) VALUES 
(5, NOW(), NOW(), '싸이버거 세트', 6900, '맘스터치 시그니처', 0, 1, 0, 'BEST', 2, 4),
(6, NOW(), NOW(), '불싸이버거', 4800, '매운맛 싸이버거', 0, 0, 0, NULL, 2, 4),
(7, NOW(), NOW(), '후라이드치킨', 16000, '바삭한 치킨', 0, 0, 0, NULL, 2, 5),
(8, NOW(), NOW(), '감자튀김', 2000, '케이준 스타일', 0, 0, 0, NULL, 2, 6);

-- Store 3
INSERT INTO item_category (item_category_id, created_at, modified_at, name, store_id) VALUES (7, NOW(), NOW(), '메인메뉴', 3);
INSERT INTO item (item_id, created_at, modified_at, name, price, description, is_sold_out, is_representative, is_hidden, badge, store_id, item_category_id) VALUES 
(9, NOW(), NOW(), '약매알밥', 5500, '약간 매운 알밥', 0, 1, 0, 'BEST', 3, 7),
(10, NOW(), NOW(), '매콤알밥', 5800, '매콤한 알밥', 0, 0, 0, 'HOT', 3, 7),
(11, NOW(), NOW(), '짜장알밥', 6000, '짜장 소스 알밥', 0, 0, 0, NULL, 3, 7);

-- Store 4
INSERT INTO item_category (item_category_id, created_at, modified_at, name, store_id) VALUES (8, NOW(), NOW(), 'Coffee', 4);
INSERT INTO item (item_id, created_at, modified_at, name, price, description, is_sold_out, is_representative, is_hidden, badge, store_id, item_category_id) VALUES 
(12, NOW(), NOW(), '아메리카노', 1500, '고소한 원두', 0, 1, 0, 'BEST', 4, 8),
(13, NOW(), NOW(), '카페라떼', 2900, '부드러운 우유', 0, 0, 0, NULL, 4, 8);

-- Store 5
INSERT INTO item_category (item_category_id, created_at, modified_at, name, store_id) VALUES (9, NOW(), NOW(), '요금', 5);
INSERT INTO item (item_id, created_at, modified_at, name, price, description, is_sold_out, is_representative, is_hidden, badge, store_id, item_category_id) VALUES 
(14, NOW(), NOW(), '3곡', 1000, '천원에 3곡', 0, 1, 0, NULL, 5, 9),
(15, NOW(), NOW(), '1시간', 5000, '한시간 무제한', 0, 0, 0, 'BEST', 5, 9);

-- Store 6
INSERT INTO item_category (item_category_id, created_at, modified_at, name, store_id) VALUES (10, NOW(), NOW(), '안주', 6), (11, NOW(), NOW(), '주류', 6);
INSERT INTO item (item_id, created_at, modified_at, name, price, description, is_sold_out, is_representative, is_hidden, badge, store_id, item_category_id) VALUES 
(16, NOW(), NOW(), '해물파전', 15000, '오징어가 듬뿍', 0, 1, 0, 'BEST', 6, 10),
(17, NOW(), NOW(), '어묵탕', 12000, '소주 안주로 딱', 0, 0, 0, 'HOT', 6, 10),
(18, NOW(), NOW(), '소주', 5000, '참이슬/처음처럼', 0, 0, 0, NULL, 6, 11);

-- 5. Partnership & Coupons
-- 5.1 Active Partnership (Store 1, Store 3)
INSERT INTO partnership (created_at, modified_at, benefit, starts_at, ends_at, store_id, organization_id) VALUES 
(NOW(), NOW(), '전 메뉴 10% 할인 (동반 1인 포함)', '2026-01-01', '2026-12-31', 1, 100), -- Team, Software Engineering Dept
(NOW(), NOW(), '음료수 서비스', '2026-02-01', '2026-06-30', 3, 101); -- Alchon, Computer Science Dept

-- 5.2 Expired Partnership (Store 2)
INSERT INTO partnership (created_at, modified_at, benefit, starts_at, ends_at, store_id, organization_id) VALUES 
(NOW(), NOW(), '사이드 메뉴 할인', '2025-01-01', '2025-12-31', 2, 100); -- MomsTouch, Past

-- 5.3 Future Partnership (Store 6)
INSERT INTO partnership (created_at, modified_at, benefit, starts_at, ends_at, store_id, organization_id) VALUES 
(NOW(), NOW(), '소주 1병 서비스', '2027-01-01', '2027-06-30', 6, 1); -- Pocha, Student Council

-- 5.4 Coupons
-- Active Coupon (Store 1, Store 2)
INSERT INTO coupon (coupon_id, created_at, modified_at, title, description, issue_starts_at, issue_ends_at, total_quantity, limit_per_user, status, benefit_type, benefit_value, min_order_amount, store_id) VALUES 
(1, NOW(), NOW(), '신학기 1000원 할인 쿠폰', '모든 메뉴에 적용 가능합니다.', '2026-02-01', '2026-03-31', 100, 1, 'ACTIVE', 'FIXED_DISCOUNT', '1000', 10000, 1),
(2, NOW(), NOW(), '감자튀김 무료 증정', '세트 메뉴 주문 시 사용 가능', '2026-01-01', '2026-12-31', 50, 1, 'ACTIVE', 'SERVICE_GIFT', '감자튀김', 15000, 2);

-- Expired Coupon (Store 1)
INSERT INTO coupon (coupon_id, created_at, modified_at, title, description, issue_starts_at, issue_ends_at, total_quantity, limit_per_user, status, benefit_type, benefit_value, min_order_amount, store_id) VALUES 
(3, NOW(), NOW(), '지난 겨울 할인', '지난 시즌 할인', '2025-12-01', '2025-12-31', 100, 1, 'EXPIRED', 'PERCENTAGE_DISCOUNT', '10', 0, 1);

-- Scheduled Coupon (Store 3)
INSERT INTO coupon (coupon_id, created_at, modified_at, title, description, issue_starts_at, issue_ends_at, total_quantity, limit_per_user, status, benefit_type, benefit_value, min_order_amount, store_id) VALUES 
(4, NOW(), NOW(), '3월 개강 이벤트', '개강 기념 할인', '2026-03-02', '2026-03-31', 200, 1, 'SCHEDULED', 'FIXED_DISCOUNT', '500', 0, 3);

-- Active but Sold Out Coupon (Store 4 - no store owner user_id but coupon exists? Let's assign to Store 2 instead)
-- Let's make Store 5 have a sold out coupon
INSERT INTO coupon (coupon_id, created_at, modified_at, title, description, issue_starts_at, issue_ends_at, total_quantity, limit_per_user, status, benefit_type, benefit_value, min_order_amount, store_id) VALUES 
(5, NOW(), NOW(), '선착순 1명 무료', '빨리 오세요', '2026-02-01', '2026-02-28', 0, 1, 'ACTIVE', 'FIXED_DISCOUNT', '5000', 0, 5);


-- 6. Events
-- Upcoming
INSERT INTO events (event_id, created_at, modified_at, title, description, latitude, longitude, start_date_time, end_date_time, status) VALUES 
(1, NOW(), NOW(), '2026 전북대학교 대동제', '2026년 전북대 대동제에 여러분을 초대합니다!', 35.846833, 127.129360, '2026-05-20 10:00:00', '2026-05-22 23:00:00', 'UPCOMING');

-- Live
INSERT INTO events (event_id, created_at, modified_at, title, description, latitude, longitude, start_date_time, end_date_time, status) VALUES 
(2, NOW(), NOW(), '소프트웨어공학과 신입생 오리엔테이션', '신입생 환영회', 35.846100, 127.129600, '2026-02-07 09:00:00', '2026-02-07 18:00:00', 'LIVE');

-- Ended
INSERT INTO events (event_id, created_at, modified_at, title, description, latitude, longitude, start_date_time, end_date_time, status) VALUES 
(3, NOW(), NOW(), '2025 졸업작품 전시회', '고생하셨습니다', 35.846100, 127.129600, '2025-11-20 09:00:00', '2025-11-22 18:00:00', 'ENDED');


INSERT INTO event_types (event_id, event_type) VALUES (1, 'SCHOOL_EVENT'), (1, 'PERFORMANCE'), (2, 'COMMUNITY'), (3, 'SCHOOL_EVENT');
INSERT INTO event_image (created_at, modified_at, event_id, image_url, order_index) VALUES 
(NOW(), NOW(), 1, 'https://example.com/festival1.jpg', 0),
(NOW(), NOW(), 2, 'https://example.com/ot.jpg', 0),
(NOW(), NOW(), 3, 'https://example.com/graduation.jpg', 0);


-- New Events (Feb 7-14)
-- 4. Fleamarket (Feb 8 11:00 - Feb 9 18:00)
INSERT INTO events (event_id, created_at, modified_at, title, description, latitude, longitude, start_date_time, end_date_time, status) VALUES 
(4, NOW(), NOW(), '전북대 벼룩시장', '안쓰는 물건 싸게 득템하세요!', 35.846833, 127.129360, '2026-02-08 11:00:00', '2026-02-09 18:00:00', 'UPCOMING');

-- 5. SE Opening Party (Feb 10 18:00 - 22:00)
INSERT INTO events (event_id, created_at, modified_at, title, description, latitude, longitude, start_date_time, end_date_time, status) VALUES 
(5, NOW(), NOW(), '소프트웨어공학과 개강총회', '2026학년도 1학기 개강총회입니다.', 35.846500, 127.129000, '2026-02-10 18:00:00', '2026-02-10 22:00:00', 'UPCOMING');

-- 6. Valentine Giveaway (Feb 13 10:00 - Feb 14 18:00)
INSERT INTO events (event_id, created_at, modified_at, title, description, latitude, longitude, start_date_time, end_date_time, status) VALUES 
(6, NOW(), NOW(), '발렌타인 데이 초콜릿 나눔', '달콤한 초콜릿 받아가세요~', 35.847000, 127.129500, '2026-02-13 10:00:00', '2026-02-14 18:00:00', 'UPCOMING');

INSERT INTO event_types (event_id, event_type) VALUES (4, 'COMMUNITY'), (5, 'SCHOOL_EVENT'), (6, 'COMMUNITY');
INSERT INTO event_image (created_at, modified_at, event_id, image_url, order_index) VALUES 
(NOW(), NOW(), 4, 'https://example.com/fleamarket.jpg', 0),
(NOW(), NOW(), 5, 'https://example.com/opening_party.jpg', 0),
(NOW(), NOW(), 6, 'https://example.com/valentine.jpg', 0);


-- 7. Reviews & Likes
INSERT INTO review (review_id, created_at, modified_at, user_id, store_id, is_verified, rating, content, status, report_count, like_count, is_private) VALUES 
(1, NOW(), NOW(), 101, 1, 1, 5, '진짜 맛있어요! 까르보나라 강추', 'PUBLISHED', 0, 2, 0),
(2, NOW(), NOW(), 102, 1, 0, 4, '분위기 좋고 친절해요', 'PUBLISHED', 0, 1, 0),
(3, NOW(), NOW(), 103, 1, 1, 5, '데이트하기 딱 좋은 곳', 'PUBLISHED', 0, 3, 0),
(4, NOW(), NOW(), 104, 2, 1, 5, '싸이버거는 진리죠', 'PUBLISHED', 0, 5, 0),
(5, NOW(), NOW(), 105, 2, 0, 3, '사람이 너무 많아서 오래 기다렸어요', 'PUBLISHED', 0, 0, 0),
(6, NOW(), NOW(), 106, 2, 1, 4, '감튀 맛집', 'PUBLISHED', 0, 1, 0);

INSERT INTO review_like (created_at, modified_at, user_id, review_id) VALUES
(NOW(), NOW(), 102, 1), (NOW(), NOW(), 103, 1),
(NOW(), NOW(), 101, 2),
(NOW(), NOW(), 101, 3), (NOW(), NOW(), 102, 3), (NOW(), NOW(), 104, 3),
(NOW(), NOW(), 101, 4), (NOW(), NOW(), 102, 4), (NOW(), NOW(), 103, 4), (NOW(), NOW(), 105, 4), (NOW(), NOW(), 106, 4),
(NOW(), NOW(), 104, 6);

INSERT INTO review_image (review_id, image_url, order_index) VALUES
(1, 'https://example.com/pasta_review1.jpg', 0);

-- 8. Favorites (For Hot Stores logic)
-- Store 1: ~3 likes, Store 2: ~2 likes, Store 3: ~1 like
INSERT INTO favorite_store (created_at, modified_at, user_id, store_id) VALUES 
(NOW(), NOW(), 101, 1), (NOW(), NOW(), 101, 2),
(NOW(), NOW(), 102, 1),
(NOW(), NOW(), 103, 1), (NOW(), NOW(), 103, 3),
(NOW(), NOW(), 104, 2);

-- 9. Store News
INSERT INTO store_news (id, created_at, modified_at, store_id, title, content, like_count, comment_count) VALUES 
(1, NOW(), NOW(), 1, '2026년 신메뉴 출시 안내', '이번 시즌 새로운 메뉴가 출시되었습니다.', 10, 2),
(2, NOW(), NOW(), 2, '임시 휴무 공지', '내부 공사로 인해 하루 쉬어갑니다.', 5, 1);

INSERT INTO store_news_like (created_at, modified_at, store_news_id, user_id) VALUES
(NOW(), NOW(), 1, 101), (NOW(), NOW(), 1, 102), (NOW(), NOW(), 1, 103), (NOW(), NOW(), 1, 104), (NOW(), NOW(), 1, 105),
(NOW(), NOW(), 1, 106), (NOW(), NOW(), 1, 107), (NOW(), NOW(), 1, 108), (NOW(), NOW(), 1, 109), (NOW(), NOW(), 1, 110),
(NOW(), NOW(), 2, 101), (NOW(), NOW(), 2, 102), (NOW(), NOW(), 2, 103), (NOW(), NOW(), 2, 104), (NOW(), NOW(), 2, 105);

INSERT INTO store_news_comment (id, created_at, modified_at, store_news_id, user_id, content) VALUES 
(1, NOW(), NOW(), 1, 101, '오 먹으러 갈게요!'),
(2, NOW(), NOW(), 1, 102, '무슨 메뉴인가요?'),
(3, NOW(), NOW(), 2, 104, '헉 헛걸음할뻔');

-- 10. Store Claims
INSERT INTO store_claim (store_claim_request_id, created_at, modified_at, store_id, user_id, biz_reg_no, representative_name, store_name, store_phone, license_image_url, status, reject_reason, admin_memo) VALUES
(1, NOW(), NOW(), 4, 15, '123-45-78901', '박사장', '컴포즈커피 전북대구정문점', '010-0000-0000', 'https://example.com/license1.jpg', 'PENDING', NULL, '검토 중'),
(2, NOW(), NOW(), 4, 16, '999-99-99999', '김도둑', '컴포즈커피', '010-1111-2222', 'https://example.com/fake.jpg', 'REJECTED', '사업자등록증 불일치', '사기 의심');

-- 11. Inquiries
INSERT INTO inquiry (inquiry_id, created_at, modified_at, user_id, type, title, content) VALUES
(1, NOW(), NOW(), 101, 'STORE_INFO_ERROR', '가게 정보 오류', '팀 레스토랑 주소가 틀려요'),
(2, NOW(), NOW(), 102, 'COUPON_BENEFIT', '쿠폰 안써짐', '쿠폰 사용이 안됩니다');

-- 12. Reports
INSERT INTO store_report (store_report_id, created_at, modified_at, detail, store_id, reporter_id) VALUES
(1, NOW(), NOW(), '불친절해요', 2, 101);
INSERT INTO store_report_reason (store_report_id, reason) VALUES (1, 'ETC');

INSERT INTO review_report (review_report_id, created_at, modified_at, review_id, reporter_id, reason, detail) VALUES
(1, NOW(), NOW(), 5, 101, 'SPAM', '광고 같아요');

-- 13. Withdrawal Feedback
INSERT INTO withdrawal_feedback (id, created_at, modified_at, detail_reason) VALUES
(1, NOW(), NOW(), '앱이 너무 느려요');
INSERT INTO withdrawal_reason (feedback_id, reason) VALUES (1, 'INCONVENIENT');

SET FOREIGN_KEY_CHECKS = 1;
