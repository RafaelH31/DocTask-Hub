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
    var id: Int,

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
    var dataDeVencimento: LocalDate?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    var atribuidoPara: User,

    @OneToMany(mappedBy = "task", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var subtarefas: List<Subtask> = mutableListOf(),

    @OneToMany(mappedBy = "task", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var documentos: List<Document> = mutableListOf()
)
