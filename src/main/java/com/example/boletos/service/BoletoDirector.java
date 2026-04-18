package com.example.boletos.service;

import com.example.boletos.builder.BoletoBuilder;
import com.example.boletos.model.Boleto;
import com.example.boletos.model.BoletoRequest;
import org.springframework.stereotype.Service;

@Service
public class BoletoDirector {

    public Boleto construir(BoletoBuilder builder, BoletoRequest request) {
        builder.reset();
        builder.definirBanco();
        builder.definirDadosBasicos(request);
        builder.montarCampoLivre();
        builder.calcularCodigoDeBarrasELinhaDigitavel();
        return builder.construir();
    }
}