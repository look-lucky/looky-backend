/* 외래 키 제약 조건 검사를 일시적으로 끕니다 */
SET FOREIGN_KEY_CHECKS = 0;

create table store_claim
(
    created_at             datetime(6)                                          not null,
    modified_at            datetime(6)                                          not null,
    store_claim_request_id bigint auto_increment                                primary key,
    store_id               bigint                                               not null,
    user_id                bigint                                               not null,
    biz_reg_no             varchar(255)                                         not null,
    created_by             varchar(255)                                         null,
    last_modified_by       varchar(255)                                         null,
    license_image_url      varchar(255)                                         not null,
    reject_reason          varchar(255)                                         null,
    representative_name    varchar(255)                                         not null,
    store_name             varchar(255)                                         not null,
    store_phone            varchar(255)                                         null,
    admin_memo             longtext                                             null,
    status                 enum ('APPROVED', 'CANCELED', 'PENDING', 'REJECTED') not null
);

create table university
(
    university_id bigint auto_increment primary key,
    email_domain  varchar(255) not null,
    name          varchar(255) not null,
    constraint UKhu0eqrtqtqnmr9dg9om9akgve unique (email_domain),
    constraint UKru212k5vib3yvu360fuy3h1g5 unique (name)
);

create table user
(
    birth_date       date                                                                            null,
    gender           tinyint                                                                         null,
    created_at       datetime(6)                                                                     not null,
    modified_at      datetime(6)                                                                     not null,
    user_id          bigint auto_increment                                                           primary key,
    created_by       varchar(255)                                                                    null,
    last_modified_by varchar(255)                                                                    null,
    name             varchar(255)                                                                    null,
    password         varchar(255)                                                                    null,
    phone            varchar(255)                                                                    null,
    social_id        varchar(255)                                                                    null,
    username         varchar(255)                                                                    not null,
    role             enum ('ROLE_ADMIN', 'ROLE_COUNCIL', 'ROLE_GUEST', 'ROLE_OWNER', 'ROLE_STUDENT') not null,
    social_type      enum ('FIREBASE', 'GOOGLE', 'KAKAO', 'LOCAL', 'NAVER')                          null,
    constraint UKsb8bbouer5wak8vyiiy4pf2bx  unique (username),
    check (`gender` between 0 and 2)
);

create table council_profile
(
    university_id bigint null,
    user_id       bigint not null
        primary key,
    constraint FK1ierqxkw09bsc3d01krbrw51o
        foreign key (user_id) references user (user_id),
    constraint FKgnm1op1popr49v4r0q50wmr4p
        foreign key (university_id) references university (university_id)
);

create table organization
(
    created_at       datetime(6)                                       not null,
    expires_at       datetime(6)                                       null,
    modified_at      datetime(6)                                       not null,
    organization_id  bigint auto_increment
        primary key,
    parent_id        bigint                                            null,
    university_id    bigint                                            not null,
    user_id          bigint                                            not null,
    created_by       varchar(255)                                      null,
    last_modified_by varchar(255)                                      null,
    name             varchar(255)                                      not null,
    category         enum ('COLLEGE', 'DEPARTMENT', 'STUDENT_COUNCIL') not null,
    constraint FK1qouw0orkiw7qtb6sp1ci4jcq
        foreign key (university_id) references university (university_id),
    constraint FKc30yedjwp9qw1f3nn2ytda7tj
        foreign key (parent_id) references organization (organization_id),
    constraint FKq0435723w14233u7xu6r92xev
        foreign key (user_id) references user (user_id)
);

create table owner_profile
(
    user_id bigint       not null
        primary key,
    email   varchar(255) null,
    name    varchar(255) null,
    phone   varchar(255) null,
    constraint UKssjjqplrysi6k30tk023hwj19
        unique (email),
    constraint FKt3yj1mwxfa8hjs448a8psjibn
        foreign key (user_id) references user (user_id)
);

