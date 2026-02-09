package com.looky.domain.item.entity;

import com.looky.common.entity.BaseEntity;
import com.looky.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Lob
    private String description;

    private String imageUrl;

    private Boolean isSoldOut = false;

    private Integer itemOrder;

    private Boolean isRepresentative = false;

    private Boolean isHidden = false;

    @Enumerated(EnumType.STRING)
    private ItemBadge badge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_category_id")
    private ItemCategory itemCategory;

    @Builder
    public Item(Store store, String name, int price, String description, String imageUrl, Boolean isSoldOut, Integer itemOrder, Boolean isRepresentative, Boolean isHidden, ItemBadge badge, ItemCategory itemCategory) {
        this.store = store;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.isSoldOut = isSoldOut != null ? isSoldOut : false;
        this.itemOrder = itemOrder;
        this.isRepresentative = isRepresentative != null ? isRepresentative : false;
        this.isHidden = isHidden != null ? isHidden : false;
        this.badge = badge;
        this.itemCategory = itemCategory;
    }

    public void updateItem(String name, Integer price, String description, String imageUrl, Boolean isSoldOut, Integer itemOrder, Boolean isRepresentative, Boolean isHidden, ItemBadge badge, ItemCategory itemCategory) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.isSoldOut = isSoldOut;
        this.itemOrder = itemOrder;
        this.isRepresentative = isRepresentative;
        this.isHidden = isHidden;
        this.badge = badge;
        this.itemCategory = itemCategory;
    }
}
