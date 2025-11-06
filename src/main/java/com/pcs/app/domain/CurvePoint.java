package com.pcs.app.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.sql.Timestamp;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "curvepoint")
public class CurvePoint {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;
    Integer curveId;
    Timestamp asOfDate;
    Double term;
    Double value;
    Timestamp creationDate;

    public CurvePoint(Integer curveId, Double term, Double value) {
        this.curveId = curveId;
        this.term = term;
        this.value = value;
    }
}