create table store
(
    latitude         double                                 null,
    longitude        double                                 null,
    need_to_check    bit                                    null,
    created_at       datetime(6)                            not null,
    modified_at      datetime(6)                            not null,
    store_id         bigint auto_increment
        primary key,
    user_id          bigint                                 not null,
    address          varchar(255)                           not null,
    biz_reg_no       varchar(255)                           null,
    branch           varchar(255)                           null,
    created_by       varchar(255)                           null,
    last_modified_by varchar(255)                           null,
    name             varchar(255)                           not null,
    store_phone      varchar(255)                           null,
    introduction     longtext                               null,
    operating_hours  longtext                               null,
    store_status     enum ('ACTIVE', 'BANNED', 'UNCLAIMED') not null,
    constraint FKn82wpcqrb21yddap4s3ttwnxj
        foreign key (user_id) references user (user_id)
);

create table coupon
(
    limit_per_user         int                                                         not null,
    total_quantity         int                                                         not null,
    coupon_id              bigint auto_increment
        primary key,
    created_at             datetime(6)                                                 not null,
    issue_ends_at          datetime(6)                                                 null,
    issue_starts_at        datetime(6)                                                 null,
    modified_at            datetime(6)                                                 not null,
    store_id               bigint                                                      not null,
    target_organization_id bigint                                                      null,
    created_by             varchar(255)                                                null,
    last_modified_by       varchar(255)                                                null,
    title                  varchar(255)                                                not null,
    description            longtext                                                    null,
    status                 enum ('ACTIVE', 'DRAFT', 'EXPIRED', 'SCHEDULED', 'STOPPED') not null,
    constraint FKf5cgu1qdsl3jwlcm6x9fqwxnh
        foreign key (target_organization_id) references organization (organization_id),
    constraint FKs7yl3mm1m0o7fmn6et7guwo89
        foreign key (store_id) references store (store_id)
);

create table favorite_store
(
    created_at       datetime(6)  not null,
    id               bigint auto_increment
        primary key,
    modified_at      datetime(6)  not null,
    store_id         bigint       not null,
    user_id          bigint       not null,
    created_by       varchar(255) null,
    last_modified_by varchar(255) null,
    constraint FKly8cfqsjue6ojuoi7tbmr8v9g
        foreign key (user_id) references user (user_id),
    constraint FKr6bk7q3ru7y3dqcis9exkpxef
        foreign key (store_id) references store (store_id)
);

create table item
(
    is_hidden         bit                         not null,
    is_representative bit                         not null,
    is_sold_out       bit                         not null,
    item_order        int                         null,
    price             int                         not null,
    created_at        datetime(6)                 not null,
    item_id           bigint auto_increment
        primary key,
    modified_at       datetime(6)                 not null,
    store_id          bigint                      not null,
    created_by        varchar(255)                null,
    image_url         varchar(255)                null,
    last_modified_by  varchar(255)                null,
    name              varchar(255)                not null,
    badge             enum ('BEST', 'HOT', 'NEW') null,
    description       longtext                    null,
    constraint FKi0c87m5jy5qxw8orrf2pugs6h
        foreign key (store_id) references store (store_id)
);

create table coupon_item
(
    coupon_id bigint not null,
    id        bigint auto_increment
        primary key,
    item_id   bigint not null,
    constraint UKkgw42kpx1q7i0lfej4twlxr6k
        unique (coupon_id, item_id),
    constraint FKhlmwrm9mifpfvidcg62fi04f1
        foreign key (item_id) references item (item_id),
    constraint FKn00k5dh46f87hue6dd10nrhjg
        foreign key (coupon_id) references coupon (coupon_id)
);

create table partnership
(
    ends_at          date         not null,
    starts_at        date         not null,
    created_at       datetime(6)  not null,
    id               bigint auto_increment
        primary key,
    modified_at      datetime(6)  not null,
    organization_id  bigint       not null,
    store_id         bigint       not null,
    benefit          varchar(255) not null,
    created_by       varchar(255) null,
    last_modified_by varchar(255) null,
    constraint FK38uv7qdc4nnfyvkl8phqvwag5
        foreign key (organization_id) references organization (organization_id),
    constraint FKryli6bf1akblg22mlhslq21tp
        foreign key (store_id) references store (store_id)
);

