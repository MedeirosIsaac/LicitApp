package licitApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LicitAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(LicitAppApplication.class, args);

        System.out.println("\n====================================================");
        System.out.println("  LicitApp rodando com sucesso!");
        System.out.println("  Acesse: http://localhost:8080/licitacoes");
        System.out.println("====================================================\n");
    }
}
