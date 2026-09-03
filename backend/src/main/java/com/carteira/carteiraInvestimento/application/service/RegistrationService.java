package com.carteira.carteiraInvestimento.application.service;
import com.carteira.carteiraInvestimento.application.port.*;
import com.carteira.carteiraInvestimento.domain.identity.*;
import com.carteira.carteiraInvestimento.domain.wallet.Carteira;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class RegistrationService {
 private final UsuarioPort usuarios; private final CarteiraPort carteiras; private final AuditoriaPort auditoria; private final PasswordHasher passwordHasher;
 public RegistrationService(UsuarioPort u, CarteiraPort c, AuditoriaPort a, PasswordHasher p) { usuarios=u; carteiras=c; auditoria=a; passwordHasher=p; }
 @Transactional public Usuario register(String nome, String email, String password, UUID correlationId) {
  String canonical=EmailCanonicalizer.canonicalize(email); PasswordPolicy.validate(password);
  if (usuarios.findByEmail(canonical).isPresent()) throw new DuplicateEmailException();
  Usuario user=Usuario.novoUsuario(nome, canonical, passwordHasher.hash(password), Role.ROLE_USER); usuarios.save(user); carteiras.save(Carteira.principal(user.id()));
  auditoria.record(new AuditoriaCommand(user.id(), TipoEvento.CADASTRO_USUARIO, ResultadoAuditoria.SUCESSO, SeveridadeAuditoria.INFO, "/api/v1/auth/register", correlationId)); return user;
 }
}
