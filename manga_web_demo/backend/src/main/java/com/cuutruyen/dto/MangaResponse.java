package com.cuutruyen.dto;

import lombok.Data;
import java.util.List;

@Data
public class MangaResponse {
    private Integer id;
    private String title;
    private String coverUrl;
    private List<String> authors;
    private List<String> genres;
    private Long views;
    private String status;
}
