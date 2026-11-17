package com.autosmart.config;

import com.autosmart.model.Alerta;
import com.autosmart.model.Auditoria;
import com.autosmart.model.Cliente;
import com.autosmart.model.Oportunidade;
import com.autosmart.model.OrdemServico;
import com.autosmart.model.PerfilCompra;
import com.autosmart.model.Proposta;
import com.autosmart.model.TestDrive;
import com.autosmart.model.Usuario;
import com.autosmart.model.Veiculo;
import com.autosmart.model.Venda;
import com.autosmart.repository.AlertaRepo;
import com.autosmart.repository.AuditoriaRepo;
import com.autosmart.repository.ClienteRepo;
import com.autosmart.repository.OportunidadeRepo;
import com.autosmart.repository.OrdemRepo;
import com.autosmart.repository.PerfilRepo;
import com.autosmart.repository.PropostaRepo;
import com.autosmart.repository.TestDriveRepo;
import com.autosmart.repository.UsuarioRepo;
import com.autosmart.repository.VeiculoRepo;
import com.autosmart.repository.VendaRepo;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.*;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Dados de demonstração. Idempotente: pode rodar várias vezes sem duplicar. */
@Component
@Order(2)
@ConditionalOnProperty(name = "autosmart.demo.enabled", havingValue = "true")
public class DadosDemo implements CommandLineRunner {
    static final String MARCA = "Dados de demonstração carregados";
    private final UsuarioRepo us;
    private final ClienteRepo cs;
    private final VeiculoRepo vs;
    private final PerfilRepo pf;
    private final TestDriveRepo td;
    private final PropostaRepo ps;
    private final VendaRepo vd;
    private final AuditoriaRepo au;
    private final PasswordEncoder enc;
    DadosDemo(UsuarioRepo us, ClienteRepo cs, VeiculoRepo vs, PerfilRepo pf, TestDriveRepo td, PropostaRepo ps, VendaRepo vd,
            AuditoriaRepo au, PasswordEncoder enc) {
        this.us = us;
        this.cs = cs;
        this.vs = vs;
        this.pf = pf;
        this.td = td;
        this.ps = ps;
        this.vd = vd;
        this.au = au;
        this.enc = enc;
    }

