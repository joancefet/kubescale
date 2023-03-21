package com.example.kubescale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;
import java.lang.Runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;


@Controller
public class KubeScaleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KubeScaleController.class);


    @Value("#{'${lista}'.split(',')}")
    private List<String> lista;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String exibirFormulario(Model model) {
        model.addAttribute("formulario", new Formulario());
        model.addAttribute("lista", lista);
    return "index";
    }

    @GetMapping("/resultado")
    public String exibirResultado() {
        return "resultado";
    }

    @PostMapping(value = "/")
    public ModelAndView enviarFormulario(Formulario formulario, RedirectAttributes attributes, HttpSession session) {
        if (!lista.contains(formulario.getSelect1())) {
            LOGGER.error("Opção inválida para o Select 1: {}", formulario.getSelect1());
            throw new IllegalArgumentException("Opção inválida para o Select 1");
        }
        String comando = "curl -X POST -u teste:teste https://jenkinsoci.quark.tec.br/jenkins/job/K8s-Quarks-SP/job/Sisprod-Reduction/buildWithParameters?namespace=" + formulario.getSelect1() + "&scale=" + formulario.getSelect2();
        LOGGER.debug("Comando curl: {}", comando);
        System.out.println(comando);
        // executar o comando curl
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(comando);
            int exitCode = process.waitFor();
            LOGGER.debug("Código de saída do comando: {}", exitCode);
            System.out.println("Código de saída do comando: " + exitCode);
            if (exitCode == 0) {
                session.setAttribute("mensagem", "Comando executado com sucesso");
            } else {
                session.setAttribute("mensagem", "Erro ao executar o comando. Código de saída: " + exitCode);
            }
        } catch (Exception e) {
            LOGGER.error("Erro ao executar comando: {}", e.getMessage());
            e.printStackTrace();
            session.setAttribute("mensagem", "Erro ao executar o comando: " + e.getMessage());
        }
        // Retornando
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("mensagem", session.getAttribute("mensagem"));
        modelAndView.setViewName("resultado");
        return modelAndView;


    }
}
