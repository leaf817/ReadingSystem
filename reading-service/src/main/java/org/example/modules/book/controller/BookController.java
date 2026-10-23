package org.example.modules.book.controller;

import org.example.common.response.ResponseResult;
import org.example.modules.book.dto.ChapterContent;
import org.example.modules.book.dto.ChapterSummary;
import org.example.modules.book.dto.HomeFeedResponse;
import org.example.modules.book.entity.BannerItem;
import org.example.modules.book.entity.Book;
import org.example.modules.book.service.BookService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @GetMapping("/books/{id}")
    public ResponseResult<Book> book(@PathVariable Long id) {
        return ResponseResult.success(bookService.getBook(id));
    }

    @GetMapping("/books/{id}/chapters")
    public ResponseResult<List<ChapterSummary>> chapters(@PathVariable Long id) {
        return ResponseResult.success(bookService.listChapters(id));
    }

    @GetMapping("/books/{id}/chapters/{seq}")
    public ResponseResult<ChapterContent> chapter(@PathVariable Long id, @PathVariable Integer seq) {
        return ResponseResult.success(bookService.getChapter(id, seq));
    }

    @GetMapping("/books/{id}/cover")
    public ResponseEntity<byte[]> bookCover(@PathVariable Long id) {
        BookService.CoverImage image = bookService.loadCoverImage(id);
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(image.contentType());
        } catch (Exception e) {
            mediaType = MediaType.IMAGE_JPEG;
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .body(image.bytes());
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
