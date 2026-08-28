package com.abrarshakhi.envelope.engine.security

import com.abrarshakhi.envelope.engine.auth.entity.Role
import com.abrarshakhi.envelope.engine.auth.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal(
    val id: Long,
    private val username: String,
    val email: String,
    private val passwordHash: String,
    val role: Role,
    private val isEmailVerified: Boolean,
    private val isAccountNonLocked: Boolean,
    private val authorities: Collection<GrantedAuthority>,
) : UserDetails {

    companion object {
        fun create(user: User): UserPrincipal {
            val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
            return UserPrincipal(
                id = user.id ?: 0L,
                username = user.username,
                email = user.email,
                passwordHash = user.passwordHash,
                role = user.role,
                isEmailVerified = user.isEmailVerified,
                isAccountNonLocked = user.isAccountNonLocked,
                authorities = authorities,
            )
        }
    }

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities
    override fun getPassword(): String = passwordHash
    override fun getUsername(): String = username
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = isAccountNonLocked
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = isEmailVerified
}
