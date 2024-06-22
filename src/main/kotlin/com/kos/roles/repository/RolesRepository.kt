import com.kos.common.WithState
import com.kos.roles.Role

interface RolesRepository : WithState<List<Role>, RolesRepository> {
    suspend fun getRoles(): List<Role>
    suspend fun insertRole(role: Role): Unit
    suspend fun deleteRole(role: Role): Unit
}
