package com.example.boletos.builder;

import com.example.boletos.model.Boleto;
import com.example.boletos.model.BoletoRequest;
import com.example.boletos.util.BoletoUtils;

public abstract class AbstractBoletoBuilder implements BoletoBuilder {

    protected Boleto boleto;

    @Override
    public void reset() {
        this.boleto = new Boleto();
    }

    @Override
    public void definirDadosBasicos(BoletoRequest request) {
        boleto.setBeneficiario(request.getBeneficiario());
        boleto.setDocumentoBeneficiario(request.getDocumentoBeneficiario());
        boleto.setAgencia(BoletoUtils.apenasNumeros(request.getAgencia()));
        boleto.setConta(BoletoUtils.apenasNumeros(request.getConta()));
        boleto.setCarteira(BoletoUtils.apenasNumeros(request.getCarteira()));
        boleto.setNossoNumero(BoletoUtils.apenasNumeros(request.getNossoNumero()));
        boleto.setPagador(request.getPagador());
        boleto.setDocumentoPagador(request.getDocumentoPagador());
        boleto.setValor(request.getValor());
        boleto.setDataDocumento(request.getDataDocumento());
        boleto.setDataVencimento(request.getDataVencimento());
        boleto.setNumeroDocumento(request.getNumeroDocumento());
        boleto.setEspecieDocumento(request.getEspecieDocumento());
        boleto.setInstrucoes(request.getInstrucoes());
    }

    @Override
    public void calcularCodigoDeBarrasELinhaDigitavel() {
        String moeda = "9";
        String fator = BoletoUtils.calcularFatorVencimento(boleto.getDataVencimento());
        String valor = BoletoUtils.formatarValor(boleto.getValor());

        String baseSemDv = boleto.getCodigoBanco() + moeda + fator + valor + boleto.getCampoLivre();
        int dvGeral = BoletoUtils.modulo11Banco(baseSemDv);
        String codigoBarras = boleto.getCodigoBanco() + moeda + dvGeral + fator + valor + boleto.getCampoLivre();

        boleto.setCodigoBarras(codigoBarras);
        boleto.setLinhaDigitavel(BoletoUtils.gerarLinhaDigitavel(codigoBarras));
    }

    @Override
    public Boleto construir() {
        return boleto;
    }
}