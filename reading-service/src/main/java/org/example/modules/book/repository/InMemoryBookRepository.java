package org.example.modules.book.repository;

import org.example.modules.book.entity.BannerItem;
import org.example.modules.book.entity.Book;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class InMemoryBookRepository {

    private final List<Book> books = new ArrayList<>();
    private final List<BannerItem> banners = new ArrayList<>();

    public InMemoryBookRepository() {
        books.add(book(1L, "星河尽头", "南柯一梦", "星河", "#1c2b4a", "#ff6b5a", "科幻", List.of("星际", "成长"), "连载", "128.6万", "少年执一把锈蚀星图，踏入无人敢越的星海禁区。", 486));
        books.add(book(2L, "剑起苍穹", "青衫客", "剑起", "#1f3a5f", "#4a90d9", "玄幻", List.of("热血", "修仙"), "连载", "256.2万", "一柄断剑，一段被掩埋的上古秘辛，少年从此逆天改命。", 920));
        books.add(book(3L, "余生请多指教", "林深见鹿", "余生", "#5c2a4a", "#e87a9a", "言情", List.of("现代", "甜宠"), "完结", "98.4万", "一场阴差阳错的重逢，把错过的十年补成温柔长卷。", 312));
        books.add(book(4L, "京城夜雨", "墨染山河", "京城", "#2b1d16", "#c47a4a", "历史", List.of("权谋", "古风"), "连载", "176.8万", "雨夜入京，刀光与朝堂同落，谁主沉浮。", 654));
        books.add(book(5L, "都市神医", "一碗汤", "神医", "#1a3a3a", "#2bbbad", "都市", List.of("医术", "爽文"), "连载", "312.1万", "隐世医术重现人间，一手回春，一手翻云。", 1102));
        books.add(book(6L, "白日梦工厂", "慢慢来", "白日", "#2d6a4f", "#ff8a65", "都市", List.of("轻松", "职场"), "连载", "64.3万", "普通打工人意外接手一座会实现愿望的工厂。", 188));
        books.add(book(7L, "月光旅店", "晚风起", "月光", "#2a2a4a", "#8b7bff", "言情", List.of("治愈", "奇幻"), "完结", "87.9万", "每晚只住一位客人的旅店，收留所有说不出口的心事。", 246));
        books.add(book(8L, "诸天兑换商", "路过青山", "诸天", "#3a1a1a", "#ff4d3a", "玄幻", List.of("穿越", "系统"), "连载", "401.5万", "一枚残破玉简，打通诸天万界的交易通道。", 1308));
        books.add(book(9L, "半城烟沙", "疏影横斜", "烟沙", "#4a3a2a", "#d4a574", "历史", List.of("边塞", "家国"), "完结", "112.0万", "黄沙漫卷旌旗，少年将军一战成名。", 420));
        books.add(book(10L, "她从光里来", "七月微凉", "光里", "#4a2a3a", "#ff6b9d", "言情", List.of("重生", "甜虐"), "连载", "145.7万", "重生归来，她只想把从前错过的光，一点点找回来。", 368));
        books.add(book(11L, "深空回响", "星尘", "深空", "#0f1c2e", "#4ecdc4", "科幻", List.of("冒险", "硬核"), "连载", "73.2万", "失联舰队留下的最后一条信号，改写人类的边界。", 210));
        books.add(book(12L, "长安故人", "白衣卿相", "长安", "#3a2a1a", "#e8b86d", "历史", List.of("朝堂", "友情"), "完结", "199.4万", "少年同窗入仕，半生荣辱，终不忘长安一诺。", 580));

        banners.add(new BannerItem(1L, "星河尽头", "穿越星际迷雾，寻一份不被遗忘的羁绊",
                "linear-gradient(135deg, #1c2b4a 0%, #3d5a80 55%, #ff6b5a 100%)", 1L));
        banners.add(new BannerItem(2L, "京城夜雨", "权谋与人心交织的古风长卷",
                "linear-gradient(135deg, #2b1d16 0%, #6b3e2e 50%, #c47a4a 100%)", 4L));
        banners.add(new BannerItem(3L, "白日梦工厂", "轻松都市，把平凡日子写成光",
                "linear-gradient(135deg, #1a3a2a 0%, #2d6a4f 50%, #ff8a65 100%)", 6L));
    }

    public List<Book> findAll() {
        return List.copyOf(books);
    }

    public List<BannerItem> findBanners() {
        return List.copyOf(banners);
    }

    public List<Book> findRecommend() {
        return books.stream().limit(8).collect(Collectors.toList());
    }

    public List<Book> findNewBooks() {
        List<Book> copy = new ArrayList<>(books);
        java.util.Collections.reverse(copy);
        return copy.stream().limit(6).collect(Collectors.toList());
    }

    public List<Book> findFinished() {
        return books.stream().filter(b -> "完结".equals(b.getStatus())).collect(Collectors.toList());
    }

    public List<Book> findHotRank(int limit) {
        return books.stream()
                .sorted(Comparator.comparingDouble((Book b) -> parseHeat(b.getHeat())).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Book> search(String keyword) {
        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            return findAll();
        }
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase(Locale.ROOT).contains(q)
                        || b.getAuthor().toLowerCase(Locale.ROOT).contains(q)
                        || b.getCategory().contains(keyword.trim()))
                .collect(Collectors.toList());
    }

    private double parseHeat(String heat) {
        try {
            return Double.parseDouble(heat.replace("万", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private Book book(Long id, String title, String author, String seed, String from, String to,
                      String category, List<String> tags, String status, String heat, String desc, int chapters) {
        return new Book(id, title, author, cover(seed, from, to), category, tags, status, heat, desc, chapters);
    }

    private String cover(String seed, String from, String to) {
        String svg = """
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
                """.formatted(from, to, seed);
        return "data:image/svg+xml," + URLEncoder.encode(svg, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
