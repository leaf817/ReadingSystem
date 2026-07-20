package org.example.modules.book.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private Long id;
    private String title;
    private String author;
    private String cover;
    private String category;
    private List<String> tags;
    private String status;
    private String heat;
    private String desc;
    private Integer chapters;
}
