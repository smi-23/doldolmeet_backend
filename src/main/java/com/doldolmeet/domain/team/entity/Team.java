package com.doldolmeet.domain.team.entity;

import com.doldolmeet.domain.team.dto.request.CreateTeamRequestDto;
import com.doldolmeet.domain.users.idol.entity.Idol;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String teamName;

    @Column
    private Integer teamSize;

    @Column
    private String teamImg;

    @Column
    private LocalDate debutDate;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
//    @OrderBy(clause = "createdAt AS")
    private List<Idol> idols = new ArrayList<>();

    public Team(CreateTeamRequestDto requestDto) {
        this.teamName = requestDto.getTeamName();
        this.teamSize = requestDto.getTeamSize();
        this.teamImg = requestDto.getTeamImg();
        this.debutDate = requestDto.getDebutDate();
    }
}
