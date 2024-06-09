package project.docutaskhub.dominio

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "boards")
class Board (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int?,

    @field:NotBlank(message = "Nome é obrigatório")
    var nome: String,

    @field:NotBlank(message = "Descrição é obrigatória")
    var descricao: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_id")
    var criadoPor: User,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuarios_quadros",
        joinColumns = [JoinColumn(name = "quadro_id")],
        inverseJoinColumns = [JoinColumn(name = "usuario_id")]
    )
    var usuarios: List<User>? = mutableListOf(),

    @OneToMany(mappedBy = "board", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var tasks: List<Task>? = mutableListOf()
) {

    constructor(nome: String, descricao: String, criadoPor: User, usuarios: List<User>?, tasks: List<Task>?) :
            this(null, nome, descricao, criadoPor)
}