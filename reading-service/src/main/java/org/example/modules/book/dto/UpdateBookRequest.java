package org.example.modules.book.dto;

import lombok.Data;

@Data
public class UpdateBookRequest {
    private String title;
    private String author;
    private String cover;
    private String category;
    private String tags;
    private String status;
    private String shelf;
    private String heat;
    private String desc;
    private Integer chapters;
}
