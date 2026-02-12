/* 외래 키 제약 조건 검사를 일시적으로 끕니다 */
SET FOREIGN_KEY_CHECKS = 0;

/* University */
create table university (
    university_id bigint auto_increment primary key,
    email_domain varchar(255) not null,
    name varchar(255) not null,
    constraint UK_university_name unique (name)
);

/* User */
create table user (
    user_id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    username varchar(255) not null,
    password varchar(255),
    gender tinyint check (gender between 0 and 2),
    birth_date date,
    role enum ('ROLE_ADMIN','ROLE_COUNCIL','ROLE_GUEST','ROLE_OWNER','ROLE_STUDENT') not null,
    social_type enum ('APPLE','GOOGLE','KAKAO','LOCAL'),
    social_id varchar(255),
    email varchar(255),
    deleted bit not null,
    deleted_at datetime(6),
    constraint UK_user_username unique (username),
    constraint UK_user_email unique (email)
);

/* User Profiles */
create table council_profile (
    user_id bigint not null primary key,
    university_id bigint,
    foreign key (user_id) references user (user_id),
    foreign key (university_id) references university (university_id)
);

create table owner_profile (
    user_id bigint not null primary key,
    name varchar(255),
    foreign key (user_id) references user (user_id)
);

create table student_profile (
    user_id bigint not null primary key,
    nickname varchar(255),
    university_id bigint,
    is_club_member bit NULL,
    foreign key (user_id) references user (user_id),
    foreign key (university_id) references university (university_id)
);

/* Withdrawal Feedback */
create table withdrawal_feedback (
    id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    detail_reason text,
    user_id bigint,
    foreign key (user_id) references user (user_id)
);

create table withdrawal_reason (
    feedback_id bigint not null,
    reason enum ('UNUSED', 'INSUFFICIENT_BENEFITS', 'INCONVENIENT', 'TOO_MANY_ADS', 'NOT_NEEDED', 'OTHER'),
    foreign key (feedback_id) references withdrawal_feedback (id)
);

/* Refresh Token */
create table refresh_token (
    user_id bigint not null primary key,
    token varchar(255),
    expiry_date datetime(6)
);

/* Organization */
create table organization (
    organization_id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    category enum ('COLLEGE','DEPARTMENT','STUDENT_COUNCIL') not null,
    name varchar(255) not null,
    expires_at datetime(6),
    university_id bigint not null,
    parent_id bigint,
    user_id bigint not null,
    foreign key (university_id) references university (university_id),
    foreign key (parent_id) references organization (organization_id),
    foreign key (user_id) references user (user_id),
    constraint UK_organization_university_name unique (university_id, name)
);

create table user_organization (
    id bigint auto_increment primary key,
    user_id bigint not null,
    organization_id bigint not null,
    foreign key (user_id) references user (user_id),
    foreign key (organization_id) references organization (organization_id)
);

/* Store */
create table store (
    store_id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    name varchar(255) not null,
    branch varchar(255),
    biz_reg_no varchar(255),
    road_address varchar(255) not null,
    jibun_address varchar(255),
    latitude double,
    longitude double,
    store_phone varchar(255),
    representative_name varchar(255),
    need_to_check bit,
    check_reason varchar(255),
    introduction longtext,
    operating_hours longtext,
    store_status enum ('ACTIVE','BANNED','UNCLAIMED') not null,
    is_suspended bit default 0 not null,
    clover_grade enum ('SEED','SPROUT','THREE_LEAF') default 'SEED' not null,
    user_id bigint,
    foreign key (user_id) references user (user_id)
);

create table store_categories (
    store_id bigint not null,
    category enum ('BAR','BEAUTY_HEALTH','CAFE','ENTERTAINMENT','ETC','RESTAURANT'),
    foreign key (store_id) references store (store_id)
);

create table store_moods (
    store_id bigint not null,
    mood enum ('GROUP_GATHERING','LATE_NIGHT','ROMANTIC','SOLO_DINING'),
    foreign key (store_id) references store (store_id)
);

