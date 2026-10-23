package org.example.modules.book.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterSummary {
    private Integer seq;
    private String title;
    private Integer wordCount;
}
