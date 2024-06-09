package project.docutaskhub.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import project.docutaskhub.dominio.Board
import project.docutaskhub.dominio.User


@Repository
interface BoardRepository : JpaRepository<Board, Int> {

    fun findAllByCriadoPor(user: User): List<Board>
    fun findAllByUsuariosContains(user: User): List<Board>

}