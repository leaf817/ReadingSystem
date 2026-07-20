package org.example.modules.book.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.modules.book.entity.BannerItem;
import org.example.modules.book.entity.Book;

import java.util.List;

@Data
@AllArgsConstructor
public class HomeFeedResponse {
    private List<BannerItem> banners;
    private List<Book> recommendBooks;
    private List<Book> newBooks;
    private List<Book> finishedBooks;
    private List<Book> hotRank;
    private List<Book> maleBooks;
    private List<Book> femaleBooks;
    private List<Book> guessBooks;
}
