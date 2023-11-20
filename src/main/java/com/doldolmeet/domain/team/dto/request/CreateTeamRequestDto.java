package com.doldolmeet.domain.team.dto.request;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamRequestDto {
    private String teamName;
    private Integer teamSize;
    private String teamImg;
    private LocalDate debutDate;
}
