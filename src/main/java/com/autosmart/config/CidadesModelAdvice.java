package com.autosmart.config;

import com.autosmart.model.Cidades;

import java.util.List;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CidadesModelAdvice {
    @ModelAttribute("cidades")
    List<String> cidades() {
        return Cidades.BAHIA;
    }

    @InitBinder void proteger(WebDataBinder binder) {
        binder.initDirectFieldAccess();
        binder.setDisallowedFields("id", "*.id", "status", "cliente", "cliente.*", "veiculo", "veiculo.*", "ativo", "responsavel");
    }
}