create table review
(
    is_private       bit                                                  not null,
    is_verified      bit                                                  not null,
    like_count       int                                                  not null,
    rating           int                                                  not null,
    report_count     int                                                  not null,
    created_at       datetime(6)                                          not null,
    modified_at      datetime(6)                                          not null,
    parent_review_id bigint                                               null,
    review_id        bigint auto_increment
        primary key,
    store_id         bigint                                               not null,
    user_id          bigint                                               not null,
    created_by       varchar(255)                                         null,
    last_modified_by varchar(255)                                         null,
    content          longtext                                             null,
    status           enum ('BANNED', 'PUBLISHED', 'REPORTED', 'VERIFIED') not null,
    constraint FK74d12ba8sxxu9vpnc59b43y30
        foreign key (store_id) references store (store_id),
    constraint FKiyf57dy48lyiftdrf7y87rnxi
        foreign key (user_id) references user (user_id),
    constraint FKnplfourh24q37geshp603tcst
        foreign key (parent_review_id) references review (review_id)
);

create table review_image
(
    order_index     int          not null,
    review_id       bigint       not null,
    review_image_id bigint auto_increment
        primary key,
    image_url       varchar(255) not null,
    constraint FK16wp089tx9nm0obc217gvdd6l
        foreign key (review_id) references review (review_id)
);

create table review_like
(
    created_at       datetime(6)  not null,
    id               bigint auto_increment
        primary key,
    modified_at      datetime(6)  not null,
    review_id        bigint       not null,
    user_id          bigint       not null,
    created_by       varchar(255) null,
    last_modified_by varchar(255) null,
    constraint FK68am9vk1s1e8n1v873meqkk0k
        foreign key (review_id) references review (review_id),
    constraint FKq4l36vpqal6v4ehikh67g8e49
        foreign key (user_id) references user (user_id)
);

create table review_report
(
    created_at       datetime(6)                                                   not null,
    modified_at      datetime(6)                                                   not null,
    reporter_id      bigint                                                        not null,
    review_id        bigint                                                        not null,
    review_report_id bigint auto_increment
        primary key,
    detail           varchar(500)                                                  null,
    created_by       varchar(255)                                                  null,
    last_modified_by varchar(255)                                                  null,
    reason           enum ('INAPPROPRIATE_CONTENT', 'IRRELEVANT', 'OTHER', 'SPAM') not null,
    constraint FK4xx5oguy1ifjy12wnku068hbf
        foreign key (reporter_id) references user (user_id),
    constraint FK5m3i8486vomorui2jcgh95mo2
        foreign key (review_id) references review (review_id)
);

create table store_categories
(
    store_id bigint                                                                      not null,
    category enum ('BAR', 'BEAUTY_HEALTH', 'CAFE', 'ENTERTAINMENT', 'ETC', 'RESTAURANT') null,
    constraint FKmpsp0y3l52rnno3o85k2y9x4o
        foreign key (store_id) references store (store_id)
);

create table store_image
(
    order_index    int          not null,
    store_id       bigint       not null,
    store_image_id bigint auto_increment
        primary key,
    image_url      varchar(255) not null,
    constraint FK8i0t3yr73c9h244pyv5mg6m4u
        foreign key (store_id) references store (store_id)
);

create table store_moods
(
    store_id bigint                                                            not null,
    mood     enum ('GROUP_GATHERING', 'LATE_NIGHT', 'ROMANTIC', 'SOLO_DINING') null,
    constraint FKfl0tdkytx4n8o9yeu3jrk92nc
        foreign key (store_id) references store (store_id)
);

create table store_news
(
    comment_count    int          not null,
    like_count       int          not null,
    created_at       datetime(6)  not null,
    id               bigint auto_increment
        primary key,
    modified_at      datetime(6)  not null,
    store_id         bigint       not null,
    content          text         not null,
    created_by       varchar(255) null,
    last_modified_by varchar(255) null,
    title            varchar(255) not null,
    constraint FKkyd94eav31myb9ab8358ldhwo
        foreign key (store_id) references store (store_id)
);

