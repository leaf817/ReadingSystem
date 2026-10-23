package org.example.modules.book.service;

import org.example.common.exception.BusinessException;
import org.example.common.response.ResponseCode;
import org.example.modules.book.dto.ChapterContent;
import org.example.modules.book.dto.ChapterSummary;
import org.example.modules.book.dto.HomeFeedResponse;
import org.example.modules.book.dto.UpdateBookRequest;
import org.example.modules.book.entity.BannerItem;
import org.example.modules.book.entity.Book;
import org.example.modules.book.entity.BookChapterEntity;
import org.example.modules.book.entity.BookCoverEntity;
import org.example.modules.book.entity.BookEntity;
import org.example.modules.book.repository.BookChapterRepository;
import org.example.modules.book.repository.BookCoverRepository;
import org.example.modules.book.repository.BookJpaRepository;
import org.example.modules.book.repository.InMemoryBookRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookService {

    private static final List<String> MALE_CATS = List.of("奇幻言情", "仙侠言情", "古代言情");
    private static final List<String> FEMALE_CATS = List.of("现代言情", "古代言情", "奇幻言情", "仙侠言情");

    private final BookJpaRepository bookJpaRepository;
    private final BookCoverRepository bookCoverRepository;
    private final BookChapterRepository bookChapterRepository;
    private final InMemoryBookRepository inMemoryBookRepository;
    private final CoverStorageService coverStorageService;

    public BookService(
            BookJpaRepository bookJpaRepository,
            BookCoverRepository bookCoverRepository,
            BookChapterRepository bookChapterRepository,
            InMemoryBookRepository inMemoryBookRepository,
            CoverStorageService coverStorageService
    ) {
        this.bookJpaRepository = bookJpaRepository;
        this.bookCoverRepository = bookCoverRepository;
        this.bookChapterRepository = bookChapterRepository;
        this.inMemoryBookRepository = inMemoryBookRepository;
        this.coverStorageService = coverStorageService;
    }

    public HomeFeedResponse getHomeFeed() {
        List<Book> all = listBooks(null);
        List<Book> maleBooks = all.stream()
                .filter(b -> MALE_CATS.contains(b.getCategory()))
                .limit(4)
                .collect(Collectors.toList());
        List<Book> femaleBooks = all.stream()
                .filter(b -> FEMALE_CATS.contains(b.getCategory()))
                .limit(4)
                .collect(Collectors.toList());
        List<Book> guessBooks = all.stream().skip(2).limit(6).collect(Collectors.toList());

        return new HomeFeedResponse(
                bannersFrom(all),
                all.stream().limit(8).collect(Collectors.toList()),
                reverseLimit(all, 6),
                all.stream().filter(b -> "完结".equals(b.getStatus())).limit(8).collect(Collectors.toList()),
                hotRank(10),
                maleBooks,
                femaleBooks,
                guessBooks
        );
    }

    public List<Book> listBooks(String keyword) {
        List<Book> all = loadBooks();
        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            return all;
        }
        return all.stream()
                .filter(b -> b.getTitle().toLowerCase(Locale.ROOT).contains(q)
                        || b.getAuthor().toLowerCase(Locale.ROOT).contains(q)
                        || b.getCategory().contains(keyword.trim()))
                .collect(Collectors.toList());
    }

    public List<Book> hotRank() {
        return hotRank(10);
    }

    public List<BannerItem> banners() {
        return bannersFrom(loadBooks());
    }

    public Book getBook(Long id) {
        return bookJpaRepository.findById(id)
                .map(this::toBook)
                .orElseThrow(() -> new BusinessException(ResponseCode.NOT_FOUND, "书籍不存在"));
    }

    @Transactional(readOnly = true)
    public List<ChapterSummary> listChapters(Long bookId) {
        ensureBookExists(bookId);
        return bookChapterRepository.findByBookIdOrderBySeqAsc(bookId).stream()
                .map(ch -> new ChapterSummary(ch.getSeq(), ch.getTitle(), ch.getWordCount()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChapterContent getChapter(Long bookId, Integer seq) {
        ensureBookExists(bookId);
        List<BookChapterEntity> chapters = bookChapterRepository.findByBookIdOrderBySeqAsc(bookId);
        if (chapters.isEmpty()) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "本书暂无正文");
        }
        int index = -1;
        for (int i = 0; i < chapters.size(); i++) {
            if (chapters.get(i).getSeq().equals(seq)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "章节不存在");
        }
        BookChapterEntity current = chapters.get(index);
        Integer prevSeq = index > 0 ? chapters.get(index - 1).getSeq() : null;
        Integer nextSeq = index < chapters.size() - 1 ? chapters.get(index + 1).getSeq() : null;
        return new ChapterContent(
                bookId,
                current.getSeq(),
                current.getTitle(),
                current.getContent(),
                current.getWordCount(),
                prevSeq,
                nextSeq,
                chapters.size()
        );
    }

    private void ensureBookExists(Long id) {
        if (!bookJpaRepository.existsById(id)) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "书籍不存在");
        }
    }

    public Book updateBook(Long id, UpdateBookRequest request) {
        BookEntity entity = bookJpaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.BAD_REQUEST, "书籍不存在"));

        String title = request.getTitle() == null ? "" : request.getTitle().trim();
        String author = request.getAuthor() == null ? "" : request.getAuthor().trim();
        String category = request.getCategory() == null ? "" : request.getCategory().trim();
        if (!StringUtils.hasText(title)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "请输入书名");
        }
        if (title.length() > 128) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "书名过长");
        }
        if (!StringUtils.hasText(author)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "请输入作者");
        }
        if (author.length() > 64) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "作者名过长");
        }
        if (!StringUtils.hasText(category)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "请选择分类");
        }

        String status = StringUtils.hasText(request.getStatus()) ? request.getStatus().trim() : entity.getStatus();
        if (!"连载".equals(status) && !"完结".equals(status)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "连载状态无效");
        }
        String shelf = StringUtils.hasText(request.getShelf()) ? request.getShelf().trim() : entity.getShelf();
        if (!"上架".equals(shelf) && !"下架".equals(shelf)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "上架状态无效");
        }

        String desc = request.getDesc() == null ? "" : request.getDesc().trim();
        if (desc.length() > 512) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "简介过长");
        }
        String heat = request.getHeat() == null ? entity.getHeat() : request.getHeat().trim();
        if (!StringUtils.hasText(heat)) {
            heat = "0";
        }
        if (heat.length() > 32) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "热度文案过长");
        }
        Integer chapters = request.getChapters();
        if (chapters == null) {
            chapters = entity.getChapters() == null ? 0 : entity.getChapters();
        }
        if (chapters < 0) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "章节数不能为负数");
        }

        String tags = request.getTags() == null ? "" : Arrays.stream(request.getTags().split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(","));
        if (tags.length() > 255) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "标签过长");
        }

        if (request.getCover() != null && !StringUtils.hasText(request.getCover().trim())) {
            clearStoredCover(entity);
        }

        entity.setTitle(title);
        entity.setAuthor(author);
        entity.setCategory(category);
        entity.setTags(tags);
        entity.setStatus(status);
        entity.setShelf(shelf);
        entity.setHeat(heat);
        entity.setDescription(desc);
        entity.setChapters(chapters);

        return toBook(bookJpaRepository.save(entity));
    }

    public Book uploadCover(Long id, MultipartFile file) {
        BookEntity entity = bookJpaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.BAD_REQUEST, "书籍不存在"));
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length == 0) {
                throw new BusinessException(ResponseCode.BAD_REQUEST, "请选择封面图片");
            }
            String mime = file.getContentType();
            if (!StringUtils.hasText(mime) || !mime.toLowerCase(Locale.ROOT).startsWith("image/")) {
                throw new BusinessException(ResponseCode.BAD_REQUEST, "仅支持图片文件");
            }
            int ver = entity.getCoverVer() == null ? 1 : entity.getCoverVer() + 1;
            BookCoverEntity cover = bookCoverRepository.findById(id).orElseGet(BookCoverEntity::new);
            cover.setBookId(id);
            cover.setMime(mime);
            cover.setData(bytes);
            cover.setVer(ver);
            bookCoverRepository.save(cover);
            entity.setCoverMime(mime);
            entity.setCoverVer(ver);
            entity.setCover("/api/books/" + id + "/cover?v=" + ver);
            bookJpaRepository.save(entity);
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "封面读取失败");
        }
        return toBook(entity);
    }

    @Transactional(readOnly = true)
    public CoverImage loadCoverImage(Long id) {
        BookEntity entity = bookJpaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.BAD_REQUEST, "书籍不存在"));
        return bookCoverRepository.findById(id)
                .filter(row -> row.getData() != null && row.getData().length > 0)
                .map(row -> new CoverImage(row.getData(), StringUtils.hasText(row.getMime()) ? row.getMime() : MediaType.IMAGE_JPEG_VALUE))
                .orElseGet(() -> {
                    String svg = defaultCoverSvg(entity.getTitle());
                    return new CoverImage(svg.getBytes(StandardCharsets.UTF_8), "image/svg+xml");
                });
    }

    public record CoverImage(byte[] bytes, String contentType) {}

    public Book toggleShelf(Long id) {
        BookEntity entity = bookJpaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.BAD_REQUEST, "书籍不存在"));
        entity.setShelf("上架".equals(entity.getShelf()) ? "下架" : "上架");
        return toBook(bookJpaRepository.save(entity));
    }

    public void deleteBook(Long id) {
        BookEntity entity = bookJpaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.BAD_REQUEST, "书籍不存在"));
        clearStoredCover(entity);
        bookChapterRepository.deleteByBookId(id);
        bookJpaRepository.deleteById(id);
    }

    private void clearStoredCover(BookEntity entity) {
        if (entity.getId() != null) {
            bookCoverRepository.deleteById(entity.getId());
        }
        coverStorageService.deleteIfStored(entity.getCover());
        entity.setCover(null);
        entity.setCoverMime(null);
        entity.setCoverVer(0);
    }

    private List<Book> hotRank(int limit) {
        return loadBooks().stream()
                .sorted(Comparator.comparingDouble((Book b) -> parseHeat(b.getHeat())).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<Book> loadBooks() {
        List<BookEntity> rows = bookJpaRepository.findAllByOrderByIdAsc();
        if (rows.isEmpty()) {
            return inMemoryBookRepository.findAll();
        }
        return rows.stream().map(this::toBook).collect(Collectors.toList());
    }

    private List<Book> reverseLimit(List<Book> all, int limit) {
        return all.stream()
                .sorted(Comparator.comparing(Book::getId).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<BannerItem> bannersFrom(List<Book> all) {
        if (all.isEmpty()) {
            return inMemoryBookRepository.findBanners();
        }
        return all.stream().limit(3).map(b -> new BannerItem(
                b.getId(),
                b.getTitle(),
                b.getDesc(),
                "linear-gradient(135deg, #1c2b4a 0%, #3d5a80 55%, #ff6b5a 100%)",
                b.getId()
        )).collect(Collectors.toList());
    }

    private Book toBook(BookEntity e) {
        List<String> tags = Arrays.stream(StringUtils.hasText(e.getTags()) ? e.getTags().split(",") : new String[0])
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        String cover = resolveCoverUrl(e);
        String shelf = StringUtils.hasText(e.getShelf()) ? e.getShelf() : "上架";
        return new Book(
                e.getId(),
                e.getTitle(),
                e.getAuthor(),
                cover,
                e.getCategory(),
                tags,
                e.getStatus(),
                shelf,
                e.getHeat(),
                e.getDescription(),
                e.getChapters()
        );
    }

    private String resolveCoverUrl(BookEntity e) {
        if (StringUtils.hasText(e.getCover()) && (e.getCover().startsWith("http://") || e.getCover().startsWith("https://"))) {
            return e.getCover();
        }
        boolean hasBlob = StringUtils.hasText(e.getCoverMime()) || (e.getCoverVer() != null && e.getCoverVer() > 0);
        if (hasBlob) {
            int ver = e.getCoverVer() == null ? 1 : e.getCoverVer();
            return "/api/books/" + e.getId() + "/cover?v=" + ver;
        }
        if (StringUtils.hasText(e.getCover()) && e.getCover().startsWith("/uploads/")) {
            return e.getCover();
        }
        return defaultCover(e.getTitle());
    }

    private String defaultCover(String title) {
        String svg = defaultCoverSvg(title);
        return "data:image/svg+xml," + URLEncoder.encode(svg, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String defaultCoverSvg(String title) {
        String seed = title == null || title.isBlank() ? "阅" : title.substring(0, Math.min(2, title.length()));
        String[] colors = pickColors(seed);
        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="240" height="320" viewBox="0 0 240 320">
                  <defs>
                    <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
                      <stop offset="0%%" stop-color="%s"/>
                      <stop offset="100%%" stop-color="%s"/>
                    </linearGradient>
                  </defs>
                  <rect width="240" height="320" fill="url(#g)"/>
                  <circle cx="180" cy="60" r="48" fill="rgba(255,255,255,0.12)"/>
                  <circle cx="40" cy="260" r="70" fill="rgba(0,0,0,0.12)"/>
                  <text x="24" y="280" fill="rgba(255,255,255,0.92)" font-size="22" font-family="serif">%s</text>
                </svg>
                """.formatted(colors[0], colors[1], seed);
    }

    private String[] pickColors(String seed) {
        int h = Math.abs(seed.hashCode());
        String[][] palette = {
                {"#1c2b4a", "#ff6b5a"},
                {"#5c2a4a", "#e87a9a"},
                {"#2b1d16", "#c47a4a"},
                {"#1a3a3a", "#2bbbad"},
                {"#2d6a4f", "#ff8a65"},
                {"#3a1a1a", "#ff4d3a"},
        };
        return palette[h % palette.length];
    }

    private double parseHeat(String heat) {
        try {
            return Double.parseDouble(heat.replace("万", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}
