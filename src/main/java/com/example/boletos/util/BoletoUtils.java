package com.example.boletos.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BoletoUtils {

    private static final LocalDate BASE_FATOR = LocalDate.of(1997, 10, 7);

    private BoletoUtils() {
    }

    public static String apenasNumeros(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replaceAll("\\D", "");
    }

    public static String leftPadZeros(String valor, int tamanho) {
        String v = valor == null ? "" : valor;
        if (v.length() >= tamanho) {
            return v.substring(0, tamanho);
        }
        return "0".repeat(tamanho - v.length()) + v;
    }

    public static String formatarValor(BigDecimal valor) {
        BigDecimal v = valor == null ? BigDecimal.ZERO : valor;
        String centavos = v.setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .toPlainString()
                .replace(".", "")
                .replace(",", "");
        return leftPadZeros(centavos, 10);
    }

    public static String calcularFatorVencimento(LocalDate vencimento) {
        long dias = ChronoUnit.DAYS.between(BASE_FATOR, vencimento);
        return leftPadZeros(String.valueOf(dias), 4);
    }

    public static int modulo10(String numero) {
        int soma = 0;
        int peso = 2;

        for (int i = numero.length() - 1; i >= 0; i--) {
            int num = Character.getNumericValue(numero.charAt(i));
            int mult = num * peso;
            if (mult > 9) {
                mult = (mult / 10) + (mult % 10);
            }
            soma += mult;
            peso = (peso == 2) ? 1 : 2;
        }

        int resto = soma % 10;
        return resto == 0 ? 0 : 10 - resto;
    }
    public static int modulo11Banco(String numero) {
        int soma = 0;
        int peso = 2;

        for (int i = numero.length() - 1; i >= 0; i--) {
            int num = Character.getNumericValue(numero.charAt(i));
            soma += num * peso;
            peso++;
            if (peso > 9) {
                peso = 2;
            }
        }

        int resto = soma % 11;
        int dv = 11 - resto;

        if (dv == 0 || dv == 10 || dv == 11) {
            return 1;
        }
        return dv;
    }

    public static String gerarLinhaDigitavel(String codigoBarras) {
        String campo1 = codigoBarras.substring(0, 4) + codigoBarras.substring(19, 24);
        campo1 = campo1 + modulo10(campo1);
        campo1 = campo1.substring(0, 5) + "." + campo1.substring(5);

        String campo2 = codigoBarras.substring(24, 34);
        campo2 = campo2 + modulo10(campo2);
        campo2 = campo2.substring(0, 5) + "." + campo2.substring(5);

        String campo3 = codigoBarras.substring(34, 44);
        campo3 = campo3 + modulo10(campo3);
        campo3 = campo3.substring(0, 5) + "." + campo3.substring(5);

        String campo4 = codigoBarras.substring(4, 5);
        String campo5 = codigoBarras.substring(5, 19);

        return campo1 + " " + campo2 + " " + campo3 + " " + campo4 + " " + campo5;
    }
}


