package com.example.boletos.builder;

import com.example.boletos.model.Boleto;
import com.example.boletos.model.BoletoRequest;

public interface BoletoBuilder {

    void reset();

    void definirBanco();

    void definirDadosBasicos(BoletoRequest request);

    void montarCampoLivre();

    void calcularCodigoDeBarrasELinhaDigitavel();

    Boleto construir();
}