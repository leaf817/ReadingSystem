package org.example.modules.book.controller;

import org.example.common.response.ResponseResult;
import org.example.modules.book.dto.HomeFeedResponse;
import org.example.modules.book.entity.BannerItem;
import org.example.modules.book.entity.Book;
import org.example.modules.book.service.BookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/home")
    public ResponseResult<HomeFeedResponse> home() {
        return ResponseResult.success(bookService.getHomeFeed());
    }

    @GetMapping("/books")
    public ResponseResult<List<Book>> books(@RequestParam(required = false) String keyword) {
        return ResponseResult.success(bookService.listBooks(keyword));
    }

    @GetMapping("/ranks/hot")
    public ResponseResult<List<Book>> hotRank() {
        return ResponseResult.success(bookService.hotRank());
    }

    @GetMapping("/banners")
    public ResponseResult<List<BannerItem>> banners() {
        return ResponseResult.success(bookService.banners());
    }
}
