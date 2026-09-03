package com.carteira.carteiraInvestimento.application.service;
import com.carteira.carteiraInvestimento.application.port.UsuarioPort;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import java.util.UUID;
public class CurrentPrincipalService { private final UsuarioPort usuarios; public CurrentPrincipalService(UsuarioPort usuarios){this.usuarios=usuarios;} public Usuario current(UUID id){return usuarios.findById(id).orElseThrow(()->new IllegalStateException("user not found"));} }
