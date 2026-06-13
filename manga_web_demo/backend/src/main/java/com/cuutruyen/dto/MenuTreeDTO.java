package com.cuutruyen.dto;

import lombok.Data;
import java.util.List;

@Data
public class MenuTreeDTO {
    private Integer id;
    private Integer parentId;
    private String menuName;
    private String menuUrl;
    private Integer sortOrder;
    private Boolean isActive;
    private String icon;
    private String target;
    private List<MenuTreeDTO> children;
}
