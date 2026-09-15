package com.carteira.carteiraInvestimento.infrastructure.persistence;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
class RepositoryUserDetailsService implements UserDetailsService {
	private final UsuarioJpaRepository usuarios;
	RepositoryUserDetailsService(UsuarioJpaRepository usuarios) { this.usuarios = usuarios; }
	@Override public UserDetails loadUserByUsername(String email) {
		UsuarioJpaEntity user = usuarios.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("user not found"));
		return new User(user.getEmail(), user.getSenhaHash(), user.isAtivo(), true, true, true, AuthorityUtils.createAuthorityList(user.getRole().name()));
	}
}
