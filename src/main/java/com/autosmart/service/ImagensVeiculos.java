package com.autosmart.service;

import com.autosmart.model.Veiculo;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

/** Imagens ilustrativas locais por modelo, sem alterar URLs personalizadas no banco. */
@Component("imagensVeiculos")
public class ImagensVeiculos {
    private static final Map<String, String> FOTOS = Map.ofEntries( Map.entry("fiat argo", "fiat-argo"), Map.entry("chevrolet onix",
            "chevrolet-onix"), Map.entry("volkswagen t-cross", "volkswagen-t-cross"), Map.entry("toyota corolla", "toyota-corolla"),
            Map.entry("hyundai hb20", "hyundai-hb20"), Map.entry("fiat toro", "fiat-toro"), Map.entry("renault kwid", "renault-kwid"),
            Map.entry("honda civic", "honda-civic"), Map.entry("jeep renegade", "jeep-renegade"), Map.entry("toyota hilux", "toyota-hilux"),
            Map.entry("nissan kicks", "nissan-kicks"), Map.entry("volkswagen polo", "volkswagen-polo"), Map.entry("hyundai creta",
            "hyundai-creta"), Map.entry("fiat cronos", "fiat-cronos"), Map.entry("toyota yaris", "toyota-yaris"), Map.entry("honda hr-v",
            "honda-hr-v"), Map.entry("chevrolet tracker", "chevrolet-tracker"), Map.entry("volkswagen virtus", "volkswagen-virtus"),
            Map.entry("nissan versa", "nissan-versa"), Map.entry("fiat pulse", "fiat-pulse"), Map.entry("jeep compass", "jeep-compass"),
            Map.entry("hyundai hb20s", "hyundai-hb20s"), Map.entry("renault duster", "renault-duster"), Map.entry("chevrolet spin",
            "chevrolet-spin") );
    public String local(Veiculo v) {
        if (v == null) return "/img/car.svg";
        String chave = normalizar(v.marca) + " " + normalizar(v.modelo);
        String arquivo = FOTOS.get(chave);
        return arquivo == null ? "/img/car.svg" : "/img/veiculos/" + arquivo + ".webp";
    }
    /** Prioriza imagem informada pelo usuário; na ausência dela, usa a local. */
    public String principal(Veiculo v) {
        if (v != null && v.fotoUrl != null && !v.fotoUrl.isBlank()) return v.fotoUrl;
        return local(v);
    }
    private String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .toLowerCase(Locale.ROOT).strip().replaceAll("\\s+", " ");
    }
}
