package com.example.boletos.builder;

import com.example.boletos.util.BoletoUtils;
import org.springframework.stereotype.Component;

@Component("nubankBuilder")
public class NubankBoletoBuilder extends AbstractBoletoBuilder {

    @Override
    public void definirBanco() {
        boleto.setBanco("nubank");
        boleto.setCodigoBanco("260");
        boleto.setNomeBanco("Nu Pagamentos / Nubank");
    }

    @Override
    public void montarCampoLivre() {
        String agencia = BoletoUtils.leftPadZeros(boleto.getAgencia(), 4);
        String conta = BoletoUtils.leftPadZeros(boleto.getConta(), 8);
        String nossoNumero = BoletoUtils.leftPadZeros(boleto.getNossoNumero(), 12);
        String complemento = "0";

        String campoLivre = agencia + conta + nossoNumero + complemento;
        boleto.setCampoLivre(BoletoUtils.leftPadZeros(campoLivre, 25));
    }
}