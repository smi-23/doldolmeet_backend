package com.doldolmeet.domain.team.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddIdolToTeamRequestDto {
    private String idolName;
    private String teamName;
}
