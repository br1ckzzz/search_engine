package com.example.web_search_engine.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "field")
@Data
@EqualsAndHashCode
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String selector;

    private float weight;
}
