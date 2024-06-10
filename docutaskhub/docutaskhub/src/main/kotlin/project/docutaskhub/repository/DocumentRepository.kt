package project.docutaskhub.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import project.docutaskhub.dominio.Document
import java.util.*

@Repository
interface DocumentRepository : JpaRepository<Document, Int> {

    fun findAllByTaskBoardId(boardId: Int): List<Document>
    fun findByIdAndTaskBoardId(documentId: Int, boardId: Int): Optional<Document>
}