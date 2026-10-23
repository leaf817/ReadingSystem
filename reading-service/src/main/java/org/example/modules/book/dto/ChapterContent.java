package org.example.modules.book.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterContent {
    private Long bookId;
    private Integer seq;
    private String title;
    private String content;
    private Integer wordCount;
    private Integer prevSeq;
    private Integer nextSeq;
    private Integer total;
}
