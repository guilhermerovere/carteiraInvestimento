package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.application.port.*;
import com.carteira.carteiraInvestimento.domain.broker.CnpjCanonicalizer;
import com.carteira.carteiraInvestimento.domain.broker.Corretora;
import java.time.Clock;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrokerApplicationService implements BrokerUseCase {
    private final CorretoraPort brokers; private final ReceitaFederalPort receita; private final CvmPort cvm;
    private final ViaCepPort viaCep; private final BrokerLocalTransactionService local; private final AuditoriaIsoladaPort isolatedAudit; private final Clock clock;
    public BrokerApplicationService(CorretoraPort brokers, ReceitaFederalPort receita, CvmPort cvm, ViaCepPort viaCep,
            BrokerLocalTransactionService local, AuditoriaIsoladaPort isolatedAudit, Clock clock) {
        this.brokers=brokers; this.receita=receita; this.cvm=cvm; this.viaCep=viaCep; this.local=local; this.isolatedAudit=isolatedAudit; this.clock=clock;
    }
    @Override
    public Corretora criar(String input, String numero, String complemento, UUID actor, UUID correlation) {
        String cnpj=CnpjCanonicalizer.canonicalize(input);
        numero=Corretora.normalizeNumero(numero); complemento=Corretora.normalizeComplemento(complemento);
        if (brokers.existsByCnpj(cnpj)) throw new DuplicateBrokerException();
        CadastroCnpj company = receita.consultar(cnpj).orElseThrow(() -> reject("cnpj-not-found", actor, correlation));
        String revenueStatus=normal(company.situacao());
        if (revenueStatus.isEmpty()) throw new BrokerUpstreamException("Invalid CNPJ provider response");
        if (!"ATIVA".equals(revenueStatus)) throw reject("receita-inactive", actor, correlation);
        RegistroCvm registration = cvm.consultar(cnpj).orElseThrow(() -> reject("cvm-not-found", actor, correlation));
        if (!registration.ativo()) throw reject("cvm-inactive", actor, correlation);
        try {
            if (!cnpj.equals(CnpjCanonicalizer.canonicalize(company.cnpj())) || !cnpj.equals(CnpjCanonicalizer.canonicalize(registration.cnpj())))
                throw new BrokerUpstreamException("Inconsistent upstream identity");
            String officialCep = canonicalCep(company.cep());
            EnderecoPostal address = viaCep.consultar(officialCep);
            if (!officialCep.equals(canonicalCep(address.cep()))) throw new BrokerUpstreamException("Inconsistent upstream address");
            Corretora broker = Corretora.nova(cnpj, company.razaoSocial(), company.nomeFantasia(), officialCep,
                    address.logradouro(), address.bairro(), address.cidade(), address.uf(), numero, complemento, clock);
            return local.create(broker, actor, correlation);
        } catch (BrokerUpstreamException exception) { throw exception; }
        catch (IllegalArgumentException exception) { throw new BrokerUpstreamException("Invalid upstream payload", exception); }
    }
    @Override @Transactional(readOnly=true) public CorretoraPage listar(int page,int size,CorretoraReadScope scope){ if(page<0||size<1||size>100)throw new IllegalArgumentException("invalid pagination"); return brokers.list(page,size,scope); }
    @Override @Transactional(readOnly=true) public Corretora consultar(UUID id,CorretoraReadScope scope){ return brokers.findById(id,scope).orElseThrow(BrokerNotFoundException::new); }
    @Override @Transactional(readOnly=true) public Corretora consultarPorCnpj(String value,CorretoraReadScope scope){ String cnpj=CnpjCanonicalizer.canonicalPath(value); return brokers.findByCnpj(cnpj,scope).orElseThrow(BrokerNotFoundException::new); }
    @Override public Corretora editar(UUID id,BrokerPatch patch,UUID actor,UUID correlation){ if(!patch.numeroPresent()&&!patch.complementoPresent())throw new IllegalArgumentException("empty patch"); if(patch.numeroPresent())Corretora.normalizeNumero(patch.numero()); if(patch.complementoPresent())Corretora.normalizeComplemento(patch.complemento()); return local.edit(id,patch,actor,correlation); }
    @Override public Corretora definirLifecycle(UUID id,boolean active,UUID actor,UUID correlation){ return local.lifecycle(id,active,actor,correlation); }
    private BrokerComplianceException reject(String reason,UUID actor,UUID correlation){
        try { isolatedAudit.recordIsoladamente(new AuditoriaCommand(actor,TipoEvento.CORRETORA_COMPLIANCE_REJEITADA,ResultadoAuditoria.FALHA,SeveridadeAuditoria.AVISO,"/api/v1/corretoras",correlation)); }
        catch(RuntimeException failure){ throw new BrokerAuditException(failure); }
        return new BrokerComplianceException(reason);
    }
    private String canonicalCep(String value){ String result=value==null?"":value.replaceAll("\\D",""); if(!result.matches("[0-9]{8}"))throw new BrokerUpstreamException("Invalid upstream CEP"); return result; }
    private String normal(String value){ return value==null?"":value.trim().toUpperCase(Locale.ROOT); }
}
