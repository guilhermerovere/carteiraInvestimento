package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.application.port.CarteiraPort;
import com.carteira.carteiraInvestimento.application.port.UsuarioPort;
import com.carteira.carteiraInvestimento.application.service.DuplicateEmailException;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import com.carteira.carteiraInvestimento.domain.wallet.Carteira;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
public class IdentityPersistenceAdapter implements UsuarioPort, CarteiraPort {
	private static final String CANONICAL_EMAIL_UNIQUE_CONSTRAINT = "ux_usuarios_email_canonico";

	private final UsuarioJpaRepository usuarios;
	private final CarteiraJpaRepository carteiras;

	public IdentityPersistenceAdapter(UsuarioJpaRepository usuarios, CarteiraJpaRepository carteiras) {
		this.usuarios = usuarios;
		this.carteiras = carteiras;
	}

	@Override
	public Optional<Usuario> findByEmail(String email) {
		return usuarios.findByEmail(email).map(this::toDomain);
	}

	@Override
	public Optional<Usuario> findById(UUID id) {
		return usuarios.findById(id).map(this::toDomain);
	}

	@Override
	public Usuario save(Usuario usuario) {
		try {
			usuarios.saveAndFlush(toEntity(usuario));
			return usuario;
		} catch (DataIntegrityViolationException exception) {
			if (isCanonicalEmailUniqueViolation(exception)) {
				throw new DuplicateEmailException(exception);
			}
			throw exception;
		}
	}

	@Override
	public Carteira save(Carteira carteira) {
		carteiras.save(new CarteiraJpaEntity(carteira.id(), carteira.usuarioId(), carteira.nome(),
				carteira.saldoCaixaBrl(), carteira.dataCriacao()));
		return carteira;
	}

	private UsuarioJpaEntity toEntity(Usuario usuario) {
		return new UsuarioJpaEntity(usuario.id(), usuario.nome(), usuario.email(), usuario.senhaHash(), usuario.role(),
				usuario.ativo(), usuario.criadoEm(), usuario.atualizadoEm());
	}

	private boolean isCanonicalEmailUniqueViolation(Throwable failure) {
		for (Throwable current = failure; current != null; current = current.getCause()) {
			if (current instanceof ConstraintViolationException violation
					&& CANONICAL_EMAIL_UNIQUE_CONSTRAINT.equals(violation.getConstraintName())) {
				return true;
			}
		}
		return false;
	}

	private Usuario toDomain(UsuarioJpaEntity entity) {
		return new Usuario(entity.getId(), entity.getNome(), entity.getEmail(), entity.getSenhaHash(), entity.getRole(),
				entity.isAtivo(), entity.getCriadoEm(), entity.getAtualizadoEm());
	}
}
