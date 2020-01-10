package uk.gov.justice.hmpps.probationteams.model;

import lombok.*;

import javax.persistence.*;

@Embeddable

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
@ToString(of = {"functionalMailbox"})
@EqualsAndHashCode(of = {"functionalMailbox"})

public class ProbationTeam {

    @Column(nullable = false)
    private String functionalMailbox;
}
