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

    private boolean isSoldOut = false;

    private Integer itemOrder;

    private boolean isRepresentative = false;

    private boolean isHidden = false;

    @Enumerated(EnumType.STRING)
    private ItemBadge badge;

    @Builder
    public Item(Store store, String name, int price, String description, String imageUrl, boolean isSoldOut, Integer itemOrder, boolean isRepresentative, boolean isHidden, ItemBadge badge) {
        this.store = store;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.isSoldOut = isSoldOut;
        this.itemOrder = itemOrder;
        this.isRepresentative = isRepresentative;
        this.isHidden = isHidden;
        this.badge = badge;
    }

    public void updateItem(String name, Integer price, String description, String imageUrl, Boolean isSoldOut, Integer itemOrder, Boolean isRepresentative, Boolean isHidden, ItemBadge badge) {
        if (name != null) {
            this.name = name;
        }
        if (price != null) {
            this.price = price;
        }
        if (description != null) {
            this.description = description;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
        if (isSoldOut != null) {
            this.isSoldOut = isSoldOut;
        }
        if (itemOrder != null) {
            this.itemOrder = itemOrder;
        }
        if (isRepresentative != null) {
            this.isRepresentative = isRepresentative;
        }
        if (isHidden != null) {
            this.isHidden = isHidden;
        }
        if (badge != null) {
            this.badge = badge;
        }
    }
}
