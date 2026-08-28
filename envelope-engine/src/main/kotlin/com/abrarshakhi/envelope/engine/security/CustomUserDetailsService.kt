package com.abrarshakhi.envelope.engine.security

import com.abrarshakhi.envelope.engine.auth.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(identifier: String): UserDetails {
        val user = userRepository.findByUsernameOrEmailIgnoreCase(identifier)
            .orElseThrow {
                UsernameNotFoundException("User not found with username or email: $identifier")
            }
        return UserPrincipal.create(user)
    }

    @Transactional(readOnly = true)
    fun loadUserById(id: Long): UserDetails {
        val user = userRepository.findById(id)
            .orElseThrow {
                UsernameNotFoundException("User not found with id: $id")
            }
        return UserPrincipal.create(user)
    }
}