create table store_news_comment
(
    created_at       datetime(6)  not null,
    id               bigint auto_increment
        primary key,
    modified_at      datetime(6)  not null,
    store_news_id    bigint       not null,
    user_id          bigint       not null,
    content          varchar(500) not null,
    created_by       varchar(255) null,
    last_modified_by varchar(255) null,
    constraint FKfi1midasd8wcod5f4h6v6el03
        foreign key (store_news_id) references store_news (id),
    constraint FKr88me79l05evunb13unlnbrqk
        foreign key (user_id) references user (user_id)
);

create table store_news_image
(
    created_at       datetime(6)  not null,
    id               bigint auto_increment
        primary key,
    modified_at      datetime(6)  not null,
    store_news_id    bigint       not null,
    created_by       varchar(255) null,
    image_url        varchar(255) not null,
    last_modified_by varchar(255) null,
    constraint FK7dw0kvdwgcx9dhlmyexnv07k1
        foreign key (store_news_id) references store_news (id)
);

create table store_news_like
(
    created_at       datetime(6)  not null,
    id               bigint auto_increment
        primary key,
    modified_at      datetime(6)  not null,
    store_news_id    bigint       not null,
    user_id          bigint       not null,
    created_by       varchar(255) null,
    last_modified_by varchar(255) null,
    constraint FKbsrconaxcebepql9n4s0ekvll
        foreign key (store_news_id) references store_news (id),
    constraint FKs4m8bo1o63ftuceu57521nt33
        foreign key (user_id) references user (user_id)
);

create table store_report
(
    created_at       datetime(6)  not null,
    modified_at      datetime(6)  not null,
    reporter_id      bigint       not null,
    store_id         bigint       not null,
    store_report_id  bigint auto_increment
        primary key,
    detail           varchar(300) null,
    created_by       varchar(255) null,
    last_modified_by varchar(255) null,
    constraint FK16sqgumx6ml0pq876hfg5xcjg
        foreign key (reporter_id) references user (user_id),
    constraint FKcq68qsfevhscen66i9xjh31df
        foreign key (store_id) references store (store_id)
);

create table store_report_reason
(
    store_report_id bigint                                                                                                                      not null,
    reason          enum ('BENEFIT_MISMATCH', 'BENEFIT_REFUSAL', 'CLOSED_OR_MOVED', 'ETC', 'EVENT_NOT_HELD', 'INFO_ERROR', 'LOCATION_MISMATCH') null,
    constraint FKi9o85jdi44c08y227c4u8sqhl
        foreign key (store_report_id) references store_report (store_report_id)
);

create table student_coupon
(
    verification_code varchar(4)                                      null,
    activated_at      datetime(6)                                     null,
    coupon_id         bigint                                          not null,
    created_at        datetime(6)                                     not null,
    expires_at        datetime(6)                                     not null,
    issued_at         datetime(6)                                     null,
    modified_at       datetime(6)                                     not null,
    student_coupon_id bigint auto_increment
        primary key,
    used_at           datetime(6)                                     null,
    user_id           bigint                                          not null,
    created_by        varchar(255)                                    null,
    last_modified_by  varchar(255)                                    null,
    status            enum ('ACTIVATED', 'EXPIRED', 'UNUSED', 'USED') not null,
    constraint FK8dfl9drre6wlg7wb69fukvi37
        foreign key (coupon_id) references coupon (coupon_id),
    constraint FKf8yim8x7d0vcqeqnq68vfp6cd
        foreign key (user_id) references user (user_id)
);

create table student_profile
(
    university_id bigint       null,
    user_id       bigint       not null
        primary key,
    nickname      varchar(255) null,
    constraint FK2romx74aqupshw03h87f7q7h2
        foreign key (user_id) references user (user_id),
    constraint FKaetvcc3qjminqmcivngevpuga
        foreign key (university_id) references university (university_id)
);

create table user_organization
(
    id              bigint auto_increment
        primary key,
    organization_id bigint not null,
    user_id         bigint not null,
    constraint FK9nwktb5dduncsh5rx4fstyoho
        foreign key (user_id) references user (user_id),
    constraint FKfdnaj8emi62iffmg6w6ykjxf4
        foreign key (organization_id) references organization (organization_id)
);

/* 외래 키 제약 조건 검사를 다시 켭니다 */
SET FOREIGN_KEY_CHECKS = 1;