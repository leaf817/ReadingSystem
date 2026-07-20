package org.example.modules.book.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerItem {
    private Long id;
    private String title;
    private String subtitle;
    private String tone;
    private Long bookId;
}
