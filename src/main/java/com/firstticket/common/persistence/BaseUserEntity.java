package com.firstticket.common.persistence;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Getter
@MappedSuperclass
@Access(AccessType.FIELD)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseUserEntity extends BaseEntity {

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    protected UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", insertable = false)
    protected UUID updatedBy;

    @Column(name = "deleted_by")
    protected UUID deletedBy;

    protected void delete(UUID deletedBy) {
        if (this.deletedAt != null) {
            return;
        }

        super.delete();
        this.deletedBy = deletedBy;
    }
}
