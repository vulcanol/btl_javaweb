package com.cuutruyen.controller;

import com.cuutruyen.dto.MenuTreeDTO;
import com.cuutruyen.entity.Menu;
import com.cuutruyen.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {
    
    private final MenuService menuService;

    /** Lấy toàn bộ menu (phẳng) - dùng cho Admin CRUD */
    @GetMapping
    public ResponseEntity<List<Menu>> getAllMenus() {
        return ResponseEntity.ok(menuService.getAllMenus());
    }

    /** Lấy cây menu (chỉ active) - dùng cho Navbar */
    @GetMapping("/tree")
    public ResponseEntity<List<MenuTreeDTO>> getMenuTree() {
        return ResponseEntity.ok(menuService.getMenuTree());
    }

    @PostMapping
    public ResponseEntity<Menu> createMenu(@RequestBody Menu menu) {
        return ResponseEntity.ok(menuService.createMenu(menu));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Menu> updateMenu(@PathVariable Integer id, @RequestBody Menu updatedMenu) {
        return menuService.updateMenu(id, updatedMenu)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Integer id) {
        if (menuService.deleteMenu(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
