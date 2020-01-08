package uk.gov.justice.hmpps.probationteams.model;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)

@Table(name = "LOCAL_DELIVERY_UNIT2")

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
@EqualsAndHashCode(of = {"probationAreaCode", "localDeliveryUnitCode"})
@ToString(of = {"id", "probationAreaCode", "localDeliveryUnitCode", "functionalMailbox"})
public class LocalDeliveryUnit2 {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "LOCAL_DELIVERY_UNIT_ID", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String probationAreaCode;

    @Column(nullable = false)
    private String localDeliveryUnitCode;

    @Column
    private String functionalMailbox;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createDateTime;

    @CreatedBy
    @Column(nullable = false)
    private String createUserId;

    @LastModifiedDate
    private LocalDateTime modifyDateTime;

    @LastModifiedBy
    private String modifyUserId;
}
