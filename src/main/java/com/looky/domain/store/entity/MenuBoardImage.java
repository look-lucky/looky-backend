package com.looky.domain.store.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "menu_board_image")
public class MenuBoardImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private int orderIndex;

    @Builder
    public MenuBoardImage(String imageUrl, int orderIndex) {
        this.imageUrl = imageUrl;
        this.orderIndex = orderIndex;
    }

    public void updateOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public void setStore(Store store) {
        this.store = store;
    }
}
