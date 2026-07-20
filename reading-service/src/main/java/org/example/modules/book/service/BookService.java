package org.example.modules.book.service;

import org.example.modules.book.dto.HomeFeedResponse;
import org.example.modules.book.entity.BannerItem;
import org.example.modules.book.entity.Book;
import org.example.modules.book.repository.InMemoryBookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookService {

    private static final Set<String> MALE_CATEGORIES = Set.of("玄幻", "都市", "历史", "科幻");

    private final InMemoryBookRepository bookRepository;

    public BookService(InMemoryBookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public HomeFeedResponse getHomeFeed() {
        List<Book> all = bookRepository.findAll();
        List<Book> maleBooks = all.stream()
                .filter(b -> MALE_CATEGORIES.contains(b.getCategory()))
                .limit(4)
                .collect(Collectors.toList());
        List<Book> femaleBooks = all.stream()
                .filter(b -> "言情".equals(b.getCategory()) || b.getTags().contains("甜宠") || b.getTags().contains("治愈"))
                .limit(4)
                .collect(Collectors.toList());
        List<Book> guessBooks = all.stream().skip(2).limit(6).collect(Collectors.toList());

        return new HomeFeedResponse(
                bookRepository.findBanners(),
                bookRepository.findRecommend(),
                bookRepository.findNewBooks(),
                bookRepository.findFinished(),
                bookRepository.findHotRank(10),
                maleBooks,
                femaleBooks,
                guessBooks
        );
    }

    public List<Book> listBooks(String keyword) {
        return bookRepository.search(keyword);
    }

    public List<Book> hotRank() {
        return bookRepository.findHotRank(10);
    }

    public List<BannerItem> banners() {
        return bookRepository.findBanners();
    }
}
