package org.example.modules.book.controller;

import org.example.common.response.ResponseResult;
import org.example.modules.book.dto.UpdateBookRequest;
import org.example.modules.book.entity.Book;
import org.example.modules.book.service.BookService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/books")
public class BookAdminController {

    private final BookService bookService;

    public BookAdminController(BookService bookService) {
        this.bookService = bookService;
    }

    @PutMapping("/{id}")
    public ResponseResult<Book> updateBook(@PathVariable Long id, @RequestBody UpdateBookRequest request) {
        return ResponseResult.success(bookService.updateBook(id, request));
    }

    @PostMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseResult<Book> uploadCover(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ResponseResult.success(bookService.uploadCover(id, file));
    }

    @PutMapping("/{id}/shelf")
    public ResponseResult<Book> toggleShelf(@PathVariable Long id) {
        return ResponseResult.success(bookService.toggleShelf(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseResult.success();
    }
}
