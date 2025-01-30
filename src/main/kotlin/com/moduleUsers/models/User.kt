import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
data class User(val name: String = "", val email: String = "", var password: String? = null, var isActive: Boolean? = null)

object Users : IntIdTable() {
    val password = varchar("password", 255)
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val isActive = bool("isActive").default(true)
}