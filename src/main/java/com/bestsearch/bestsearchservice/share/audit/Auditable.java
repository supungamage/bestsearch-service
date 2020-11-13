package com.bestsearch.bestsearchservice.share.audit;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class Auditable<U> {

	@CreatedBy
	protected U createdBy;

	@CreatedDate
	protected LocalDateTime createdAt;

	@LastModifiedBy
	protected U modifiedBy;

	@LastModifiedDate
	protected LocalDateTime modifiedAt;
}
