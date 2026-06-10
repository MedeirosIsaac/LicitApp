package licitApp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;


import licitApp.model.Contratacao;
import licitApp.enums.Modalidade;
import licitApp.enums.StatusContratacao;
    
@Controller
public class MainController {

    @GetMapping("/dashboard")
    public String abrirDashboard(Model model) {
        model.addAttribute("usuario", new UsuarioFalso("Isaac", "Costa", "Analista CI"));
        model.addAttribute("stats", new StatsFalsas(1, 1, 0, 0));
        
        Contratacao c1 = new Contratacao(
                "PE 044/2026", 
                "TechNorte Soluções Ltda.", 
                "Aquisição de licenças de software para gestão hospitalar.", 
                Modalidade.PREGAO_ELETRONICO, 
                StatusContratacao.EM_ANALISE, 
                1250400.00
        );
        
        List<Contratacao> listaDeProcessos = new ArrayList<>();
        listaDeProcessos.add(c1);
        
        model.addAttribute("contratacoes", listaDeProcessos); 

        return "dashboard"; 
    }

    @GetMapping("/empresas")
    public String abrirEmpresas(Model model) {
        model.addAttribute("usuario", new UsuarioFalso("Isaac", "Costa", "Analista CI"));
        return "empresas"; 
    }

    @GetMapping("/analise/{id}")
    public String abrirAnalise(@PathVariable String id, Model model) {
        model.addAttribute("usuario", new UsuarioFalso("Isaac", "Costa", "Analista CI"));
        
        // Criamos o objeto e forçamos ele a ter o mesmo ID que veio da URL
        Contratacao cAnalise = new Contratacao("PE 044/2026", "TechNorte Soluções Ltda.", "Aquisição de licenças...", Modalidade.PREGAO_ELETRONICO, StatusContratacao.EM_ANALISE, 125000);
        cAnalise.setId(id); 
        
        model.addAttribute("contratacao", cAnalise);
        model.addAttribute("minuta", new DocFalso(100L));
        model.addAttribute("regularizacao", new DocFalso(101L));

        return "analise"; 
    }

    record UsuarioFalso(String nome, String sobrenome, String perfil) {}
    record StatsFalsas(int total, int emAnalise, int aprovados, int pendentes) {}
    record DocFalso(Long id) {}
}