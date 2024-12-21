package com.redhat.example.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class KRecord extends PanacheEntity {
    public Long key;
    public String value;
}