create table store_holidays (
    store_id bigint not null,
    holiday_date date,
    foreign key (store_id) references store (store_id)
);

create table store_image (
    store_image_id bigint auto_increment primary key,
    store_id bigint not null,
    image_url varchar(255) not null,
    order_index int not null,
    foreign key (store_id) references store (store_id)
);

create table store_university (
    id bigint auto_increment primary key,
    store_id bigint not null,
    university_id bigint not null,
    foreign key (store_id) references store (store_id),
    foreign key (university_id) references university (university_id)
);

create table store_claim (
    store_claim_request_id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    store_id bigint not null,
    user_id bigint not null,
    biz_reg_no varchar(255) not null,
    representative_name varchar(255) not null,
    store_name varchar(255) not null,
    store_phone varchar(255),
    license_image_url varchar(255) not null,
    status enum ('APPROVED','CANCELED','PENDING','REJECTED') not null,
    reject_reason varchar(255),
    admin_memo longtext
);

create table store_report (
    store_report_id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    detail varchar(300),
    store_id bigint not null,
    reporter_id bigint not null,
    foreign key (store_id) references store (store_id),
    foreign key (reporter_id) references user (user_id)
);

create table store_report_reason (
    store_report_id bigint not null,
    reason enum ('BENEFIT_MISMATCH','BENEFIT_REFUSAL','CLOSED_OR_MOVED','ETC','EVENT_NOT_HELD','INFO_ERROR','LOCATION_MISMATCH'),
    foreign key (store_report_id) references store_report (store_report_id)
);

/* Item */
create table item_category (
    item_category_id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    name varchar(255) not null,
    store_id bigint not null,
    foreign key (store_id) references store (store_id)
);

create table item (
    item_id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    name varchar(255) not null,
    price int not null,
    description longtext,
    image_url varchar(255),
    is_sold_out bit not null,
    item_order int,
    is_representative bit not null,
    is_hidden bit not null,
    badge enum ('BEST','HOT','NEW'),
    store_id bigint not null,
    item_category_id bigint,
    foreign key (store_id) references store (store_id),
    foreign key (item_category_id) references item_category (item_category_id)
);

/* Coupon */
create table coupon (
    coupon_id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    title varchar(255) not null,
    issue_starts_at datetime(6),
    issue_ends_at datetime(6),
    valid_days int not null,
    total_quantity int,
    limit_per_user int not null,
    status enum ('ACTIVE','EXPIRED','WITHDRAWN_BY_OWNER') not null,
    benefit_type enum ('FIXED_DISCOUNT','PERCENTAGE_DISCOUNT','SERVICE_GIFT') not null,
    benefit_value varchar(255),
    min_order_amount int,
    download_count int default 0 not null,
    store_id bigint not null,
    foreign key (store_id) references store (store_id)
);

    /* coupon_item table removed */

create table student_coupon (
    student_coupon_id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    verification_code varchar(4),
    status enum ('ACTIVATED','EXPIRED','UNUSED','USED') not null,
    issued_at datetime(6),
    activated_at datetime(6),
    used_at datetime(6),
    expires_at datetime(6) not null,
    user_id bigint not null,
    coupon_id bigint not null,
    foreign key (user_id) references user (user_id),
    foreign key (coupon_id) references coupon (coupon_id)
);

/* Events */
create table events (
    event_id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    title varchar(255) not null,
    description text,
    latitude double,
    longitude double,
    start_date_time datetime(6) not null,
    end_date_time datetime(6) not null,
    status enum ('ENDED','LIVE','UPCOMING') not null,
    university_id bigint,
    foreign key (university_id) references university (university_id)
);

create table event_types (
    event_id bigint not null,
    event_type enum ('COMMUNITY','FLEA_MARKET','FOOD_EVENT','PERFORMANCE','POPUP_STORE','SCHOOL_EVENT'),
    foreign key (event_id) references events (event_id)
);

