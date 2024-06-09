package project.docutaskhub.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import project.docutaskhub.dominio.Document

@Repository
interface DocumentRepository : JpaRepository<Document, Int> {

}