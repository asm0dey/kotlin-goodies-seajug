package com.example.bookservice

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface JdbcBookRepository : CrudRepository<Book, Long> {
    
    @Query("SELECT * FROM BOOKS WHERE UPPER(genre) = UPPER(:genre)")
    fun findByGenre(genre: String): List<Book>
}