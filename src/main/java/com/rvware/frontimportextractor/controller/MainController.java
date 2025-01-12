package com.rvware.frontimportextractor.controller;

import com.rvsoftware.extractextrato.exception.BusinessException;
import com.rvsoftware.extractextrato.model.Transacao;
import com.rvsoftware.extractextrato.model.enums.BancoEnum;
import com.rvsoftware.extractextrato.service.ExporterService;
import com.rvsoftware.extractextrato.service.ExtractorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    private Map<String, ExtractorService> extractorServiceMap;
    private final ExporterService exporterService;

    public MainController(Map<String, ExtractorService> extractorServiceMap,
                          ExporterService exporterService) {
        this.extractorServiceMap = extractorServiceMap;
        this.exporterService = exporterService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/upload")
    public String upload() {
        return "upload";
    }

    @PostMapping("/upload")
    public ResponseEntity<byte[]> handleFileUpload(@RequestParam("file") MultipartFile multipartFile, Model model,
                                                   @RequestParam("banco") String bancoSelecionado) throws FileNotFoundException {

        //TODO - melhorar para listar os enums do html
        BancoEnum bancoEnum = getBancoEnum(bancoSelecionado);
        ExtractorService extractorService = extractorServiceMap.get(getQualifierServiceExtractor(bancoEnum));
        List<Transacao> transacao = extractorService.importarTransacoes(getFile(multipartFile));
        ByteArrayOutputStream byteArrayOutputStream = exporterService.gerarCsvToOutputStream(transacao);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=processed-file.csv");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/plain");
        return new ResponseEntity<>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
    }

    private String getQualifierServiceExtractor(BancoEnum bancoEnum) {
        Map<BancoEnum, String> map = new EnumMap<>(BancoEnum.class);
        map.put(BancoEnum.BTG_PACTUAL, ExtractorService.BTG_EXTRACTOR);
        map.put(BancoEnum.BANCO_DO_BRASIL, ExtractorService.BB_EXTRACTOR);

        return map.get(bancoEnum);
    }

    private BancoEnum getBancoEnum(String bancoSelecionado) {
        if (bancoSelecionado.equalsIgnoreCase("btg")) {
            return BancoEnum.BTG_PACTUAL;
        }

        if(bancoSelecionado.equalsIgnoreCase("bb")) {
            return BancoEnum.BANCO_DO_BRASIL;
        }
        throw new BusinessException("Banco não identificado");
    }

    private File getFile(MultipartFile multipartFile) {
        try {
            File tmpFile = File.createTempFile("upload-", ".temp");
            multipartFile.transferTo(tmpFile);
            tmpFile.deleteOnExit();
            return tmpFile;
        } catch (IOException e) {
            throw new BusinessException("Erro ao importar arquivo");
        }
    }

}
