package com.example.traffic.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "CCTV_INFO")
public class CctvInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double coordX;
    private Double coordY;
    private Double minX;
    private Double maxX;
    private Double minY;
    private Double maxY;
    private String streamUrl;
    @Column(name = "LINE_NAME")
    private String lineName;
    @Column(name = "LOCATION_NAME")
    private String locationName;

}
