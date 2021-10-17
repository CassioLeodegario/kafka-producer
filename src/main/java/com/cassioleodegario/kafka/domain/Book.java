package com.cassioleodegario.kafka.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Book {

    @NotNull(message = "Book id cannot be null")
    private Integer bookId;
    @NotBlank(message = "Book name cannot be empty")
    private String bookName;
    @NotBlank(message = "Book author cannot be empty")
    private String bookAuthor;

}
