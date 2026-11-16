package com.back.global.jpa.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * 모든 엔티티가 공통으로 가지는 필드(id, 생성일, 수정일)를 모아둔 부모 클래스.
 * <p>
 * {@code @MappedSuperclass} : 이 클래스 자체는 테이블이 되지 않고, 상속받은 엔티티의 테이블에 칼럼만 추가된다.
 * {@code @EntityListeners(AuditingEntityListener.class)} : 저장/수정 시점에 createDate, modifyDate 를 자동으로 채운다.
 * (동작하려면 애플리케이션 클래스에 {@code @EnableJpaAuditing} 이 있어야 한다.)
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY) // INSERT 할 때 DB가 1, 2, 3... 순서로 번호를 매긴다.
    private int id;

    @CreatedDate
    private LocalDateTime createDate;

    @LastModifiedDate
    private LocalDateTime modifyDate;
}