create table event_image (
    id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    event_id bigint not null,
    image_url varchar(255) not null,
    order_index int,
    foreign key (event_id) references events (event_id)
);

/* Partnership */
create table partnership (
    id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    benefit varchar(255) not null,
    starts_at date not null,
    ends_at date not null,
    store_id bigint not null,
    organization_id bigint not null,
    foreign key (store_id) references store (store_id),
    foreign key (organization_id) references organization (organization_id)
);

/* Favorite Store */
create table favorite_store (
    id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    user_id bigint not null,
    store_id bigint not null,
    foreign key (user_id) references user (user_id),
    foreign key (store_id) references store (store_id)
);

/* Review */
create table review (
    review_id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    user_id bigint not null,
    store_id bigint not null,
    parent_review_id bigint,
    is_verified bit not null,
    rating int,
    content longtext,
    status enum ('BANNED','PUBLISHED','REPORTED','VERIFIED') not null,
    report_count int not null,
    is_private bit not null,
    like_count int not null,
    foreign key (user_id) references user (user_id),
    foreign key (store_id) references store (store_id),
    foreign key (parent_review_id) references review (review_id)
);

create table review_image (
    review_image_id bigint auto_increment primary key,
    review_id bigint not null,
    image_url varchar(255) not null,
    order_index int not null,
    foreign key (review_id) references review (review_id)
);

create table review_like (
    id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    user_id bigint not null,
    review_id bigint not null,
    foreign key (user_id) references user (user_id),
    foreign key (review_id) references review (review_id)
);

create table review_report (
    review_report_id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    review_id bigint not null,
    reporter_id bigint not null,
    reason enum ('MALICIOUS_SLANDER','INAPPROPRIATE_CONTENT','RIGHTS_VIOLATION','PRIVACY_INFRINGEMENT','COMMERCIAL_PROMOTION','FRAUDULENT_REVIEW','IRRELEVANT','OTHER') not null,
    detail varchar(500),
    foreign key (review_id) references review (review_id),
    foreign key (reporter_id) references user (user_id)
);

/* Store News */
create table store_news (
    id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    store_id bigint not null,
    title varchar(255) not null,
    content text not null,
    like_count int not null,
    comment_count int not null,
    foreign key (store_id) references store (store_id)
);

create table store_news_comment (
    id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    store_news_id bigint not null,
    user_id bigint not null,
    content varchar(500) not null,
    foreign key (store_news_id) references store_news (id),
    foreign key (user_id) references user (user_id)
);

create table store_news_image (
    id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    store_news_id bigint not null,
    image_url varchar(255) not null,
    order_index int not null,
    foreign key (store_news_id) references store_news (id)
);

create table store_news_like (
    id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    store_news_id bigint not null,
    user_id bigint not null,
    foreign key (store_news_id) references store_news (id),
    foreign key (user_id) references user (user_id)
);

/* Inquiry */
create table inquiry (
    inquiry_id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    user_id bigint not null,
    type enum ('COUPON_BENEFIT','MAP_LOCATION','STORE_INFO_ERROR','EVENT_PARTICIPATION','ALERT_ACCOUNT','PROPOSAL_OTHER') not null,
    title varchar(14) not null,
    content varchar(500) not null,
    foreign key (user_id) references user (user_id)
);

create table inquiry_image (
    inquiry_image_id bigint auto_increment primary key,
    created_at datetime(6) not null,
    modified_at datetime(6) not null,
    created_by varchar(255),
    last_modified_by varchar(255),
    inquiry_id bigint not null,
    image_url varchar(255) not null,
    order_index int not null,
    foreign key (inquiry_id) references inquiry (inquiry_id)
);

create table email_verifications (
    id bigint auto_increment primary key,
    email varchar(320) not null,
    code varchar(10) not null,
    expires_at datetime(6) not null,
    verified bit not null,
    verified_expires_at datetime(6),
    constraint uk_email_verifications_email unique (email)
);

/* 외래 키 제약 조건 검사를 다시 켭니다 */
SET FOREIGN_KEY_CHECKS = 1;