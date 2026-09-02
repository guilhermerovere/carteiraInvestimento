package com.carteira.carteiraInvestimento.application.port;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import java.util.Optional;
import java.util.UUID;
public interface UsuarioPort { Optional<Usuario> findByEmail(String email); Optional<Usuario> findById(UUID id); Usuario save(Usuario usuario); }
