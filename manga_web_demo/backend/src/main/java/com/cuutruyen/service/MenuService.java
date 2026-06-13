package com.cuutruyen.service;

import com.cuutruyen.dto.MenuTreeDTO;
import com.cuutruyen.entity.Menu;
import com.cuutruyen.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;

    /** Trả về toàn bộ danh sách phẳng (dùng cho admin CRUD) */
    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }

    /** Trả về cây menu (chỉ active) dùng cho navbar */
    public List<MenuTreeDTO> getMenuTree() {
        List<Menu> allMenus = menuRepository.findByIsActiveTrueOrderBySortOrderAsc();
        return buildTree(allMenus, null);
    }

    private List<MenuTreeDTO> buildTree(List<Menu> allMenus, Integer parentId) {
        return allMenus.stream()
            .filter(m -> parentId == null ? m.getParentId() == null : parentId.equals(m.getParentId()))
            .map(m -> {
                MenuTreeDTO dto = new MenuTreeDTO();
                dto.setId(m.getId());
                dto.setParentId(m.getParentId());
                dto.setMenuName(m.getMenuName());
                dto.setMenuUrl(m.getMenuUrl());
                dto.setSortOrder(m.getSortOrder());
                dto.setIsActive(m.getIsActive());
                dto.setIcon(m.getIcon());
                dto.setTarget(m.getTarget());
                dto.setChildren(buildTree(allMenus, m.getId()));
                return dto;
            })
            .collect(Collectors.toList());
    }

    public Menu createMenu(Menu menu) {
        if (menu.getIsActive() == null) menu.setIsActive(true);
        if (menu.getSortOrder() == null) menu.setSortOrder(1);
        if (menu.getTarget() == null) menu.setTarget("_self");
        return menuRepository.save(menu);
    }

    public Optional<Menu> updateMenu(Integer id, Menu updatedMenu) {
        return menuRepository.findById(id).map(menu -> {
            if (updatedMenu.getMenuName() != null) menu.setMenuName(updatedMenu.getMenuName());
            if (updatedMenu.getMenuUrl() != null) menu.setMenuUrl(updatedMenu.getMenuUrl());
            if (updatedMenu.getParentId() != null) menu.setParentId(updatedMenu.getParentId());
            if (updatedMenu.getSortOrder() != null) menu.setSortOrder(updatedMenu.getSortOrder());
            if (updatedMenu.getIsActive() != null) menu.setIsActive(updatedMenu.getIsActive());
            if (updatedMenu.getIcon() != null) menu.setIcon(updatedMenu.getIcon());
            if (updatedMenu.getTarget() != null) menu.setTarget(updatedMenu.getTarget());
            // Allow explicit null for parentId (make root)
            menu.setParentId(updatedMenu.getParentId());
            return menuRepository.save(menu);
        });
    }

    public boolean deleteMenu(Integer id) {
        if (menuRepository.existsById(id)) {
            menuRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
