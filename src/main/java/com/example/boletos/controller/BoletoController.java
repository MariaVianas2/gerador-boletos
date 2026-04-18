package com.example.boletos.controller;

import com.example.boletos.model.Boleto;
import com.example.boletos.model.BoletoRequest;
import com.example.boletos.service.BoletoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boletos")
@CrossOrigin(origins = "*")
public class BoletoController {

    private final BoletoService boletoService;

    public BoletoController(BoletoService boletoService) {
        this.boletoService = boletoService;
    }

    @PostMapping("/gerar")
    public Boleto gerar(@RequestBody BoletoRequest request) {
        return boletoService.gerarBoleto(request);
    }

    @PostMapping("/download")
    public ResponseEntity<byte[]> download(@RequestBody BoletoRequest request) {
        Boleto boleto = boletoService.gerarBoleto(request);
        byte[] pdf = boletoService.gerarPdf(boleto);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=boleto_" + boleto.getBanco() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}