    @Override public void run(String... args) {
        user("Administrador", "admin@autosmart.com", "admin123", Usuario.Perfil.ADMIN);
        user("Gerente Comercial", "gerente@autosmart.com", "gerente123", Usuario.Perfil.GERENTE);
        user("Vendedor", "vendedor@autosmart.com", "vendedor123", Usuario.Perfil.VENDEDOR);
        user("Vendedora 2", "vendedor2@autosmart.com", "vendedor123", Usuario.Perfil.VENDEDOR);
        user("Consultor de Serviços", "consultor@autosmart.com", "consultor123", Usuario.Perfil.CONSULTOR);
        user("Mecânico", "mecanico@autosmart.com", "mecanico123", Usuario.Perfil.MECANICO);
        String[][] carros = {
            {
                "Fiat","Argo","Drive 1.3","2024","Branco","12000","Hatch","Manual","5","13.5","68900","Salvador"
            },
            {
                "Chevrolet","Onix","LT Turbo","2025","Prata","5000","Hatch","Automático","5","14.2","92500","Lauro de Freitas"
            },
            {
                "Volkswagen","T-Cross","Comfortline","2024","Cinza","18000","SUV","Automático","5","12.4","128000","Camaçari"
            },
            {
                "Toyota","Corolla","XEi 2.0","2023","Preto","30000","Sedan","Automático","5","11.8","139000","Feira de Santana"
            },
            {
                "Hyundai","HB20","Sense 1.0","2023","Vermelho","22000","Hatch","Manual","5","14.8","72000","Vitória da Conquista"
            },
            {
                "Fiat","Toro","Freedom","2024","Azul","15000","Picape","Automático","5","10.9","165000","Ilhéus"
            },
            {
                "Renault","Kwid","Zen 1.0","2024","Branco","8000","Hatch","Manual","5","15.2","62900","Feira de Santana"
            },
            {
                "Honda","Civic","EXL 2.0","2023","Cinza","26000","Sedan","Automático","5","12.1","148000","Salvador"
            },
            {
                "Jeep","Renegade","Longitude","2024","Verde","14000","SUV","Automático","5","11.5","135000","Vitória da Conquista"
            },
            {
                "Toyota","Hilux","SR 2.8","2023","Branco","41000","Picape","Automático","5","9.8","245000","Barreiras"
            },
            {
                "Nissan","Kicks","Advance","2024","Laranja","9000","SUV","Automático","5","12.8","119000","Lauro de Freitas"
            },
            {
                "Volkswagen","Polo","Track 1.0","2025","Prata","3000","Hatch","Manual","5","14.0","74900","Camaçari"
            },
            {
                "Hyundai","Creta","Comfort","2024","Preto","11000","SUV","Automático","5","12.0","124000","Ilhéus"
            },
            {
                "Fiat","Cronos","Drive 1.3","2024","Vermelho","10000","Sedan","Manual","5","14.5","89900","Alagoinhas"
            },
            {
                "Toyota","Yaris","XL 1.5","2024","Prata","18000","Hatch","Automático","5","13.8","98900","Salvador"
            },
            {
                "Honda","HR-V","EX 1.5","2023","Branco","29000","SUV","Automático","5","12.1","137900","Feira de Santana"
            },
            {
                "Chevrolet","Tracker","LT 1.0 Turbo","2024","Azul","16000","SUV","Automático","5","12.4","119900","Salvador"
            },
            {
                "Volkswagen","Virtus","Comfortline 200 TSI","2024","Cinza","21000","Sedan","Automático","5","13.0","114900","Lauro de Freitas"
            },
            {
                "Nissan","Versa","Advance 1.6","2023","Branco","25000","Sedan","Automático","5","12.7","101900","Camaçari"
            },
            {
                "Fiat","Pulse","Audace 1.0 Turbo","2024","Preto","14000","SUV","Automático","5","12.5","109900","Ilhéus"
            },
            {
                "Jeep","Compass","Longitude T270","2023","Cinza","32000","SUV","Automático","5","10.5","154900","Salvador"
            },
            {
                "Hyundai","HB20S","Comfort Plus 1.0","2024","Prata","12000","Sedan","Manual","5","13.4","87900","Feira de Santana"
            },
            {
                "Renault","Duster","Iconic 1.6","2023","Branco","27000","SUV","Automático","5","11.3","112900","Vitória da Conquista"
            },
            {
                "Chevrolet","Spin","Premier 1.8","2023","Prata","35000","SUV","Automático","7","10.1","112900","Salvador"
            }
        };
        for (String[] c : carros) {
            if (vs.existsByModeloAndVersao(c[1], c[2])) continue;
            Veiculo v = new Veiculo();
            v.marca = c[0];
            v.modelo = c[1];
            v.versao = c[2];
            v.ano = Integer.valueOf(c[3]);
            v.cor = c[4];
            v.km = Integer.valueOf(c[5]);
            v.categoria = c[6];
            v.cambio = c[7];
            v.lugares = Integer.valueOf(c[8]);
            v.consumo = Double.valueOf(c[9]);
            v.preco = new BigDecimal(c[10]);
            v.cidade = c[11];
            v.localizacao = "Pátio " + c[11];
            v.finalidades = "Hatch".equals(v.categoria) ? "Cidade,Família" :
            ("SUV".equals(v.categoria) || "Sedan".equals(v.categoria) ? "Família,Viagens,Cidade" : "Trabalho,Viagens");
            v.caracteristicas = v.consumo >= 13 ? "Econômico,Ar-condicionado" : "Ar-condicionado";
            vs.save(v);
        }
        String[][] pessoas = {
            {
                "Ana Beatriz Santos","100.000.000-01","(71) 99100-0001","Salvador","A"
            },
            {
                "Carlos Eduardo Lima","100.000.000-02","(75) 99100-0002","Feira de Santana","A"
            },
            {
                "Juliana Almeida","100.000.000-03","(77) 99100-0003","Vitória da Conquista","A"
            },
            {
                "Roberto Nascimento","100.000.000-04","(71) 99100-0004","Camaçari","A"
            },
            {
                "Fernanda Oliveira","100.000.000-05","(71) 99100-0005","Lauro de Freitas","A"
            },
            {
                "Marcos Vinícius Costa","100.000.000-06","(73) 99100-0006","Ilhéus","A"
            },
            {
                "Patrícia Souza","100.000.000-07","(74) 99100-0007","Juazeiro","A"
            },
            {
                "Rafael Mendes","100.000.000-08","(71) 99100-0008","Simões Filho","A"
            },
            {
                "Luciana Barbosa","100.000.000-09","(73) 99100-0009","Itabuna","A"
            },
            {
                "Thiago Ferreira","100.000.000-10","(73) 99100-0010","Porto Seguro","I"
            }
        };
        for (String[] p : pessoas) {
            if (cs.existsByCpf(p[1])) continue;
            Cliente c = new Cliente();
            c.nome = p[0];
            c.cpf = p[1];
            c.telefone = p[2];
            c.cidade = p[3];
            c.ativo = p[4].equals("A");
            c.email = Normalizer.normalize(p[0].split(" ")[0].toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "") + "@exemplo.com";
            cs.save(c);
        }
        if (au.existsByAcao(MARCA)) return;
        perfil("100.000.000-01", "95000", "Cidade", "Hatch", "Automático", 4, 12.0);
        perfil("100.000.000-02", "75000", "Família", "Hatch", "Manual", 5, 13.0);
        perfil("100.000.000-03", "140000", "Viagens", "Sedan", "Automático", 5, 11.0);
        perfil("100.000.000-04", "130000", "Família", "SUV", "Automático", 5, 11.0);
        perfil("100.000.000-07", "90000", "Cidade", "Sedan", "Manual", 5, 13.0);
        LocalDate amanha = LocalDate.now().plusDays(1);
        drive("100.000.000-01", "Onix", "vendedor@autosmart.com", amanha.atTime(9, 0));
        drive("100.000.000-04", "T-Cross", "vendedor2@autosmart.com", amanha.atTime(10, 30));
        drive("100.000.000-07", "Cronos", "vendedor@autosmart.com", amanha.atTime(14, 0));
        drive("100.000.000-08", "HB20", "vendedor2@autosmart.com", amanha.plusDays(1).atTime(11, 0));
        proposta("100.000.000-02", "Polo", "1500", "Tapetes e película", 7, "vendedor@autosmart.com", Proposta.Status.ABERTA, 0);
        proposta("100.000.000-03", "Corolla", "3000", "Kit multimídia", 7, "vendedor@autosmart.com", Proposta.Status.APROVADA, 5);
        proposta("100.000.000-04", "T-Cross", "0", "", 7, "vendedor2@autosmart.com", Proposta.Status.RECUSADA, 0);
        proposta("100.000.000-05", "Kwid", "0", "", 7, "vendedor2@autosmart.com", Proposta.Status.APROVADA, 3);
        proposta("100.000.000-06", "Civic", "5000", "Película e proteção de pintura", 10, "vendedor@autosmart.com", Proposta.Status.ABERTA, 0);
        audit("admin@autosmart.com", MARCA);
        op("100.000.000-01", "Site", "Quer test-drive do Onix", Oportunidade.Etapa.TEST_DRIVE, 1);
        op("100.000.000-08", "Indicação", "Pesquisando hatch econômico", Oportunidade.Etapa.CONTATO, 3);
        op("100.000.000-09", "WhatsApp", "Primeiro contato", Oportunidade.Etapa.NOVO, 0);
        op("100.000.000-06", "Loja", "Aguardando aprovação da proposta", Oportunidade.Etapa.PROPOSTA, 2);
        os("100.000.000-03", "Corolla", "Revisão de 10.000 km", "Desgaste normal", "Troca de óleo e filtros", "Óleo 5W30, filtro de óleo",
                "480.00", OrdemServico.Status.ENCERRADA);
        os("100.000.000-04", "T-Cross", "Barulho na suspensão", "Bucha da bandeja desgastada", "Troca da bucha", "Bucha da bandeja",
                "650.00", OrdemServico.Status.EM_ANDAMENTO);
        alerta("PÓS-VENDA", "Revisão do Corolla (cliente Juliana Almeida)", 30);
        alerta("COMERCIAL", "Retornar contato com Luciana Barbosa", 0);
    }
    @org.springframework.beans.factory.annotation.Autowired OportunidadeRepo ops;
    @org.springframework.beans.factory.annotation.Autowired OrdemRepo oss;
    @org.springframework.beans.factory.annotation.Autowired AlertaRepo als;
    private void op(String cpf, String origem, String obs, Oportunidade.Etapa e, int dias) {
        Oportunidade o = new Oportunidade();
        o.cliente = cli(cpf);
        o.origem = origem;
        o.observacoes = obs;
        o.etapa = e;
        o.proximaAcao = LocalDate.now().plusDays(dias);
        o.responsavel = "vendedor@autosmart.com";
        ops.save(o);
    }
    private void os(String cpf, String modelo, String desc, String diag, String ativ, String pecas, String valor, OrdemServico.Status st) {
        OrdemServico o = new OrdemServico();
        o.cliente = cli(cpf);
        o.veiculo = car(modelo);
        o.descricao = desc;
        o.diagnostico = diag;
        o.atividades = ativ;
        o.pecas = pecas;
        o.valor = new BigDecimal(valor);
        o.status = st;
        o.abertura = LocalDate.now().minusDays(2);
        o.responsavel = "consultor@autosmart.com";
        oss.save(o);
    }
    private void alerta(String tipo, String msg, int dias) {
        Alerta a = new Alerta();
        a.tipo = tipo;
        a.mensagem = msg;
        a.data = LocalDate.now().plusDays(dias);
        a.responsavel = "vendedor@autosmart.com";
        als.save(a);
    }
    private void user(String nome, String email, String senha, Usuario.Perfil perfil) {
        if (us.findByEmail(email).isPresent()) return;
        Usuario u = new Usuario();
        u.nome = nome;
        u.email = email;
        u.senha = enc.encode(senha);
        u.perfil = perfil;
        us.save(u);
    }
    private Cliente cli(String cpf) {
        return cs.findAll().stream().filter(c -> cpf.equals(c.cpf)).findFirst().orElseThrow();
    }
    private Veiculo car(String modelo) {
        return vs.findAll().stream().filter(v -> modelo.equals(v.modelo)).findFirst().orElseThrow();
    }
    private void audit(String usuario, String acao) {
        Auditoria a = new Auditoria();
        a.usuario = usuario;
        a.acao = acao;
        a.data = LocalDateTime.now();
        au.save(a);
    }
    private void perfil(String cpf, String orc, String fin, String cat, String cambio, int ocup, double cons) {
        PerfilCompra p = new PerfilCompra();
        p.cliente = cli(cpf);
        p.orcamento = new BigDecimal(orc);
        p.finalidade = fin;
        p.categoria = cat;
        p.cambio = cambio;
        p.ocupantes = ocup;
        p.consumoMin = cons;
        pf.save(p);
    }
    private void drive(String cpf, String modelo, String vendedor, LocalDateTime inicio) {
        TestDrive t = new TestDrive();
        t.cliente = cli(cpf);
        t.veiculo = car(modelo);
        t.vendedor = vendedor;
        t.inicio = inicio;
        td.save(t);
        audit(vendedor, "Test-drive agendado: " + t.veiculo.descricao());
    }
    private void proposta(String cpf, String modelo, String desc, String acess, int dias, String resp, Proposta.Status st, int entregaDias) {
        Veiculo v = car(modelo);
        Proposta p = new Proposta();
        p.cliente = cli(cpf);
        p.veiculo = v;
        p.desconto = new BigDecimal(desc);
        p.valor = v.preco.subtract(p.desconto);
        p.precoBase = v.preco;
        p.acessorios = acess;
        p.validade = LocalDate.now().plusDays(dias);
        p.responsavel = resp;
        p.status = st;
        ps.save(p);
        if (st == Proposta.Status.ABERTA) v.status = Veiculo.Status.RESERVADO;
        if (st == Proposta.Status.APROVADA) {
            v.status = Veiculo.Status.VENDIDO;
            Venda x = new Venda();
            x.proposta = p;
            x.data = LocalDate.now();
            x.entrega = LocalDate.now().plusDays(entregaDias);
            vd.save(x);
        }
        vs.save(v);
        audit(resp, "Proposta #" + p.id + " (" + st + ") para " + p.cliente.nome);
    }
}
