package project.docutaskhub.dominio

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import project.docutaskhub.enums.DocumentType


@Entity
@Table(name = "documents")
data class Document(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @field:NotBlank(message = "Nome é obrigatório")
    var nome: String,

    @field:NotBlank(message = "Arquivo é obrigatório")
    var arquivo: ByteArray,

    @Enumerated(EnumType.STRING)
    var type: DocumentType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    var task: Task

)