package org.example.modules.book.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "book")
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(nullable = false, length = 64)
    private String author;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String cover;

    @Column(name = "cover_mime", length = 64)
    private String coverMime;

    @Column(name = "cover_ver", nullable = false)
    private Integer coverVer = 0;

    @Column(nullable = false, length = 32)
    private String category;

    @Column(nullable = false, length = 255)
    private String tags = "";

    /** 连载 | 完结 */
    @Column(nullable = false, length = 16)
    private String status = "连载";

    /** 上架 | 下架 */
    @Column(nullable = false, length = 16)
    private String shelf = "上架";

    @Column(nullable = false, length = 32)
    private String heat = "0";

    @Column(nullable = false, length = 512)
    private String description = "";

    @Column(nullable = false)
    private Integer chapters = 0;
}
