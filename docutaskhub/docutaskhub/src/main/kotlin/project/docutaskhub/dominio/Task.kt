package project.docutaskhub.dominio

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import project.docutaskhub.enums.Status
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "tasks")
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @field:NotBlank(message = "Título é obrigatório")
    var titulo: String,

    var descricao: String,

    @Enumerated(EnumType.STRING)
    var status: Status,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    var board: Board,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    var criadoPor: User,

    @Column(name = "cor")
    var cor: String?,

    @Column(name = "data_criacao")
    var dataDeCriacao: LocalDateTime = LocalDateTime.now(),

    @Column(name = "data_atualizacao")
    var dataDeAtualizacao: LocalDateTime?,

    @Column(name = "data_vencimento")
    var dataDeVencimento: LocalDate? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    var atribuidoPara: User,

    @OneToMany(mappedBy = "task", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var documentos: List<Document> = mutableListOf()
){
    // Construtor secundário para criar instâncias sem especificar o ID
    constructor(
        titulo: String, descricao: String, status: Status, board: Board, criadoPor: User,
        atribuidoPara: User, cor: String? = null, dataDeCriacao: LocalDateTime = LocalDateTime.now(),
        dataDeAtualizacao: LocalDateTime? = null, dataDeVencimento: LocalDate? = null,
        documentos: List<Document> = mutableListOf()
    ) : this(null, titulo, descricao, status, board, criadoPor, cor, dataDeCriacao, dataDeAtualizacao,
        dataDeVencimento, atribuidoPara, documentos)
}
