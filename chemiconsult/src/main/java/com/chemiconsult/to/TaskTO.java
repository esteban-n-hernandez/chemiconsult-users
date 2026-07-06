package com.chemiconsult.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskTO {

    private Long id;
    private String title;
    private String description;
    private String status;
    private Long userId;
    private String userName;

}
