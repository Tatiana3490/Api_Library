package com.svalero.Api_Library.DTO;

import lombok.Data;

@Data
public class BookDTO {
    private long id;
    private String title;
    private String genre;
    private boolean available;
    private String authorName;
    private String categoryName;

}
