function formatarDataHoje() {
    const hoje = new Date();
    return hoje.toISOString().split("T")[0];
}

function formatarDataVencimento() {
    const hoje = new Date();
    hoje.setDate(hoje.getDate() + 10);
    return hoje.toISOString().split("T")[0];
}

function montarPayload() {
    document.getElementById("dataDocumento").value = formatarDataHoje();
    document.getElementById("dataVencimento").value = formatarDataVencimento();

    return {
        banco: document.getElementById("banco").value,
        beneficiario: document.getElementById("beneficiario").value,
        documentoBeneficiario: document.getElementById("documentoBeneficiario").value,
        agencia: document.getElementById("agencia").value,
        conta: document.getElementById("conta").value,
        carteira: document.getElementById("carteira").value,
        nossoNumero: document.getElementById("nossoNumero").value,
        pagador: document.getElementById("pagador").value,
        documentoPagador: document.getElementById("documentoPagador").value,
        valor: parseFloat(document.getElementById("valor").value),
        dataDocumento: document.getElementById("dataDocumento").value,
        dataVencimento: document.getElementById("dataVencimento").value,
        numeroDocumento: document.getElementById("numeroDocumento").value,
        especieDocumento: document.getElementById("especieDocumento").value,
        instrucoes: document.getElementById("instrucoes").value
    };
}

async function baixarPdf() {
    const payload = montarPayload();
    const resultado = document.getElementById("resultado");

    if (!payload.banco || !payload.beneficiario || !payload.pagador || !payload.valor) {
        resultado.innerText = "Preencha banco, beneficiário, pagador e valor.";
        return;
    }

    try {
        const response = await fetch("/api/boletos/download", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            resultado.innerText = "Erro ao gerar boleto.";
            return;
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `boleto_${payload.banco}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);

        resultado.innerText = "Boleto gerado com sucesso.";
    } catch (error) {
        resultado.innerText = "Erro ao conectar com o servidor.";
    }
}