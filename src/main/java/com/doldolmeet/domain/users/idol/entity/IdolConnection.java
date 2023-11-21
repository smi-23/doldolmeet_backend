package com.doldolmeet.domain.users.idol.entity;

import com.doldolmeet.domain.commons.Role;
import com.doldolmeet.domain.commons.UserCommons;
import com.doldolmeet.domain.users.fan.entity.Fan;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class IdolConnection {
    // 아이돌 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idolConnectionId;

    @ManyToOne
    @JoinColumn(name = "idolId")
    private Idol idol;

    // 대기방에 들어온 팬
    @ManyToOne
    @JoinColumn(name = "fanId")
    private Fan fan;






//    public void setIdolConnection(Idol idolId, Fan fanId) {
//        this.idol = idolId;
//        this.fan = fanId;
//
//    }
}
