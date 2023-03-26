package com.hps.matchingsystem.service.impl.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private Integer userId;

    private Integer rating;

    private Integer waitingTime;  //玩家的等待时间
}
