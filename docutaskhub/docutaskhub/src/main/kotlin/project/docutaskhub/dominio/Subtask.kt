package project.docutaskhub.dominio

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jdk.jshell.Snippet.Status
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "subtasks")
data class Subtask(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int,

    @field:NotBlank(message = "Título é obrigatório")
    var titulo: String,

    var descricao: String,

    @Enumerated(EnumType.STRING)
    var status: Status,

    @Column(name = "cor")
    var cor: String?,

    @Column(name = "data_criacao")
    var dataDeCriacao: LocalDateTime = LocalDateTime.now(),

    @Column(name = "data_atualizacao")
    var dataDeAtualizacao: LocalDateTime?,

    @Column(name = "data_vencimento")
    var dataDeVencimento: LocalDate?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    var task: Task,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    var criadoPor: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    var atribuidoPara: User,

    )