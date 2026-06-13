package com.cuutruyen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchasedChapterId implements Serializable {
    private Integer userId;
    private Integer chapterId;
}
