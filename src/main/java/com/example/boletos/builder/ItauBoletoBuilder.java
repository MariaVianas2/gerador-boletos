package com.example.boletos.builder;

import com.example.boletos.util.BoletoUtils;
import org.springframework.stereotype.Component;

@Component("itauBuilder")
public class ItauBoletoBuilder extends AbstractBoletoBuilder {

    @Override
    public void definirBanco() {
        boleto.setBanco("itau");
        boleto.setCodigoBanco("341");
        boleto.setNomeBanco("Itaú");
    }

    @Override
    public void montarCampoLivre() {
        String carteira = BoletoUtils.leftPadZeros(boleto.getCarteira(), 3);
        String nossoNumero = BoletoUtils.leftPadZeros(boleto.getNossoNumero(), 8);
        String agencia = BoletoUtils.leftPadZeros(boleto.getAgencia(), 4);
        String conta = BoletoUtils.leftPadZeros(boleto.getConta(), 5);
        String dac = "0";
        String zerosFinais = "000";

        String campoLivre = carteira + nossoNumero + agencia + conta + dac + zerosFinais;
        boleto.setCampoLivre(BoletoUtils.leftPadZeros(campoLivre, 25));
    }
}