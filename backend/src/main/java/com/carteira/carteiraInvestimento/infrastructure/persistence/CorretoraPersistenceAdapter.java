package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.application.port.CorretoraPage;
import com.carteira.carteiraInvestimento.application.port.CorretoraPort;
import com.carteira.carteiraInvestimento.application.port.CorretoraReadScope;
import com.carteira.carteiraInvestimento.application.service.DuplicateBrokerException;
import com.carteira.carteiraInvestimento.domain.broker.Corretora;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class CorretoraPersistenceAdapter implements CorretoraPort {
    private static final String UNIQUE = "uk_corretoras_cnpj";
    private final CorretoraJpaRepository repository;
    public CorretoraPersistenceAdapter(CorretoraJpaRepository repository) { this.repository = repository; }
    @Override public boolean existsByCnpj(String cnpj) { return repository.existsByCnpj(cnpj); }
    @Override public Corretora saveAndFlush(Corretora broker) {
        try { return domain(repository.saveAndFlush(entity(broker))); }
        catch (DataIntegrityViolationException exception) {
            if (unique(exception)) throw new DuplicateBrokerException(exception);
            throw exception;
        }
    }
    @Override public Optional<Corretora> findById(UUID id, CorretoraReadScope scope) {
        return (scope == CorretoraReadScope.ACTIVE_ONLY ? repository.findByIdAndAtivoTrue(id) : repository.findById(id)).map(this::domain);
    }
    @Override public Optional<Corretora> findByCnpj(String cnpj, CorretoraReadScope scope) {
        return (scope == CorretoraReadScope.ACTIVE_ONLY ? repository.findByCnpjAndAtivoTrue(cnpj) : repository.findByCnpj(cnpj)).map(this::domain);
    }
    @Override public CorretoraPage list(int page, int size, CorretoraReadScope scope) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("razaoSocial"), Sort.Order.asc("id")));
        var result = scope == CorretoraReadScope.ACTIVE_ONLY
                ? repository.findAll((root, query, cb) -> cb.isTrue(root.get("ativo")), pageable)
                : repository.findAll(pageable);
        return new CorretoraPage(result.getContent().stream().map(this::domain).toList(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
    private boolean unique(Throwable failure) {
        for (Throwable current=failure; current!=null; current=current.getCause())
            if (current instanceof ConstraintViolationException violation && UNIQUE.equals(violation.getConstraintName())) return true;
        return false;
    }
    private CorretoraJpaEntity entity(Corretora b) { return new CorretoraJpaEntity(b.id(),b.cnpj(),b.razaoSocial(),b.nomeFantasia(),b.cep(),b.logradouro(),b.bairro(),b.cidade(),b.uf(),b.numero(),b.complemento(),b.ativo(),b.criadoEm(),b.atualizadoEm()); }
    private Corretora domain(CorretoraJpaEntity b) { return new Corretora(b.getId(),b.getCnpj(),b.getRazaoSocial(),b.getNomeFantasia(),b.getCep(),b.getLogradouro(),b.getBairro(),b.getCidade(),b.getUf(),b.getNumero(),b.getComplemento(),b.isAtivo(),b.getCriadoEm(),b.getAtualizadoEm()); }
}
