package com.example.boletos.builder;

import com.example.boletos.util.BoletoUtils;
import org.springframework.stereotype.Component;

@Component("bradescoBuilder")
public class BradescoBoletoBuilder extends AbstractBoletoBuilder {

    @Override
    public void definirBanco() {
        boleto.setBanco("bradesco");
        boleto.setCodigoBanco("237");
        boleto.setNomeBanco("Bradesco");
    }

    @Override
    public void montarCampoLivre() {
        String agencia = BoletoUtils.leftPadZeros(boleto.getAgencia(), 4);
        String carteira = BoletoUtils.leftPadZeros(boleto.getCarteira(), 2);
        String nossoNumero = BoletoUtils.leftPadZeros(boleto.getNossoNumero(), 11);
        String conta = BoletoUtils.leftPadZeros(boleto.getConta(), 7);
        String zero = "0";

        String campoLivre = agencia + carteira + nossoNumero + conta + zero;
        boleto.setCampoLivre(BoletoUtils.leftPadZeros(campoLivre, 25));
    }
}