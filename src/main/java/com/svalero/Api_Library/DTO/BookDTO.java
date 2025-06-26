package com.svalero.Api_Library.DTO;

import com.svalero.Api_Library.domain.BookCategory;
import lombok.Data;

@Data
public class BookDTO {
    private long id;
    private String title;
    private String genre;
    private boolean available;
    private AuthorDTO author;
    private BookCategoryDTO category;

}
