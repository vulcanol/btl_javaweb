package com.cuutruyen.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Menu")
@Data
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Integer id;

    @Column(name = "parent_id")
    private Integer parentId;

    // Giữ column name = "title" để tương thích với DB cũ
    // (DB cũ có cột title NOT NULL)
    @Column(name = "title", nullable = false)
    private String menuName;

    // Giữ column name = "url" để tương thích với DB cũ
    @Column(name = "url", nullable = false)
    private String menuUrl;

    @Column(name = "sort_order")
    private Integer sortOrder = 1;

    // Đổi từ is_hidden (boolean ẩn) → is_active (boolean hiện)
    // Dùng lại cột is_hidden, logic đảo ngược trong service
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "icon")
    private String icon;

    @Column(name = "target")
    private String target = "_self";
}
