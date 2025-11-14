package com.pcs.app.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "trade")
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;
    String account;
    String type;
    Double buyQuantity;
    Double sellQuantity;
    Double buyPrice;
    Double sellPrice;
    String benchmark;
    Timestamp tradeDate;
    String security;
    String status;
    String trader;
    String book;
    String creationName;
    Timestamp creationDate;
    String revisionName;
    Timestamp revisionDate;
    String dealName;
    String dealType;
    String sourceListId;
    String side;

    public Trade(String account, String type) {
        this.account = account;
        this.type = type;
    }
}
