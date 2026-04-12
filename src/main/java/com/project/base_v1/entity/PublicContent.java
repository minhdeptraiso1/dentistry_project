package com.project.base_v1.entity;

import com.project.base_v1.enums.PublicContentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;


import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "public_contents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicContent extends BaseAuditEntity {

    @Id
    UUID id;

    @Column(name = "ref_id")
    UUID refId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", nullable = false, length = 50)
    PublicContentType refType;

    @Column(length = 255)
    String slug;

    @Column(nullable = false, length = 255)
    String title;

    @Column(length = 255)
    String subtitle;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(columnDefinition = "TEXT")
    String content;

    @Column(name = "image_url", columnDefinition = "TEXT")
    String imageUrl;

    @Column(name = "sub_image_urls", columnDefinition = "TEXT")
    String subImageUrls;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    String thumbnailUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_data", columnDefinition = "jsonb")
    String extraData;

    @Column(nullable = false)
    Boolean active;

    @Column(nullable = false)
    Boolean featured;

    @Column(name = "sort_order", nullable = false)
    Integer sortOrder;
}