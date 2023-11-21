package com.doldolmeet.domain.users.idol.entity;

import com.doldolmeet.domain.commons.Role;
import com.doldolmeet.domain.team.entity.Team;
import com.doldolmeet.domain.commons.UserCommons;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Idol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Embedded
    private UserCommons userCommons;

    @Column(nullable = false)
    private String stageName;

    @Column(nullable = false, unique = true)
    private String meetingRoomSession;

    @Column(nullable = false, unique = true)
    private String waitingRoomSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public void setTeam(Team team) {
        this.team = team;
        team.getIdols().add(this);
    }

    public void setUserCommons(String username, String password, Role role) {
        this.userCommons.setUsername(username);
        this.userCommons.setPassword(password);
        this.userCommons.setRole(role);
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }
}
